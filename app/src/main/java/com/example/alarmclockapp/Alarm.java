// app/src/main/java/com/example/alarmclockapp/Alarm.java
package com.example.alarmclockapp;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "alarms")
public class Alarm {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private int hour;
    private int minute;
    private boolean isEnabled;
    private String alarmTone;

    // Constructor
    public Alarm(int hour, int minute, boolean isEnabled, String alarmTone) {
        this.hour = hour;
        this.minute = minute;
        this.isEnabled = isEnabled;
        this.alarmTone = alarmTone;
    }

    // Getters and Setters

    public int getId() {
        return id;
    }

    public void setId(int id) { // Room sets this automatically
        this.id = id;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) { this.hour = hour; }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) { this.minute = minute; }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) { isEnabled = enabled; }

    public String getAlarmTone() {
        return alarmTone;
    }

    public void setAlarmTone(String alarmTone) { this.alarmTone = alarmTone; }
}
