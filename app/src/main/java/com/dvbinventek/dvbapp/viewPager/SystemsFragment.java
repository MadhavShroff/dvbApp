package com.dvbinventek.dvbapp.viewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.dvbinventek.dvbapp.LaunchActivity;
import com.dvbinventek.dvbapp.R;
import com.google.android.material.button.MaterialButtonToggleGroup;

public class SystemsFragment extends Fragment {

    public static void disableShutdown() {
        //TODO: Disable shutdown button logic
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_systems, container, false);
        view.findViewById(R.id.calibration).setOnClickListener(v -> {
            startActivity(new Intent(v.getContext(), LaunchActivity.class));
        });
        ((MaterialButtonToggleGroup) view.findViewById(R.id.toggleGroup)).addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            //TODO: Implement logic to perform system wide conversion from hPa to cm H2O
        });
        return view;
    }

}
