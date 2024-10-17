// app/src/main/java/com/example/alarmclockapp/SetAlarmActivity.java
package com.example.alarmclockapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;



import java.util.Calendar;

public class SetAlarmActivity extends AppCompatActivity {

    private Button buttonPickTime, buttonPickTone, buttonSaveAlarm;
    private TextView textViewSelectedTime, textViewSelectedTone;

    private int selectedHour = -1;
    private int selectedMinute = -1;
    private Uri selectedToneUri;

    private static final int REQUEST_CODE_PICK_TONE = 1;

    private AlarmViewModel alarmViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_alarm); // Ensure this points to your correct layout

        // Initialize views
        buttonPickTime = findViewById(R.id.buttonPickTime);
        buttonPickTone = findViewById(R.id.buttonPickTone);
        buttonSaveAlarm = findViewById(R.id.buttonSaveAlarm);
        textViewSelectedTime = findViewById(R.id.textViewSelectedTime);
        textViewSelectedTone = findViewById(R.id.textViewSelectedTone);

        // Initialize ViewModel
        alarmViewModel = new ViewModelProvider(this).get(AlarmViewModel.class);

        // Set onClickListener for Time Picker
        buttonPickTime.setOnClickListener(v -> showTimePickerDialog());

        // Set onClickListener for Tone Picker
        buttonPickTone.setOnClickListener(v -> showTonePicker());

        // Set onClickListener for Save Alarm
        buttonSaveAlarm.setOnClickListener(v -> saveAlarm());
    }

    private void showTimePickerDialog() {
        // Use TimePickerDialog
        android.app.TimePickerDialog timePickerDialog = new android.app.TimePickerDialog(
                SetAlarmActivity.this,
                (view, hourOfDay, minute) -> {
                    selectedHour = hourOfDay;
                    selectedMinute = minute;
                    String time = String.format("%02d:%02d %s",
                            (hourOfDay == 0 || hourOfDay == 12) ? 12 : hourOfDay % 12,
                            minute,
                            (hourOfDay >= 12) ? "PM" : "AM");
                    textViewSelectedTime.setText("Selected Time: " + time);
                },
                Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                Calendar.getInstance().get(Calendar.MINUTE),
                false
        );
        timePickerDialog.show();
    }

    private void showTonePicker() {
        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Alarm Tone");
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, (Uri) null);
        startActivityForResult(intent, REQUEST_CODE_PICK_TONE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Handle Tone Picker result
        if (requestCode == REQUEST_CODE_PICK_TONE && resultCode == RESULT_OK) {
            selectedToneUri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            if (selectedToneUri != null) {
                Ringtone ringtone = RingtoneManager.getRingtone(this, selectedToneUri);
                String toneName = ringtone.getTitle(this);
                textViewSelectedTone.setText("Selected Tone: " + toneName);
            } else {
                textViewSelectedTone.setText("Selected Tone: Default");
            }
        }
    }

    private void saveAlarm() {
        if (selectedHour == -1 || selectedMinute == -1) {
            Toast.makeText(this, "Please select a time for the alarm.", Toast.LENGTH_SHORT).show();
            return;
        }

        String alarmTone = (selectedToneUri != null) ? selectedToneUri.toString() : "";

        // Create Alarm object
        Alarm alarm = new Alarm(selectedHour, selectedMinute, true, alarmTone);

        // Insert into database with callback to get the generated ID
        alarmViewModel.insert(alarm, new AlarmRepository.InsertCallback() {
            @Override
            public void onInsertCompleted(long id) {
                alarm.setId((int) id); // Assuming id fits into int

                Toast.makeText(SetAlarmActivity.this, "Alarm Saved", Toast.LENGTH_SHORT).show();

                // Schedule the alarm
                scheduleAlarm(alarm);

                // Finish activity
                finish();
            }
        });
    }

    private void scheduleAlarm(Alarm alarm) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("alarmId", alarm.getId());
        intent.putExtra("alarmTone", alarm.getAlarmTone()); // Pass the tone

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                alarm.getId(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Set the alarm time
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, alarm.getHour());
        calendar.set(Calendar.MINUTE, alarm.getMinute());
        calendar.set(Calendar.SECOND, 0);

        // If the time is before now, set for the next day
        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        // Set the alarm
        alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                pendingIntent
        );
    }
}
