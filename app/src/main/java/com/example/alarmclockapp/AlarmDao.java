// app/src/main/java/com/example/alarmclockapp/AlarmDao.java
package com.example.alarmclockapp;

import androidx.lifecycle.LiveData;
import androidx.room.*;

import java.util.List;

@Dao
public interface AlarmDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insertAlarm(Alarm alarm); // Returns the generated ID

    @Update
    void update(Alarm alarm);

    @Delete
    void delete(Alarm alarm);

    @Query("SELECT * FROM alarms ORDER BY hour, minute")
    LiveData<List<Alarm>> getAllAlarms();
}
