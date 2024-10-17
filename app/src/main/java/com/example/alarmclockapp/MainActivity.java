// app/src/main/java/com/example/alarmclockapp/MainActivity.java
package com.example.alarmclockapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;


import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView textViewTime, textViewDate;
    private FloatingActionButton fabSetAlarm;
    private RecyclerView recyclerViewAlarms;
    private AlarmAdapter alarmAdapter;
    private AlarmViewModel alarmViewModel;

    private Handler handler = new Handler();
    private Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Ensure this points to your correct layout

        // Initialize views
        textViewTime = findViewById(R.id.textViewTime);
        textViewDate = findViewById(R.id.textViewDate);
        fabSetAlarm = findViewById(R.id.fabSetAlarm);
        recyclerViewAlarms = findViewById(R.id.recyclerViewAlarms);

        // Initialize ViewModel
        alarmViewModel = new ViewModelProvider(this).get(AlarmViewModel.class);

        // Setup RecyclerView
        recyclerViewAlarms.setLayoutManager(new LinearLayoutManager(this));
        alarmAdapter = new AlarmAdapter(alarmViewModel); // Pass the ViewModel here
        recyclerViewAlarms.setAdapter(alarmAdapter);

        // Observe alarms LiveData
        alarmViewModel.getAllAlarms().observe(this, new Observer<List<Alarm>>() {
            @Override
            public void onChanged(List<Alarm> alarms) {
                alarmAdapter.setAlarms(alarms);
            }
        });

        // Update time and date every second
        updateTimeAndDate();

        // Set onClickListener for FAB
        fabSetAlarm.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SetAlarmActivity.class);
            startActivity(intent);
        });
    }

    private void updateTimeAndDate() {
        runnable = new Runnable() {
            @Override
            public void run() {
                // Update time
                String currentTime = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());
                textViewTime.setText(currentTime);

                // Update date
                String currentDate = new SimpleDateFormat("EEEE, MMM d, yyyy", Locale.getDefault()).format(new Date());
                textViewDate.setText(currentDate);

                // Schedule next update after 1 second
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(runnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }
}
