package com.dvbinventek.dvbapp.viewPager;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ViewPagerFragmentAdapter extends FragmentStateAdapter {

    public ViewPagerFragmentAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new ControlsFragment();
            case 1:
                return new MonitoringFragment();
            case 2:
                return new AlarmsFragment();
            case 3:
                return new ToolsFragment();
            case 4:
                return new PatientFragment();
            case 5:
                return new EventsFragment();
            case 6:
                return new SystemsFragment();
        }
        return null;
    }

    @Override
    public int getItemCount() {
        return 7;
    }
}