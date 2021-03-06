package com.abdalqader27.myalarm.fragments.sound;

import android.content.Context;

import androidx.annotation.Nullable;

import com.abdalqader27.myalarm.data.SoundData;
import com.abdalqader27.myalarm.fragments.BasePagerFragment;
import com.abdalqader27.myalarm.interfaces.ContextFragmentInstantiator;
import com.abdalqader27.myalarm.interfaces.SoundChooserListener;

import java.lang.ref.WeakReference;


public abstract class BaseSoundChooserFragment extends BasePagerFragment implements SoundChooserListener {

    private SoundChooserListener listener;

    public void setListener(SoundChooserListener listener) {
        this.listener = listener;
    }

    @Override
    public void onSoundChosen(SoundData sound) {
        if (listener != null)
            listener.onSoundChosen(sound);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        listener = null;
    }

    abstract static class Instantiator extends ContextFragmentInstantiator {

        private WeakReference<SoundChooserListener> listener;

        public Instantiator(Context context, SoundChooserListener listener) {
            super(context);
            this.listener = new WeakReference<>(listener);
        }

        @Nullable
        @Override
        public BasePagerFragment newInstance(int position) {
            SoundChooserListener listener = this.listener.get();
            if (listener != null)
                return newInstance(position, listener);
            else return null;
        }

        abstract BasePagerFragment newInstance(int position, SoundChooserListener listener);
    }

}
