package countedhours.hourscount.Database;

public class GeofenceTransitions {

    /*
    This table is used to store the geofenceTransitions that occur in a day and
    +----------------------------------------------+
    |        TABLE NAME = GEOFENCETRANSITIONS      |
    |----------------------------------------------+
    |                      |                       |
    |     TRANSITION_TYPE  |      TIME_OCCURRED    |
    |        String        |        long           |
    +----------------------------------------------+
     */

    public GeofenceTransitions() {}

    public static final String GEO_FENCE_TRANSITIONS_TABLE = "GEOFENCE_TRANSITIONS_TABLE";
    public static final String TRANSITION_TYPE = "TRANSITION_TYPE";
    public static final String TIME_OCCURRED = "TIME_OCCURRED";


    public static final String CREATE_TABLE_GEOFENCE_TRANSITIONS = "CREATE TABLE "
            + GEO_FENCE_TRANSITIONS_TABLE + "(" + TRANSITION_TYPE + " INTEGER," + TIME_OCCURRED
            + " INTEGER" + ")";


    public int getTransitionType() {
        return TransitionType;
    }

    public void setTransitionType(int transitionType) {
        TransitionType = transitionType;
    }

    public long getTimeOccurred() {
        return TimeOccurred;
    }

    public void setTimeOccurred(long timeOccurred) {
        TimeOccurred = timeOccurred;
    }

    private Integer TransitionType;
    private long TimeOccurred;

    public GeofenceTransitions(int TRANSITION_TYPE, long timeOccurred) {
        this.TransitionType = TRANSITION_TYPE;
        this.TimeOccurred = timeOccurred;
    }

}
