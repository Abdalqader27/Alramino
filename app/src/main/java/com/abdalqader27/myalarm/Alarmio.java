package com.abdalqader27.myalarm;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.widget.Toast;

import androidx.fragment.app.FragmentManager;
import androidx.multidex.MultiDexApplication;

import com.abdalqader27.myalarm.data.AlarmData;
import com.abdalqader27.myalarm.data.PreferenceData;
import com.abdalqader27.myalarm.data.SoundData;
import com.abdalqader27.myalarm.data.TimerData;
import com.abdalqader27.myalarm.services.SleepReminderService;
import com.abdalqader27.myalarm.services.TimerService;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.source.MediaSourceFactory;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;



public class Alarmio extends MultiDexApplication implements Player.EventListener {


    public static final String NOTIFICATION_CHANNEL_STOPWATCH = "stopwatch";
    public static final String NOTIFICATION_CHANNEL_TIMERS = "timers";


    private Ringtone currentRingtone;

    private List<AlarmData> alarms;
    private List<TimerData> timers;

    private List<AlarmioListener> listeners;
    private ActivityListener listener;

    private SimpleExoPlayer player;
    private HlsMediaSource.Factory hlsMediaSourceFactory;
    private ProgressiveMediaSource.Factory progressiveMediaSourceFactory;
    private String currentStream;

    @Override
    public void onCreate() {
        super.onCreate();
      //  DebugUtils.setup(this);

        listeners = new ArrayList<>();
        alarms = new ArrayList<>();
        timers = new ArrayList<>();

        player = new SimpleExoPlayer.Builder(this).build();
        player.addListener(this);

        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "exoplayer2example"), null);
        hlsMediaSourceFactory = new HlsMediaSource.Factory(dataSourceFactory);
        progressiveMediaSourceFactory = new ProgressiveMediaSource.Factory(dataSourceFactory);

        int alarmLength = PreferenceData.ALARM_LENGTH.getValue(this);
        for (int id = 0; id < alarmLength; id++) {
            alarms.add(new AlarmData(id, this));
        }

        int timerLength = PreferenceData.TIMER_LENGTH.getValue(this);
        for (int id = 0; id < timerLength; id++) {
            TimerData timer = new TimerData(id, this);
            if (timer.isSet())
                timers.add(timer);
        }

        if (timerLength > 0)
            startService(new Intent(this, TimerService.class));

        SleepReminderService.refreshSleepTime(this);
    }

    public List<AlarmData> getAlarms() {
        return alarms;
    }

    public List<TimerData> getTimers() {
        return timers;
    }

    /**
     * Create a new alarm, assigning it an unused preference id.
     *
     * @return          The newly instantiated [AlarmData](./data/AlarmData).
     */
    public AlarmData newAlarm() {
        AlarmData alarm = new AlarmData(alarms.size(), Calendar.getInstance());
        alarm.sound = SoundData.fromString(PreferenceData.DEFAULT_ALARM_RINGTONE.getValue(this, ""));
        alarms.add(alarm);
        onAlarmCountChanged();
        return alarm;
    }

    /**
     * Remove an alarm and all of its its preferences.
     *
     * @param alarm     The alarm to be removed.
     */
    public void removeAlarm(AlarmData alarm) {
        alarm.onRemoved(this);

        int index = alarms.indexOf(alarm);
        alarms.remove(index);
        for (int i = index; i < alarms.size(); i++) {
            alarms.get(i).onIdChanged(i, this);
        }

        onAlarmCountChanged();
        onAlarmsChanged();
    }

    /**
     * Update preferences to show that the alarm count has been changed.
     */
    public void onAlarmCountChanged() {
        PreferenceData.ALARM_LENGTH.setValue(this, alarms.size());
    }

    /**
     * Notify the application of changes to the current alarms.
     */
    public void onAlarmsChanged() {
        for (AlarmioListener listener : listeners) {
            listener.onAlarmsChanged();
        }
    }

    /**
     * Create a new timer, assigning it an unused preference id.
     *
     * @return          The newly instantiated [TimerData](./data/TimerData).
     */
    public TimerData newTimer() {
        TimerData timer = new TimerData(timers.size());
        timers.add(timer);
        onTimerCountChanged();
        return timer;
    }

    /**
     * Remove a timer and all of its preferences.
     *
     * @param timer     The timer to be removed.
     */
    public void removeTimer(TimerData timer) {
        timer.onRemoved(this);

        int index = timers.indexOf(timer);
        timers.remove(index);
        for (int i = index; i < timers.size(); i++) {
            timers.get(i).onIdChanged(i, this);
        }

        onTimerCountChanged();
        onTimersChanged();
    }

    /**
     * Update the preferences to show that the timer count has been changed.
     */
    public void onTimerCountChanged() {
        PreferenceData.TIMER_LENGTH.setValue(this, timers.size());
    }

    /**
     * Notify the application of changes to the current timers.
     */
    public void onTimersChanged() {
        for (AlarmioListener listener : listeners) {
            listener.onTimersChanged();
        }
    }
    public void onTimerStarted() {
        startService(new Intent(this, TimerService.class));
    }
    public boolean isRingtonePlaying() {
        return currentRingtone != null && currentRingtone.isPlaying();
    }


    public void playRingtone(Ringtone ringtone) {
        if (!ringtone.isPlaying()) {
            stopCurrentSound();
            ringtone.play();
        }

        currentRingtone = ringtone;
    }
    private void playStream(String url, String type, MediaSourceFactory factory) {
        stopCurrentSound();
        player.prepare(factory.createMediaSource(Uri.parse(url)));
        player.setPlayWhenReady(true);

        currentStream = url;
    }
    public void playStream(String url, String type) {
        playStream(url, type, hlsMediaSourceFactory);
    }
    public void playStream(String url, String type, AudioAttributes attributes) {
        player.stop();
        player.setAudioAttributes(attributes);
        playStream(url, type);
    }
    public void stopStream() {
        player.stop();
        currentStream = null;
    }

    public void setStreamVolume(float volume) {
        player.setVolume(volume);
    }
    public boolean isPlayingStream(String url) {
        return currentStream != null && currentStream.equals(url);
    }
    public void stopCurrentSound() {
        if (isRingtonePlaying())
            currentRingtone.stop();

        stopStream();
    }

    public void addListener(AlarmioListener listener) {
        listeners.add(listener);
    }

    public void removeListener(AlarmioListener listener) {
        listeners.remove(listener);
    }

    public void setListener(ActivityListener listener) {
        this.listener = listener;

    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {
    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
    }

    @Override
    public void onLoadingChanged(boolean isLoading) {
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        switch (playbackState) {
            case Player.STATE_BUFFERING:
            case Player.STATE_READY:
            // We are idle while switching from HLS to Progressive streaming
            case Player.STATE_IDLE:
                break;
            default:
                currentStream = null;
                break;
        }
    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {
    }

    @Override
    public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {
    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
        String lastStream = currentStream;
        currentStream = null;
        Exception exception;
        switch (error.type) {
            case ExoPlaybackException.TYPE_RENDERER:
                exception = error.getRendererException();
                break;
            case ExoPlaybackException.TYPE_SOURCE:
                if (lastStream != null && error.getSourceException().getMessage().contains("does not start with the #EXTM3U header")) {
                    playStream(lastStream, SoundData.TYPE_RADIO, progressiveMediaSourceFactory);
                    return;
                }
                exception = error.getSourceException();
                break;
            case ExoPlaybackException.TYPE_UNEXPECTED:
                exception = error.getUnexpectedException();
                break;
            default:
                return;
        }

        exception.printStackTrace();
        Toast.makeText(this, exception.getClass().getName() + ": " + exception.getMessage(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPositionDiscontinuity(int reason) {
    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
    }

    @Override
    public void onSeekProcessed() {
    }

    public void requestPermissions(String... permissions) {
        if (listener != null)
            listener.requestPermissions(permissions);
    }

    public FragmentManager getFragmentManager() {
        if (listener != null)
            return listener.gettFragmentManager();
        else return null;
    }

    public interface AlarmioListener {
        void onAlarmsChanged();

        void onTimersChanged();
    }

    public interface ActivityListener {
        void requestPermissions(String... permissions);

        FragmentManager gettFragmentManager(); //help
    }

}
