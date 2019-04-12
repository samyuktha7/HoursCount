package countedhours.hourscount.Fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.budiyev.android.circularprogressbar.CircularProgressBar;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import countedhours.hourscount.Activities.ProfileActivity;
import countedhours.hourscount.CommonUtils;
import countedhours.hourscount.R;
import countedhours.hourscount.service.GeoIntentService;


public class Today_Fragment extends Fragment {

    private TextView mTimeRemaining, mHoursCompleted, mInOffice, mLastCheckedIn, mLastCheckedOut, mToday;
    private Button mPauseButton, mStartButton;
    private CircularProgressBar mProgressBar;

    private String TAG = "HC_"+Today_Fragment.class.getSimpleName();
    private DateFormat formatter;
    private SharedPreferences mSharedPreferences;
    private Handler updateTimeHandler = new Handler();
    private CommonUtils mUtils;

    private boolean inOffice;
    private boolean alreadyStarted = false;
    private String checkOut;
    private boolean firstUpdate = false;

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
        mLastCheckedOut = v.findViewById(R.id.LastCheckedOut);
        mPauseButton = v.findViewById(R.id.pauseButton);
        mStartButton = v.findViewById(R.id.startButton);
        mProgressBar = v.findViewById(R.id.progressBar);
        mToday = v.findViewById(R.id.todaysDate);

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
            mUtils = CommonUtils.getInstance(this.getActivity());

            //Display Today's Date
            Date c = Calendar.getInstance().getTime();
            SimpleDateFormat df = new SimpleDateFormat("MMMM dd yyyy , EEEE");
            mToday.setText(df.format(c));

            mSharedPreferences = this.getActivity().getSharedPreferences(mUtils.SP_NAME_TIME, Context.MODE_PRIVATE);
            if (mSharedPreferences != null) {

                //start the UI handler to update every second.
                if (!alreadyStarted) {
                    firstUpdate = true;
                    updateTimeHandler.postDelayed(updateTimerThread, 0);
                }
                alreadyStarted = true;
            } else {
                Log.d(TAG, "sharedpreferences Context null");
            }
        } else {
            Log.w(TAG, "context is null");
        }

        /*
        This block pause button and start button is for testing purposes. must be deleted later
         */
        mPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), GeoIntentService.class);
                intent.putExtra("pause", 7);
                getActivity().startService(intent);
            }
        });

        mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), GeoIntentService.class);
                intent.putExtra("pause", 5);
                getActivity().startService(intent);
            }
        });
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
                firstUpdate = false;
            } else {
                Log.w(TAG, "out of office");

                // reset during updateThreadHandler will update the UI with reset values.
                boolean resetEverything = mSharedPreferences.getBoolean(mUtils.SP_RESET, false);
                if (resetEverything) {
                    long totalTime = mSharedPreferences.getLong(mUtils.SP_TOTALTIME, 0);
                    if (totalTime == 0) {
                        updateUI(0, true);
                    }
                } else {
                    /*
                    when you are out-of-office, we do not update the UI. and when you enter onResume()
                    it does not persist the old paused timings, instead no values are set (looks like reset)
                    firstUpdate is a boolean which will update the old paused values.
                     */
                    if (firstUpdate) {
                        long totalTime = mSharedPreferences.getLong(mUtils.SP_TOTALTIME, 0);
                        updateUI(totalTime, false);
                    }
                }

                //update checkOutTime always
                checkOut = mSharedPreferences.getString(mUtils.SP_LASTCHECKOUT, null);
                if (checkOut != null) {
                    mLastCheckedOut.setText(checkOut);
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

        long startTime = mSharedPreferences.getLong(mUtils.SP_STARTTIME, 0);
        if (startTime != 0) {
            long totalTime = mSharedPreferences.getLong(mUtils.SP_TOTALTIME, 0);
            long bufferTime = System.currentTimeMillis() - startTime;
            totalTime = totalTime + bufferTime;
            Log.d(TAG, "time() " + totalTime);
            updateUI(totalTime, false);
        } else {
            Log.d(TAG, "startTime equal to 0");
            updateUI(0, true);
        }
    }

    /*
    checks whether we are in office and updates UI
     */
    private boolean checkInOffice() {
        boolean check = mSharedPreferences.getBoolean(mUtils.SP_InOFFICE, false);
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
        Log.d(TAG, "updateUI() reset = "+reset);
         if (!reset) {
             //setting the time Elapsed
             mHoursCompleted.setText(formatter.format(new Date(totalTime)) + "  Finished");

             //Calculating and setting the time Remaining
             long eightHoursADay = 8 * 60 * 60000;
             if (eightHoursADay >= totalTime) {
                 long timeToWork = ((8 * 60 * 60000) - totalTime);
                 mTimeRemaining.setText(formatter.format(new Date(timeToWork)) + " Remaining ");

                 float percentage = (((float)totalTime * 100)/eightHoursADay);
                 Log.d(TAG, "percentage on circular progress bar "+percentage);
                 mProgressBar.setProgress(percentage);
             } else {
                 Log.d(TAG, "8 hours finished. No need to calculate time remaining");
                 mTimeRemaining.setText("OVER TIME");
                 mTimeRemaining.setTextColor(getResources().getColor(R.color.secondary));
                 mProgressBar.setProgress(100);
             }



             String checkIn = mSharedPreferences.getString(mUtils.SP_FIRSTCHECKIN, null);
             if (checkIn != null) {
                 mLastCheckedIn.setText(checkIn);
             }

             checkOut = mSharedPreferences.getString(mUtils.SP_LASTCHECKOUT, null);
             if (checkOut != null) {
                 mLastCheckedOut.setText(checkOut);
             }
         } else {
             mHoursCompleted.setText("00:00:00 Finished");
             mTimeRemaining.setText("08:00:00 Remaining");
             mLastCheckedIn.setText("00:00");
             mLastCheckedOut.setText("00:00");
             mInOffice.setText("Out Of Office");
             mProgressBar.setProgress(0);

             //Notify reset complete
             SharedPreferences.Editor editor = mSharedPreferences.edit();
             editor.putBoolean(mUtils.SP_RESET, false);
             editor.apply();
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
