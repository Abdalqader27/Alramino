package me.jfenn.alarmio.activities;

import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.abdalqader27.myalarm.Alarmio;
import com.abdalqader27.myalarm.R;
import com.abdalqader27.myalarm.data.AlarmData;
import com.abdalqader27.myalarm.data.PreferenceData;
import com.abdalqader27.myalarm.data.SoundData;
import com.abdalqader27.myalarm.data.TimerData;
import com.abdalqader27.myalarm.services.SleepReminderService;
import com.abdalqader27.myalarm.utils.FormatUtils;
import com.afollestad.aesthetic.AestheticActivity;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import me.jfenn.slideactionview.SlideActionListener;

public class AlarmActivity extends AestheticActivity implements SlideActionListener {

    public static final String EXTRA_ALARM = "james.alarmio.AlarmActivity.EXTRA_ALARM";
    public static final String EXTRA_TIMER = "james.alarmio.AlarmActivity.EXTRA_TIMER";

    private View overlay;
    private TextView date;
    private TextView time;
    private Alarmio alarmio;
    private Vibrator vibrator;
    private AudioManager audioManager;

    private boolean isAlarm;
    private long triggerMillis;
    private AlarmData alarm;
    private TimerData timer;
    private SoundData sound;
    private boolean isVibrate;

    private boolean isSlowWake;
    private long slowWakeMillis;

    private int currentVolume;
    private int minVolume;
    private int originalVolume;
    private int volumeRange;

    private Handler handler;
    private Runnable runnable;

    private EditText textContrroller;
    private Button buttonSubmit;
    private boolean isDark;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);
        alarmio = (Alarmio) getApplicationContext();
        textContrroller = findViewById(R.id.textContrroller);
        overlay = findViewById(R.id.overlay);
        date = findViewById(R.id.date);
        time = findViewById(R.id.time);
        buttonSubmit = findViewById(R.id.buttonSubmit);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);

        isSlowWake = PreferenceData.SLOW_WAKE_UP.getValue(this);
        slowWakeMillis = PreferenceData.SLOW_WAKE_UP_TIME.getValue(this);

        isAlarm = getIntent().hasExtra(EXTRA_ALARM);
        if (isAlarm) {
            alarm = getIntent().getParcelableExtra(EXTRA_ALARM);
            isVibrate = alarm.isVibrate;
            if (alarm.hasSound())
                sound = alarm.getSound();
        } else if (getIntent().hasExtra(EXTRA_TIMER)) {
            timer = getIntent().getParcelableExtra(EXTRA_TIMER);
            isVibrate = timer.isVibrate;
            if (timer.hasSound())
                sound = timer.getSound();
        } else finish();

        date.setText(FormatUtils.format(new Date(), FormatUtils.FORMAT_DATE + ", " + FormatUtils.getShortFormat(this)));

        if (sound != null && !sound.isSetVolumeSupported()) {
            // Use the backup method if it is not supported

            audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            originalVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
            if (isSlowWake) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    minVolume = audioManager.getStreamMinVolume(AudioManager.STREAM_ALARM);
                } else {
                    minVolume = 0;
                }
                volumeRange = originalVolume - minVolume;
                currentVolume = minVolume;

                audioManager.setStreamVolume(AudioManager.STREAM_ALARM, minVolume, 0);
            }
        }

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        triggerMillis = System.currentTimeMillis();
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                long elapsedMillis = System.currentTimeMillis() - triggerMillis;
                String text = FormatUtils.formatMillis(elapsedMillis);
                time.setText(String.format("-%s", text.substring(0, text.length() - 3)));

                if (isVibrate) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                        vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                    else vibrator.vibrate(500);
                }

                if (sound != null && !sound.isPlaying(alarmio))
                    sound.play(alarmio);

                if (alarm != null && isSlowWake) {
                    float slowWakeProgress = (float) elapsedMillis / slowWakeMillis;

                    WindowManager.LayoutParams params = getWindow().getAttributes();
                    params.screenBrightness = Math.max(0.01f, Math.min(1f, slowWakeProgress));
                    getWindow().setAttributes(params);
                    getWindow().addFlags(WindowManager.LayoutParams.FLAGS_CHANGED);

                    if (sound != null && sound.isSetVolumeSupported()) {
                        float newVolume = Math.min(1f, slowWakeProgress);

                        sound.setVolume(alarmio, newVolume);
                    } else if (currentVolume < originalVolume) {
                        // Backup volume setting behavior
                        int newVolume = minVolume + (int) Math.min(originalVolume, slowWakeProgress * volumeRange);
                        if (newVolume != currentVolume) {
                            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, newVolume, 0);
                            currentVolume = newVolume;
                        }
                    }
                }

                handler.postDelayed(this, 1000);
            }
        };
        handler.post(runnable);

        if (sound != null)
            sound.play(alarmio);

        SleepReminderService.refreshSleepTime(alarmio);
        buttonSubmit.setOnClickListener(v -> {
            if (textContrroller.getText().toString().trim().equals("28")) {
                overlay.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                finish();
            } else {
                Toast.makeText(getApplicationContext(), "", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(getApplicationContext(),"مافي مجال خل المسائلة ",Toast.LENGTH_LONG).show();
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAnnoyingness();
    }

    private void stopAnnoyingness() {
        if (handler != null)
            handler.removeCallbacks(runnable);

        if (sound != null && sound.isPlaying(alarmio)) {
            sound.stop(alarmio);

            if (isSlowWake && !sound.isSetVolumeSupported()) {
                audioManager.setStreamVolume(AudioManager.STREAM_ALARM, originalVolume, 0);
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        finish();
        startActivity(new Intent(intent));
    }

    @Override
    public void onSlideLeft() {
        final int[] minutes = new int[]{2, 5, 10, 20, 30, 60};
        CharSequence[] names = new CharSequence[minutes.length + 1];
        for (int i = 0; i < minutes.length; i++) {
            names[i] = FormatUtils.formatUnit(AlarmActivity.this, minutes[i]);
        }

        names[minutes.length] = getString(R.string.title_snooze_custom);

        stopAnnoyingness();
        new AlertDialog.Builder(AlarmActivity.this, isDark ? R.style.Theme_AppCompat_Dialog_Alert : R.style.Theme_AppCompat_Light_Dialog_Alert)
                .setItems(names, (dialog, which) -> {
                    if (which < minutes.length) {
                        TimerData timer = alarmio.newTimer();
                        timer.setDuration(TimeUnit.MINUTES.toMillis(minutes[which]), alarmio);
                        timer.setVibrate(AlarmActivity.this, isVibrate);
                        timer.setSound(AlarmActivity.this, sound);
                        timer.set(alarmio, ((AlarmManager) AlarmActivity.this.getSystemService(Context.ALARM_SERVICE)));
                        alarmio.onTimerStarted();

                        finish();
                    }
                })
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.dismiss())
                .show();

        overlay.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
    }

    @Override
    public void onSlideRight() {
        overlay.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
        finish();
    }
}
