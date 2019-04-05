package countedhours.hourscount.BroadcastReceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import countedhours.hourscount.CommonUtils;

/*
This Broadcast Receiver AlarmReceiver will receive events every day to perform following operations
1. Stores the total hours in corresponding day of the week
2. calculates and Stores total weeks time in shared Preferences
3. clear start Time and total time in shared Preferences and keeps it ready for next day
 */
public class AlarmReceiver extends BroadcastReceiver {

    private String TAG = "HC_"+AlarmReceiver.class.getSimpleName();
    private CommonUtils mUtils;

    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.d(TAG, "onReceive()");
        mUtils = new CommonUtils();
        SharedPreferences mSharedPreferences = context.getSharedPreferences("TIME", Context.MODE_PRIVATE);
        long totalTime = mSharedPreferences.getLong("TotalTime",-1);
        if (totalTime != -1) {
            //Stores the total time in day_of_week field.
            int day_of_week = mUtils.getDayOfTheWeek();

            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putFloat(String.valueOf(day_of_week), ((float) totalTime / (60 * 60000)));

            //stores the totalTime for Week (In Hours)
            float totalWeekTime = mSharedPreferences.getFloat("TotalWeekTime", 0);
            float totalTimeInHours = ((float) totalTime / (60 * 60000));
            totalWeekTime = totalWeekTime + totalTimeInHours;
            editor.putFloat("TotalWeekTime", totalWeekTime);
            Log.d(TAG, "totalWeekTime = "+totalWeekTime);

            //resets
            //clears the startTime and totalTime to calculate for next day.
            editor.putLong("StartTime", 0);
            editor.putLong("TotalTime", 0);

            //clearsFirstCheckIn and LastCheckout
            editor.putString("LastCheckedOut", null);

            //clears InOffice boolean
            editor.putBoolean("InOffice", false);

            //updates the UI to reset everything
            editor.putBoolean("reset", true);

            editor.apply();
        } else {
            Log.d(TAG, "Might be a non-working-day");
        }
    }
}
