package countedhours.hourscount.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.android.gms.location.Geofence;

import countedhours.hourscount.TimerTask;


/*
This class is SqLIteOpenHelper which performs all the operations such as reading and writing to databases
Database name = timeSheets.db
Table names :
    1.ADDRESS_INFO
    2.CHECK_TIMINGS_TABLE
    3.WEEK_TIMINGS
    4.SHEETS_TABLE
 */
public class SqLiteDatabaseHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION =3;
    private static final String DATABASE_NAME = "timeSheets.db";
    public SQLiteDatabase mDatabase;
    private TimerTask mTimerTask;
    private static SqLiteDatabaseHelper mInstance = null;
    private String TAG = "SqLiteDatabaseHelper";

    public SqLiteDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }
    private Context mCxt;

    public static SqLiteDatabaseHelper getInstance(Context ctx) {
        Log.d("SqLiteDatabaseHelper", "SqLiteDatabaseHelper(): getInstance()");
        /**
         * use the application context as suggested by CommonsWare.
         * this will ensure that you dont accidentally leak an Activitys
         * context (see this article for more information:
         * http://android-developers.blogspot.nl/2009/01/avoiding-memory-leaks.html)
         */
        if (mInstance == null) {
            mInstance = new SqLiteDatabaseHelper(ctx.getApplicationContext());
        }
        return mInstance;
    }

    /**
     * constructor should be private to prevent direct instantiation.
     * make call to static factory method "getInstance()" instead.
     */
    private SqLiteDatabaseHelper(Context ctx) {
        super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
        Log.d(TAG, "SqLiteDatabaseHelper() constructor");
        mCxt= ctx;
        mTimerTask = TimerTask.getInstance(mCxt);
    }

    
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        Log.d(TAG, "onCreate()");
        sqLiteDatabase.execSQL(SheetsData.CREATE_TABLE_SHEETS_DATA);
        sqLiteDatabase.execSQL(CheckInOutTimings.CREATE_TABLE_CHECKINOUT_TIMINGS);
        sqLiteDatabase.execSQL(AddressInformation.CREATE_TABLE_ADDRESS_INFORMATION);
        sqLiteDatabase.execSQL(WeekData.CREATE_TABLE_WEEK_TIMINGS);
        sqLiteDatabase.execSQL(GeofenceTransitions.CREATE_TABLE_GEOFENCE_TRANSITIONS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public synchronized SQLiteDatabase openDatabase() {
        mDatabase = this.getWritableDatabase();
        return mDatabase;
    }

    /*
    Inserts the values into geofenceTransitionValues
     */
    public synchronized int insertGeofenceTransitionValues(int transitionType, long timeOccurred) {
        Log.d(TAG, "insertGeofenceTransitionValues");
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(GeofenceTransitions.TRANSITION_TYPE, transitionType);
        values.put(GeofenceTransitions.TIME_OCCURRED, timeOccurred);
        int row_id=(int)db.insert(GeofenceTransitions.GEO_FENCE_TRANSITIONS_TABLE, null, values);
        db.close();

        if(transitionType == Geofence.GEOFENCE_TRANSITION_ENTER) {
            Log.d(TAG, "Geofence Enter");
            mTimerTask.startTimer();
        } else if (transitionType == Geofence.GEOFENCE_TRANSITION_EXIT) {
            Log.d(TAG, "Geofence Exit");
            mTimerTask.pauseTimer();
        }

        return row_id;
    }

    /*
    Resets ths values in GeofenceTransitionTable. Usually cleared once every day.
     */
    public synchronized void resetGeofencevalues() {
        Log.d(TAG, "resetGeofenceValues()");
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from "+ GeofenceTransitions.GEO_FENCE_TRANSITIONS_TABLE);
        db.close();
        retrieveGeofenceValues();
    }

    /*
    Fetches the values in GEO_FENCE_TRANSITIONS_TABLE TABLE.
     */
    public Cursor retrieveGeofenceValues() {
        Log.d(TAG, "retrieveGeofenceValues()");
        String selectQuery = "SELECT  * FROM " + GeofenceTransitions.GEO_FENCE_TRANSITIONS_TABLE;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        Log.d(TAG, "retreiveGeofenceValues(): cursor count :"+cursor.getCount());
        return cursor;
    }

    public int insertCheckInOutValues(long check_in, long check_out) {
        Log.d(TAG, "insertCheckInOutValues()");
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(CheckInOutTimings.CHECK_IN, check_in);
        values.put(CheckInOutTimings.CHECK_OUT, check_out);
        int row_id=(int)db.insert(CheckInOutTimings.CHECK_TIMINGS_TABLE, null, values);
        db.close();
        return row_id;
    }

    public int insertSheetsValues(String weekStart, String weekEnd, Float hours) {
        Log.d(TAG, "insertSheetsValues()");
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SheetsData.COLUMN_WEEK_START, weekStart);
        values.put(SheetsData.COLUMN_WEEK_END, weekEnd);
        values.put(SheetsData.COLUMN_HOURS, hours);
        int row_id=(int)db.insert(SheetsData.SHEETS_TABLE, null, values);
        db.close();
        return row_id;
    }

    public int insertAddressInfoValues(String Address, Double latitude, Double longitude) {
        Log.d(TAG, "insertAddressInfoValues()");
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(AddressInformation.ADDRESS, Address);
        values.put(AddressInformation.LATITUDE, latitude);
        values.put(AddressInformation.LONGITUDE, longitude);
        int row_id=(int)db.insert(AddressInformation.ADDRESS_INFO, null, values);
        db.close();
        return row_id;
    }

    public int insertWeekValues(String weekday, float totalHours) {
        Log.d(TAG, "insertWeekValues()");
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(WeekData.DATE, weekday);
        values.put(WeekData.HOURS, totalHours);
        int row_id=(int)db.insert(WeekData.WEEK_TIMINGS, null, values);
        db.close();
        return row_id;
    }

    public AddressInformation fetchAddressGeo(String address) {
        Log.d(TAG, "fetch Address Geo Locations from Database");
        AddressInformation addressInformation = new AddressInformation();
        if(address != null) {
            Cursor dbCursor = retrieveAddressInfo();
            while(dbCursor.moveToNext()) {
                if (dbCursor.getString(dbCursor.getColumnIndex(AddressInformation.ADDRESS)).equals(address)){
                    addressInformation.setAddress(dbCursor.getString(dbCursor.getColumnIndex(AddressInformation.ADDRESS)));
                    addressInformation.setLatitude(dbCursor.getDouble(dbCursor.getColumnIndex(AddressInformation.LATITUDE)));
                    addressInformation.setLongitude(dbCursor.getDouble(dbCursor.getColumnIndex(AddressInformation.LONGITUDE)));
                    return addressInformation;
                }
            }
            Log.d(TAG,"fetchGeoAddress(): address not found in database");
        } else {
            Log.w(TAG, "fetchGeoAddress(): address is null");
        }
        return null;
    }

    public AddressInformation getLastSavedAddressInfo() {
        Log.d(TAG, "getLastSavedAddressInfo()");
        Cursor dbCursor = retrieveAddressInfo();
        AddressInformation lastSavedAddress= new AddressInformation();
        if(dbCursor.getCount() >0) {
            dbCursor.moveToPosition(dbCursor.getCount() - 1);
            lastSavedAddress.setAddress(dbCursor.getString(dbCursor.getColumnIndex(AddressInformation.ADDRESS)));
            lastSavedAddress.setLatitude(dbCursor.getDouble(dbCursor.getColumnIndex(AddressInformation.LATITUDE)));
            lastSavedAddress.setLongitude(dbCursor.getDouble(dbCursor.getColumnIndex(AddressInformation.LONGITUDE)));
        } else {
            Log.d(TAG, "getLastSavedAddressInfo(): database is empty");
        }
        return lastSavedAddress;
    }

    public Cursor retrieveAddressInfo() {
        Log.d(TAG, "retrieveAddressInfo()");
        String selectQuery = "SELECT  * FROM " + AddressInformation.ADDRESS_INFO;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        Log.d(TAG, "retreiveAddressInfo(): cursor count :"+cursor.getCount());
        return cursor;
    }
}
