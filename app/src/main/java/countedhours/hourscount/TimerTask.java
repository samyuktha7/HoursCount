package countedhours.hourscount;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;

public class TimerTask {

    private long mStartTime;
    private long mBufferTime;
    private long mTotalTime;
    private String TAG = "TimerTask";
    private boolean alreadyStarted = false;
    private Handler updateTimeHandler = new Handler();
    private Context mContext;

    public TimerTask(Context context) {
        this.mContext = context;
    }

    public void startTimer() {
        if (!alreadyStarted) {
            updateTimeHandler.postDelayed(updateTimerThread, 0);
        }
        Log.d(TAG, "startTimer() ");
        mStartTime = System.currentTimeMillis();
        Log.d(TAG, "startTimer(): Time elapsed = "+mTotalTime);
        alreadyStarted = false;
    }

    public void pauseTimer() {
        Log.d(TAG, "pauseTimer()");
        mBufferTime = System.currentTimeMillis() - mStartTime;
        mTotalTime = mTotalTime + mBufferTime;
        Log.d(TAG, "pauseTimer(): Time elapsed ="+mTotalTime);
    }

    private Runnable updateTimerThread = new Runnable() {

        public void run() {

            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
            SharedPreferences.Editor editor = sp.edit();
            mBufferTime = System.currentTimeMillis() - mStartTime;
            float timeInMinutes = (mTotalTime + mBufferTime)/60000;
            Log.d(TAG, "timeInMinutes() "+timeInMinutes);
            editor.putFloat("Time", timeInMinutes);
            editor.apply();

           updateTimeHandler.postDelayed(this, 60000);
        }

    };
}
