// app/src/main/java/com/example/alarmclockapp/AlarmActivity.java
package com.example.alarmclockapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
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


import java.util.Calendar;
import java.util.List;


public class AlarmActivity extends AppCompatActivity {

    private TextView textViewAlarmStatus;
    private Button buttonSnooze, buttonDismiss;

    private AlarmViewModel alarmViewModel;
    private Alarm currentAlarm;

    private Ringtone ringtone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        // Initialize views
        textViewAlarmStatus = findViewById(R.id.textViewAlarmStatus);
        buttonSnooze = findViewById(R.id.buttonSnooze);
        buttonDismiss = findViewById(R.id.buttonDismiss);

        // Initialize ViewModel
        alarmViewModel = new ViewModelProvider(this).get(AlarmViewModel.class);

        // Get Alarm ID from intent
        int alarmId = getIntent().getIntExtra("alarmId", -1);

        // Fetch Alarm details
        alarmViewModel.getAllAlarms().observe(this, new Observer<List<Alarm>>() {
            @Override
            public void onChanged(List<Alarm> alarms) {
                for (Alarm alarm : alarms) {
                    if (alarm.getId() == alarmId) {
                        currentAlarm = alarm;
                        break;
                    }
                }
                if (currentAlarm != null) {
                    textViewAlarmStatus.setText("Alarm for " + formatTime(currentAlarm.getHour(), currentAlarm.getMinute()) + " is Ringing!");
                    playRingtone();
                } else {
                    textViewAlarmStatus.setText("Alarm not found!");
                }
            }
        });

        // Set onClickListeners
        buttonSnooze.setOnClickListener(v -> snoozeAlarm());
        buttonDismiss.setOnClickListener(v -> dismissAlarm());
    }

    private String formatTime(int hour, int minute) {
        String period = (hour >= 12) ? "PM" : "AM";
        int formattedHour = (hour == 0 || hour == 12) ? 12 : hour % 12;
        return String.format("%02d:%02d %s", formattedHour, minute, period);
    }

    private void playRingtone() {
        Uri toneUri;
        if (currentAlarm != null && currentAlarm.getAlarmTone() != null && !currentAlarm.getAlarmTone().isEmpty()) {
            toneUri = Uri.parse(currentAlarm.getAlarmTone());
        } else {
            toneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (toneUri == null) { // Fallback to notification sound
                toneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            }
        }

        ringtone = RingtoneManager.getRingtone(this, toneUri);
        if (ringtone != null) {
            ringtone.play();
        }
    }

    private void snoozeAlarm() {
        if (currentAlarm != null) {
            // Cancel current alarm
            cancelAlarm(currentAlarm);

            // Schedule snooze (e.g., 5 minutes later)
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            Intent intent = new Intent(this, AlarmReceiver.class);
            intent.putExtra("alarmId", currentAlarm.getId());
            intent.putExtra("alarmTone", currentAlarm.getAlarmTone());

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    this,
                    currentAlarm.getId(),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MINUTE, 5); // 5 minutes snooze

            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent
            );

            // Stop ringtone
            if (ringtone != null && ringtone.isPlaying()) {
                ringtone.stop();
            }

            // Close activity
            finish();
        }
    }

    private void dismissAlarm() {
        if (currentAlarm != null) {
            // Cancel current alarm
            cancelAlarm(currentAlarm);

            // Stop ringtone
            if (ringtone != null && ringtone.isPlaying()) {
                ringtone.stop();
            }

            // Close activity
            finish();
        }
    }

    private void cancelAlarm(Alarm alarm) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                alarm.getId(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.cancel(pendingIntent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ringtone != null && ringtone.isPlaying()) {
            ringtone.stop();
        }
    }
}
