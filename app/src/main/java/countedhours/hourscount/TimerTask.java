package countedhours.hourscount;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;
import countedhours.hourscount.Interfaces.IUpdateTimeElapsed;

public class TimerTask {

    private long mStartTime;
    private long mBufferTime;
    private long mTotalTime;
    private String TAG = "TimerTask";
    private boolean alreadyStarted = false;
    private Handler updateTimeHandler = new Handler();
    private static TimerTask mInstance = null;
    private Context mContext;
    private List<IUpdateTimeElapsed> mUpdateTineElapsedListeners = new ArrayList<>();

    public TimerTask(Context context) {
        this.mContext = context;
    }

    public static TimerTask getInstance(Context ctx) {
        Log.d("TimerTask", "TimerTask(): getInstance()");
        /**
         * use the application context as suggested by CommonsWare.
         * this will ensure that you dont accidentally leak an Activitys
         * context (see this article for more information:
         * http://android-developers.blogspot.nl/2009/01/avoiding-memory-leaks.html)
         */
        if (mInstance == null) {
            mInstance = new TimerTask(ctx.getApplicationContext());
        }
        return mInstance;
    }

    public void startTimer() {
        mStartTime = System.currentTimeMillis();
        if (!alreadyStarted) {
            updateTimeHandler.postDelayed(updateTimerThread, 0);
        }
        Log.d(TAG, "startTimer(): Time elapsed = "+mTotalTime);
        alreadyStarted = false;
    }

    public void pauseTimer() {
        mBufferTime = System.currentTimeMillis() - mStartTime;
        mTotalTime = mTotalTime + mBufferTime;
        Log.d(TAG, "pauseTimer(): Time elapsed ="+mTotalTime);
    }

    /*
    registers Listeners to IupdateTimeElapsed interface
     */
    public void registerUpdateTimeElapsed(IUpdateTimeElapsed listener) {
        Log.d(TAG, "registerUpdateTimeElapsed() ");
        if (!mUpdateTineElapsedListeners.contains(listener)) {
           mUpdateTineElapsedListeners.add(listener);

        }
    }

    /*
    unregisters Listeners to IupdateTimeElapsed interface
     */
    public void unregisterupdateTimeElapsed(IUpdateTimeElapsed listener) {
        Log.d(TAG, "unregisterUpdateTimeElapsed()");
        mUpdateTineElapsedListeners.remove(listener);
    }

    /*
    This Runnable updates to all the listeners the time elapsed every two minutes
     */
    private Runnable updateTimerThread = new Runnable() {
        public void run() {

            mBufferTime = System.currentTimeMillis() - mStartTime;
            long time = mTotalTime + mBufferTime;
            Log.d(TAG, "time() "+time);

            //updates all the listeners
            for (IUpdateTimeElapsed listener : mUpdateTineElapsedListeners) {
                listener.onUpdateTimeElapsed(time);
            }

            //self called every 2 minutes.
           updateTimeHandler.postDelayed(this, 60000);
        }
    };
}
