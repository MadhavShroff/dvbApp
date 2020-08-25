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
import com.dvbinventek.dvbapp.MainActivity;
import com.dvbinventek.dvbapp.R;
import com.dvbinventek.dvbapp.SendPacket;
import com.google.android.material.button.MaterialButtonToggleGroup;

import io.reactivex.rxjava3.core.Observable;

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
            SendPacket sp = new SendPacket();
            sp.writeInfo(SendPacket.RNTM, 0);
            sp.writeInfo(SendPacket.RNTM, 276);
            String s;
            if (checkedId == R.id.hPa) {
                s = "hpa";
                sp.writeInfo(2, 59);
            } else {
                s = "cmh2o";
                sp.writeInfo(1, 59);
            }
            sp.sendToDevice();
            Observable.just(s).subscribe(ToolsFragment.hpaObserver);
            Observable.just(s).subscribe(AlarmsFragment.hpaObserver);
            Observable.just(s).subscribe(MonitoringFragment.hpaObserver);
            Observable.just(s).subscribe(ControlsFragment.hpaObserver);
            Observable.just(s).subscribe(MainActivity.hpaObserver);
        });
        return view;
    }

}
