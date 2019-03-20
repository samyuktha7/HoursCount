package countedhours.hourscount.Activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import countedhours.hourscount.BroadcastReceivers.AlarmReceiver;
import countedhours.hourscount.Database.AddressInformation;
import countedhours.hourscount.Database.SqLiteDatabaseHelper;
import countedhours.hourscount.R;
import countedhours.hourscount.service.GeoIntentService;

public class ProfileActivity extends AppCompatActivity {

    private EditText mAddressInformation;
    private String mAddress;
    private Button mSaveAddressButton;
    private SqLiteDatabaseHelper dbHelper;
    private String TAG = "ProfileActivity";
    public Double[] latLong = new Double[2];
    private Address mLocation;
    private SharedPreferences sharedPreferences;
    private Geofence mGeofence;
    private GeofencingClient mGeofencingClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");
        setContentView(R.layout.activity_profile);

        mAddressInformation = (EditText) findViewById(R.id.addressField);
        mSaveAddressButton = (Button) findViewById(R.id.Enter);
        dbHelper = new SqLiteDatabaseHelper(this, "AddressCommit", null, 1);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

        sharedPreferences = getSharedPreferences("ADDRESS_INFO", Context.MODE_PRIVATE);
        mAddress = getAddressFromSharedPreferences();
        if (mAddress != null) {
            Log.d(TAG, "address found in shared preferences");
            setAddressField(mAddress, false, false);
//            startGeoFence();

            // this should actually be on save button click listener ( after the adddress is stored in the database). This code is added here only for testing purposes
//            startService();

        } else {
            AddressInformation addressInformation = dbHelper.getLastSavedAddressInfo();
            if (addressInformation != null) {
                Log.d(TAG, "address found in database");
                mAddress = addressInformation.getAddress();
                storeAddressInSharedPreferences(mAddress);
                setAddressField(mAddress, false, false);
            } else {
                Log.w(TAG, "address not found in Shared Preferences and database. Please Enter a new one");
                setAddressField(null, true, true);
            }
        }

        mSaveAddressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSaveAddressButton.getText() == "EDIT") {
                    Log.d(TAG, "edit button pressed");
                    setAddressField(String.valueOf(mAddressInformation.getText()), true, true);
                } else if (mSaveAddressButton.getText() == "SAVE") {
                    Log.d(TAG, "Save button pressed");
                    mAddress = String.valueOf(mAddressInformation.getText());
                    mLocation = getLocationFromAddress(mAddress);
                    if (mLocation != null) {
                        Log.d(TAG, "lat " + mLocation.getLatitude() + " long " + mLocation.getLongitude());
                        // save it in the database and Shared Preferences.
                        storeAddressInSharedPreferences(mAddress);
                        int row_id = storeAddressInDatabase(mAddress, mLocation.getLatitude(), mLocation.getLongitude());
                        if (row_id > 0) {
                            Log.d(TAG, "saved in database: row_id " + row_id);
                            Toast.makeText(ProfileActivity.this, "Address Stored", Toast.LENGTH_SHORT).show();
                            setAddressField(mAddress, false, false);
                            // this code should be here, not in onResume().
//                            startService();

                            //startGeoFence
                            startGeoFence();
                            startAlarmToClearDatabase();

                        } else {
                            Log.d(TAG, "could not store in database");
                            storeAddressInSharedPreferences(null);
                            setAddressField(null, true, true);
                        }
                    } else {
                        Log.d(TAG, "location is null");
                        Toast.makeText(ProfileActivity.this, "Address not Valid. Enter New address", Toast.LENGTH_SHORT).show();
                        storeAddressInSharedPreferences(null);
                        setAddressField(null, true, true);
                    }
                }
            }
        });
    }

    private void startGeoFence() {
        Log.d(TAG, "startGeoFence() ");
        // Create a Geofence
        String id = UUID.randomUUID().toString();

        mGeofencingClient = LocationServices.getGeofencingClient(ProfileActivity.this);

        /* geofence is the area to be monitored.
        It is marked using a builder, which takes a unique id for every geofence.
        It determines the circular duration using latitude, longitude and radius.
        Transition types are enter and exit into the geofence, for which it will shoot a pending intent
        */
        mGeofence = new Geofence.Builder()
                .setRequestId(id)
                .setCircularRegion(mLocation.getLatitude(), mLocation.getLongitude(), 50)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER
                        | Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();


        /*
        Creates a request for the geofence area that is created. Takes geofence as the add geofence parameter.
         */
        GeofencingRequest geofencingRequest = new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(mGeofence)
                .build();


        Intent intent = new Intent(this, GeoIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        PendingIntent geofencePendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mGeofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)
                    .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "success. Geofence added");
                            // Geofences added
                            // ...
                        }
                    })
                    .addOnFailureListener(this, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "Could not add Geofence");
                            // Failed to add geofences
                            // ...
                        }
                    });
        } else {
            Log.d(TAG, "permission not granted, please check");
        }

    }

    /*
    set the alarm everyday to clear the database. AlarmReceiver is the broadcast receiver receives the alarm
    and performs the operation.
     */
    private void startAlarmToClearDatabase() {
        Log.d(TAG, "startAlarmToClearDatabase()");
        AlarmManager alarmMgr = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(ProfileActivity.this, AlarmReceiver.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(ProfileActivity.this, 0, intent, 0);

        // Set the alarm to start at approximately 00:00 h(24h format).
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 00);

        //repeteat alarm every 24hours
        alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, alarmIntent);
    }


//    public void startService() {
//        //Stored in Shared Preferences and database successfully, start the alarm manager to
//        //service every day in a particular timezone
//
//        // every day at 7 am
//        Calendar startTime = Calendar.getInstance();
////                            Calendar stopTime = Calendar.getInstance();
//
////                            // if it's after or equal 7 am schedule for next day
////                            if (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) >= 17) {
////                                Log.d(TAG, "alarm will start from tomorrrow");
////                                startTime.add(Calendar.DAY_OF_YEAR, 1); // add, not set!
////                            }
////                            startTime.set(Calendar.HOUR_OF_DAY, 16);
////                            startTime.set(Calendar.MINUTE, 07);
////                            startTime.set(Calendar.SECOND, 0);
//
//        Intent intent = new Intent(ProfileActivity.this, CountService.class);
//        PendingIntent pendingIntent = PendingIntent.getService(ProfileActivity.this, 0, intent,
//                0);
//        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
//
//        if (alarmManager != null) {
//            Log.d(TAG, "Alarm set to the calendar time and will run every day");
//            alarmManager.setInexactRepeating(AlarmManager.RTC, startTime.getTimeInMillis(),
//                    30*60*1000, pendingIntent);
//        }
//    }

    public Address getLocationFromAddress(String strAddress) {
        Geocoder coder = new Geocoder(this);
        List<Address> address;

        try {
            Log.d(TAG, "getLocationFromAddress(): " + strAddress);
            address = coder.getFromLocationName(strAddress, 1);
            if (address == null) {
                Log.d(TAG, "address null. Enter new one");
                return null;
            }
            return address.get(0);
        } catch (Exception e) {
            Log.d(TAG, "exception Occurred" + e);
            return null;
        }
    }

    public void storeAddressInSharedPreferences(String address) {
        Log.d(TAG, "storeAddressInSharedPreferences " + address);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("Address", address);
        editor.apply();
    }

    public String getAddressFromSharedPreferences() {
        Log.d(TAG, "getAddressFromSharedPreferences() ");
        return sharedPreferences.getString("Address", null);
    }

    public int storeAddressInDatabase(String address, double latitude, double longitude) {
        Log.d(TAG, "storeAddressInDatabase() " + address);
        return dbHelper.insertAddressInfoValues(mAddress, latLong[0], latLong[1]);
    }

    public void setAddressField(String address, boolean enabled, boolean save) {
        Log.d(TAG, "setAddressField = " + address + "; enabled = " + enabled + "; save =" + save);
        if (address != null) {
            mAddressInformation.setText(address);
        } else {
            mAddressInformation.setText(" ");
        }

        mAddressInformation.setEnabled(enabled);
        if (enabled) {
            if (address != null) {
                mAddressInformation.setSelection(address.length());
            }
        }

        if (save) {
            mSaveAddressButton.setText("SAVE");
        } else {
            mSaveAddressButton.setText("EDIT");
        }

    }

    @Override
    protected void onPause() {
        super.onPause();

    }
}
