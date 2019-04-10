package countedhours.hourscount.Activities;

import android.Manifest;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.support.annotation.NonNull;
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
import countedhours.hourscount.BroadcastReceivers.WeeklyReceiver;
import countedhours.hourscount.CommonUtils;
import countedhours.hourscount.R;
import countedhours.hourscount.service.GeoIntentService;

public class ProfileActivity extends AppCompatActivity {

    private EditText mAddressInformation;
    private String mAddress;
    private Button mSaveAddressButton;
    private String TAG = "HC_"+ProfileActivity.class.getSimpleName();
    private Address mLocation;
    private SharedPreferences sharedPreferences;
    private Geofence mGeofence;
    private GeofencingClient mGeofencingClient;
    private SharedPreferences.Editor mEditor;
    private String temp;
    private CommonUtils mUtils;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");
        setContentView(R.layout.activity_profile);

        mAddressInformation = (EditText) findViewById(R.id.addressField);
        mSaveAddressButton = (Button) findViewById(R.id.Enter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

        mUtils = CommonUtils.getInstance(ProfileActivity.this);
        sharedPreferences = getSharedPreferences("ADDRESS_INFO", Context.MODE_PRIVATE);

        mAddress = getAddressFromSharedPreferences();
        if (mAddress != null) {
            Log.d(TAG, "address found in shared preferences");
            setAddressField(mAddress, false, false);
        } else {
                Log.w(TAG, "address not found in Shared Preferences. Please Enter a new one");
                setAddressField(null, true, true);
        }

        mSaveAddressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSaveAddressButton.getText() == "EDIT") {
                    Log.d(TAG, "edit button pressed");
                    setAddressField(String.valueOf(mAddressInformation.getText()), true, true);
//                    temp = String.valueOf(mAddressInformation.getText());
                } else if (mSaveAddressButton.getText() == "SAVE") {
                    Log.d(TAG, "Save button pressed");
                    mAddress = String.valueOf(mAddressInformation.getText());

                    //This temp checking should be there. Please add later. removed for easy testing.
                    // checks if the address is changed.
//                    if (!temp.equals(mAddress)) {
                       showAlertDialog();
//                    } else {
//                        setAddressField(mAddress, false, false);
//                        Toast.makeText(ProfileActivity.this, "Address is not changed", Toast.LENGTH_SHORT).show();
//                    }
                }
            }

        });
    }

    private void showAlertDialog() {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setMessage("Resetting address will reset all your values");
        builder1.setCancelable(true);

        builder1.setPositiveButton(
                "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                       storeNewAddress();
                    }
                });

        builder1.setNegativeButton(
                "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        setAddressField(temp, false, false);
                    }
                });

        AlertDialog alert11 = builder1.create();
        alert11.show();
    }

    private void storeNewAddress() {
        mLocation = getLocationFromAddress(mAddress);
        if (mLocation != null) {
            Log.d(TAG, "lat " + mLocation.getLatitude() + " long " + mLocation.getLongitude());
            // save it in Shared Preferences.
            storeAddressInSharedPreferences(mAddress);
            Toast.makeText(ProfileActivity.this, "Address Stored", Toast.LENGTH_SHORT).show();
            setAddressField(mAddress, false, false);
            // this code should be here, not in onResume().

            mUtils.resetEverything(ProfileActivity.this, true);
            //startGeoFence
            startGeoFence();
            startAlarmToPerformOp();
        } else {
            Log.d(TAG, "location is null");
            Toast.makeText(ProfileActivity.this, "Address not Valid. Enter New address", Toast.LENGTH_SHORT).show();
            storeAddressInSharedPreferences(null);
            setAddressField(null, true, true);
        }
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
    private void startAlarmToPerformOp() {
        Log.d(TAG, "startAlarmToPerformOp()");
        AlarmManager alarmMgr = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);

        //Perform Daily Operations
        Intent intent = new Intent(ProfileActivity.this, AlarmReceiver.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(ProfileActivity.this, 0, intent, 0);
        // Set the alarm to start at approximately 00:00 h(24h format).
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 23); // For 11:55 PM
        calendar.set(Calendar.MINUTE, 55);
        calendar.set(Calendar.SECOND, 0);
        //repeteat alarm every 24hours
        alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, alarmIntent);


        //perform Weekly operations
        Intent intentWeek = new Intent(ProfileActivity.this, WeeklyReceiver.class);
        PendingIntent weekIntent = PendingIntent.getBroadcast(ProfileActivity.this, 0, intent, 0);

        Calendar weekCalendar = Calendar.getInstance();
        weekCalendar.set(Calendar.HOUR_OF_DAY, 23);
        weekCalendar.set(Calendar.MINUTE, 58);
        weekCalendar.set(Calendar.SECOND, 0);

        alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                (7*AlarmManager.INTERVAL_DAY), weekIntent);

    }

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
        mEditor = sharedPreferences.edit();
        mEditor.putString("Address", address);
        mEditor.apply();
    }

    public String getAddressFromSharedPreferences() {
        Log.d(TAG, "getAddressFromSharedPreferences() ");
        return sharedPreferences.getString("Address", null);
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
