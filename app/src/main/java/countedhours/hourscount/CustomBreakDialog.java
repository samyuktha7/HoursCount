package countedhours.hourscount;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class CustomBreakDialog extends Dialog {

    private String TAG = "HC_"+CustomBreakDialog.class.getSimpleName();
    private Activity mActivity;

    private EditText mBreakName, mBreakTime;
    private SharedPreferences mSharedPreferences;
    private CommonUtils mUtils;
    private SharedPreferences.Editor mEditor;


    public CustomBreakDialog(Activity a) {
        super(a);
        this.mActivity = a;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.addcustomdialog);

        mBreakName = (EditText) findViewById(R.id.breakName);
        mBreakTime = (EditText) findViewById(R.id.breakTime);
        Button mOk = (Button) findViewById(R.id.ok);
        Button mCancel = (Button) findViewById(R.id.cancel);

        mUtils = CommonUtils.getInstance(mActivity);
        mSharedPreferences = mActivity.getSharedPreferences(mUtils.SP_NAME_TIME, Context.MODE_PRIVATE);

        mOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "OK Button Clicked");

                /*
                Checks if both fields are non-empty
                 */
                if (!isEmpty(mBreakTime) && !isEmpty(mBreakName)) {
                    // check if the breaktime has only numbers entered
                    String text = mBreakTime.getText().toString();
                    try {
                        int num = Integer.parseInt(text);
                        long breakTime = num * 60000;
                        Log.d(TAG, "Break Time =" + num);
                        mEditor= mSharedPreferences.edit();

                        /*
                        There are four cases to consider
                            *  startTime != 0 && totalTime != 0 (calculate total, check and total-break)
                            * startTime != 0 && totalTime == 0 (calculate total, check and start+break)
                            * startTime == 0 && totalTime !=0  ( check and total-break)
                            * stratTime == 0 && totalTime == 0 ( invalid case)
                            *
                            * Also, trigger alarm if the breakTime changes our total time
                         */
                        long startTime = mSharedPreferences.getLong(mUtils.SP_STARTTIME, 0);
                        long totalTime = mSharedPreferences.getLong(mUtils.SP_TOTALTIME, 0);
                        if (startTime != 0) {
                            long bufferTime = System.currentTimeMillis() - startTime;
                            if (totalTime != 0) {
                                Log.d(TAG, "case 1: startTime and totalTime != 0");

                                if (checkIfBreakTimeIsValid(totalTime, breakTime)) {
                                    Log.d(TAG, "case 1.1 : total > break Time");
                                    calculateTimeAfterBreak(totalTime, breakTime);
                                } else {
                                    totalTime = totalTime + bufferTime;
                                    Log.d(TAG, "case 1.1 : total+ buffer > break Time");
                                    if (checkIfBreakTimeIsValid(totalTime, breakTime)) {
                                        startTime = startTime + (totalTime - breakTime);
                                        totalTime = 0;
                                        mEditor.putLong(mUtils.SP_STARTTIME, startTime);
                                        mEditor.putLong(mUtils.SP_TOTALTIME, totalTime);
                                        mEditor.apply();
                                        triggerAlarms(totalTime);
                                    } else {
                                        showToast();
                                    }
                                }
                            } else {
                                Log.d(TAG, "case 1: startTime != 0 and totalTime == 0. First checkIn and no checkout yet");
                                if (checkIfBreakTimeIsValid(bufferTime, breakTime)) {
                                    startTime = startTime + breakTime;
                                    mEditor.putLong(mUtils.SP_STARTTIME, startTime);
                                    mEditor.apply();

                                    //StartTime changed. Trigger alarms
                                    bufferTime = System.currentTimeMillis() - startTime;
                                    totalTime = totalTime + bufferTime;
                                    triggerAlarms(totalTime);
                                } else {
                                    showToast();
                                }
                            }
                        } else {
                            if (totalTime != 0) {
                                Log.d(TAG, "Case 3 : StartTime == 0 and totalTime != 0");
                                if (checkIfBreakTimeIsValid(totalTime, breakTime)) {
                                    calculateTimeAfterBreak(totalTime, breakTime);

                                    //to update UI when Out of office and custom berak is added
                                    mUtils.firstUpdateTodays = true;
                                } else {
                                    showToast();
                                }
                            } else {
                                Log.d(TAG, "case 4 : startTime == 0 and totalTime == 0. Day has not started");
                                showToast();
                            }
                        }
                    } catch (NumberFormatException e) {
                        showToast();
                    }
                } else {
                    Log.d(TAG, "Fields empty");
                    showToast();
                }
                hide();
            }
        });

        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "cancel button clicked : dialog hide");
                hide();
            }
        });
    }

    private boolean checkIfBreakTimeIsValid(long total, long breakTime) {
        return  total >= breakTime;
    }

    private void calculateTimeAfterBreak(long total, long breakTime) {
        total = total - breakTime;
        Log.d(TAG, "Total Time now = "+total);
        mEditor.putLong(mUtils.SP_TOTALTIME, total);
        mEditor.apply();
        triggerAlarms(total);

    }

    private void triggerAlarms(long total) {
        mUtils.triggerEightHourAlarm(mActivity, total);
        mUtils.triggerWeeklyAlarm(mActivity, total);
    }

    private boolean isEmpty(EditText etText) {
        return !(etText.getText().toString().length() > 0);
    }

    private void showToast() {
        Toast.makeText(mActivity, "Invalid Entry", Toast.LENGTH_SHORT).show();
        hide();
    }

}
