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
import countedhours.hourscount.R;

/*
This broadcast receiver is responsible for pushing a notification, if 8 hours are completed.
 */
public class pushNotificationAlarm extends BroadcastReceiver {

    private String TAG = "HC_"+pushNotificationAlarm.class.getSimpleName();
    private SharedPreferences mSharedPreferences;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onRecieve()");

        mSharedPreferences = context.getSharedPreferences("TIME", Context.MODE_PRIVATE);
        long startTime = mSharedPreferences.getLong("StartTime", 0);
        long totalTime = mSharedPreferences.getLong("TotalTime", 0);
        if (startTime != 0) {
            long bufferTime = System.currentTimeMillis() - startTime;
            totalTime = totalTime + bufferTime;
            Log.d(TAG, "time() " + totalTime);

            /*
            Usually Alarm Manager is received after 8 hours. when we receive, we still double check
            the time, if its more than or equal to 8 hours in time, then it pushes a notification.
             */
            if (totalTime >= (8 * 60 * 60000)) {
                // Create an explicit intent for an Activity in your app
                Intent i = new Intent(context, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, i, 0);

                // Notification builder is used to build and customize your own notification
                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, "sam");
                mBuilder.setSmallIcon(R.drawable.timer);
                mBuilder.setContentTitle("Hours Count");
                mBuilder.setContentText("You finished working 8 hours");
                mBuilder.setContentIntent(pendingIntent);
                mBuilder.setAutoCancel(true);

                //For Notification to appear, we call npotify with a unique ID.
                NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.notify(1, mBuilder.build());
            } else {
                Log.w(TAG, "8 hours not completed, ignoring the notification");
            }
        } else {
            Log.w(TAG, "Out of office, ignoring the notification");
        }
    }
}
