package com.alaatv.component.player;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import timber.log.Timber;

public abstract class PlayerOnGestureRegisterListener implements View.OnTouchListener {
    private GestureDetector gestureDetector;
    
    public PlayerOnGestureRegisterListener( Context context, View view ) {
        gestureDetector =
                new GestureDetector(context, new GestureListener(new GestureListener.Callback() {
                    
                    @Override
                    public void onSingleTapUp( ) {
                        onClick(view);
                    }
                    
                    @Override
                    public void onLongPress( ) {
                        onLongClick(view);
                    }
                    
                    @Override
                    public void onDoubleTapEvent( ) {
                        onDoubleTaps(view);
                    }
                    
                    @Override
                    public void onSwipeR( ) {
                        onSwipeRight(view);
                    }
                    
                    @Override
                    public void onSwipeL( ) {
                        onSwipeLeft(view);
                    }
                    
                    @Override
                    public void onSwipeT( ) {
                        onSwipeTop(view);
                    }
                    
                    @Override
                    public void onSwipeB( ) {
                        onSwipeBottom(view);
                    }
                }));
    }
    
    public abstract void onLongClick( View view );
    
    public abstract void onDoubleTaps( View view );
    
    public abstract void onSwipeRight( View view );
    
    public abstract void onSwipeLeft( View view );
    
    public abstract void onSwipeBottom( View view );
    
    public abstract void onSwipeTop( View view );
    
    public abstract void onClick( View view );
    
    @Override
    public boolean onTouch( View v, MotionEvent event ) {
        return gestureDetector.onTouchEvent(event);
    }
    
    private static class GestureListener extends GestureDetector.SimpleOnGestureListener {
        Callback callback;
        
        GestureListener( Callback callback ) {
            this.callback = callback;
        }
        
        @Override
        public boolean onSingleTapUp( MotionEvent e ) {
            if ( callback != null ) {
                callback.onSingleTapUp();
            }
            return super.onSingleTapUp(e);
        }
        
        @Override
        public void onLongPress( MotionEvent e ) {
            if ( callback != null ) {
                callback.onLongPress();
            }
            super.onLongPress(e);
        }
        
        @Override
        public boolean onFling( MotionEvent e1, MotionEvent e2, float velocityX, float velocityY ) {
            try {
                float diffY = e2.getY() - e1.getY();
                float diffX = e2.getX() - e1.getX();
                if ( Math.abs(diffX) > Math.abs(diffY) ) {
                    if ( Math.abs(diffX) > PlayerConstants.SWIPE_THRESHOLD &&
                         Math.abs(velocityX) > PlayerConstants.SWIPE_VELOCITY_THRESHOLD ) {
                        if ( callback != null ) {
                            if ( diffX > 0 ) {
                                callback.onSwipeR();
                            } else {
                                callback.onSwipeL();
                            }
                        }
                        return true;
                    }
                } else if ( Math.abs(diffY) > PlayerConstants.SWIPE_THRESHOLD &&
                            Math.abs(velocityY) > PlayerConstants.SWIPE_VELOCITY_THRESHOLD ) {
                    if ( callback != null ) {
                        if ( diffY > 0 ) {
                            callback.onSwipeB();
                        } else {
                            callback.onSwipeT();
                        }
                    }
                    return true;
                }
            }
            catch ( Exception exception ) {
                Timber.e(exception);
            }
            return false;
        }
        
        @Override
        public boolean onDown( MotionEvent e ) {
            return true;
        }
        
        @Override
        public boolean onDoubleTapEvent( MotionEvent e ) {
            if ( callback != null ) {
                callback.onDoubleTapEvent();
            }
            return super.onDoubleTapEvent(e);
        }
        
        protected interface Callback {
            void onSingleTapUp( );
            
            void onLongPress( );
            
            void onDoubleTapEvent( );
            
            void onSwipeR( );
            
            void onSwipeL( );
            
            void onSwipeT( );
            
            void onSwipeB( );
        }
    }
    
}
