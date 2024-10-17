// app/src/main/java/com/example/alarmclockapp/BootReceiver.java
package com.example.alarmclockapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;


import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            // Reschedule all active alarms
            AlarmDatabase db = AlarmDatabase.getInstance(context);
            AlarmDao alarmDao = db.alarmDao();
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                LiveData<List<Alarm>> liveAlarms = alarmDao.getAllAlarms();
                liveAlarms.observeForever(new Observer<List<Alarm>>() {
                    @Override
                    public void onChanged(List<Alarm> alarms) {
                        for (Alarm alarm : alarms) {
                            if (alarm.isEnabled()) {
                                AlarmScheduler.scheduleAlarm(context, alarm);
                            }
                        }
                        liveAlarms.removeObserver(this);
                    }
                });
            });
        }
    }
}
