package com.alaatv.component.player;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.google.ads.interactivemedia.v3.api.AdDisplayContainer;
import com.google.ads.interactivemedia.v3.api.AdErrorEvent;
import com.google.ads.interactivemedia.v3.api.AdEvent;
import com.google.ads.interactivemedia.v3.api.AdPodInfo;
import com.google.ads.interactivemedia.v3.api.AdsLoader;
import com.google.ads.interactivemedia.v3.api.AdsManager;
import com.google.ads.interactivemedia.v3.api.AdsManagerLoadedEvent;
import com.google.ads.interactivemedia.v3.api.AdsRequest;
import com.google.ads.interactivemedia.v3.api.ImaSdkFactory;
import com.google.ads.interactivemedia.v3.api.ImaSdkSettings;
import com.google.ads.interactivemedia.v3.api.player.AdMediaInfo;
import com.google.ads.interactivemedia.v3.api.player.VideoAdPlayer;
import com.google.ads.interactivemedia.v3.api.player.VideoAdPlayer.VideoAdPlayerCallback;
import com.google.ads.interactivemedia.v3.api.player.VideoProgressUpdate;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.analytics.AnalyticsListener;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * Takes control of audio playback from the AudioPlayerService to play some ads and then returns
 * control afterwards.
 */
public final class PlayerImaService
        implements AdErrorEvent.AdErrorListener, AdEvent.AdEventListener, AdsLoader.AdsLoadedListener {
    
    
    private final Context                         context;
    private final PlayerSetting.SharedVideoPlayer sharedVideoPlayer;
    private final Player                          exoPlayer;
    private final List<VideoAdPlayerCallback>     callbacks;
    private final ImaSdkFactory                   sdkFactory;
    private final ImaSdkSettings                  imaSdkSettings;
    private final DefaultDataSourceFactory        dataSourceFactory;
    boolean isAdsEnded = false;
    private Bundle             bundle;
    private PlayerView         playerView;
    private AdsLoader          adsLoader;
    private AdsManager         adsManager;
    private AdMediaInfo        currentAd;
    private ImaProgressTracker progressTracker;
    
    
    PlayerImaService(
            Context context,
            DefaultDataSourceFactory dataSourceFactory,
            PlayerSetting.SharedVideoPlayer sharedVideoPlayer, PlayerView playerView ) {
        this.context = context;
        this.sharedVideoPlayer = sharedVideoPlayer;
        this.exoPlayer = sharedVideoPlayer.getPlayer();
        this.callbacks = new ArrayList<>();
        this.sdkFactory = ImaSdkFactory.getInstance();
        this.imaSdkSettings = ImaSdkFactory.getInstance().createImaSdkSettings();
        this.dataSourceFactory = dataSourceFactory;
        this.playerView = playerView;
        sharedVideoPlayer.addAnalyticsListener(new ImaListener());
        playerView.setControllerHideDuringAds(true);
        bundle = new Bundle();
        bundle.putString("ads", "ads1");
        
        
    }
    
    public void init( AdDisplayContainer adDisplayContainer ) {
        ImaVideoAdPlayer imaVideoAdPlayer = new ImaVideoAdPlayer();
        adDisplayContainer.setPlayer(imaVideoAdPlayer);
        imaSdkSettings.setAutoPlayAdBreaks(true);
        imaSdkSettings.setLanguage("fa");
        adsLoader = sdkFactory.createAdsLoader(context, imaSdkSettings, adDisplayContainer);
        adsLoader.addAdErrorListener(this);
        adsLoader.addAdsLoadedListener(this);
        
        progressTracker = new ImaProgressTracker(imaVideoAdPlayer);
    }
    
    
    public void adsLoadedListenerRemove(){
        if(adsLoader!=null && adsManager!=null) {
            adsLoader.removeAdsLoadedListener(this);
            adsManager.destroy();
        }
    }
    
    void requestAds( String adTagUrl ) {
        AdsRequest request = sdkFactory.createAdsRequest();
        request.setAdTagUrl(adTagUrl);
        // The ContentProgressProvider is only needed for scheduling ads with VMAP ad requests
        request.setContentProgressProvider(
                ( ) -> new VideoProgressUpdate(exoPlayer.getCurrentPosition(), exoPlayer.getDuration()));
        adsLoader.requestAds(request);
    }
    
    @Override
    public void onAdsManagerLoaded( AdsManagerLoadedEvent adsManagerLoadedEvent ) {
        adsManager = adsManagerLoadedEvent.getAdsManager();
        adsManager.addAdErrorListener(this);
        adsManager.addAdEventListener(this);
        adsManager.init();
    }
    
    @Override
    public void onAdError( AdErrorEvent adErrorEvent ) {
        Timber.e("Ad Error: %s", adErrorEvent.getError().getMessage());
        isAdsEnded = true;
    }
    
    @Override
    public void onAdEvent( AdEvent adEvent ) {
        Timber.i("Event: %s", adEvent.getType());
        switch ( adEvent.getType() ) {
            case LOADED:
                // If preloading we may to call start() at a particular time offset, instead of immediately.
                if ( adsManager != null ) {
                    adsManager.start();
                }
                
                break;
            case CONTENT_PAUSE_REQUESTED:

                if ( sharedVideoPlayer != null ) {
                    sharedVideoPlayer.claim();
                }
                
                break;
            case CONTENT_RESUME_REQUESTED:
                if ( sharedVideoPlayer != null ) {
                    sharedVideoPlayer.release();
                }
                break;
            
            case SKIPPED: {
                if ( adsManager != null ) {
                    adsManager.skip();
                }
                break;
            }
            
            case ALL_ADS_COMPLETED:
                if ( adsManager != null ) {
                    adsManager.destroy();
                    adsManager = null;
                    
                }
                break;
            default:
                break;
        }
    }
    
    
    public boolean isAdsEnded( ) {
        return isAdsEnded;
    }
    
    private void notifyEnded( ) {
        for ( VideoAdPlayerCallback callback : callbacks ) {
            callback.onEnded(currentAd);
            isAdsEnded = true;
        }
    }
    
    static class ImaProgressTracker implements Handler.Callback {
        
        private final Handler          messageHandler;
        private final ImaVideoAdPlayer player;
        
        ImaProgressTracker( ImaVideoAdPlayer player ) {
            this.messageHandler = new Handler(this);
            this.player = player;
        }
        
        @Override
        public boolean handleMessage( Message msg ) {
            switch ( msg.what ) {
                case PlayerConstants.ImaQUIT:
                    // Don't remove START message since it is yet to send updates. The QUIT message comes from
                    // the current ad being updated, hence we remove UPDATE messages.
                    messageHandler.removeMessages(PlayerConstants.ImaUPDATE);
                    break;
                case PlayerConstants.ImaUPDATE:
                    // Intentional fallthrough. START message is introduced as a way to differentiate the
                    // beginning (the START of progress event) and progress itself (UPDATEs). Handling for
                    // both
                    // the messages are same.
                case PlayerConstants.ImaSTART:
                    player.sendProgressUpdate();
                    messageHandler.removeMessages(PlayerConstants.ImaUPDATE);
                    messageHandler.sendEmptyMessageDelayed(PlayerConstants.ImaUPDATE, PlayerConstants.ImaUPDATE_PERIOD_MS);
                    break;
                default:
                    break;
            }
            return true;
        }
        
        void start( ) {
            messageHandler.sendEmptyMessage(PlayerConstants.ImaSTART);
        }
        
        void stop( ) {
            messageHandler.sendMessageAtFrontOfQueue(Message.obtain(messageHandler, PlayerConstants.ImaQUIT));
        }
    }
    
    
    class ImaVideoAdPlayer implements VideoAdPlayer {
        @Override
        public void loadAd( AdMediaInfo adMediaInfo, AdPodInfo adPodInfo ) {

        }
        
        @Override
        public void playAd( AdMediaInfo adMediaInfo ) {
            String url = adMediaInfo.getUrl();
            progressTracker.start();
            if ( currentAd == adMediaInfo ) {
                for ( VideoAdPlayerCallback callback : callbacks ) {
                    callback.onResume(adMediaInfo);
                }
            } else {
                currentAd = adMediaInfo;
                for ( VideoAdPlayerCallback callback : callbacks ) {
                    callback.onPlay(adMediaInfo);
                }
                MediaSource mediaSource =
                        new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(url));
                sharedVideoPlayer.prepare(mediaSource);
            }
            exoPlayer.setPlayWhenReady(true);
            isAdsEnded = false;
            playerView.hideController();
            
            
        }
        
        @Override
        public void pauseAd( AdMediaInfo adMediaInfo ) {
            exoPlayer.setPlayWhenReady(false);
            progressTracker.stop();
            for ( VideoAdPlayerCallback callback : callbacks ) {
                callback.onPause(adMediaInfo);
            }
        }
        
        @Override
        public void stopAd( AdMediaInfo adMediaInfo ) {
            progressTracker.stop();
            exoPlayer.setPlayWhenReady(false);
            notifyEnded();
        }
        
        @Override
        public void release( ) {
            if ( adsManager != null ) {
                adsManager.destroy();
            }
            
        }
        
        @Override
        public void addCallback( VideoAdPlayerCallback callback ) {
            callbacks.add(callback);
        }
        
        @Override
        public void removeCallback( VideoAdPlayerCallback callback ) {
            callbacks.remove(callback);
        }
        
        void sendProgressUpdate( ) {
            for ( VideoAdPlayerCallback callback : callbacks ) {
                callback.onAdProgress(currentAd, getAdProgress());
            }
        }
        
        @Override
        public VideoProgressUpdate getAdProgress( ) {
            if ( currentAd == null ) {
                return null;
            }
            
            return new VideoProgressUpdate(exoPlayer.getCurrentPosition(), exoPlayer.getDuration());
        }
        
        @Override
        public int getVolume( ) {
            return (int) ( 100 * exoPlayer.getAudioComponent().getVolume() );
        }
    }
    
    /**
     * Encapsulates callbacks for ExoPlayer changes, and lets IMA know the state of playback
     */
    class ImaListener implements AnalyticsListener {
        @Override
        public void onPlayerStateChanged(
                AnalyticsListener.EventTime eventTime, boolean playWhenReady, int playbackState ) {
            if ( currentAd == null ) {
                // This may be null if state changes after stopAd for a given mediaInfo
                return;
            }
            switch ( playbackState ) {
                case Player.STATE_BUFFERING:
                    for ( VideoAdPlayerCallback callback : callbacks ) {
                        callback.onBuffering(currentAd);
                    }
                    break;
                case Player.STATE_READY:
                    for ( VideoAdPlayerCallback callback : callbacks ) {
                        callback.onLoaded(currentAd);
                    }
                    break;
                case Player.STATE_ENDED:
                    // Handles when the media item in the source is completed.
                    notifyEnded();
                    break;
                default:
                    break;
            }
        }
    }
}
