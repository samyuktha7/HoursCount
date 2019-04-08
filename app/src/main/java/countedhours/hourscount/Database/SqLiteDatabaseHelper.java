package countedhours.hourscount.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


/*
This class is SqLIteOpenHelper which performs all the operations such as reading and writing to databases
Database name = timeSheets.db
Table name :  SHEETS_TABLE
 */
public class SqLiteDatabaseHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION =3;
    private static final String DATABASE_NAME = "timeSheets.db";
    public SQLiteDatabase mDatabase;
    private static SqLiteDatabaseHelper mInstance = null;
    private String TAG = "SqLiteDatabaseHelper";

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
    }

    
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        Log.d(TAG, "onCreate()");
        sqLiteDatabase.execSQL(SheetsData.CREATE_TABLE_SHEETS_DATA);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public int insertSheetsValues(String weekEnd, Float hours) {
        Log.d(TAG, "insertSheetsValues()");
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SheetsData.COLUMN_WEEK_END, weekEnd);
        values.put(SheetsData.COLUMN_HOURS, hours);
        int row_id=(int)db.insert(SheetsData.SHEETS_TABLE, null, values);
        db.close();
        return row_id;
    }

    public Cursor retrieveSheetsInfo() {
        Log.d(TAG, "retrieveSheetsInfo()");
        String selectQuery = "SELECT  * FROM " + SheetsData.SHEETS_TABLE;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        Log.d(TAG, "retreiveSheets(): cursor count :"+cursor.getCount());
        return cursor;
    }
}
