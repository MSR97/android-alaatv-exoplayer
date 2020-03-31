package com.alaatv.component.player;

import android.content.Context;
import android.media.audiofx.LoudnessEnhancer;
import android.os.Build;

import com.google.android.exoplayer2.audio.AudioListener;

import timber.log.Timber;

public class PlayerLoudnessEnhancerController implements AudioListener {
    private static final String TAG = PlayerLoudnessEnhancerController.class.getSimpleName();
    
    
    private LoudnessEnhancer enhancer;
    private boolean          released       = false;
    private int              audioSessionId = 0;
    
    PlayerLoudnessEnhancerController( Context context, int audioSessionId ) {
        try {
            this.audioSessionId = audioSessionId;
            if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT ) {
                enhancer = new LoudnessEnhancer(audioSessionId);
            }
        }
        catch ( Throwable x ) {
            Timber.tag(TAG).d(x, "Failed to create enhancer");
        }
    }
    
    boolean isEnabled( ) {
        try {
            return isAvailable() && enhancer.getEnabled();
        }
        catch ( Exception e ) {
            return false;
        }
    }
    
    private boolean isAvailable( ) {
        return enhancer != null;
    }
    
    void enable( ) {
        if ( enhancer != null )
            enhancer.setEnabled(true);
    }
    
    void disable( ) {
        enhancer.setEnabled(false);
    }
    
    public float getGain( ) {
        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT ) {
            return enhancer.getTargetGain();
        }
        return 0;
    }
    
    void setGain( int gain ) {
        if ( enhancer != null )
            enhancer.setTargetGain(gain);
    }
    
    void release( ) {
        if ( isAvailable() ) {
            enhancer.release();
            released = true;
        }
    }
    
}