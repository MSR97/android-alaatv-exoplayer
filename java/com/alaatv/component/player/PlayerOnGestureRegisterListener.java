package com.alaatv.component.player;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import java.lang.ref.WeakReference;

import timber.log.Timber;

public abstract class PlayerOnGestureRegisterListener implements View.OnTouchListener {
    private WeakReference<GestureDetector> gestureDetectorWeakReference;
    
    public PlayerOnGestureRegisterListener( Context context, View view ) {
        GestureDetector gestureDetector =
                new GestureDetector(context, new GestureListener(new GestureListener.Callback() {
                    WeakReference<View> viewWeakReference = new WeakReference<>(view);
                    
                    @Override
                    public void onSingleTapUp( ) {
                        if ( getView() == null ) {
                            return;
                        }
                        onClick(getView());
                    }
                    
                    View getView( ) {
                        return viewWeakReference.get();
                    }
                    
                    @Override
                    public void onLongPress( ) {
                        if ( getView() == null ) {
                            return;
                        }
                        onLongClick(getView());
                    }
                    
                    @Override
                    public void onDoubleTapEvent( ) {
                        if ( getView() == null ) {
                            return;
                        }
                        onDoubleTaps(getView());
                    }
                    
                    @Override
                    public void onSwipeR( ) {
                        if ( getView() == null ) {
                            return;
                        }
                        onSwipeRight(getView());
                    }
                    
                    @Override
                    public void onSwipeL( ) {
                        if ( getView() == null ) {
                            return;
                        }
                        onSwipeLeft(getView());
                    }
                    
                    @Override
                    public void onSwipeT( ) {
                        if ( getView() == null ) {
                            return;
                        }
                        onSwipeTop(getView());
                    }
                    
                    @Override
                    public void onSwipeB( ) {
                        if ( getView() == null ) {
                            return;
                        }
                        onSwipeBottom(getView());
                    }
                }));
        gestureDetectorWeakReference = new WeakReference<>(gestureDetector);
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
        if ( gestureDetectorWeakReference.get() != null ) {
            return gestureDetectorWeakReference.get().onTouchEvent(event);
        }
        return false;
    }
    
    private static class GestureListener extends GestureDetector.SimpleOnGestureListener {
        WeakReference<Callback> callback;
        
        GestureListener( Callback callback ) {
            this.callback = new WeakReference<>(callback);
        }
        
        @Override
        public boolean onSingleTapUp( MotionEvent e ) {
            if ( callback.get() != null ) {
                callback.get().onSingleTapUp();
            }
            return super.onSingleTapUp(e);
        }
        
        @Override
        public void onLongPress( MotionEvent e ) {
            if ( callback.get() != null ) {
                callback.get().onLongPress();
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
                        if ( callback.get() != null ) {
                            if ( diffX > 0 ) {
                                callback.get().onSwipeR();
                            } else {
                                callback.get().onSwipeL();
                            }
                        }
                        return true;
                    }
                } else if ( Math.abs(diffY) > PlayerConstants.SWIPE_THRESHOLD &&
                            Math.abs(velocityY) > PlayerConstants.SWIPE_VELOCITY_THRESHOLD ) {
                    if ( callback.get() != null ) {
                        if ( diffY > 0 ) {
                            callback.get().onSwipeB();
                        } else {
                            callback.get().onSwipeT();
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
            if ( callback.get() != null ) {
                callback.get().onDoubleTapEvent();
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
