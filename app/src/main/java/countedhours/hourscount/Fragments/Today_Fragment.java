package countedhours.hourscount.Fragments;

import android.os.Bundle;
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
import countedhours.hourscount.Interfaces.IUpdateTimeElapsed;
import countedhours.hourscount.R;
import countedhours.hourscount.TimerTask;


public class Today_Fragment extends Fragment implements IUpdateTimeElapsed {

    private TextView mTimeRemaining, mHoursCompleted;
    private String TAG = "TodayFragment";
    private TimerTask mTimerTask;
    private DateFormat formatter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_today_, container, false);
        mHoursCompleted = v.findViewById(R.id.timeElapsed);
        mTimeRemaining = v.findViewById(R.id.timeLeftOut);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");

        formatter = new SimpleDateFormat("HH:mm:ss", Locale.US);
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));

        if (this.getActivity() != null) {
            mTimerTask = TimerTask.getInstance(this.getActivity());
            mTimerTask.registerUpdateTimeElapsed(this);
        } else {
            Log.w(TAG, "context is null");
        }
    }

    @Override
    public void onUpdateTimeElapsed(long time) {
        Log.d(TAG, "onUpdateTimeElapsed() "+time);

        //setting the time Elapsed
        mHoursCompleted.setText(formatter.format(new Date(time)) + " Finished");

        //Calculating and setting the time Remaining
        long timeToWork = ((8 * 60 * 60000) - time);
        mTimeRemaining.setText(formatter.format(new Date(timeToWork)) + " Remaining ");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause()");
    }

    @Override
    public void onStop() {
        super.onStop();
        mTimerTask.unregisterupdateTimeElapsed(this);
    }
}
