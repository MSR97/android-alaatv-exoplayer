package com.alaatv.component.player;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.MutableLiveData;

import com.alaatv.component.R;
import com.google.ads.interactivemedia.v3.api.AdDisplayContainer;
import com.google.ads.interactivemedia.v3.api.ImaSdkFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.analytics.AnalyticsListener;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.ShuffleOrder;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.lang.ref.WeakReference;
import java.util.HashMap;

import timber.log.Timber;


class PlayerSetting {
    
    MutableLiveData<Boolean>        isTappedFullScreen = new MutableLiveData<>();
    PlayerOnGestureRegisterListener playerOnGestureRegisterListener;
    private WeakReference<Context>    contextWeakReference;
    private WeakReference<PlayerView> playerViewWeakReference;
    
    private PlaybackStateListener            playbackStateListener;
    private SimpleExoPlayer                  player;
    private boolean                          playWhenReady              = true;
    private int                              currentWindow              = 0;
    private long                             playbackPosition           = 0;
    private HashMap<String, String>          hashMapQuality             = new HashMap<>();
    private PlaybackParameters               paramLow;
    private PlaybackParameters               paramNormal;
    private PlaybackParameters               paramMedium;
    private PlaybackParameters               paramHigh;
    private boolean                          isAdPlaying;
    private ConcatenatingMediaSource         contentMediaSource;
    private AdDisplayContainer               adContainer;
    private DefaultDataSourceFactory         dataSourceFactory;
    private ViewGroup                        mAdUiContainer;
    private MediaSource                      mediaSource;
    private PlayerImaService                 playerImaService;
    private boolean                          isControllerVisible        = true;
    private ImageView                        forwardSwipe;
    private ImageView                        backwardSwipe;
    private View                             exoControllerPanel;
    private boolean                          isFullscreenByDoubleTapped = false;
    private SeekBar                          brightnessSeekBar;
    private SeekBar                          volumeSeekBar;
    private String                           vastAdsServerUrl           = "#";
    private PlayerLoudnessEnhancerController PLayerLoudnessEnhancerController;
    private TextView                         textPercentageView;
    private boolean                          isPlayerFullScreen         = false;
    private ErrorCallback                    errorCallback              = null;
    
    PlayerSetting( PlayerView playerView, Context context, ViewGroup mAdUiContainer ) {
        playerViewWeakReference = new WeakReference<>(playerView);
        contextWeakReference = new WeakReference<>(context);
        playbackStateListener = new PlaybackStateListener(context, playerImaService, ( ) -> {
            if ( errorCallback != null ) {
                errorCallback.onErrorCallBack();
            }
        });
        paramLow = new PlaybackParameters(PlayerConstants.SPEED_LOW);
        paramNormal = new PlaybackParameters(PlayerConstants.SPEED_NORMAL);
        paramMedium = new PlaybackParameters(PlayerConstants.SPEED_MEDIUM);
        paramHigh = new PlaybackParameters(PlayerConstants.SPEED_HIGH);
        this.mAdUiContainer = mAdUiContainer;
        onGesture(context, playerView);
    }
    
    private void onGesture( @NonNull Context context, @NonNull PlayerView playerView ) {
        playerOnGestureRegisterListener = new PlayerOnGestureRegisterListener(context, playerView) {
            @Override
            public void onDoubleTaps( View view ) {
                if ( playerImaService.isAdsEnded() ) {
                    if ( ! isFullscreenByDoubleTapped ) {
                        isFullscreenByDoubleTapped = true;
                        isTappedFullScreen.postValue(true);
                    } else {
                        isFullscreenByDoubleTapped = false;
                        isTappedFullScreen.postValue(false);
                        
                    }
                }
                
            }
            
            @Override
            public void onSwipeRight( View view ) {
                
                player.seekTo(player.getContentPosition() + 30000);
                playerView.showController();
                backwardSwipe.setVisibility(View.GONE);
                exoControllerPanel.setVisibility(View.GONE);
                if ( forwardSwipe != null ) {
                    forwardSwipe.setVisibility(View.VISIBLE);
                    forwardSwipe.postDelayed(( ) -> {
                        forwardSwipe.setVisibility(View.GONE);
                        exoControllerPanel.setVisibility(View.VISIBLE);
                        isControllerVisible = true;
                        // playerView.hideController();
                    }, 500);
                }
            }
            
            
            @Override
            public void onSwipeLeft( View view ) {
                if ( player.getCurrentPosition() > 0 ) {
                    player.seekTo(player.getContentPosition() - 10000);
                    playerView.showController();
                    forwardSwipe.setVisibility(View.GONE);
                    exoControllerPanel.setVisibility(View.GONE);
                    if ( backwardSwipe != null ) {
                        backwardSwipe.setVisibility(View.VISIBLE);
                        backwardSwipe.postDelayed(( ) -> {
                            backwardSwipe.setVisibility(View.GONE);
                            exoControllerPanel.setVisibility(View.VISIBLE);
                            isControllerVisible = true;
                        }, 500);
                    }
                }
            }
            
            @Override
            public void onSwipeBottom( View view ) {
                playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
            }
            
            @Override
            public void onSwipeTop( View view ) {
                playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
            }
            
            
            @Override
            public void onClick( View view ) {
                boolean isAdsEnded = true;
                if ( playerImaService != null ) {
                    isAdsEnded = playerImaService.isAdsEnded;
                }
                if ( isAdsEnded ) {
                    playerView.showController();
                    if ( isControllerVisible ) {
                        playerView.hideController();
                        isControllerVisible = false;
                        brightnessSeekBar.setVisibility(View.GONE);
                        volumeSeekBar.setVisibility(View.GONE);
                        
                    } else {
                        isControllerVisible = true;
                        playerView.showController();
                        exoControllerPanel.setVisibility(View.VISIBLE);
                        if ( isPlayerFullScreen ) {
                            brightnessSeekBar.setVisibility(View.VISIBLE);
                            volumeSeekBar.setVisibility(View.VISIBLE);
                        }
                        
                    }
                    
                }
                
            }
            
            @Override
            public void onLongClick( View view ) {
            
            }
        };
        
    }
    
    public void setErrorCallback( ErrorCallback errorCallback ) {
        this.errorCallback = errorCallback;
        
    }
    
    @Nullable
    protected PlayerView getPlayerView( ) {
        return playerViewWeakReference.get();
    }
    
    void initializePlayer( int quality, boolean playWithAds ) {
        Context context = getContext();
        if ( context == null ) {
            return;
        }
        if ( player == null ) {
            DefaultTrackSelector trackSelector = new DefaultTrackSelector();
            trackSelector.setParameters(
                    trackSelector.buildUponParameters().setMaxVideoSizeSd());
            player = ExoPlayerFactory.newSimpleInstance(context, trackSelector);
        }
        initializeDisplay();
        Timber.d(String.valueOf(quality));
        String urlQuality = hashMapQuality.get(String.valueOf(quality));
        
        if ( urlQuality != null ) {
            Uri uri = Uri.parse(urlQuality);
            dataSourceFactory =
                    new DefaultDataSourceFactory(
                            context, Util.getUserAgent(context, context.getString(R.string.app_name)));
            contentMediaSource =
                    new ConcatenatingMediaSource(
                            /* isAtomic= */ false,
                            /* useLazyPreparation= */ true,
                            new ShuffleOrder.DefaultShuffleOrder(/* length= */ 0));
            mediaSource =
                    new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
            contentMediaSource.addMediaSource(mediaSource);
            mediaSource = buildMediaSource(uri);
            player.setPlayWhenReady(playWhenReady);
            player.seekTo(currentWindow, playbackPosition);
            player.addListener(playbackStateListener);
            contentMediaSource.addMediaSource(mediaSource);
            player.prepare(contentMediaSource, false, false);
            if ( playWithAds ) {
                playAds();
            }
            
            
            PlayerControllerVisibility();
            setBrightnessSeekBar();
            setVolumeSeekBar();
        }
    }
    
    public void setVideoUrls( HashMap<String, String> hashMapQuality ) {
        this.hashMapQuality = hashMapQuality;
    }
    
    void setPlayerFullScreen( boolean playerFullScreen ) {
        isPlayerFullScreen = playerFullScreen;
    }
    
    void setVastAdsServerUrl( String vastAdsServerUrl ) {
        this.vastAdsServerUrl = vastAdsServerUrl;
    }
    
    void setTextPercentageView( TextView textPercentageView ) {
        this.textPercentageView = textPercentageView;
    }
    
    void setVolumeSeekBar( SeekBar volumeSeekBar ) {
        this.volumeSeekBar = volumeSeekBar;
    }
    
    void setBrightnessSeekBar( SeekBar brightnessSeekBar ) {
        this.brightnessSeekBar = brightnessSeekBar;
    }
    
    void setForwardSwipe( ImageView forwardSwipe ) {
        this.forwardSwipe = forwardSwipe;
    }
    
    void setBackwardSwipe( ImageView backwardSwipe ) {
        this.backwardSwipe = backwardSwipe;
    }
    
    void setExoControllerPanel( View exoControllerPanel ) {
        this.exoControllerPanel = exoControllerPanel;
    }
    
    void setSPEED_HIGH( ) {
        player.setPlaybackParameters(paramHigh);
    }
    
    void setSPEED_MEDIUM( ) {
        player.setPlaybackParameters(paramMedium);
    }
    
    void setSPEED_NORMAL( ) {
        player.setPlaybackParameters(paramNormal);
    }
    
    void setSPEED_LOW( ) {
        player.setPlaybackParameters(paramLow);
    }
    
    @Nullable
    protected Context getContext( ) {
        return contextWeakReference.get();
    }
    
    private void initializeDisplay( ) {
        PlayerView playerView = getPlayerView();
        if ( playerView == null ) {
            return;
        }
        
        playerView.setPlayer(player);
        playerView.setBackgroundColor(Color.BLACK);
        playerView.setShutterBackgroundColor(Color.BLACK);
        playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
        textPercentageView.setVisibility(View.GONE);
        playerView.setOnTouchListener(playerOnGestureRegisterListener);
    }
    
    @Nullable
    private MediaSource buildMediaSource( Uri uri ) {
        Context context = getContext();
        if ( context == null ) {
            return null;
        }
        
        DataSource.Factory dataSourceFactory =
                new DefaultDataSourceFactory(context, "AlaaPlayer");
        
        return new ProgressiveMediaSource.Factory(dataSourceFactory)
                       .createMediaSource(uri);
    }
    
    private void playAds( ) {
        Context context = getContext();
        if ( context == null ) {
            return;
        }
        PlayerView playerView = getPlayerView();
        if ( playerView == null ) {
            return;
        }
        
        adContainer = createAdDisplayContainer(mAdUiContainer);
        playerImaService =
                new PlayerImaService(context, dataSourceFactory, new SharedVideoPlayer(), playerView);
        initializeAds(adContainer);
        requestAd(vastAdsServerUrl);
        
    }
    
    private void PlayerControllerVisibility( ) {
        PlayerView playerView = getPlayerView();
        if ( playerView == null ) {
            return;
        }
        
        
        playerView.setControllerVisibilityListener(visibility -> {
            if ( visibility == 0 ) {
                isControllerVisible = false;
            } else if ( visibility == 1 ) {
                isControllerVisible = true;
            }
        });
    }
    
    private void setBrightnessSeekBar( ) {
        Context context = getContext();
        if ( context == null ) {
            return;
        }
        
        ContentResolver conresolver = context.getContentResolver();
        brightnessSeekBar.setProgress(50);
        brightnessSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onProgressChanged( SeekBar arg0, int arg1, boolean arg2 ) {
                
                float BackLightValue = (float) arg1 / 100;
                //backLightSetting.setText(String.valueOf(BackLightValue));
                textPercentageView.setVisibility(View.VISIBLE);
                textPercentageView.setText(arg1 + "%");
                textPercentageView.setTextColor(Color.WHITE);
                Window window = ( (FragmentActivity) context ).getWindow();
                WindowManager.LayoutParams
                        layoutParams =
                        window.getAttributes();
                layoutParams.screenBrightness = BackLightValue;
                window.setAttributes(layoutParams);
                window.addFlags(WindowManager.LayoutParams.FLAGS_CHANGED);
                Timber.d("BackLightValue: %s", BackLightValue);
                
            }
            
            @Override
            public void onStartTrackingTouch( SeekBar arg0 ) {
            
            
            }
            
            @Override
            public void onStopTrackingTouch( SeekBar arg0 ) {
                textPercentageView.setVisibility(View.GONE);
                
                
            }
        });
        brightnessSeekBar.setMax(100);
    }
    
    private void setVolumeSeekBar( ) {
        Context context = getContext();
        if ( context == null ) {
            return;
        }
        
        
        volumeSeekBar.setProgress(50);
        volumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onProgressChanged( SeekBar seekBar, int i, boolean b ) {
                float volumeValue = (float) i / 100;
                player.setVolume(volumeValue);
                textPercentageView.setVisibility(View.VISIBLE);
                textPercentageView.setText(i + "%");
                PLayerLoudnessEnhancerController =
                        new PlayerLoudnessEnhancerController(context, player.getAudioSessionId());
                if ( i < 100 ) {
                    volumeBoosterDisable();
                    textPercentageView.setTextColor(Color.WHITE);
                } else {
                    textPercentageView.setText(i + "%");
                    textPercentageView.setTextColor(Color.RED);
                    volumeBoosterEnable(getGain(i));
                }
            }
            
            @Override
            public void onStartTrackingTouch( SeekBar seekBar ) {
            
            }
            
            @Override
            public void onStopTrackingTouch( SeekBar seekBar ) {
                textPercentageView.setVisibility(View.GONE);
                
            }
        });
        
    }
    
    private static AdDisplayContainer createAdDisplayContainer(
            ViewGroup companionView ) {
        ImaSdkFactory      sdkFactory         = ImaSdkFactory.getInstance();
        AdDisplayContainer adDisplayContainer = sdkFactory.createAdDisplayContainer();
        adDisplayContainer.setAdContainer(companionView);
        return adDisplayContainer;
    }
    
    private void initializeAds( AdDisplayContainer adc ) {
        playerImaService.init(adc);
    }
    
    private void requestAd( String adTagUrl ) {
        playerImaService.requestAds(adTagUrl);
    }
    
    private void volumeBoosterDisable( ) {
        Context context = getContext();
        if ( context == null ) {
            return;
        }
        
        
        if ( player != null && PLayerLoudnessEnhancerController != null &&
             player.getPlayWhenReady() && player.isPlaying() ) {
            if ( PLayerLoudnessEnhancerController.isEnabled() ) {
                volumeSeekBar.setProgressDrawable(context.getResources().getDrawable(R.drawable.player_brightness_seek_bar));
                volumeSeekBar.setThumb(context.getResources().getDrawable(R.drawable.player_volume_seekbar_thumb));
                PLayerLoudnessEnhancerController.disable();
                PLayerLoudnessEnhancerController.release();
                if ( player.getAudioComponent() != null ) {
                    player.getAudioComponent().addAudioListener(PLayerLoudnessEnhancerController);
                }
            }
        }
    }
    
    private void volumeBoosterEnable( int gain ) {
        Context context = getContext();
        if ( context == null ) {
            return;
        }
        
        
        if ( player != null && PLayerLoudnessEnhancerController != null && player.isPlaying() ) {
            volumeSeekBar.setProgressDrawable(context.getResources().getDrawable(R.drawable.player_brightness_seek_bar_boosted));
            volumeSeekBar.setThumb(context.getResources().getDrawable(R.drawable.player_volume_seekbar_thumb_buoosted));
            PLayerLoudnessEnhancerController.enable();
            PLayerLoudnessEnhancerController.setGain(gain);
            if ( player.getAudioComponent() != null ) {
                player.getAudioComponent().addAudioListener(PLayerLoudnessEnhancerController);
            }
        }
    }
    
    private int getGain( int i ) {
        return i * 10;
    }
    
    void startPlayer( ) {
        if ( player != null ) {
            
            player.setPlayWhenReady(true);
            player.getPlaybackState();
            
            
        }
    }
    
    public long getPlaybackPosition( ) {
        return playbackPosition;
    }
    
    public void playerRetry( ) {
        if ( player != null ) {
            player.retry();
        }
    }
    
    void releasePlayer( ) {
        if ( player != null ) {
            playbackPosition = player.getCurrentPosition();
            currentWindow = player.getCurrentWindowIndex();
            playWhenReady = player.getPlayWhenReady();
            player.removeListener(playbackStateListener);
            player.release();
            player = null;
        }
    }
    
    void pausePlayer( ) {
        if ( player != null ) {
            player.setPlayWhenReady(false);
            player.getPlaybackState();
            
        }
    }
    
    public boolean isVideoPlaying( ) {
        if ( player != null ) {
            return player.isPlaying();
        }
        return false;
        
    }
    
    private static class PlaybackStateListener implements com.google.android.exoplayer2.Player.EventListener {
        WeakReference<Context>          mContext;
        WeakReference<PlayerImaService> playerImaServiceWeakReference;
        Callback                        mCallback;
        
        PlaybackStateListener( Context context, PlayerImaService playerImaService,
                               @Nullable Callback onError ) {
            mContext = new WeakReference<>(context);
            playerImaServiceWeakReference = new WeakReference<>(playerImaService);
            mCallback = onError;
        }
        
        public void setCallback( Callback onError ) {
            this.mCallback = onError;
        }
        
        @Override
        public void onPlayerStateChanged( boolean playWhenReady,
                                          int playbackState ) {
            String stateString;
            
            switch ( playbackState ) {
                case ExoPlayer.STATE_IDLE:
                    stateString = "ExoPlayer.STATE_IDLE      -";
                    break;
                case ExoPlayer.STATE_BUFFERING:
                    stateString = "ExoPlayer.STATE_BUFFERING -";
                    
                    break;
                case ExoPlayer.STATE_READY:
                    stateString = "ExoPlayer.STATE_READY     -";
                    
                    break;
                case ExoPlayer.STATE_ENDED:
                    stateString = "ExoPlayer.STATE_ENDED     -";
                    break;
                default:
                    stateString = "UNKNOWN_STATE             -";
                    break;
            }
            Timber.d("changed state to " + stateString
                     + " playWhenReady: " + playWhenReady);
        }
        
        @Override
        public void onPlayerError( ExoPlaybackException error ) {
            Context context = mContext.get();
            if ( context == null ) {
                return;
            }
            Toast
                    toast =
                    Toast.makeText(context, "خطا در بارگذاری ، اینترنت را بررسی نمایید ", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
            
            toast.show();
            PlayerImaService playerImaService = playerImaServiceWeakReference.get();
            if ( playerImaService != null ) {
                playerImaService.adsLoadedListenerRemove();
            }
            if ( mCallback != null ) {
                mCallback.onError();
            }
            
        }
        
        interface Callback {
            void onError( );
        }
    }
    
    class SharedVideoPlayer {
        void claim( ) {
            isAdPlaying = true;
            if ( player != null ) {
                player.setPlayWhenReady(false);
            }
        }
        
        void release( ) {
            if ( isAdPlaying ) {
                isAdPlaying = false;
                if ( contentMediaSource != null && player != null ) {
                    player.prepare(contentMediaSource);
                    player.setPlayWhenReady(true);
                    
                }
            }
        }
        
        void prepare( MediaSource mediaSource ) {
            if ( mediaSource != null && player != null )
                player.prepare(mediaSource);
        }
        
        void addAnalyticsListener( AnalyticsListener listener ) {
            player.addAnalyticsListener(listener);
        }
        
        Player getPlayer( ) {
            return player;
        }
    }
    
}
