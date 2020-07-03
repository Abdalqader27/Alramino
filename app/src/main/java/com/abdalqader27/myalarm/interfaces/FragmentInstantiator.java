package com.abdalqader27.myalarm.interfaces;

import androidx.annotation.Nullable;

import com.abdalqader27.myalarm.fragments.BasePagerFragment;

public interface FragmentInstantiator {
    @Nullable
    BasePagerFragment newInstance(int position);
    @Nullable
    String getTitle(int position);
}
