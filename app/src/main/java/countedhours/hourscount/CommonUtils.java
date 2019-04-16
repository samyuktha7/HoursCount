package countedhours.hourscount;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.Calendar;
import java.util.Date;

import countedhours.hourscount.BroadcastReceivers.pushNotificationAlarm;
import countedhours.hourscount.BroadcastReceivers.pushWeeklyNotificationAlarm;

import static android.content.Context.ALARM_SERVICE;

/*
Common methods used by different classes are declared here, to avoid duplicate code
 */
public class CommonUtils {

    private String TAG = "HC_"+CommonUtils.class.getSimpleName();
    private static CommonUtils mInstance = null;
    private SharedPreferences sharedPreferences;


    public String SP_NAME_ADDRESS = "Address_Info";
    public String SP_OFFICEADDRESS = "Address";

    public String SP_FIRSTCHECKIN = "firstCheckIn";
    public String SP_LASTCHECKOUT = "lastCheckOut";
    public String SP_InOFFICE = "InOffice";
    public String SP_STARTTIME = "StartTime";
    public String SP_TOTALTIME = "TotalTime";
    public String SP_RESET = "reset";
    public String SP_NAME_TIME = "time";
    public String SP_TOTALWEEKTIME = "TotalWeekTime";
    public String SP_TODAYSDATE = "TodaysDate";

    public boolean firstUpdateTodays = false;

    public static CommonUtils getInstance(Context ctx) {
        Log.d("CommonUtils", "getInstance()");
        /**
         * use the application context as suggested by CommonsWare.
         * this will ensure that you dont accidentally leak an Activitys
         * context (see this article for more information:
         * http://android-developers.blogspot.nl/2009/01/avoiding-memory-leaks.html)
         */
        if (mInstance == null) {
            mInstance = new CommonUtils(ctx.getApplicationContext());
        }
        return mInstance;
    }

    /**
     * constructor should be private to prevent direct instantiation.
     * make call to static factory method "getInstance()" instead.
     */
    public CommonUtils(Context ctx) {
        Log.d(TAG, "CommonUtils constructor");
    }
    /*
    This method returns the day of week (today)
     */
    public int getDayOfTheWeek() {
        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        Log.d(TAG, "DayOfTheWeek = "+day);
        return day;
    }

    /*
    This time calculates the elapsed buffer time and adds to the total time
    */
    public long calculateElapsedTime(long startTime, long totalTime) {
        long buffer = System.currentTimeMillis() - startTime;
        return totalTime + buffer;
    }

    /*
    This method resets all the values in SharedPreferences. Also clears weekly values, if the
    address is changed.
     */
    public void resetEverything(Context context, boolean addressChange) {

        SharedPreferences.Editor editor = sharedPreferences.edit();

        //clears the startTime and totalTime to calculate for next day.
        editor.putLong(SP_STARTTIME, 0);
        editor.putLong(SP_TOTALTIME, 0);

        //clearsFirstCheckIn and LastCheckout
        editor.putString(SP_FIRSTCHECKIN, null);
        editor.putString(SP_LASTCHECKOUT, null);

        //clears InOffice boolean
        editor.putBoolean(SP_InOFFICE, false);

        //updates the UI to reset everything
        editor.putBoolean(SP_RESET, true);

        if (addressChange) {
            editor.putFloat(SP_TOTALWEEKTIME, 0);

            //clears all days values in a week
            for (int i = 1; i <= 7; i++) {
                editor.putFloat(String.valueOf(i), 0);
            }
        }
        editor.apply();
    }

    /*
    Triggers the alarm after 8 hours are finished. ( = now + (8 - total))
    */
    public void triggerEightHourAlarm(Context context, long totalTime) {
        Log.d(TAG, "triggerAlarm()");

        //starts an Alarm to trigger after (8 - total hours) - which will push a notification
        Intent i = new Intent(context, pushNotificationAlarm.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0,
                i, 0);

        // calculates 8-total time to trigger the alarm
        long currentTime = System.currentTimeMillis();
        long alarmTriggerTime = currentTime + ((8 * 60 * 60000) - totalTime);
        Log.d(TAG, "alarm triggered after "+ (((8 * 60 * 60000) - totalTime))/60000 + " minutes");

        // Schedule the alarm. Triggers the alarm at currentTime + after 8 hours time.
        AlarmManager am = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        am.setExact(AlarmManager.RTC_WAKEUP, alarmTriggerTime, sender);
    }

    /*
    Triggers the alarm if the total Weekly hours are somewhere between 30 -40, which triggers an alarm
    when it reaches 40 hours.
    */
    public void triggerWeeklyAlarm(Context ctx, long totalTime) {
        Log.d(TAG, "triggerWeeklyAlarm()");

        sharedPreferences = ctx.getSharedPreferences(SP_NAME_TIME, Context.MODE_PRIVATE);
        float totalWeeksTime = sharedPreferences.getFloat(SP_TOTALWEEKTIME, 0);
        long totalWeeksInMillis = ((long) totalWeeksTime * 60 * 60000);
        long total = totalTime + totalWeeksInMillis;
        Log.d(TAG, "totalweeks ="+totalWeeksInMillis+" total = "+total);

        long week = 40 * 60 * 60000;
        long totalInHours = total * 60 * 60000;

        if (totalInHours >= 30 && totalInHours <=40) {
            //starts an Alarm to trigger after (40 - total) - which will push a notification
            Intent in = new Intent(ctx, pushWeeklyNotificationAlarm.class);
            PendingIntent sender = PendingIntent.getBroadcast(ctx, 0,
                    in, 0);

            // calculates 40-total time to trigger the alarm
            long currentTime = System.currentTimeMillis();

            long alarmTriggerTime = currentTime + (week - total);
            Log.d(TAG, "weekly() alarm triggered after "+ ((week - total) / 60000));

            // Schedule the alarm. Triggers the alarm at currentTime + after 8 hours time.
            AlarmManager am = (AlarmManager) ctx.getSystemService(ALARM_SERVICE);
            am.setExact(AlarmManager.RTC_WAKEUP, alarmTriggerTime, sender);
        }

    }

}
