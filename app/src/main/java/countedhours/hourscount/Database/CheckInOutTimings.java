package countedhours.hourscount.Database;

public class CheckInOutTimings {

    /*
    This table is used to store the check in and out timings in a day.
    +----------------------------------------------+
    |        TABLE NAME = INOUTTIMINGS             |
    |----------------------------------------------+
    |                      |                       |
    |     CHECK_IN(long)   |      CHECK_OUT(long)        |
    |                      |                       |
    +----------------------------------------------+
     */

    public CheckInOutTimings() {}

    public static final String CHECK_TIMINGS_TABLE = "IN_OUT_TIMINGS";
    public static final String CHECK_IN= "CHECK_IN";
    public static final String CHECK_OUT = "CHECK_OUT";


    public static final String CREATE_TABLE_CHECKINOUT_TIMINGS = "CREATE TABLE "
            + CHECK_TIMINGS_TABLE + "(" + CHECK_IN + " INTEGER," + CHECK_OUT
            + " INTEGER" + ")";

    private long check_in;
    private long check_out;

    public CheckInOutTimings(long check_in, long check_out) {
        this.check_in = check_in;
        this.check_out = check_out;
    }

    public long getCheck_in() {
        return check_in;
    }

    public long getCheck_out() {
        return check_out;
    }

    public void setCheck_in(long check_in) {
        this.check_in = check_in;
    }

    public void setCheck_out(long check_out) {
        this.check_out = check_out;
    }

}
