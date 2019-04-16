package countedhours.hourscount;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.RemoteViews;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Implementation of App Widget functionality.
 */
public class HoursCountWidget extends AppWidgetProvider {




    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.hours_count_widget);
        CommonUtils mUtils = CommonUtils.getInstance(context);

        SharedPreferences sharedPreferences = context.getSharedPreferences(mUtils.SP_NAME_TIME, Context.MODE_PRIVATE);

        long startTime = sharedPreferences.getLong(mUtils.SP_STARTTIME, 0);
        long totalTime = sharedPreferences.getLong(mUtils.SP_TOTALTIME, 0);
        if (startTime != 0) {
            totalTime = mUtils.calculateElapsedTime(startTime, totalTime);
        }

        //Calculating and setting the time Remaining
        long timeToWork = ((8 * 60 * 60000) - totalTime);

        DateFormat formatter = new SimpleDateFormat("HH:mm:ss", Locale.US);
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));

        views.setTextViewText(R.id.hoursDone, formatter.format(new Date(totalTime)) + " Finished");
        views.setTextViewText(R.id.hoursLeft, formatter.format(new Date(timeToWork)) + " Remaining ");

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

