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
import countedhours.hourscount.service.GeoIntentService;

/*
This Broadcast receiver can be triggered only by the system, when the location providers are changed. i.e.,
when the location is enabled/disabled. we pause and start the timer, according to it.
 */
public class NotifyLocationDisable extends BroadcastReceiver {

    private String TAG = "HC_"+NotifyLocationDisable.class.getSimpleName();
    private SharedPreferences mSharedPreferences;
    private CommonUtils mUtils;
    private Context mContext;
    private NotificationManager mNotificationManager;

    @Override
    public void onReceive(Context context, Intent intent) {
        // an Intent broadcast.
        Log.d(TAG, "onReceive() ");
        mContext = context;

        mUtils = CommonUtils.getInstance(context);
        mSharedPreferences = context.getSharedPreferences(mUtils.SP_NAME_TIME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        if (mUtils.ifLocationAvailable(context)) {
            Log.i(TAG, "gpsSwitchStateReceiver.onReceive() location is enabled ");
            editor.putBoolean(mUtils.SP_AUTOMATICMODE, true);
            mNotificationManager.cancelAll();
        } else {
            Log.w(TAG, "gpsSwitchStateReceiver.onReceive() location disabled ");
            /*
            Notification is pushed "Now Entering Manual Mode". Please Enter CheckIn and CheckOut Manually.
            sharePreferences = Manual/Automatic.
             */
            notifyManualMode();
            editor.putBoolean(mUtils.SP_AUTOMATICMODE, false);
        }
        editor.apply();
    }

    private void notifyManualMode() {
        Log.d(TAG, "Pushing the Notification : Manual Mode");
        // Create an explicit intent for an Activity in your app
        Intent i = new Intent(mContext, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, i, 0);

        // Notification builder is used to build and customize your own notification
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext, "sam");
        mBuilder.setSmallIcon(R.drawable.timer);
        mBuilder.setContentTitle("Hours Count");
        mBuilder.setContentText("Manual Mode. Enter Check-In, Check-Out Manually");
        mBuilder.setContentIntent(pendingIntent);
        mBuilder.setAutoCancel(true);

        //For Notification to appear, we call npotify with a unique ID.
        mNotificationManager.notify(1, mBuilder.build());
    }
}
