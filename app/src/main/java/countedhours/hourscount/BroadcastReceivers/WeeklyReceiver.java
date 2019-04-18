package countedhours.hourscount.BroadcastReceivers;

import android.arch.persistence.room.Room;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import countedhours.hourscount.CommonUtils;
import countedhours.hourscount.Database.DatabaseHelper;
import countedhours.hourscount.Database.Sheets;

/*
This BroadcastReceiver will receive events once a week which does weekly clearing operations
1. gets the totalWeekTime
2. Todays Date (week end date)
3. Store the above two values in database
4. Resets all the values in a week.
 */
public class WeeklyReceiver extends BroadcastReceiver {

    private String TAG = WeeklyReceiver.class.getSimpleName();
    private SharedPreferences mSharedPreferences;
    private CommonUtils mUtils;
    private DatabaseHelper dbHelper;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive()");

        mUtils = CommonUtils.getInstance(context);
        dbHelper = Room.databaseBuilder(context,DatabaseHelper.class,  "sheets.db").allowMainThreadQueries().build();
        mSharedPreferences = context.getSharedPreferences(mUtils.SP_NAME_TIME, Context.MODE_PRIVATE);

        //get the totalWeekTime (float) from shared preferences.
        float totalWeekTime = mSharedPreferences.getFloat(mUtils.SP_TOTALWEEKTIME, 0);

        //get WeekEndDate (i.e., Todays Date)
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        Date date = new Date();
        String todayDate = dateFormat.format(date);

        if (todayDate != null) {
            // store it in database - Sheets Data ( Week End date, Total Week Time)
            Sheets newentry = new Sheets();
            newentry.setWeekend(todayDate);
            newentry.setHours(totalWeekTime);

            dbHelper.dao().insertValues(newentry);
        }

        // clear monday, tuesday, wednesday, thursday, friday, saturday, sunday and totalTime values
        ResetAllValuesInWeek();
    }

    private void ResetAllValuesInWeek() {
        Log.d(TAG, "ResetAllValuesInWeek() ");

        // clear the totalWeekTime (float) value in  shared preferences.
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putFloat(mUtils.SP_TOTALWEEKTIME, 0);

        //clears all days values in a week
        for (int i = 1; i <=7; i++) {
            editor.putFloat(String.valueOf(i), 0);
        }
        editor.apply();
    }
}
