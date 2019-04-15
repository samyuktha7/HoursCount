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

    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.d(TAG, "onReceive()");
        CommonUtils mUtils = CommonUtils.getInstance(context);
        SharedPreferences mSharedPreferences = context.getSharedPreferences(mUtils.SP_NAME_TIME, Context.MODE_PRIVATE);

        long startTime = mSharedPreferences.getLong(mUtils.SP_STARTTIME, 0);
        long totalTime = mSharedPreferences.getLong(mUtils.SP_TOTALTIME, -1);
        if (startTime != 0) {
            long bufferTime = System.currentTimeMillis() - startTime;
            totalTime = totalTime + bufferTime;
            Log.d(TAG, "time() " + totalTime);
        }

        if (totalTime != -1) {
            //Stores the total time in day_of_week field.
            int day_of_week = mUtils.getDayOfTheWeek();

            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putFloat(String.valueOf(day_of_week), ((float) totalTime / (60 * 60000)));

            //stores the totalTime for Week (In Hours)
            float totalWeekTime = mSharedPreferences.getFloat(mUtils.SP_TOTALWEEKTIME, 0);
            float totalTimeInHours = ((float) totalTime / (60 * 60000));
            totalWeekTime = totalWeekTime + totalTimeInHours;
            editor.putFloat(mUtils.SP_TOTALWEEKTIME, totalWeekTime);
            Log.d(TAG, "totalWeekTime = "+totalWeekTime);
            editor.apply();

            mUtils.resetEverything(context, false);
        } else {
            Log.d(TAG, "Might be a non-working-day");
        }
    }
}
