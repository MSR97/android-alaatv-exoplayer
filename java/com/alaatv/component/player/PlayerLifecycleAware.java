package com.alaatv.component.player;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import org.jetbrains.annotations.Contract;

import timber.log.Timber;


public class PlayerLifecycleAware implements LifecycleObserver {
    
    PlayerSetting mPlayerSetting;
    
    @Contract(pure = true)
    PlayerLifecycleAware( PlayerSetting playerSetting ) {
        mPlayerSetting = playerSetting;
        Timber.d("Observer PlayerLifecycleAware");
    }
    
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    protected void onCreate( ) {
        Timber.d("Observer ON_CREATE");
    }
    
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    protected void onStart( ) {
        mPlayerSetting.startPlayer();
        //Log.i("EXO", "Observer ON_Start");
        Timber.d("Observer onStart");
        
        
    }
    
    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    protected void onResume( ) {
        mPlayerSetting.startPlayer();
        Timber.d("Observer onResume");
        
        
    }
    
    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    protected void onPause( ) {
        mPlayerSetting.pausePlayer();
        Timber.d("Observer onPause");
    }
    
    
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    protected void onStop( ) {
        Timber.d("Observer ON_Stop");
    }
    
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    protected void onDestroy( ) {
        mPlayerSetting.releasePlayer();
        
        Timber.d("Observer ON_Destroy");
    }
}
