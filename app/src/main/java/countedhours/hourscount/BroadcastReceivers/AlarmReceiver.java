package countedhours.hourscount.BroadcastReceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/*
This Broadcast Receiver AlarmReceiver will receive events every day to perform following operations
1. Stores the total hours in corresponding day of the week
2. calculates and Stores total weeks time in shared Preferences
3. clear start Time and total time in shared Preferences and keeps it ready for next day
 */
public class AlarmReceiver extends BroadcastReceiver {

    private String TAG = "AlarmReceiver";

    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.d(TAG, "onReceive()");
        SharedPreferences mSharedPreferences = context.getSharedPreferences("TIME", Context.MODE_PRIVATE);
        long totalTime = mSharedPreferences.getLong("TotalTime",-1);
        if (totalTime != -1) {
            //Stores the total time in day_of_week field.
            Date now = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(now);
            int day_of_week = calendar.get(Calendar.DAY_OF_WEEK);
            Log.d(TAG, " day of the week = "+day_of_week);
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putLong(String.valueOf(day_of_week), totalTime);

            //stores the totalTime for Week (In Hours)
            float totalWeekTime = mSharedPreferences.getFloat("TotalWeekTime", 0);
            float totalTimeInHours = (totalTime / (60 * 60000));
            totalWeekTime = totalWeekTime + totalTimeInHours;
            editor.putFloat("TotalWeekTime", totalWeekTime);
            Log.d(TAG, "totalWeekTime = "+totalWeekTime);

            //clears the startTime and totalTime to calculate for next day.
            editor.putLong("StartTime", 0);
            editor.putLong("TotalTime", 0);

            editor.apply();
        } else {
            Log.d(TAG, "totalTime 0. Might be non-working-day");
        }
    }
}
