package com.dvbinventek.dvbapp.viewPager;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.dvbinventek.dvbapp.LaunchActivity;
import com.dvbinventek.dvbapp.MainActivity;
import com.dvbinventek.dvbapp.R;
import com.dvbinventek.dvbapp.SendPacket;
import com.google.android.material.button.MaterialButtonToggleGroup;

import java.lang.ref.WeakReference;

import io.reactivex.rxjava3.core.Observable;

public class SystemsFragment extends Fragment {

    public static WeakReference<Button> selfTest;

    public static void disableShutdown() {
        //TODO: Disable shutdown button logic
    }

    public static void disableSelftest(boolean b) {
        if (b) {
            selfTest.get().setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#2f2f2f")));
            selfTest.get().setTextColor(Color.parseColor("#515151"));
        } else {
            selfTest.get().setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#00b686")));
            selfTest.get().setTextColor(Color.parseColor("#000000"));
        }
        selfTest.get().setEnabled(b);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_systems, container, false);
        view.findViewById(R.id.selfTest).setOnClickListener(v -> {
            startActivity(new Intent(v.getContext(), LaunchActivity.class));
        });
        selfTest = new WeakReference<>(view.findViewById(R.id.selfTest));
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
