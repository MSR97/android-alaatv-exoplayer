package com.alaatv.component.player;

import android.view.View;


class PlayerController implements PlayerHandler {
    PlayerSetting playerSetting;
    
    PlayerController( PlayerSetting playerSetting ) {
        this.playerSetting = playerSetting;
    }
    
    @Override
    public void startPlayButton( View view ) {
        view.setVisibility(View.GONE);
        playerSetting.initializePlayer(PlayerConstants.QUALITY_HD_480, true);
    }
    
    @Override
    public void setQuality240( ) {
        playerSetting.releasePlayer();
        playerSetting.initializePlayer(PlayerConstants.QUALITY_SD_240, false);
        
    }
    
    @Override
    public void setQuality480( ) {
        playerSetting.releasePlayer();
        playerSetting.initializePlayer(PlayerConstants.QUALITY_HD_480, false);
    }
    
    @Override
    public void setQuality720( ) {
        playerSetting.releasePlayer();
        playerSetting.initializePlayer(PlayerConstants.QUALITY_FullHD_720, false);
    }
    
    @Override
    public void setSpeedHigh( ) {
        playerSetting.setSPEED_HIGH();
        
    }
    
    @Override
    public void setSpeedMedium( ) {
        playerSetting.setSPEED_MEDIUM();
    }
    
    @Override
    public void setSpeedNormal( ) {
        playerSetting.setSPEED_NORMAL();
    }
    
    @Override
    public void setSpeedLow( ) {
        playerSetting.setSPEED_LOW();
    }
    
    
}
