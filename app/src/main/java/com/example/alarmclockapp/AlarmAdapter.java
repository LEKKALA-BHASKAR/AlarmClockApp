// app/src/main/java/com/example/alarmclockapp/AlarmAdapter.java
package com.example.alarmclockapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Calendar;
import java.util.List;

public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.AlarmHolder> {

    private List<Alarm> alarms;
    private AlarmViewModel alarmViewModel;
    private Context context;

    // Constructor accepting AlarmViewModel
    public AlarmAdapter(AlarmViewModel alarmViewModel) {
        this.alarmViewModel = alarmViewModel;
    }

    @NonNull
    @Override
    public AlarmHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.alarm_item, parent, false);
        return new AlarmHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull AlarmHolder holder, int position) {
        if (alarms != null) {
            Alarm currentAlarm = alarms.get(position);
            String time = String.format("%02d:%02d %s",
                    (currentAlarm.getHour() == 0 || currentAlarm.getHour() == 12) ? 12 : currentAlarm.getHour() % 12,
                    currentAlarm.getMinute(),
                    (currentAlarm.getHour() >= 12) ? "PM" : "AM");
            holder.textViewTime.setText(time);

            // Prevent triggering listener during binding
            holder.switchAlarm.setOnCheckedChangeListener(null);

            // Set the switch state
            holder.switchAlarm.setChecked(currentAlarm.isEnabled());

            // Set switch listener
            holder.switchAlarm.setOnCheckedChangeListener((buttonView, isChecked) -> {
                currentAlarm.setEnabled(isChecked);
                alarmViewModel.update(currentAlarm);
                if (isChecked) {
                    scheduleAlarm(currentAlarm);
                } else {
                    cancelAlarm(currentAlarm);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        if (alarms != null)
            return alarms.size();
        else return 0;
    }

    public void setAlarms(List<Alarm> alarms) {
        this.alarms = alarms;
        notifyDataSetChanged();
    }

    class AlarmHolder extends RecyclerView.ViewHolder {
        private TextView textViewTime;
        private Switch switchAlarm;

        public AlarmHolder(@NonNull View itemView) {
            super(itemView);
            textViewTime = itemView.findViewById(R.id.textViewAlarmTime);
            switchAlarm = itemView.findViewById(R.id.switchAlarm);
        }
    }

    private void scheduleAlarm(Alarm alarm) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("alarmId", alarm.getId());
        intent.putExtra("alarmTone", alarm.getAlarmTone()); // Pass the tone

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
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

    private void cancelAlarm(Alarm alarm) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                alarm.getId(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.cancel(pendingIntent);
    }
}
