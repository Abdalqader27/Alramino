package com.abdalqader27.myalarm.fragments.sound;

import android.content.Context;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.abdalqader27.myalarm.R;
import com.abdalqader27.myalarm.adapters.SoundsAdapter;
import com.abdalqader27.myalarm.data.SoundData;
import com.abdalqader27.myalarm.fragments.BasePagerFragment;
import com.abdalqader27.myalarm.interfaces.SoundChooserListener;

import java.util.ArrayList;
import java.util.List;


public class RingtoneSoundChooserFragment extends BaseSoundChooserFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sound_chooser_list, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        List<SoundData> sounds = new ArrayList<>();
        RingtoneManager manager = new RingtoneManager(getContext());
        manager.setType(RingtoneManager.TYPE_RINGTONE);
        Cursor cursor = manager.getCursor();
        int count = cursor.getCount();
        if (count > 0 && cursor.moveToFirst()) {
            do {
                sounds.add(new SoundData(cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX), SoundData.TYPE_RINGTONE, cursor.getString(RingtoneManager.URI_COLUMN_INDEX) + "/" + cursor.getString(RingtoneManager.ID_COLUMN_INDEX)));
            } while (cursor.moveToNext());
        }

        SoundsAdapter adapter = new SoundsAdapter(getAlarmio(), sounds);
        adapter.setListener(this);
        recyclerView.setAdapter(adapter);

        return view;
    }

    @Override
    public String getTitle(Context context) {
        return context.getString(R.string.title_ringtones);
    }

    public static class Instantiator extends BaseSoundChooserFragment.Instantiator {

        public Instantiator(Context context, SoundChooserListener listener) {
            super(context, listener);
        }

        @Override
        BasePagerFragment newInstance(int position, SoundChooserListener listener) {
            BaseSoundChooserFragment fragment = new RingtoneSoundChooserFragment();
            fragment.setListener(listener);
            return fragment;
        }

        @Override
        public String getTitle(Context context, int position) {
            return context.getString(R.string.title_ringtones);
        }
    }

}
