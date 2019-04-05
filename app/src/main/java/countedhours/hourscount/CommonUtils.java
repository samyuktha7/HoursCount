package countedhours.hourscount;

import android.util.Log;

import java.util.Calendar;
import java.util.Date;

public class CommonUtils {

    private String TAG = "HC_"+CommonUtils.class.getSimpleName();

    public int getDayOfTheWeek() {
        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        Log.d(TAG, "DayOfTheWeek = "+day);
        return day;
    }
}
