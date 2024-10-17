// app/src/main/java/com/example/alarmclockapp/AlarmViewModel.java
package com.example.alarmclockapp;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class AlarmViewModel extends AndroidViewModel {
    private AlarmRepository repository;
    private LiveData<List<Alarm>> allAlarms;

    public AlarmViewModel(@NonNull Application application) {
        super(application);
        repository = new AlarmRepository(application);
        allAlarms = repository.getAllAlarms();
    }

    public void insert(Alarm alarm, AlarmRepository.InsertCallback callback) {
        repository.insert(alarm, callback);
    }

    public void update(Alarm alarm) {
        repository.update(alarm);
    }

    public void delete(Alarm alarm) {
        repository.delete(alarm);
    }

    public LiveData<List<Alarm>> getAllAlarms() {
        return allAlarms;
    }
}
