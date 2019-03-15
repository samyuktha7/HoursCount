package countedhours.hourscount.Activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.List;

import countedhours.hourscount.Database.AddressInformation;
import countedhours.hourscount.Database.SqLiteDatabaseHelper;
import countedhours.hourscount.R;

public class ProfileActivity extends AppCompatActivity {

    private EditText mAddressInformation;
    private String mAddress;
    private Button mSaveAddressButton;
    private SqLiteDatabaseHelper dbHelper;
    private String TAG = "ProfileActivity";
    public Double[] latLong = new Double[2];
    private Address mLocation;
    private SharedPreferences sharedPreferences;

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
        if(mAddress != null) {
            Log.d(TAG, "address found in shared preferences");
            setAddressField(mAddress, false, false);
        } else {
            AddressInformation addressInformation = dbHelper.getLastSavedAddressInfo();
            if (addressInformation != null)
            {
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
                    mAddress= String.valueOf(mAddressInformation.getText());
                    mLocation = getLocationFromAddress(mAddress);
                    if (mLocation != null) {
                        Log.d(TAG, "lat " + mLocation.getLatitude() + " long " + mLocation.getLongitude());
                        // save it in the database and Shared Preferences.
                        storeAddressInSharedPreferences(mAddress);
                        int row_id = storeAddressInDatabase(mAddress, mLocation.getLatitude(), mLocation.getLongitude());
                        if (row_id > 0) {
                            Log.d(TAG, "saved in database: row_id "+row_id);
                            Toast.makeText(ProfileActivity.this, "Address Stored", Toast.LENGTH_SHORT).show();
                            setAddressField(mAddress, false, false);
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

    public Address getLocationFromAddress(String strAddress) {
        Geocoder coder = new Geocoder(this);
        List<Address> address;

        try {
            Log.d(TAG, "getLocationFromAddress(): "+strAddress);
            address = coder.getFromLocationName(strAddress, 1);
            if (address == null) {
                Log.d(TAG, "address null. Enter new one");
                return null;
            }
            return address.get(0);
        } catch (Exception e) {
            Log.d(TAG, "exception Occurred"+e);
            return null;
        }
    }

    public void storeAddressInSharedPreferences(String address) {
        Log.d(TAG, "storeAddressInSharedPreferences "+address);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("Address", address);
        editor.apply();
    }

    public String getAddressFromSharedPreferences() {
        Log.d(TAG, "getAddressFromSharedPreferences() ");
       return sharedPreferences.getString("Address", null);
    }

    public int storeAddressInDatabase(String address, double latitude, double longitude ) {
        Log.d(TAG, "storeAddressInDatabase() "+address);
        return dbHelper.insertAddressInfoValues(mAddress, latLong[0], latLong[1]);
    }

    public void setAddressField(String address, boolean enabled, boolean save) {
        Log.d(TAG, "setAddressField = "+address+"; enabled = "+enabled+"; save ="+save);
        if(address != null) {
            mAddressInformation.setText(address);
        } else {
            mAddressInformation.setText(" ");
        }

        mAddressInformation.setEnabled(enabled);
        if (enabled) {
            if(address != null) {
                mAddressInformation.setSelection(address.length());
            }
        }

        if(save) {
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
