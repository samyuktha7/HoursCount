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
    private Activity c;

    private EditText mBreakName, mBreakTime;
    private SharedPreferences mSharedPreferences;
    private CommonUtils mUtils;
    private SharedPreferences.Editor mEditor;

    public CustomBreakDialog(Activity a) {
        super(a);
        this.c = a;
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

        mUtils = CommonUtils.getInstance(c);
        mSharedPreferences =c.getSharedPreferences(mUtils.SP_NAME_TIME, Context.MODE_PRIVATE);
        mEditor = mSharedPreferences.edit();


        mOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "OK Button Clicked");

                if (!isEmpty(mBreakTime) && !isEmpty(mBreakName)) {

                    // check if the breaktime has only numbers entered
                    String text = mBreakTime.getText().toString();
                    try {
                        int num = Integer.parseInt(text);
                        long breakTime = num * 60000;
                        Log.d(TAG, "Break Time =" + num);
                         /*
                        get total time from shared preferences. If it is not equal to zero, minus and put it back.
                        If its equal to zero, calculate the total time
                        * If its still zero, toast "Break Time exceeds Total Time"
                        */
                        long totalTime = mSharedPreferences.getLong(mUtils.SP_TOTALTIME, 0);
                        if (totalTime != 0) {
                            if (totalTime > breakTime) {
                                calculateTimeAfterBreak(totalTime, breakTime);
                            } else {
                                showToast();
                            }
                        } else {
                            Log.d(TAG, "total time = 0. Calculating ");
                            long startTime = mSharedPreferences.getLong(mUtils.SP_STARTTIME, 0);
                            if (startTime != 0) {
                                long bufferTime = System.currentTimeMillis() - startTime;
                                totalTime = totalTime + bufferTime;
                                if (totalTime != 0 && totalTime > breakTime) {
                                    calculateTimeAfterBreak(totalTime, breakTime);
                                }
                            }
                        }
                        mEditor.apply();
                    } catch (NumberFormatException e) {
                        showToast();
                    }

                    hide();
                } else {
                    Log.d(TAG, "Fields empty");
                    showToast();
                }
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

    private void calculateTimeAfterBreak(long total, long breakTime) {
        Log.d(TAG, "break time < total time. Removing breaktime from total time now");
        total = total - breakTime;
        Log.d(TAG, "Total Time now = "+total);
        mEditor.putLong(mUtils.SP_TOTALTIME, total);
    }

    private boolean isEmpty(EditText etText) {
        return !(etText.getText().toString().length() > 0);
    }

    private void showToast() {
        Toast.makeText(c, "Invalid Entry", Toast.LENGTH_SHORT).show();
        hide();
    }

}
