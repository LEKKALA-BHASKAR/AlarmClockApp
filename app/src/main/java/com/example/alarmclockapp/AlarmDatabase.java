// app/src/main/java/com/example/alarmclockapp/AlarmDatabase.java
package com.example.alarmclockapp;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Alarm.class}, version = 1, exportSchema = true) // exportSchema=true to export Room's schema
public abstract class AlarmDatabase extends RoomDatabase {

    private static AlarmDatabase instance;

    public abstract AlarmDao alarmDao();

    public static synchronized AlarmDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            AlarmDatabase.class, "alarm_database")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}
