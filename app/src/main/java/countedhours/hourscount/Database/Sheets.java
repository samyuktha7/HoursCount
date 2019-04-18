package countedhours.hourscount.Database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;



 /*
    This table stores the sheets data for all the weeks with  week end date and hours covered during that week.
    sheets data will store hours of every week. Every end of the week, database will be updated with the week ending hours.
    +------------------------------------------------+
    |                 TABLE NAME = Sheets        |
    | -----------------------------------------------|
    |                        |                       |
    |      weekend (string)  |    hours  (float)     |
    |                        |                       |
    +------------------------------------------------+
     */

/*
Using Room database. Entity is the table name.
 */
@Entity
public class Sheets {

    @PrimaryKey
    @android.support.annotation.NonNull
    private String weekend;

    @ColumnInfo
    private float hours;

    public String getWeekend() {
        return weekend;
    }

    public void setWeekend(String weekend) {
        this.weekend = weekend;
    }

    public float getHours() {
        return hours;
    }

    public void setHours(float hours) {
        this.hours = hours;
    }
}
