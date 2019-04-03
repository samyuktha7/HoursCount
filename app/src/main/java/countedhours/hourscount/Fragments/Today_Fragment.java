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

    private TextView mTimeRemaining, mHoursCompleted, mInOffice, mLastCheckedIn;
    private String TAG = "TodayFragment";
    private DateFormat formatter;
    private SharedPreferences mSharedPreferences;
    private boolean alreadyStarted = false;
    private Handler updateTimeHandler = new Handler();
    private boolean inOffice;
    private boolean resetEverything = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_today_, container, false);

        //Initializing UI components
        mHoursCompleted = v.findViewById(R.id.timeElapsed);
        mTimeRemaining = v.findViewById(R.id.timeLeftOut);
        mInOffice = v.findViewById(R.id.InOffice);
        mLastCheckedIn = v.findViewById(R.id.LastCheckedIn);
        return v;
    }


    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");

        //Used for Time displaying Time worked and Time remaining
        formatter = new SimpleDateFormat("HH:mm:ss", Locale.US);
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));

        //If the context is not null, get Shared Preferences.
        if (this.getActivity() != null) {
            mSharedPreferences = this.getActivity().getSharedPreferences("TIME", Context.MODE_PRIVATE);
            if (mSharedPreferences != null) {

                //start the UI handler to update every second.
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

    /*
    updateTimerThread updates the UI page every second.
    ------loop -----
    If InOffice, update the UI otherwise
    reset the values.
    ---loopEnd------
     */
    private Runnable updateTimerThread = new Runnable() {
        public void run() {
            if (checkInOffice()) {
                updateTimeElapsed();
            } else {
                Log.w(TAG, "out of office");
                // reset during updateThreadHandler will update the UI with reset values.
                if (!resetEverything) {
                    long totalTime = mSharedPreferences.getLong("TotalTime", 0);
                    if (totalTime == 0) {
                        updateUI(0, true);
                    }
                    resetEverything = true;
                }
            }
            //loop
            updateTimeHandler.postDelayed(this, 1000);
        }
    };


    /*
    This method is called by the handler updating, only when we are in the office.
    Checks the startTime is not equal to 0, updates UI to values,
    otherwise update UI values to reset values.
     */
    private void updateTimeElapsed() {
        Log.d(TAG, "updateTimeElapsed()");

        long startTime = mSharedPreferences.getLong("StartTime", 0);
        long totalTime = mSharedPreferences.getLong("TotalTime", 0);
        if (startTime != 0) {
            long bufferTime = System.currentTimeMillis() - startTime;
            totalTime = totalTime + bufferTime;
            Log.d(TAG, "time() " + totalTime);
            updateUI(totalTime, false);
            String checkIn = mSharedPreferences.getString("LastCheckedIn", null);
            if (checkIn != null) {
                mLastCheckedIn.setText(checkIn);
            }
        } else {
            Log.d(TAG, "startTime equal to 0");
            updateUI(0, true);
        }
    }

    /*
    checks whether we are in office and updates UI
     */
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

    /*
    updates UI with real or reset values
     */
    private void updateUI(long totalTime, boolean reset) {
        Log.d(TAG, "updateUI()");
         if (!reset) {
             //setting the time Elapsed
             mHoursCompleted.setText(formatter.format(new Date(totalTime)) + " Finished");

             //Calculating and setting the time Remaining
             long timeToWork = ((8 * 60 * 60000) - totalTime);
             mTimeRemaining.setText(formatter.format(new Date(timeToWork)) + " Remaining ");
         } else {
             mHoursCompleted.setText("0 hours finished");
             mTimeRemaining.setText("8 hours remaining");
             mLastCheckedIn.setText("00:00");
             mInOffice.setText("Out Of Office");
         }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause()");
        updateTimeHandler.removeCallbacks(updateTimerThread);
        alreadyStarted = false;
        inOffice = false;
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}
