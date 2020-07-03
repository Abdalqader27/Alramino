package com.abdalqader27.myalarm.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.abdalqader27.myalarm.Alarmio;
import com.abdalqader27.myalarm.data.TimerData;

import java.util.List;


public class TimerService extends Service {

    private static final int NOTIFICATION_ID = 427;

    private final IBinder binder = new LocalBinder();

    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (timers.size() > 0) {
                handler.removeCallbacks(this);
                handler.postDelayed(this, 10);
            } else stopForeground(true);
        }
    };

    private List<TimerData> timers;

    @Override
    public void onCreate() {
        super.onCreate();
        timers = ((Alarmio) getApplicationContext()).getTimers();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handler.removeCallbacks(runnable);
        runnable.run();
        return super.onStartCommand(intent, flags, startId);
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        //listener = null;
        return super.onUnbind(intent);
    }

    public class LocalBinder extends Binder {
        public TimerService getService() {
            return TimerService.this;
        }
    }
}
