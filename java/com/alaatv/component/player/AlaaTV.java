package com.alaatv.component.player;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Handler;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import com.alaatv.component.R;
import com.alaatv.component.databinding.AlaaPlayerBinding;
import com.alaatv.util.Util;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.HashMap;

import timber.log.Timber;

public class AlaaTV extends FrameLayout implements LifecycleObserver {
    
    WeakReference<PlayerLifecycleAware> observerWeekReference;
    WeakReference<LifecycleOwner>       lifecycleOwnerWeakReference;
    @Nullable
    private AlaaPlayerBinding        mBinding;
    private PlayerSetting            mPlayerSetting;
    private LinearLayout             linearLayout_quality;
    private LinearLayout             linearLayout_exo_controller;
    private PlayerController         mPlayerController;
    private ImageButton              imageButtonFullscreen;
    private ImageButton              imageButtonSetting_quality;
    private Dialog                   mFullScreenDialog;
    private Button                   btQuality720;
    private Button                   btQuality480;
    private Button                   btQuality240;
    private ImageButton              exoLock;
    private ImageButton              exoSpeed;
    private ImageButton              exoUnlock;
    private Button                   speed05x;
    private Button                   speed1x;
    private Button                   speed15x;
    private Button                   speed2x;
    private SeekBar                  brightnessSeekBar;
    private SeekBar                  volumeSeekBar;
    private TextView                 textPercentageView;
    private AppCompatImageView       playButton;
    //
    private PlayerDataViewModel      mPlayerDataViewModel;
    private MutableLiveData<Boolean> isMaxSpeedForRed        = new MediatorLiveData<>();
    private Handler                  orientationDelayHandler = new Handler();
    boolean isProductIntroductionVideo;
    
    public AlaaTV( Context context, AttributeSet attrs ) {
        super(context, attrs);
        LayoutInflater inflater = LayoutInflater.from(context);
        if ( ! ( context instanceof LifecycleOwner ) ) {
            throw new ClassCastException("LifecycleOwnerNotFoundException");
        }
        LifecycleOwner lifecycleOwner = (LifecycleOwner) context;
        lifecycleOwnerWeakReference = new WeakReference<>(lifecycleOwner);
    
        mPlayerDataViewModel =
                new ViewModelProvider((ViewModelStoreOwner) context).get(PlayerDataViewModel.class);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.Player, 0, 0);
        
        try {
            //Set Media links
            isProductIntroductionVideo =
                    a.getBoolean(R.styleable.Player_product_introduction_video, false);
            String videoUrl720 = a.getString(R.styleable.Player_video_url_720);
            String videoUrl480 = a.getString(R.styleable.Player_video_url_480);
            String videoUrl240 = a.getString(R.styleable.Player_video_url_240);
            mPlayerDataViewModel.setVideoUrls(videoUrl720, videoUrl480, videoUrl240);
            //Set vast url
            String vastUrl = a.getString(R.styleable.Player_vast_url);
            if ( vastUrl != null && ! isProductIntroductionVideo ) {
                mPlayerDataViewModel.setVastUrl(vastUrl);
            }
            
        }
        finally {
            a.recycle();
        }
    
        mBinding = AlaaPlayerBinding.inflate(inflater, this, true);
    
        mBinding.playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
        
        mPlayerSetting =
                new PlayerSetting(mBinding.playerView, context, mBinding.frameVideoLayout);
        mPlayerDataViewModel.getVastUrl().observe(lifecycleOwner, vast -> {
            if ( vast == null ) {
                return;
            }
            if ( ! isProductIntroductionVideo ) {
                mPlayerSetting.setVastAdsServerUrl(vast);
            }
        });
        mPlayerDataViewModel.getVideoUrls().observe(lifecycleOwner, videoUrlsHashMap -> {
            if ( videoUrlsHashMap != null ) {
                mPlayerSetting.releasePlayer();
                mPlayerSetting.setVideoUrls(videoUrlsHashMap);
                mPlayerSetting.startPlayer();
            }
        });
    
        mPlayerDataViewModel.getPoster().observe(lifecycleOwner, poster -> {
            if ( poster == null ) {
                return;
            }
            mBinding.videoThumbnail.setVisibility(VISIBLE);
            if ( playButton != null ) {
                playButton.setVisibility(VISIBLE);
            }
            mBinding.playerView.setUseArtwork(true);
            Timber.d("poster: %s", poster);
            Util.loadImage(mBinding.videoThumbnail, poster, 1.78F);
        });
        observerWeekReference = new WeakReference<>(new PlayerLifecycleAware(mPlayerSetting));
//        mLifecycleOwner.getLifecycle().addObserver(mObserver);
        
        mPlayerController = new PlayerController(mPlayerSetting);
        mFullScreenDialog = new Dialog(context, R.style.DialogFullScreenTheme);
        initPlayerDisplay();
        playerControllerClickListener();
        isPlayerFullScreen();
    
        mPlayerSetting.setErrorCallback(( ) -> {
            if ( linearLayout_exo_controller != null && playButton != null ) {
                linearLayout_exo_controller.setVisibility(GONE);
                playButton.setVisibility(VISIBLE);
                playButton.setOnClickListener(v -> {
                    mPlayerSetting.playerRetry();
                    playButton.setVisibility(GONE);
                    linearLayout_exo_controller.setVisibility(VISIBLE);
    
                });
            }
        });
    }
    
    private void initPlayerDisplay( ) {
        if ( mBinding != null ) {
            playButton = mBinding.cardVideo.findViewById(R.id.playbutton);
            imageButtonFullscreen = mBinding.frameVideoLayout.findViewById(R.id.exo_fullscreen);
            imageButtonSetting_quality = mBinding.frameVideoLayout.findViewById(R.id.exo_quality);
            linearLayout_quality = mBinding.frameVideoLayout.findViewById(R.id.linier_quality);
            exoLock = mBinding.frameVideoLayout.findViewById(R.id.exo_lock);
            exoSpeed = mBinding.frameVideoLayout.findViewById(R.id.exo_speed);
            speed05x = mBinding.frameVideoLayout.findViewById(R.id.bt_speed5x);
            speed1x = mBinding.frameVideoLayout.findViewById(R.id.bt_speed1x);
            speed15x = mBinding.frameVideoLayout.findViewById(R.id.bt_speed1_5x);
            speed2x = mBinding.frameVideoLayout.findViewById(R.id.bt_speed2x);
            linearLayout_exo_controller =
                    mBinding.frameVideoLayout.findViewById(R.id.exo_controller_bar);
            exoUnlock = mBinding.frameVideoLayout.findViewById(R.id.exo_unlock);
            btQuality240 = mBinding.frameVideoLayout.findViewById(R.id.tv_quality240);
            btQuality720 = mBinding.frameVideoLayout.findViewById(R.id.tv_quality480);
            btQuality480 = mBinding.frameVideoLayout.findViewById(R.id.tv_quality720);
            ImageView backwardSwipe = mBinding.frameVideoLayout.findViewById(R.id.backward_swip);
            ImageView forwardSwipe  = mBinding.frameVideoLayout.findViewById(R.id.forward_swip);
            View
                    exoControllerPanel =
                    mBinding.frameVideoLayout.findViewById(R.id.exo_controller_panel);
            brightnessSeekBar = mBinding.frameVideoLayout.findViewById(R.id.brightness_seekBar);
            textPercentageView = mBinding.frameVideoLayout.findViewById(R.id.text_percentage_view);
            volumeSeekBar = mBinding.frameVideoLayout.findViewById(R.id.volume_seekBar);
            mPlayerSetting.setExoControllerPanel(exoControllerPanel);
            mPlayerSetting.setBackwardSwipe(backwardSwipe);
            mPlayerSetting.setForwardSwipe(forwardSwipe);
        }
        
        brightnessSeekBar.setVisibility(View.GONE);
        volumeSeekBar.setVisibility(View.GONE);
        imageButtonSetting_quality.setOnClickListener(v -> {
            if ( ! isProductIntroductionVideo )
                qualityControlVisibility();
        });
        mPlayerSetting.setBrightnessSeekBar(brightnessSeekBar);
        mPlayerSetting.setVolumeSeekBar(volumeSeekBar);
        mPlayerSetting.setTextPercentageView(textPercentageView);
        textPercentageView.setVisibility(View.GONE);
    }
    
    private void isPlayerFullScreen( ) {
        if ( lifecycleOwnerWeakReference.get() == null ) {
            return;
        }
        mPlayerSetting.isTappedFullScreen.observe(lifecycleOwnerWeakReference.get(), aBoolean -> {
            if ( aBoolean ) {
                fullScreenSetting();
                if ( Util.getValueOrDefault(mPlayerDataViewModel.getFullscreen().getValue(), false) ) {
                    FragmentActivity activity = getActivity();
                    if ( activity != null ) {
                        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                    }
                } else {
                    orientationDelayHandler();
                }
            }
        });
    }
    
    public void setPoster( @Nullable String poster ) {
        mPlayerDataViewModel.setPoster(poster);
    }
    
    @SuppressLint("SourceLockedOrientationActivity")
    private void playerControllerClickListener( ) {
        
        playButton.setOnClickListener(this::startPlayButton);
        if ( mBinding != null ) {
            mBinding.videoThumbnail.setOnClickListener(this::startPlayButton);
            mBinding.playerView.setOnClickListener(v -> {
                linearLayout_quality.setVisibility(View.GONE);
                mPlayerDataViewModel.setIsLinearLayoutQuality(false);
            });
        }
        
        btQuality240.setOnClickListener(v -> {
            mPlayerController.setQuality240();
            btQuality240.setTextColor(Color.BLACK);
            btQuality480.setTextColor(Color.WHITE);
            btQuality720.setTextColor(Color.WHITE);
            imageButtonSetting_quality.setImageResource(R.drawable.lq);
            speed1xTextColorSelected();
        });
        btQuality480.setOnClickListener(v -> {
            mPlayerController.setQuality480();
            btQuality240.setTextColor(Color.WHITE);
            btQuality480.setTextColor(Color.BLACK);
            btQuality720.setTextColor(Color.WHITE);
            imageButtonSetting_quality.setImageResource(R.drawable.fq);
            speed1xTextColorSelected();
            
        });
        btQuality720.setOnClickListener(v -> {
            mPlayerController.setQuality720();
            btQuality240.setTextColor(Color.WHITE);
            btQuality480.setTextColor(Color.WHITE);
            btQuality720.setTextColor(Color.BLACK);
            speed15x.setTextColor(Color.WHITE);
            speed1xTextColorSelected();
            imageButtonSetting_quality.setImageResource(R.drawable.player_hq);
        });
        
        
        speed05x.setOnClickListener(v -> {
            speed05xTextColorSelected();
            mPlayerController.setSpeedLow();
        });
        speed1x.setOnClickListener(v -> {
            speed1xTextColorSelected();
            mPlayerController.setSpeedNormal();
            
        });
        speed15x.setOnClickListener(v -> {
            speed15TextColorSelected();
            mPlayerController.setSpeedMedium();
            
            
        });
        speed2x.setOnClickListener(v -> {
            speed2xTextColorSelected();
            mPlayerController.setSpeedHigh();
            
        });
    
        exoLock.setOnClickListener(v -> {
            linearLayout_exo_controller.setVisibility(View.INVISIBLE);
            exoUnlock.setVisibility(View.VISIBLE);
            mPlayerDataViewModel.setExoPlayerLock(true);
        });
        exoUnlock.setOnClickListener(v -> {
            linearLayout_exo_controller.setVisibility(View.VISIBLE);
            exoUnlock.setVisibility(View.INVISIBLE);
            mPlayerDataViewModel.setExoPlayerLock(false);
        });
        exoSpeed.setOnClickListener(v -> {
            speedControlVisibility();
            if ( lifecycleOwnerWeakReference.get() != null ) {
                isMaxSpeedForRed.observe(lifecycleOwnerWeakReference.get(), aBoolean -> {
                    if ( aBoolean ) {
                        exoSpeed.setImageResource(R.drawable.player_speedicon_red);
                    } else {
                        exoSpeed.setImageResource(R.drawable.player_speedicon);
                    }
                });
            }
        });
        imageButtonFullscreen.setOnClickListener(v -> {
            FragmentActivity activity = getActivity();
            if ( activity != null ) {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
            }
            fullScreenSetting();
            if ( ! Util.getValueOrDefault(mPlayerDataViewModel.getFullscreen().getValue(), false) ) {
                orientationDelayHandler();
            }
        });
    }
    
    
    private void speed1xTextColorSelected( ) {
        isMaxSpeedForRed.setValue(false);
        speed05x.setTextColor(Color.WHITE);
        speed1x.setTextColor(Color.BLACK);
        speed15x.setTextColor(Color.WHITE);
        speed2x.setTextColor(Color.WHITE);
    }
    
    private void speed05xTextColorSelected( ) {
        isMaxSpeedForRed.setValue(false);
        speed05x.setTextColor(Color.BLACK);
        speed1x.setTextColor(Color.WHITE);
        speed15x.setTextColor(Color.WHITE);
        speed2x.setTextColor(Color.WHITE);
    }
    
    private void speed15TextColorSelected( ) {
        isMaxSpeedForRed.setValue(true);
        speed05x.setTextColor(Color.WHITE);
        speed1x.setTextColor(Color.WHITE);
        speed15x.setTextColor(Color.BLACK);
        speed2x.setTextColor(Color.WHITE);
    }
    
    private void speed2xTextColorSelected( ) {
        isMaxSpeedForRed.setValue(true);
        speed05x.setTextColor(Color.WHITE);
        speed1x.setTextColor(Color.WHITE);
        speed15x.setTextColor(Color.WHITE);
        speed2x.setTextColor(Color.BLACK);
    }
    
    
    private void orientationDelayHandler( ) {
        final FragmentActivity activity = getActivity();
        Runnable runnableOrientationDelayHandler = ( ) -> {
            if ( activity != null ) {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            }
        };
        Runnable runnableOrientationMessageHandler = ( ) -> {
            if ( getContext().getResources().getConfiguration().orientation ==
                 Configuration.ORIENTATION_LANDSCAPE ) {
                Toast.makeText(getContext(), "بچرخونش تا از تمام صفحه خارج بشی :)", Toast.LENGTH_SHORT).show();
            }
        };
        if ( activity != null && orientationDelayHandler != null ) {
            if ( android.provider.Settings.System.getInt(( activity ).getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0) ==
                 1 ) {
                orientationDelayHandler.postDelayed(runnableOrientationDelayHandler, 3000);
                orientationDelayHandler.postDelayed(runnableOrientationMessageHandler, 3500);
            }
        }
    }
    
    public void onVideoConfigurationChanged( @NotNull Configuration newConfig ) {
        final FragmentActivity activity = getActivity();
        if ( newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE ) {
            if ( mPlayerSetting != null && mPlayerDataViewModel.isVideoStarted() &&
                 ! Util.getValueOrDefault(mPlayerDataViewModel.getFullscreen().getValue(), false) ) {
                enterFullScreenFunc();
                if ( activity != null ) {
                    activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                }
            }
            
        } else if ( newConfig.orientation == Configuration.ORIENTATION_PORTRAIT &&
                    ! mPlayerDataViewModel.isExoPlayerLock() ) {
            if ( mPlayerSetting != null && mPlayerDataViewModel.isVideoStarted() &&
                 Util.getValueOrDefault(mPlayerDataViewModel.getFullscreen().getValue(), false) ) {
                exitFullScreenFunc();
                if ( activity != null ) {
                    activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                }
            }
        }
    }
    
    
    @Nullable
    private FragmentActivity getActivity( ) {
        Context context = getContext();
        while ( context instanceof ContextWrapper ) {
            if ( context instanceof Activity ) {
                return (FragmentActivity) context;
            }
            context = ( (ContextWrapper) context ).getBaseContext();
        }
        return null;
    }
    
    void startPlayButton( View view ) {
        Util.animClick(view);
        if ( mBinding != null ) {
            mBinding.videoThumbnail.setVisibility(View.GONE);
        }
        playButton.setVisibility(GONE);
        FragmentActivity activity = getActivity();
        if ( activity != null ) {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }
        mPlayerController.startPlayButton(view);
        mPlayerDataViewModel.setVideoStarted(true);
        
    }
    
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    protected void onStart( ) {
        initBinding();
        Timber.d("Observer\\AlaaTV ON_Start");
    }
    
    private void initBinding( ) {
        if ( mBinding == null ) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            mBinding = AlaaPlayerBinding.inflate(inflater, this, true);
        }
        if ( mFullScreenDialog == null ) {
            mFullScreenDialog = new Dialog(getContext(), R.style.DialogFullScreenTheme);
        }
    }
    
    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    protected void onResume( ) {
        initBinding();
        Timber.d("Observer\\AlaaTV ON_RESUME");
    }
    
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    protected void onDestroy( ) {
        if ( lifecycleOwnerWeakReference.get() != null ) {
            Timber.d("Observer\\AlaaTV ON_Destroy : remove observers from LiveData");
            mPlayerSetting.isTappedFullScreen.removeObservers(lifecycleOwnerWeakReference.get());
            mPlayerDataViewModel.getVideoUrls().removeObservers(lifecycleOwnerWeakReference.get());
            mPlayerDataViewModel.getPoster().removeObservers(lifecycleOwnerWeakReference.get());
            mPlayerDataViewModel.getVastUrl().removeObservers(lifecycleOwnerWeakReference.get());
            isMaxSpeedForRed.removeObservers(lifecycleOwnerWeakReference.get());
            return;
        }
        mFullScreenDialog = null;
        mBinding = null;
        Timber.d("Observer\\AlaaTV ON_Destroy : lifecycleOwner is null");
    }
    
    private void enterFullScreenProperties( ) {
        textPercentageView.setVisibility(View.GONE);
        brightnessSeekBar.setVisibility(View.VISIBLE);
        volumeSeekBar.setVisibility(View.VISIBLE);
    }
    
    public void setVideoUrls( @NonNull HashMap<String, String> videoUrls ) {
        mPlayerDataViewModel.setVideoUrls(videoUrls);
    }
    
    public void setVideoUrls( @Nullable String videoUrl720, @Nullable String videoUrl480, @Nullable String videoUrl240 ) {
        mPlayerDataViewModel.setVideoUrls(videoUrl720, videoUrl480, videoUrl240);
    }
    
    public void setVastUrl( @Nullable String vastUrl ) {
        mPlayerDataViewModel.setVastUrl(vastUrl);
    }
    
    private void enterFullScreenFunc( ) {
        FragmentActivity activity = getActivity();
        mPlayerDataViewModel.setFullscreen(true);
        enterFullScreenProperties();
        if ( getContext() != null ) {
            imageButtonFullscreen.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.player_ic_fullscreen_exit24dp));
        }
        if ( mBinding != null ) {
            ( (ViewGroup) mBinding.playerView.getParent() ).removeView(mBinding.playerView);
        }
        mFullScreenDialog.setCancelable(false);
        mFullScreenDialog.addContentView(mBinding.playerView, new ViewGroup.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT));
        if ( activity != null ) {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            if ( activity.getActionBar() != null ) {
                activity.getActionBar().hide();
            }
        }
        if ( mFullScreenDialog.getWindow() != null ) {
            mFullScreenDialog.getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
        
        if ( mFullScreenDialog.getWindow() != null ) {
            mFullScreenDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            mFullScreenDialog.getWindow().setLayout(( WindowManager.LayoutParams.MATCH_PARENT ), ( WindowManager.LayoutParams.MATCH_PARENT ));
        }
        mFullScreenDialog.show();
        mPlayerSetting.setPlayerFullScreen(true);
    }
    
    void qualityControlVisibility( ) {
        if ( mPlayerDataViewModel.isSpeed() ) {
            mPlayerDataViewModel.setIsLinearLayoutQuality(false);
        }
        if ( ! Util.getValueOrDefault(mPlayerDataViewModel.getIsLinearLayoutQuality().getValue(), false) ) {
            linearLayout_quality.setVisibility(View.VISIBLE);
            btQuality240.setVisibility(View.VISIBLE);
            btQuality480.setVisibility(View.VISIBLE);
            btQuality720.setVisibility(View.VISIBLE);
            speed05x.setVisibility(View.GONE);
            speed1x.setVisibility(View.GONE);
            speed15x.setVisibility(View.GONE);
            speed2x.setVisibility(View.GONE);
            mPlayerDataViewModel.setIsLinearLayoutQuality(true);
            mPlayerDataViewModel.setQuality(true);
            mPlayerDataViewModel.setSpeed(false);
        } else {
            
            linearLayout_quality.setVisibility(View.GONE);
            mPlayerDataViewModel.setIsLinearLayoutQuality(false);
            
            
        }
    }
    
    public void observe( @NonNull Lifecycle lifecycle ) {
        if ( lifecycleOwnerWeakReference.get() == null || observerWeekReference.get() == null ) {
            return;
        }
        lifecycleOwnerWeakReference.get().getLifecycle().removeObserver(observerWeekReference.get());
        lifecycleOwnerWeakReference.get().getLifecycle().removeObserver(this);
        lifecycle.addObserver(this);
        lifecycle.addObserver(observerWeekReference.get());
    }
    
    @SuppressLint("SourceLockedOrientationActivity")
    private void exitFullScreenFunc( ) {
        FragmentActivity activity = getActivity();
        imageButtonFullscreen.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.player_ic_fullscreen_black_24dp));
        if ( activity != null ) {
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            if ( activity.getActionBar() != null ) {
                activity.getActionBar().show();
            }
        }
    
        LayoutParams
                params =
                null;
        if ( mBinding != null ) {
            params = (LayoutParams) mBinding.playerView.getLayoutParams();
    
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            params.height =
                    getContext().getResources().getDimensionPixelSize(R.dimen.player_height);
            mBinding.playerView.setLayoutParams(params);
    
            mPlayerDataViewModel.setFullscreen(false);
            ( (ViewGroup) mBinding.playerView.getParent() ).removeView(mBinding.playerView);
            ( (ConstraintLayout) mBinding.frameVideoLayout.getParent() ).addView(mBinding.playerView);
        }
        mFullScreenDialog.dismiss();
        
        if ( activity != null ) {
            WindowManager.LayoutParams layoutParams = activity.getWindow().getAttributes();
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            layoutParams.screenBrightness = - 1f;
            activity.getWindow().setAttributes(layoutParams);
        }
        
        
        brightnessSeekBar.setProgress(50);
        brightnessSeekBar.setVisibility(View.GONE);
        volumeSeekBar.setVisibility(View.GONE);
        volumeSeekBar.setProgress(50);
        textPercentageView.setVisibility(View.GONE);
        
        mPlayerSetting.setPlayerFullScreen(false);
        
        if ( activity != null ) {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        }
    }
    
    void speedControlVisibility( ) {
        if ( mPlayerDataViewModel.isQuality() ) {
            mPlayerDataViewModel.setIsLinearLayoutQuality(false);
        }
        if ( ! Util.getValueOrDefault(mPlayerDataViewModel.getIsLinearLayoutQuality().getValue(), false) ) {
            linearLayout_quality.setVisibility(View.VISIBLE);
            speed05x.setVisibility(View.VISIBLE);
            speed1x.setVisibility(View.VISIBLE);
            speed15x.setVisibility(View.VISIBLE);
            speed2x.setVisibility(View.VISIBLE);
            btQuality240.setVisibility(View.GONE);
            btQuality480.setVisibility(View.GONE);
            btQuality720.setVisibility(View.GONE);
            mPlayerDataViewModel.setIsLinearLayoutQuality(true);
            mPlayerDataViewModel.setSpeed(true);
            mPlayerDataViewModel.setQuality(false);
        } else {
            linearLayout_quality.setVisibility(View.GONE);
            mPlayerDataViewModel.setIsLinearLayoutQuality(false);
        }
    }
    
    void fullScreenSetting( ) {
        if ( Util.getValueOrDefault(mPlayerDataViewModel.getFullscreen().getValue(), false) ) {
            exitFullScreenFunc();
        } else {
            enterFullScreenFunc();
        }
    }
}
