package countedhours.hourscount.BroadcastReceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import countedhours.hourscount.Database.GeofenceTransitions;
import countedhours.hourscount.Database.SqLiteDatabaseHelper;

public class AlarmReceiver extends BroadcastReceiver {

    private GeofenceTransitions mGeofenceTransitionTable;
    private SqLiteDatabaseHelper dbhelper;

    @Override
    public void onReceive(final Context context, Intent intent) {
        dbhelper = new SqLiteDatabaseHelper(context, "AddressCommit", null, 1);
        dbhelper.resetGeofencevalues();
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        throw new UnsupportedOperationException("Not yet implemented");




    }
}
