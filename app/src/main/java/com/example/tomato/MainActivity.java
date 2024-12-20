// MainActivity.java
package com.example.tomato;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private TextView timerTextView;
    private NumberPicker hourPicker;
    private NumberPicker minutePicker;
    private NumberPicker secondPicker;
    private Button startPauseButton;
    private Button resetButton;
    private Button recordButton;
    private TextView modeTextView;

    private Timer timer;
    private int totalSeconds;
    private int remainingSeconds;
    private boolean isRunning = false;
    private TimerMode currentMode = TimerMode.POMODORO;
    private String focusPurpose;

    enum TimerMode {
        POMODORO, SHORT_BREAK, LONG_BREAK
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences sharedPreferences = getSharedPreferences("FocusRecords", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        timerTextView = findViewById(R.id.timerTextView);
        hourPicker = findViewById(R.id.hourPicker);
        minutePicker = findViewById(R.id.minutePicker);
        secondPicker = findViewById(R.id.secondPicker);
        startPauseButton = findViewById(R.id.startPauseButton);
        resetButton = findViewById(R.id.resetButton);
        recordButton = findViewById(R.id.recordButton);
        modeTextView = findViewById(R.id.modeTextView);

        hourPicker.setMinValue(0);
        hourPicker.setMaxValue(23);
        minutePicker.setMinValue(0);
        minutePicker.setMaxValue(59);
        secondPicker.setMinValue(0);
        secondPicker.setMaxValue(59);

        // Set default values to 0
        hourPicker.setValue(0);
        minutePicker.setValue(0);
        secondPicker.setValue(0);

        // Initial visibility
        timerTextView.setVisibility(View.GONE);
        hourPicker.setVisibility(View.VISIBLE);
        minutePicker.setVisibility(View.VISIBLE);
        secondPicker.setVisibility(View.VISIBLE);

        updateTimerDisplay();

        startPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRunning) {
                    pauseTimer();
                } else {
                    showFocusPurposeDialog();
                }
            }
        });

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetTimer();
            }
        });

        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RecordActivity.class);
                startActivity(intent);
            }
        });
    }

    private void showFocusPurposeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("請選擇專注目的");

        final EditText editText = new EditText(MainActivity.this); //final一個editText

        builder.setView(editText);

        builder.setPositiveButton("確定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                focusPurpose = editText.getText().toString();
                modeTextView.setText("專注於 " + focusPurpose); // Update modeTextView
                startTimer();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void startTimer() {
        if (!isRunning) {
            int hours = hourPicker.getValue();
            int minutes = minutePicker.getValue();
            int seconds = secondPicker.getValue();
            totalSeconds = (hours * 3600) + (minutes * 60) + seconds;
            remainingSeconds = totalSeconds;

            // Update visibility
            timerTextView.setVisibility(View.VISIBLE);
            hourPicker.setVisibility(View.GONE);
            minutePicker.setVisibility(View.GONE);
            secondPicker.setVisibility(View.GONE);

            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (remainingSeconds > 0) {
                                remainingSeconds--;
                                updateTimerDisplay();
                            } else {
                                completeTimer();
                            }
                        }
                    });
                }
            }, 0, 1000);

            isRunning = true;
            startPauseButton.setText("暫停");
        }
    }

    private void pauseTimer() {
        if (timer != null) {
            timer.cancel();
        }
        isRunning = false;
        startPauseButton.setText("開始");
    }

    private void resetTimer() {
        if (timer != null) {
            timer.cancel();
        }
        remainingSeconds = totalSeconds;
        isRunning = false;
        updateTimerDisplay();
        startPauseButton.setText("開始");

        // Update visibility
        timerTextView.setVisibility(View.GONE);
        hourPicker.setVisibility(View.VISIBLE);
        minutePicker.setVisibility(View.VISIBLE);
        secondPicker.setVisibility(View.VISIBLE);
    }

    private void completeTimer() {
        pauseTimer();
        saveCompletionTime();

        // Save focus record to SQLite database
        FocusRecordManager recordManager = new FocusRecordManager(this);
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;
        recordManager.insertFocusRecord(currentDate, hours, minutes, seconds, focusPurpose);
        recordManager.close();

        Intent intent = new Intent(MainActivity.this, RecordActivity.class);
        intent.putExtra("duration", getFormattedDuration());
        intent.putExtra("endTime", getCurrentTime());
        startActivity(intent);

        switch (currentMode) {
            case POMODORO:
                currentMode = TimerMode.SHORT_BREAK;
                remainingSeconds = 5 * 60; // Short break 5 minutes
                break;
            case SHORT_BREAK:
                currentMode = TimerMode.POMODORO;
                remainingSeconds = 25 * 60; // Back to 25 minutes work
                break;
        }
        updateTimerDisplay();

        // Revert modeTextView text
        modeTextView.setText("專注模式");

        // Update visibility
        timerTextView.setVisibility(View.GONE);
        hourPicker.setVisibility(View.VISIBLE);
        minutePicker.setVisibility(View.VISIBLE);
        secondPicker.setVisibility(View.VISIBLE);
    }

    private String getFormattedDuration() {
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;
        return String.format(Locale.getDefault(), "總專心時間 %02d:%02d:%02d", hours, minutes, seconds);
    }

    private String getCurrentTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
    }

    private void saveCompletionTime() {
        SharedPreferences sharedPreferences = getSharedPreferences("FocusRecords", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        editor.putString(String.valueOf(System.currentTimeMillis()), currentTime);
        editor.apply();
    }

    private void updateTimerDisplay() {
        int hours = remainingSeconds / 3600;
        int minutes = (remainingSeconds % 3600) / 60;
        int seconds = remainingSeconds % 60;

        hourPicker.setValue(hours);
        minutePicker.setValue(minutes);
        secondPicker.setValue(seconds);

        String timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        timerTextView.setText(timeString);
    }
}
// this code is only for the reference, it may not work as it is, you need to modify it as per your requirement.