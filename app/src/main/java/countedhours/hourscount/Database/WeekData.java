package countedhours.hourscount.Database;

public class WeekData {

    /*
    This table stores the week information to calculate 40 hours per week. It stores date and total number of hours per day
    +----------------------------------------------+
    |        TABLE NAME = WEEKTIMINGS              |
    |----------------------------------------------+
    |                      |                       |
    |       DATE (String)  |  TOTAL_HOURS (float)  |
    |                      |                       |
    +----------------------------------------------+
     */

    public static final String WEEK_TIMINGS = "WEEKTIMINGS";
    public static final String DATE= "DATE";
    public static final String HOURS = "TOTAL_HOURS";

    private String week_date;
    private float totalHours;

    public static final String CREATE_TABLE_WEEK_TIMINGS = "CREATE TABLE "
            + WEEK_TIMINGS + "(" + DATE + " TEXT," + HOURS
            + " REAL," + ")";

    public String getWeek_date() {
        return week_date;
    }

    public void setWeek_date(String week_date) {
        this.week_date = week_date;
    }

    public float getTotalHours() {
        return totalHours;
    }

    public void setTotalHours(float totalHours) {
        this.totalHours = totalHours;
    }
}
