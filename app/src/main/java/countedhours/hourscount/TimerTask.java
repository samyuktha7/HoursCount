package countedhours.hourscount;

import android.util.Log;

public class TimerTask {

    private long mStartTime;
    private long mBufferTime;
    private long mTotalTime;
    private String TAG = "TimerTask";

    public void startTimer() {
        Log.d(TAG, "startTimer() ");
        mStartTime = System.currentTimeMillis();
        Log.d(TAG, "startTimer(): Time elapsed = "+mTotalTime);
    }

    public void pauseTimer() {
        Log.d(TAG, "pauseTimer()");
        mBufferTime = System.currentTimeMillis() - mStartTime;
        mTotalTime = mTotalTime + mBufferTime;
        Log.d(TAG, "pauseTimer(): Time elapsed ="+mTotalTime);
    }
}
