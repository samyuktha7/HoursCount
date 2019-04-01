package countedhours.hourscount.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import countedhours.hourscount.R;


public class Today_Fragment extends Fragment {

    private TextView mTimeRemaining, mHoursCompleted, mInOffice;
    private String TAG = "TodayFragment";
    private DateFormat formatter;
    private SharedPreferences mSharedPreferences;
    private boolean alreadyStarted = false;
    private Handler updateTimeHandler = new Handler();
    private boolean inOffice = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_today_, container, false);
        mHoursCompleted = v.findViewById(R.id.timeElapsed);
        mTimeRemaining = v.findViewById(R.id.timeLeftOut);
        mInOffice = v.findViewById(R.id.InOffice);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");

        formatter = new SimpleDateFormat("HH:mm:ss", Locale.US);
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));

        if (this.getActivity() != null) {
            mSharedPreferences = this.getActivity().getSharedPreferences("TIME", Context.MODE_PRIVATE);
            if (mSharedPreferences != null) {
                updateTimeElapsed();
                if (!alreadyStarted) {
                    updateTimeHandler.postDelayed(updateTimerThread, 0);
                }
                alreadyStarted = true;
            } else {
                Log.d(TAG, "sharedpreferences Context null");
            }
        } else {
            Log.w(TAG, "context is null");
        }
    }

    private Runnable updateTimerThread = new Runnable() {
        public void run() {
            if (checkInOffice()) {
                updateTimeElapsed();
            } else {
                Log.w(TAG, "out of office");
            }
            updateTimeHandler.postDelayed(this, 1000);
        }
    };


    private void updateTimeElapsed() {
        Log.d(TAG, "updateTimeElapsed()");

        long startTime = mSharedPreferences.getLong("StartTime", 0);
        long totalTime = mSharedPreferences.getLong("TotalTime", 0);
        if (startTime != 0) {
            long bufferTime = System.currentTimeMillis() - startTime;
            totalTime = totalTime + bufferTime;
            Log.d(TAG, "time() " + totalTime);
            updateUI(totalTime);
        }
    }

    private boolean checkInOffice() {
        boolean check = mSharedPreferences.getBoolean("InOffice", false);
        if (inOffice != check) {
            inOffice = check;
            if (inOffice) {
                mInOffice.setText("In Office");
            } else {
                mInOffice.setText("Out Of Office");
            }
        }
        Log.d(TAG, "checkInOffice() "+inOffice);
        return inOffice;
    }

    private void updateUI(long totalTime) {
        Log.d(TAG, "updateUI()");
        //setting the time Elapsed
        mHoursCompleted.setText(formatter.format(new Date(totalTime)) + " Finished");

        //Calculating and setting the time Remaining
        long timeToWork = ((8 * 60 * 60000) - totalTime);
        mTimeRemaining.setText(formatter.format(new Date(timeToWork)) + " Remaining ");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause()");
        updateTimeHandler.removeCallbacks(updateTimerThread);
        alreadyStarted = false;
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}
