package countedhours.hourscount;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Calendar;
import java.util.Date;

/*
Common methods used by different classes are declared here, to avoid duplicate code
 */
public class CommonUtils {

    private String TAG = "HC_"+CommonUtils.class.getSimpleName();
    private static CommonUtils mInstance = null;


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
    This method resets all the values in SharedPreferences. Also clears weekly values, if the
    address is changed.
     */
    public void resetEverything(Context context, boolean addressChange) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("TIME", Context.MODE_PRIVATE);
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
}
