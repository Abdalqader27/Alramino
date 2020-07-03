package com.abdalqader27.myalarm.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.AlarmClock;
import android.provider.Settings;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.abdalqader27.myalarm.Alarmio;
import com.abdalqader27.myalarm.R;
import com.abdalqader27.myalarm.data.PreferenceData;
import com.abdalqader27.myalarm.dialogs.AlertDialog;
import com.abdalqader27.myalarm.fragments.BaseFragment;
import com.abdalqader27.myalarm.fragments.HomeFragment;
import com.abdalqader27.myalarm.fragments.SplashFragment;
import com.afollestad.aesthetic.AestheticActivity;

import java.lang.ref.WeakReference;


public class MainActivity extends AestheticActivity implements FragmentManager.OnBackStackChangedListener, Alarmio.ActivityListener {

    public static final String EXTRA_FRAGMENT = "me.jfenn.alarmio.MainActivity.EXTRA_FRAGMENT";
    public static final int FRAGMENT_STOPWATCH = 2;

    private Alarmio alarmio;
    private WeakReference<BaseFragment> fragmentRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        alarmio = (Alarmio) getApplicationContext();
        alarmio.setListener(this);

        if (savedInstanceState == null) {
            BaseFragment fragment = createFragmentFor(getIntent());
            if (fragment == null)
                return;

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment, fragment)
                    .commit();

            fragmentRef = new WeakReference<>(fragment);
        } else {
            BaseFragment fragment;

            if (fragmentRef == null || (fragment = fragmentRef.get()) == null)
                fragment = new HomeFragment();

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment, fragment)
                    .commit();

            fragmentRef = new WeakReference<>(fragment);
        }

        getSupportFragmentManager().addOnBackStackChangedListener(this);

        // background permissions info
        if (Build.VERSION.SDK_INT >= 23 && !PreferenceData.INFO_BACKGROUND_PERMISSIONS.getValue(this, false)) {
            AlertDialog alert = new AlertDialog(this);
            alert.setTitle(getString(R.string.info_background_permissions_title));
            alert.setContent(getString(R.string.info_background_permissions_body));
            alert.setListener((dialog, ok) -> {
                if (ok) {
                    PreferenceData.INFO_BACKGROUND_PERMISSIONS.setValue(MainActivity.this, true);
                    startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION));
                }
            });
            alert.show();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (isActionableIntent(intent)) {
            FragmentManager manager = getSupportFragmentManager();
            BaseFragment newFragment = createFragmentFor(intent);
            BaseFragment fragment = fragmentRef != null ? fragmentRef.get() : null;

            if (newFragment == null || newFragment.equals(fragment)) // check that fragment isn't already displayed
                return;

            if (newFragment instanceof HomeFragment && manager.getBackStackEntryCount() > 0) // clear the back stack
                manager.popBackStack(manager.getBackStackEntryAt(0).getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);

            FragmentTransaction transaction = manager.beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_up_sheet, R.anim.slide_out_up_sheet, R.anim.slide_in_down_sheet, R.anim.slide_out_down_sheet)
                    .replace(R.id.fragment, newFragment);

            if (fragment instanceof HomeFragment && !(newFragment instanceof HomeFragment))
                transaction.addToBackStack(null);

            fragmentRef = new WeakReference<>(newFragment);
            transaction.commit();
        }
    }
    @Nullable
    private BaseFragment createFragmentFor(Intent intent) {
        BaseFragment fragment = fragmentRef != null ? fragmentRef.get() : null;
        int fragmentId = intent.getIntExtra(EXTRA_FRAGMENT, -1);

        if (fragmentId == FRAGMENT_STOPWATCH) {
            return fragment;
        }
        if (Intent.ACTION_MAIN.equals(intent.getAction()) || intent.getAction() == null)
            return new SplashFragment();

        Bundle args = new Bundle();
        args.putString(HomeFragment.INTENT_ACTION, intent.getAction());

        BaseFragment newFragment = new HomeFragment();
        newFragment.setArguments(args);
        return newFragment;
    }

    private boolean isActionableIntent(Intent intent) {
        return intent.hasExtra(EXTRA_FRAGMENT) || AlarmClock.ACTION_SHOW_ALARMS.equals(intent.getAction()) || AlarmClock.ACTION_SET_TIMER.equals(intent.getAction()) || AlarmClock.ACTION_SET_ALARM.equals(intent.getAction()) || Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && AlarmClock.ACTION_SHOW_TIMERS.equals(intent.getAction());

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (alarmio != null)
            alarmio.setListener(null);

        alarmio = null;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState != null ? outState : new Bundle());
    }

    @Override
    protected void onPause() {
        super.onPause();
        alarmio.stopCurrentSound();
    }

    @Override
    public void onBackStackChanged() {
        BaseFragment fragment = (BaseFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
        fragmentRef = new WeakReference<>(fragment);
    }

    @Override
    public void requestPermissions(String... permissions) {
        ActivityCompat.requestPermissions(this, permissions, 0);
    }

    @Override
    public FragmentManager gettFragmentManager() {
        return getSupportFragmentManager();
    }
}
