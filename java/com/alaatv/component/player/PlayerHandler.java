package com.alaatv.component.player;

import android.view.View;

public interface PlayerHandler {
    void startPlayButton( View view );
    
    void setQuality240( );
    
    void setQuality480( );
    
    void setQuality720( );
    
    void setSpeedHigh( );
    
    void setSpeedMedium( );
    
    void setSpeedNormal( );
    
    void setSpeedLow( );
}
