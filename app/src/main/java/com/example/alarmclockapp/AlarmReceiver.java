// app/src/main/java/com/example/alarmclockapp/AlarmReceiver.java
package com.example.alarmclockapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class AlarmReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "AlarmChannel";
    private static final int NOTIFICATION_ID = 1;

    @Override
    public void onReceive(Context context, Intent intent) {
        // Retrieve Alarm ID and Tone
        int alarmId = intent.getIntExtra("alarmId", -1);
        String alarmTone = intent.getStringExtra("alarmTone");

        // Create notification
        createNotificationChannel(context);

        // Intent to open AlarmActivity when notification is clicked
        Intent alarmIntent = new Intent(context, AlarmActivity.class);
        alarmIntent.putExtra("alarmId", alarmId);
        alarmIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                alarmId,
                alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Build notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_alarm_notification) // Ensure this drawable exists
                .setContentTitle("Alarm")
                .setContentText("Your alarm is ringing.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        // Show notification
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(alarmId, builder.build());

        // Play alarm tone
        playAlarmTone(context, alarmTone);
    }

    private void createNotificationChannel(Context context) {
        // Create the NotificationChannel, only on API 26+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Alarm Channel";
            String description = "Channel for Alarm notifications";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            // Register the channel with the system
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void playAlarmTone(Context context, String alarmTone) {
        Uri toneUri;
        if (alarmTone != null && !alarmTone.isEmpty()) {
            toneUri = Uri.parse(alarmTone);
        } else {
            toneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (toneUri == null) { // Fallback to notification sound
                toneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            }
        }

        Ringtone ringtone = RingtoneManager.getRingtone(context, toneUri);
        if (ringtone != null) {
            ringtone.play();
        }
    }
}
