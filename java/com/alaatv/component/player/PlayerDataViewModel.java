package com.alaatv.component.player;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.HashMap;
import java.util.Objects;

import static com.alaatv.component.player.PlayerConstants.QUALITY_FullHD_720;
import static com.alaatv.component.player.PlayerConstants.QUALITY_HD_480;
import static com.alaatv.component.player.PlayerConstants.QUALITY_SD_240;

public class PlayerDataViewModel extends ViewModel {
    private MutableLiveData<DifferentQualityUrl>
            differentQualityUrl =
            new MutableLiveData<>(new DifferentQualityUrl());
    
    private MutableLiveData<String>
            mVastUrl =
            new MutableLiveData<>("https://alaatv.com/vast.xml");
    
    private MutableLiveData<Boolean> fullscreen            = new MutableLiveData<>(false);
    private MutableLiveData<Boolean> isLinearLayoutQuality = new MutableLiveData<>(false);
    private boolean                  isSpeed               = false;
    private boolean                  isQuality             = false;
    private boolean                  isExoPlayerLock       = false;
    private boolean                  isVideoStarted        = false;
    private LiveData<HashMap<String, String>>
                                     videoUrls             =
            Transformations.switchMap(differentQualityUrl, input -> new MutableLiveData<>(input.getVideoUrls()));
    
    private MutableLiveData<String> mPoster = new MutableLiveData<>();
    
    @NonNull
    public MutableLiveData<String> getPoster( ) {
        return mPoster;
    }
    
    public void setPoster( @Nullable String poster ) {
        if ( poster == null || ! Objects.equals(mPoster.getValue(), poster) ) {
            mPoster.setValue(poster);
        }
    }
    
    public boolean isSpeed( ) {
        return isSpeed;
    }
    
    public void setSpeed( boolean speed ) {
        isSpeed = speed;
    }
    
    public boolean isQuality( ) {
        return isQuality;
    }
    
    public void setQuality( boolean quality ) {
        isQuality = quality;
    }
    
    public boolean isExoPlayerLock( ) {
        return isExoPlayerLock;
    }
    
    public void setExoPlayerLock( boolean exoPlayerLock ) {
        isExoPlayerLock = exoPlayerLock;
    }
    
    public boolean isVideoStarted( ) {
        return isVideoStarted;
    }
    
    public void setVideoStarted( boolean videoStarted ) {
        isVideoStarted = videoStarted;
    }
    
    public LiveData<HashMap<String, String>> getVideoUrls( ) {
        return videoUrls;
    }
    
    public void setVideoUrls( @NonNull HashMap<String, String> videoUrls ) {
        setVideoUrls(videoUrls.get(String.valueOf(QUALITY_FullHD_720)), videoUrls.get(String.valueOf(QUALITY_HD_480)), videoUrls.get(String.valueOf(QUALITY_SD_240)));
    }
    
    public void setVideoUrls( @Nullable String l720, @Nullable String l480, @Nullable String l240 ) {
        DifferentQualityUrl update = new DifferentQualityUrl(l720, l480, l240);
        if ( Objects.equals(differentQualityUrl.getValue(), update) ) {
            return;
        }
        differentQualityUrl.setValue(update);
    }
    
    public MutableLiveData<Boolean> getFullscreen( ) {
        return fullscreen;
    }
    
    public void setFullscreen( boolean fullscreen ) {
        if ( this.fullscreen.getValue() != null && this.fullscreen.getValue() == fullscreen ) {
            return;
        }
        this.fullscreen.setValue(fullscreen);
    }
    
    public MutableLiveData<String> getVastUrl( ) {
        return mVastUrl;
    }
    
    public void setVastUrl( String vastUrl ) {
        if ( Objects.equals(this.mVastUrl.getValue(), vastUrl) ) {
            return;
        }
        this.mVastUrl.setValue(vastUrl);
    }
    
    public MutableLiveData<Boolean> getIsLinearLayoutQuality( ) {
        return isLinearLayoutQuality;
    }
    
    public void setIsLinearLayoutQuality( boolean isLinearLayoutQuality ) {
        if ( this.isLinearLayoutQuality.getValue() != null &&
             this.isLinearLayoutQuality.getValue() == isLinearLayoutQuality ) {
            return;
        }
        this.isLinearLayoutQuality.setValue(isLinearLayoutQuality);
    }
    
    static class DifferentQualityUrl {
        final String mVideoUrl720;
        final String mVideoUrl480;
        final String mVideoUrl240;
        
        public DifferentQualityUrl( ) {
            this(null, null, null);
        }
        
        public DifferentQualityUrl( String videoUrl720, String videoUrl480, String videoUrl240 ) {
            this.mVideoUrl720 = videoUrl720 == null ? null : videoUrl720.trim();
            this.mVideoUrl480 = videoUrl480 == null ? null : videoUrl480.trim();
            this.mVideoUrl240 = videoUrl240 == null ? null : videoUrl240.trim();
        }
        
        @NonNull
        public HashMap<String, String> getVideoUrls( ) {
            HashMap<String, String> videoQualityAddress = new HashMap<>();
            if ( mVideoUrl240 != null ) {
                videoQualityAddress.put(String.valueOf(QUALITY_SD_240), mVideoUrl240);
            }
            if ( mVideoUrl480 != null ) {
                videoQualityAddress.put(String.valueOf(QUALITY_HD_480), mVideoUrl480);
            }
            if ( mVideoUrl720 != null ) {
                videoQualityAddress.put(String.valueOf(QUALITY_FullHD_720), mVideoUrl720);
            }
            return videoQualityAddress;
        }
        
        @Override
        public int hashCode( ) {
            return new HashCodeBuilder()
                           .append(mVideoUrl720)
                           .append(mVideoUrl480)
                           .append(mVideoUrl240)
                           .toHashCode();
        }
        
        @Override
        public boolean equals( Object other ) {
            if ( other == this ) {
                return true;
            }
            if ( ! ( other instanceof DifferentQualityUrl ) ) {
                return false;
            }
            DifferentQualityUrl rhs = ( (DifferentQualityUrl) other );
            return new EqualsBuilder()
                           .append(mVideoUrl720, rhs.mVideoUrl720)
                           .append(mVideoUrl480, rhs.mVideoUrl480)
                           .append(mVideoUrl240, rhs.mVideoUrl240)
                           .isEquals();
        }
    }
}
