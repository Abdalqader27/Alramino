package com.abdalqader27.myalarm.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.viewpager.widget.ViewPager;

import com.abdalqader27.myalarm.Alarmio;
import com.abdalqader27.myalarm.R;
import com.abdalqader27.myalarm.adapters.SimplePagerAdapter;
import com.abdalqader27.myalarm.data.SoundData;
import com.abdalqader27.myalarm.fragments.sound.AlarmSoundChooserFragment;
import com.abdalqader27.myalarm.fragments.sound.FileSoundChooserFragment;
import com.abdalqader27.myalarm.fragments.sound.RadioSoundChooserFragment;
import com.abdalqader27.myalarm.fragments.sound.RingtoneSoundChooserFragment;
import com.abdalqader27.myalarm.interfaces.SoundChooserListener;
import com.afollestad.aesthetic.Aesthetic;
import com.google.android.material.tabs.TabLayout;


public class SoundChooserDialog extends DialogFragment implements SoundChooserListener {

    private SoundChooserListener listener;
    private View view;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.AppTheme);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Dialog dialog = getDialog();
        if (dialog != null) {
            Window window = dialog.getWindow();
            if (window != null) {
                WindowManager.LayoutParams params = window.getAttributes();
                if (params != null) {
                    params.windowAnimations = R.style.SlideDialogAnimation;
                }
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.dialog_sound_chooser, container, false);

        Aesthetic.Companion.get()
                .colorPrimary()
                .take(1)
                .subscribe(integer -> view.setBackgroundColor(integer));

        TabLayout tabLayout = view.findViewById(R.id.tabLayout);
        ViewPager viewPager = view.findViewById(R.id.viewPager);

        AlarmSoundChooserFragment alarmFragment = new AlarmSoundChooserFragment();
        RingtoneSoundChooserFragment ringtoneFragment = new RingtoneSoundChooserFragment();
        RadioSoundChooserFragment radioFragment = new RadioSoundChooserFragment();
        FileSoundChooserFragment fileFragment = new FileSoundChooserFragment();

        alarmFragment.setListener(this);
        ringtoneFragment.setListener(this);
        radioFragment.setListener(this);
        fileFragment.setListener(this);

        viewPager.setAdapter(new SimplePagerAdapter(
                getContext(), getChildFragmentManager(),
                new AlarmSoundChooserFragment.Instantiator(view.getContext(), this),
                new RingtoneSoundChooserFragment.Instantiator(view.getContext(), this),
                new RadioSoundChooserFragment.Instantiator(view.getContext(), this),
                new FileSoundChooserFragment.Instantiator(view.getContext(), this))
        );

        tabLayout.setupWithViewPager(viewPager);
        return view;
    }

    public void setListener(SoundChooserListener listener) {
        this.listener = listener;
    }

    @Override
    public void onSoundChosen(SoundData sound) {
        if (listener != null)
            listener.onSoundChosen(sound);

        dismiss();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        if (view != null)
            ((Alarmio) view.getContext().getApplicationContext()).stopCurrentSound();
    }
}
