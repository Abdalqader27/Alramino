package com.abdalqader27.myalarm.fragments;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.abdalqader27.myalarm.R;
import com.abdalqader27.myalarm.adapters.PreferenceAdapter;
import com.abdalqader27.myalarm.data.preference.AlertWindowPreferenceData;
import com.abdalqader27.myalarm.data.preference.BasePreferenceData;
import com.abdalqader27.myalarm.interfaces.ContextFragmentInstantiator;

import java.util.ArrayList;
import java.util.Arrays;

import io.reactivex.functions.Consumer;


public class SettingsFragment extends BasePagerFragment implements Consumer {
    private RecyclerView recyclerView;
    private PreferenceAdapter preferenceAdapter;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_recycler, container, false);
        recyclerView = v.findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));

        ArrayList<BasePreferenceData> list = new ArrayList<>(Arrays.asList());

        if (Build.VERSION.SDK_INT >= 23)
            list.add(0, new AlertWindowPreferenceData());

        preferenceAdapter = new PreferenceAdapter(list);
        recyclerView.setAdapter(preferenceAdapter);

        return v;
    }

    @Override
    public String getTitle(Context context) {
        return context.getString(R.string.title_settings);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (recyclerView != null && preferenceAdapter != null) {
            recyclerView.post(() -> preferenceAdapter.notifyDataSetChanged());
        }
    }

    @Override
    public void accept(Object o) throws Exception {
        if (recyclerView != null && preferenceAdapter != null) {
            recyclerView.post(() -> preferenceAdapter.notifyDataSetChanged());
        }
    }

    public static class Instantiator extends ContextFragmentInstantiator {

        public Instantiator(Context context) {
            super(context);
        }

        @Override
        public String getTitle(Context context, int position) {
            return context.getString(R.string.title_settings);
        }

        @Nullable
        @Override
        public BasePagerFragment newInstance(int position) {
            return new SettingsFragment();
        }
    }

}
