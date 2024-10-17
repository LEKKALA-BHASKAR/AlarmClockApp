// app/src/main/java/com/example/alarmclockapp/AlarmRepository.java
package com.example.alarmclockapp;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.List;

public class AlarmRepository {
    private AlarmDao alarmDao;
    private LiveData<List<Alarm>> allAlarms;

    public AlarmRepository(Application application) {
        AlarmDatabase database = AlarmDatabase.getInstance(application);
        alarmDao = database.alarmDao();
        allAlarms = alarmDao.getAllAlarms();
    }

    public void insert(Alarm alarm, InsertCallback callback) {
        new InsertAlarmAsyncTask(alarmDao, callback).execute(alarm);
    }

    public void update(Alarm alarm) {
        new UpdateAlarmAsyncTask(alarmDao).execute(alarm);
    }

    public void delete(Alarm alarm) {
        new DeleteAlarmAsyncTask(alarmDao).execute(alarm);
    }

    public LiveData<List<Alarm>> getAllAlarms() {
        return allAlarms;
    }

    // Callback interface to get inserted ID
    public interface InsertCallback {
        void onInsertCompleted(long id);
    }

    private static class InsertAlarmAsyncTask extends AsyncTask<Alarm, Void, Long> {
        private AlarmDao alarmDao;
        private InsertCallback callback;

        private InsertAlarmAsyncTask(AlarmDao alarmDao, InsertCallback callback) {
            this.alarmDao = alarmDao;
            this.callback = callback;
        }

        @Override
        protected Long doInBackground(Alarm... alarms) {
            return alarmDao.insertAlarm(alarms[0]);
        }

        @Override
        protected void onPostExecute(Long id) {
            if (callback != null) {
                callback.onInsertCompleted(id);
            }
        }
    }

    private static class UpdateAlarmAsyncTask extends AsyncTask<Alarm, Void, Void> {
        private AlarmDao alarmDao;

        private UpdateAlarmAsyncTask(AlarmDao alarmDao) {
            this.alarmDao = alarmDao;
        }

        @Override
        protected Void doInBackground(Alarm... alarms) {
            alarmDao.update(alarms[0]);
            return null;
        }
    }

    private static class DeleteAlarmAsyncTask extends AsyncTask<Alarm, Void, Void> {
        private AlarmDao alarmDao;

        private DeleteAlarmAsyncTask(AlarmDao alarmDao) {
            this.alarmDao = alarmDao;
        }

        @Override
        protected Void doInBackground(Alarm... alarms) {
            alarmDao.delete(alarms[0]);
            return null;
        }
    }
}
