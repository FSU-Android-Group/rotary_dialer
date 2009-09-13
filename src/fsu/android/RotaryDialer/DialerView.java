/*
 * This code is a modification of the Android LunarLander example.
 */

package fsu.android.RotaryDialer;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;


/**
 * View that draws, takes keystrokes, etc. for a simple LunarLander game.
 * 
 * Has a mode which RUNNING, PAUSED, etc. draw() renders the
 * ship, and does an invalidate() to prompt another draw() as soon as possible
 * by the system.
 */
class DialerView extends SurfaceView implements SurfaceHolder.Callback {
   
	class DialerThread extends Thread {
        /*
         * State-tracking constants
         */
        public static final int STATE_LOSE = 1;
        public static final int STATE_PAUSE = 2;
        public static final int STATE_READY = 3;
        public static final int STATE_RUNNING = 4;
        public static final int STATE_WIN = 5;
        
        public static final String PAUSE_TXT = "paused";
        public static final String READY_TXT = "ready";
        
        /*
         * Member (state) fields
         */
        /** The drawable to use as the background of the animation canvas */
        private Bitmap mBackgroundImage;

        /**
         * Current height of the surface/canvas.
         * 
         * @see #setSurfaceSize
         */
        private int mCanvasHeight = 10;

        /**
         * Current width of the surface/canvas.
         * 
         * @see #setSurfaceSize
         */
        private int mCanvasWidth = 10;

        /** Message handler used by thread to interact with TextView */
        private Handler mHandler;

        
        /** The state of the game. One of READY, RUNNING, PAUSE, LOSE, or WIN */
        private int mMode;

        /** Indicate whether the surface has been created & is ready to draw */
        private boolean mRun = false;

        /** Scratch rect object. */
        private RectF mScratchRect;

        /** Handle to the surface manager object we interact with */
        private SurfaceHolder mSurfaceHolder;

        public DialerThread(SurfaceHolder surfaceHolder, Context context,
        					Handler handler) {
        	// get handles to some important objects
            mSurfaceHolder = surfaceHolder;
            mHandler = handler;
            mContext = context;
            mScratchRect = new RectF(0, 0, 0, 0);
            setBackground();
        }
        
        private void setBackground() {
            mBackgroundImage = Bitmap.createBitmap(mCanvasWidth, mCanvasHeight,
					   Bitmap.Config.ARGB_4444);
        	Canvas tCan = new Canvas(mBackgroundImage);
        	
        	tCan.drawColor(Color.RED);
        	float radius = mCanvasWidth < mCanvasHeight ? mCanvasWidth / 2f :
        				   mCanvasHeight / 2f;
        	radius -= 18f;
        	
        	Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        	paint.setColor(Color.WHITE);
        	Log.d("setBackground", "Here I is");
        	tCan.drawCircle(mCanvasWidth / 2f, mCanvasHeight / 2f, radius, paint);
        }
        
        
        
        public void doStart() {
            synchronized (mSurfaceHolder) {
                setState(STATE_RUNNING);
            }
        }

        public void pause() {
            synchronized (mSurfaceHolder) {
                if (mMode == STATE_RUNNING) setState(STATE_PAUSE);
            }
        }

        /**
         * Restores game state from the indicated Bundle. Typically called when
         * the Activity is being restored after having been previously
         * destroyed.
         * 
         * @param savedState Bundle containing the game state
         */
        public synchronized void restoreState(Bundle savedState) {
            synchronized (mSurfaceHolder) {
                setState(STATE_PAUSE);
                //return values from bundle
            }
        }

        @Override
        public void run() {
            while (mRun) {
                Canvas c = null;
                try {
                    c = mSurfaceHolder.lockCanvas(null);
                    synchronized (mSurfaceHolder) {
                        doDraw(c);
                    }
                } finally {
                    // do this in a finally so that if an exception is thrown
                    // during the above, we don't leave the Surface in an
                    // inconsistent state
                    if (c != null) {
                        mSurfaceHolder.unlockCanvasAndPost(c);
                    }
                }
            }
        }

        /**
         * Dump game state to the provided Bundle. Typically called when the
         * Activity is being suspended.
         * 
         * @return Bundle with this view's state
         */
        public Bundle saveState(Bundle map) {
            synchronized (mSurfaceHolder) {
            	//put values into Bundle
            }
            return map;
        }

       
        /**
         * Used to signal the thread whether it should be running or not.
         * Passing true allows the thread to run; passing false will shut it
         * down if it's already running. Calling start() after this was most
         * recently called with false will result in an immediate shutdown.
         * 
         * @param b true to run, false to shut down
         */
        public void setRunning(boolean b) {
            mRun = b;
        }

        /**
         * Sets the game mode. That is, whether we are running, paused, in the
         * failure state, in the victory state, etc.
         * 
         * @see #setState(int, CharSequence)
         * @param mode one of the STATE_* constants
         */
        public void setState(int mode) {
            synchronized (mSurfaceHolder) {
                setState(mode, null);
            }
        }

        /**
         * Sets the game mode. That is, whether we are running, paused, in the
         * failure state, in the victory state, etc.
         * 
         * @param mode one of the STATE_* constants
         * @param message string to add to screen or null
         */
        public void setState(int mode, CharSequence message) {
            /*
             * This method optionally can cause a text message to be displayed
             * to the user when the mode changes. Since the View that actually
             * renders that text is part of the main View hierarchy and not
             * owned by this thread, we can't touch the state of that View.
             * Instead we use a Message + Handler to relay commands to the main
             * thread, which updates the user-text View.
             */
            synchronized (mSurfaceHolder) {
                mMode = mode;

                if (mMode == STATE_RUNNING) {
                    Message msg = mHandler.obtainMessage();
                    Bundle b = new Bundle();
                    b.putString("text", "");
                    b.putInt("viz", View.INVISIBLE);
                    msg.setData(b);
                    mHandler.sendMessage(msg);
                } else {
                    CharSequence str = "";
                    if (mMode == STATE_READY)
                        str = READY_TXT;
                    else if (mMode == STATE_PAUSE)
                        str = PAUSE_TXT;
                   
                    if (message != null) {
                        str = message + "\n" + str;
                    }

                    Message msg = mHandler.obtainMessage();
                    Bundle b = new Bundle();
                    b.putString("text", str.toString());
                    b.putInt("viz", View.VISIBLE);
                    msg.setData(b);
                    mHandler.sendMessage(msg);
                }
            }
        }

        /* Callback invoked when the surface dimensions change. */
        public void setSurfaceSize(int width, int height) {
            // synchronized to make sure these all change atomically
            synchronized (mSurfaceHolder) {
                mCanvasWidth = width;
                mCanvasHeight = height;
            }
        }

        /**
         * Resumes from a pause.
         */
        public void unpause() {
            setState(STATE_RUNNING);
        }

        /**
         * Handles a key-down event.
         * 
         * @param keyCode the key that was pressed
         * @param msg the original event object
         * @return true
         */
        //boolean doKeyDown(int keyCode, KeyEvent msg) {
          //  synchronized (mSurfaceHolder) {
         		//do stuff
         //       return false;
       //     }
       // }

        /**
         * Handles a key-up event.
         * 
         * @param keyCode the key that was pressed
         * @param msg the original event object
         * @return true if the key was handled and consumed, or else false
         */
       

        /**
         * Draws the canvas
         */
        private void doDraw(Canvas canvas) {
            // Draw the background image. Operations on the Canvas accumulate
            // so this is like clearing the screen.
            canvas.drawBitmap(mBackgroundImage, 0, 0, null);
          //  mScratchRect.set(4, 4, 4 + fuelWidth, 4 + UI_BAR_HEIGHT);


            
            /*
            // Draw the ship with its current rotation
            canvas.save();
            canvas.rotate((float) mHeading, (float) mX, mCanvasHeight
                    - (float) mY);
            if (mMode == STATE_LOSE) {
                mCrashedImage.setBounds(xLeft, yTop, xLeft + mLanderWidth, yTop
                        + mLanderHeight);
                mCrashedImage.draw(canvas);
            } else if (mEngineFiring) {
                mFiringImage.setBounds(xLeft, yTop, xLeft + mLanderWidth, yTop
                        + mLanderHeight);
                mFiringImage.draw(canvas);
            } else {
                mLanderImage.setBounds(xLeft, yTop, xLeft + mLanderWidth, yTop
                        + mLanderHeight);
                mLanderImage.draw(canvas);
            }
            canvas.restore();
        	*/
        }
	}
        
    /** Handle to the application context, used to e.g. fetch Drawables. */
    private Context mContext;

    /** Pointer to the text view to display "Paused.." etc. */
    private TextView mStatusText;

    /** The thread that actually draws the animation */
    private DialerThread thread;

    public DialerView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // register our interest in hearing about changes to our surface
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);

        // create thread only; it's started in surfaceCreated()
        thread = new DialerThread(holder, context, new Handler() {
            @Override
            public void handleMessage(Message m) {
               // mStatusText.setVisibility(m.getData().getInt("viz"));
                //mStatusText.setText(m.getData().getString("text"));
            }
        });

        setFocusable(true); // make sure we get key events
    }

    /**
     * Fetches the animation thread corresponding to this LunarView.
     * 
     * @return the animation thread
     */
    public DialerThread getThread() {
        return thread;
    }

    /**
     * Standard window-focus override. Notice focus lost so we can pause on
     * focus lost. e.g. user switches to take a call.
     */
    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        if (!hasWindowFocus) thread.pause();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
    	
    	
    	
    	
    	return false;	//not handled
    }
    
    /**
     * Installs a pointer to the text view used for messages.
     */
    public void setTextView(TextView textView) {
        mStatusText = textView;
    }

    /* Callback invoked when the surface dimensions change. */
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
            int height) {
        thread.setSurfaceSize(width, height);
        thread.setBackground();
    }

    /*
     * Callback invoked when the Surface has been created and is ready to be
     * used.
     */
    public void surfaceCreated(SurfaceHolder holder) {
        // start the thread here so that we don't busy-wait in run()
        // waiting for the surface to be created
        thread.setRunning(true);
        thread.start();
    }

    /*
     * Callback invoked when the Surface has been destroyed and must no longer
     * be touched. WARNING: after this method returns, the Surface/Canvas must
     * never be touched again!
     */
    public void surfaceDestroyed(SurfaceHolder holder) {
        // we have to tell thread to shut down & wait for it to finish, or else
        // it might touch the Surface after we return and explode
        boolean retry = true;
        thread.setRunning(false);
        while (retry) {
            try {
                thread.join();
                retry = false;
            } catch (InterruptedException e) {
            }
        }
    }
}
