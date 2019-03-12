package countedhours.hourscount.Database;

public class SheetsData {

    /*
    This table stores the sheets data for all the weeks with week start date, week end date and hours covered during that week.
    +---------------------------------------------------------------+
    |                 TABLE NAME = SHEETSDATA                       |
    | ------------------------------------------------------------- |
    |                     |                     |                   |
    |  WEEK_START(String) |   WEEK_END (string) |    HOURS  (float) |
    |                     |                     |                   |
    +---------------------------------------------------------------+
     */

    public static final String SHEETS_TABLE = "SHEETSDATA";
    public static final String COLUMN_WEEK_START = "WEEK_START";
    public static final String COLUMN_WEEK_END = "WEEK_END";
    public static final String COLUMN_HOURS = "HOURS";

    public String weekStart;
    public String weekEnd;
    public float hours;


    public static final String CREATE_TABLE_SHEETS_DATA = "CREATE TABLE "
            + SHEETS_TABLE + "(" + COLUMN_WEEK_START + " TEXT," + COLUMN_WEEK_END
            + " TEXT," + COLUMN_HOURS + " REAL" + ")";

    public void setWeekStart(String weekStart) {
        this.weekStart = weekStart;
    }

    public void setWeekEnd(String weekEnd) {
        this.weekEnd = weekEnd;
    }

    public float getHours() {
        return hours;
    }

    public void setHours(float hours) {
        this.hours = hours;
    }

    public String getWeekStart() {
        return  weekStart;
    }

    public String getWeekEnd() {
        return  weekEnd;
    }
}
