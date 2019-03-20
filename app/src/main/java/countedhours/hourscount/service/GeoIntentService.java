package countedhours.hourscount.service;

import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

import countedhours.hourscount.Database.SqLiteDatabaseHelper;

import static com.google.android.gms.common.GooglePlayServicesUtil.getErrorString;


/*
This Intent Service is triggered by transition through GEOFENCE. It enters onHandleIntent, does operations and calls onDestroy immediately
 */
public class GeoIntentService extends IntentService {

    private String TAG = "GeoIntentService";
    private SqLiteDatabaseHelper dbhelper;

    public GeoIntentService() {
        super(GeoIntentService.class.getSimpleName());
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public GeoIntentService(String name) {
        super(name);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    /**
     * This method is invoked on the worker thread with a request to process.
     * Only one Intent is processed at a time, but the processing happens on a
     * worker thread that runs independently from other application logic.
     * So, if this code takes a long time, it will hold up other requests to
     * the same IntentService, but it will not hold up anything else.
     * When all requests have been handled, the IntentService stops itself,
     * so you should not call {@link #stopSelf}.
     *
     * @param intent The value passed to {@link
     *               Context#startService(Intent)}.
     *               This may be null if the service is being restarted after
     *               its process has gone away; see
     *               {@link Service#onStartCommand}
     *               for details.
     */
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.d(TAG, "onHandleIntent() ");
        dbhelper = new SqLiteDatabaseHelper(this, "AddressCommit", null, 1);

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            String errorMessage = getErrorString(geofencingEvent.getErrorCode());
            Log.e(TAG, errorMessage);
            return;
        }

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            Log.d(TAG, "GEOFENCE_TRANSITION_ENTER");
            toast("Geofence Entered", Toast.LENGTH_SHORT);
            int id = insertIntoGeofenceTransitionDatabse(Geofence.GEOFENCE_TRANSITION_ENTER);
            Log.d(TAG, "id ="+id+" Enter");
        } else if(geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            Log.d(TAG, "GEOFENCE_TRANSITION_EXIT");
            toast("Geofence Exited", Toast.LENGTH_SHORT);
            int id = insertIntoGeofenceTransitionDatabse(Geofence.GEOFENCE_TRANSITION_EXIT);
            Log.d(TAG, "id ="+id+" Exit");
        }
    }

    private void toast(final String text, final int duration) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), text, duration).show();
            }
        });
    }

    private int insertIntoGeofenceTransitionDatabse(int transition) {
        return dbhelper.insertGeofenceTransitionValues(transition, System.currentTimeMillis());
    }


    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy()");
        super.onDestroy();
    }
}
