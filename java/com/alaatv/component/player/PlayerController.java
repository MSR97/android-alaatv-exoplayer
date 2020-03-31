package com.alaatv.component.player;

import android.view.View;

import java.lang.ref.WeakReference;


class PlayerController implements PlayerHandler {
    WeakReference<PlayerSetting> playerSettingWeakReference;
    
    PlayerController( PlayerSetting playerSetting ) {
        playerSettingWeakReference = new WeakReference<>(playerSetting);
    }
    
    @Override
    public void startPlayButton( View view ) {
        PlayerSetting playerSetting = playerSettingWeakReference.get();
        if ( playerSetting == null ) {
            return;
        }
        view.setVisibility(View.GONE);
        playerSetting.initializePlayer(PlayerConstants.QUALITY_HD_480, true);
    }
    
    @Override
    public void setQuality240( ) {
        PlayerSetting playerSetting = playerSettingWeakReference.get();
        if ( playerSetting == null ) {
            return;
        }
        playerSetting.releasePlayer();
        playerSetting.initializePlayer(PlayerConstants.QUALITY_SD_240, false);
        
    }
    
    @Override
    public void setQuality480( ) {
        PlayerSetting playerSetting = playerSettingWeakReference.get();
        if ( playerSetting == null ) {
            return;
        }
        playerSetting.releasePlayer();
        playerSetting.initializePlayer(PlayerConstants.QUALITY_HD_480, false);
    }
    
    @Override
    public void setQuality720( ) {
        PlayerSetting playerSetting = playerSettingWeakReference.get();
        if ( playerSetting == null ) {
            return;
        }
        playerSetting.releasePlayer();
        playerSetting.initializePlayer(PlayerConstants.QUALITY_FullHD_720, false);
    }
    
    @Override
    public void setSpeedHigh( ) {
        PlayerSetting playerSetting = playerSettingWeakReference.get();
        if ( playerSetting == null ) {
            return;
        }
        playerSetting.setSPEED_HIGH();
        
    }
    
    @Override
    public void setSpeedMedium( ) {
        PlayerSetting playerSetting = playerSettingWeakReference.get();
        if ( playerSetting == null ) {
            return;
        }
        playerSetting.setSPEED_MEDIUM();
        
    }
    
    @Override
    public void setSpeedNormal( ) {
        PlayerSetting playerSetting = playerSettingWeakReference.get();
        if ( playerSetting == null ) {
            return;
        }
        playerSetting.setSPEED_NORMAL();
        
    }
    
    @Override
    public void setSpeedLow( ) {
        PlayerSetting playerSetting = playerSettingWeakReference.get();
        if ( playerSetting == null ) {
            return;
        }
        playerSetting.setSPEED_LOW();
        
    }
    
    
}
