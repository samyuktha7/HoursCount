package countedhours.hourscount.BroadcastReceivers;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import countedhours.hourscount.Activities.MainActivity;
import countedhours.hourscount.CommonUtils;
import countedhours.hourscount.R;

public class pushWeeklyNotificationAlarm extends BroadcastReceiver {


    private String TAG = "HC_"+pushWeeklyNotificationAlarm.class.getSimpleName();
    private SharedPreferences mSharedPreferences;
    private CommonUtils mUtils;
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "pushweeklyNotifications");
        mSharedPreferences = context.getSharedPreferences(mUtils.SP_NAME_TIME, Context.MODE_PRIVATE);

        /*
        Checks if the total weeks time 40 hours is reached.
         */
        float totalWeeksTime = mSharedPreferences.getFloat(mUtils.SP_TOTALWEEKTIME, 0);
        long totalTime = mSharedPreferences.getLong(mUtils.SP_TOTALTIME,0);
        long totalWeeksInMillis = ((long) totalWeeksTime * 60 * 60000);
        long total = totalTime + totalWeeksInMillis;
        if (total != 0) {
            long week = 40 * 60 * 60000;
            /*
            Only push a notification if total 40 hours are done.
             */
            if (total >= week) {

                // Create an explicit intent for an Activity in your app
                Intent i = new Intent(context, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, i, 0);

                // Notification builder is used to build and customize your own notification
                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, "sam");
                mBuilder.setSmallIcon(R.drawable.timer);
                mBuilder.setContentTitle("Hours Count");
                mBuilder.setContentText("You finished working 40 hours this week");
                mBuilder.setContentIntent(pendingIntent);
                mBuilder.setAutoCancel(true);

                //For Notification to appear, we call npotify with a unique ID.
                NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.notify(1, mBuilder.build());

            } else {
                Log.w(TAG, "40 hours not completed, ignoring the notification");
            }
        } else {
            Log.d(TAG, "Total hours = 0. Address might have reset");
        }
    }
}
