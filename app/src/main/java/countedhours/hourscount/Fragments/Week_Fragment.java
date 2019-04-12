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

import com.budiyev.android.circularprogressbar.CircularProgressBar;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import countedhours.hourscount.CommonUtils;
import countedhours.hourscount.R;

/*
when this fragment opens
    *get the day of the week
    *get the total Time of the day - update in sharedPreference - of the day
    * calculate the totalWeekTime
    * updateUI
All the values of todays values and TotalWeeks time values are reset - when the address is changed.
Also, when the week is completed.
 */
public class Week_Fragment extends Fragment {

    private String TAG = "HC_"+Week_Fragment.class.getSimpleName();

    private TextView mMon, mTue, mWed, mThur, mFri, mSat, mSun, mTotal, mWeekStartDate, mWeekEndDate;
    private CircularProgressBar mWeekProgress;

    private int mDayOfTheWeek;
    private CommonUtils mUtils;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;
    private boolean firstUpdate;
    private boolean alreadyStarted;
    private Handler updateTimeHandler = new Handler();
    private DecimalFormat df;
    private float  mTodaysUpdatedTime, mTotalWeeksTime;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_week, container, false);
        mMon = v.findViewById(R.id.mondayHours);
        mTue = v.findViewById(R.id.tuesdayHours);
        mWed = v.findViewById(R.id.wednesdayHours);
        mThur = v.findViewById(R.id.thursdayHours);
        mFri = v.findViewById(R.id.fridayHours);
        mSat = v.findViewById(R.id.saturdayHours);
        mSun = v.findViewById(R.id.sundayHours);
        mTotal = v.findViewById(R.id.totalHours);

        mWeekStartDate = v.findViewById(R.id.weekStartDate);
        mWeekEndDate = v.findViewById(R.id.weekEndDate);

        mWeekProgress = (CircularProgressBar) v.findViewById(R.id.weekProgress);

        mUtils = new CommonUtils(this.getActivity());
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");

        df = new DecimalFormat();
        df.setMaximumFractionDigits(1);

        if (this.getActivity() != null) {

            //week start date of week
            Calendar c1 = Calendar.getInstance();
            c1.set(Calendar.DAY_OF_WEEK, 1);
            Date weekStart = c1.getTime();
            SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
            mWeekStartDate.setText(df.format(weekStart));

            //week end date of week
            Calendar c2 = Calendar.getInstance();
            c2.set(Calendar.DAY_OF_WEEK, 7);
            Date weekEnd = c2.getTime();
            mWeekEndDate.setText(" - "+df.format(weekEnd));

            mSharedPreferences = this.getActivity().getSharedPreferences(mUtils.SP_NAME_TIME, Context.MODE_PRIVATE);

            //gets the day of the week
            mDayOfTheWeek = mUtils.getDayOfTheWeek();

            firstUpdate = true;
            //updates Todays Values
            updateToday();

            if (!alreadyStarted) {
                //Starts handler after 30 mins, as already basic updateUI is done.
                updateTimeHandler.postDelayed(updateTimerThread, 0);
            }
            alreadyStarted = true;
        }
    }

    /*
    updateToday will calculate todays total time and week time and also updates in SharedPreferences
     */
    private void updateToday() {
        Log.d(TAG, "updateToday()");
        mEditor = mSharedPreferences.edit();

        //calculate and updates Todays time
        long startTime = mSharedPreferences.getLong(mUtils.SP_STARTTIME, 0);
        long totalTime = mSharedPreferences.getLong(mUtils.SP_TOTALTIME, 0);
        if (startTime != 0) {
            long bufferTime = System.currentTimeMillis() - startTime;
            totalTime = totalTime + bufferTime;
            Log.d(TAG, "time() " + totalTime);
        }

        //updates Todays time in hours format.
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        mTodaysUpdatedTime = ((float) totalTime) / (60 * 60000);
        mEditor.putFloat(String.valueOf(mDayOfTheWeek), mTodaysUpdatedTime);

        //Calculate and updates weeks Time
        mTotalWeeksTime = mSharedPreferences.getFloat(mUtils.SP_TOTALWEEKTIME, 0);
        if (totalTime != 0) {
            mTotalWeeksTime = mTotalWeeksTime + mTodaysUpdatedTime;
        }
        mEditor.apply();

        Log.d(TAG, "TodaysUpdatedTime = "+mTodaysUpdatedTime +"  WeeksUpdatedTime = "+mTotalWeeksTime);

        updateUI();
    }

    /*
    This runnable runs every 30 minutes, which calculates and updates the UI only if we are in the office
     */
    private Runnable updateTimerThread = new Runnable() {
        @Override
        public void run() {
            if (checkInOffice()) {
                updateToday();
            }
            updateTimeHandler.postDelayed(this, 20000);
        }
    };


    /*
    This method updates the UI.
        - updates entire values once.
        - updates only changing values otherwise (todays and total hours)
     */
    private void updateUI() {
        Log.d(TAG, "updateUI() : firstUpdate = "+firstUpdate);
        if (firstUpdate) {
            // update all the days values first time
            for (int i = 1; i <=7 ; i++) {
                //updateUI - Everyday
                float temp = mSharedPreferences.getFloat(String.valueOf(i), 0);
                setTodaysValue(i , temp);
            }
            firstUpdate = false;
        } else {
            //update only todays day value in handler loop
            setTodaysValue(mDayOfTheWeek, mTodaysUpdatedTime);
        }

        float weekly = 40;
        //updateUI - Weeks
        if (mTotalWeeksTime > weekly) {
            mTotal.setTextColor(getResources().getColor(R.color.secondary));
        }
        mTotal.setText(df.format(mTotalWeeksTime));

        //updateCircularUI - total weeks time in 40 hours
        float percentage = (((float) mTotalWeeksTime * 100) / 40 );
        Log.d(TAG, "percentage is  "+percentage);
        mWeekProgress.setProgress(percentage);
    }

    /*
    sets the UI value of days based on day_of_the_week with hours value.
    It updatesin Float (2 point decimal) - number of hours format
     */
    private void setTodaysValue(int i, float temp) {
        Log.d(TAG, "setTodaysValue(): updating  = "+i+"  temp = "+temp);
        String time = df.format(temp);
        float daily = 8;
        switch (i) {
            case 1 :
                mSun.setText(time);
                if (temp > daily) {
                    mSun.setTextColor(getResources().getColor(R.color.secondary));
                }
                break;
            case 2 :
                mMon.setText(time);
                if (temp > daily) {
                    mMon.setTextColor(getResources().getColor(R.color.secondary));
                }
                break;
            case 3 :
                mTue.setText(time);
                if (temp > daily) {
                    mTue.setTextColor(getResources().getColor(R.color.secondary));
                }
                break;
            case 4 :
                mWed.setText(time);
                if (temp > daily) {
                    mWed.setTextColor(getResources().getColor(R.color.secondary));
                }
                break;
            case 5 :
                mThur.setText(time);
                if (temp > daily) {
                    mThur.setTextColor(getResources().getColor(R.color.secondary));
                }
                break;
            case 6 :
                mFri.setText(time);
                if (temp > daily) {
                    mFri.setTextColor(getResources().getColor(R.color.secondary));
                }
                break;
            case 7 :
                mSat.setText(time);
                if (temp > daily) {
                    mSat.setTextColor(getResources().getColor(R.color.secondary));
                }
                break;
        }
    }

    /*
    checks whether the person is in Office.
    Only updates the UI values, if we are in Office.
     */
    private boolean checkInOffice() {
        boolean InOffice = mSharedPreferences.getBoolean(mUtils.SP_InOFFICE, false);
        Log.d(TAG, "checkInOffice "+InOffice);
        return InOffice;
    }

    @Override
    public void onPause() {
        super.onPause();
        updateTimeHandler.removeCallbacks(updateTimerThread);
        alreadyStarted = false;
    }
}
