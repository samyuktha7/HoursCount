package countedhours.hourscount.Fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.budiyev.android.circularprogressbar.CircularProgressBar;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import countedhours.hourscount.CommonUtils;
import countedhours.hourscount.CustomBreakDialog;
import countedhours.hourscount.R;
import countedhours.hourscount.service.GeoIntentService;


public class Today_Fragment extends Fragment {

    private TextView mTimeRemaining, mHoursCompleted, mInOffice, mLastCheckedIn, mLastCheckedOut, mToday, mMode;
    private Button mPauseButton, mStartButton;
    private CircularProgressBar mProgressBar;
    private FloatingActionButton mFab;

    private String TAG = "HC_"+Today_Fragment.class.getSimpleName();
    private DateFormat formatter;
    private SharedPreferences mSharedPreferences;
    private Handler updateTimeHandler = new Handler();
    private CommonUtils mUtils;

    private boolean inOffice;
    private boolean alreadyStarted = false;
    private boolean showWarningOnce = true;
    private String checkOut;
    private Context mContext;
    private boolean isInManualMode;
    private int progress = 0;

    private long eightHoursADay = 8 * 60 * 60000;

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
        mFab = v.findViewById(R.id.fab);
        mMode = v.findViewById(R.id.mode);
        mMode.setVisibility(View.GONE);
        return v;
    }


    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");

        //If the context is not null, get Shared Preferences.
        if (this.getActivity() != null) {
            mUtils = CommonUtils.getInstance(this.getActivity());
            mContext = this.getActivity();
            setTodaysDate();
            if (!mUtils.isNetworkAvailable(this.getActivity())) {
                handleManualModeUI(false);
            } else {
                handleAutomaticModeUI();
            }

            mSharedPreferences = this.getActivity().getSharedPreferences(mUtils.SP_NAME_TIME, Context.MODE_PRIVATE);
            if (mSharedPreferences != null) {
                //start the UI handler to update every second.
                if (!alreadyStarted) {
                    mUtils.firstUpdateTodays = true;

                    //Used for Time displaying Time worked and Time remaining
                    //DEclaring here, because initializing once will be enough.
                    formatter = new SimpleDateFormat("HH:mm:ss", Locale.US);
                    formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
                    //strating the thread
                    updateTimeHandler.postDelayed(updateTimerThread, 0);
                    alreadyStarted = true;
                }
            } else {
                Log.d(TAG, "sharedpreferences Context null");
            }

            //AddCustomBreak
            mFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    long startTime = mSharedPreferences.getLong(mUtils.SP_STARTTIME, 0);
                    if (startTime != 0) {
                        CustomBreakDialog cdd = new CustomBreakDialog(getActivity());
                        cdd.show();
                    } else {
                        long totalTime = mSharedPreferences.getLong(mUtils.SP_TOTALTIME, 0);
                        if (totalTime != 0) {
                            CustomBreakDialog cdd = new CustomBreakDialog(getActivity());
                            cdd.show();
                        } else {
                            Toast.makeText(getContext(), "No Office Hours", Toast.LENGTH_SHORT ).show();
                        }
                    }
                }
            });
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
            // setUp Mode UI
            checkMode();

            if (checkInOffice()) {
                updateTimeElapsed();
                mUtils.firstUpdateTodays = false;
            } else {
                Log.w(TAG, "out of office");

                // reset during updateThreadHandler will update the UI with reset values.
                boolean resetEverything = mSharedPreferences.getBoolean(mUtils.SP_RESET, false);
                if (resetEverything) {
                    Log.d(TAG, "resetEverything");
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
                    Log.d(TAG, "value "+mUtils.firstUpdateTodays);
                    if (mUtils.firstUpdateTodays) {
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
            totalTime = mUtils.calculateElapsedTime(startTime, totalTime);
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
             if (eightHoursADay >= totalTime) {
                 long timeToWork = ((8 * 60 * 60000) - totalTime);
                 mTimeRemaining.setText(formatter.format(new Date(timeToWork)) + " Remaining ");

                 int percentage = (int) (((float)totalTime * 100)/eightHoursADay);
                 setProgress(percentage);
             } else {
                 Log.d(TAG, "8 hours finished. No need to calculate time remaining");
                 mTimeRemaining.setText("OVER TIME");
                 mTimeRemaining.setTextColor(getResources().getColor(R.color.secondary));
                 setProgress(100);
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
             setProgress(0);

             //Notify reset complete
             SharedPreferences.Editor editor = mSharedPreferences.edit();
             editor.putBoolean(mUtils.SP_RESET, false);
             editor.apply();
         }
    }

    private void setTodaysDate() {
        //Display Today's Date
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("MMMM dd yyyy , EEEE");
        Log.d(TAG, "Todays date is "+df.format(c));
        mToday.setText(df.format(c));
    }

    /*
    Only set the progress, if it changes (in Integer)
     */
    private void setProgress(int percentage) {
        if (progress != percentage) {
            Log.d(TAG, "setProgress() "+percentage);
            mProgressBar.setProgress(percentage);
            progress = percentage;
        }
    }

    private void checkMode() {
        Log.d(TAG, "checkMode()");
        boolean mode = mSharedPreferences.getBoolean(mUtils.SP_AUTOMATICMODE, false);
        if (isInManualMode != mode) {
            if (!mode) {
                handleManualModeUI(true);
            } else {
                handleAutomaticModeUI();
            }
            isInManualMode = mode;
        }
    }

    private void handleManualModeUI(boolean location) {
        Log.d(TAG, "Manual Mode()");
        mMode.setVisibility(View.VISIBLE);
        mStartButton.setVisibility(View.VISIBLE);
        mPauseButton.setVisibility(View.VISIBLE);
        if (showWarningOnce) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            if (location) {
                builder.setMessage("This App will better perform in Automatic Mode. Turn ON Location Services");
                builder.setPositiveButton(
                        "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent myAppSettings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                        Uri.parse("package:" + mContext.getPackageName()));
                                myAppSettings.addCategory(Intent.CATEGORY_DEFAULT);
                                myAppSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(myAppSettings);
                            }
                        });
            } else {
                builder.setMessage("This App will better perform in Automatic Mode. Turn ON Internet Connectivity");
                builder.setPositiveButton(
                        "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent intent=new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                                startActivity(intent);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        });

            }
            builder.setCancelable(true);


            builder.setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
            showWarningOnce = false;
        }
    }

    private void handleAutomaticModeUI() {
        Log.d(TAG, "handleAutomaticModeUI");
        showWarningOnce = true;
        mMode.setVisibility(View.INVISIBLE);
        mStartButton.setVisibility(View.INVISIBLE);
        mPauseButton.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause()");
        updateTimeHandler.removeCallbacks(updateTimerThread);
        alreadyStarted = false;
        inOffice = false;
        progress = 0;
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}
