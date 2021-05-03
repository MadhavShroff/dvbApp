package com.dvbinventek.dvbapp;

import android.content.ComponentName;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.dvbinventek.dvbapp.viewPager.ControlsFragment;
import com.google.android.material.button.MaterialButton;

import java.util.Objects;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;

public class StandbyFragment extends Fragment {

    public static boolean controlsConfirmClicked = false;
    public static boolean alarmConfirmClicked = false;
    public static boolean isInView = false;
    public static Observer<Long> confirmButtonClickObserver;
    public CompositeDisposable disposables = new CompositeDisposable();

    public static void setIsInView(boolean b) {
        isInView = b;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_standby, container, false);
        AppCompatButton startVentilation = view.findViewById(R.id.startVentilation);

        view.findViewById(R.id.launch_controls).setOnClickListener(v -> ((ViewPager2) Objects.requireNonNull(getActivity()).findViewById(R.id.viewPager)).setCurrentItem(0));
        view.findViewById(R.id.launch_alarm).setOnClickListener(v -> ((ViewPager2) Objects.requireNonNull(getActivity()).findViewById(R.id.viewPager)).setCurrentItem(2));

        startVentilation.setEnabled(false);
        startVentilation.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#62727b")));
        confirmButtonClickObserver = new Observer<Long>() {
            @Override
            public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {
                disposables.add(d);
            }

            @Override
            public void onNext(@io.reactivex.rxjava3.annotations.NonNull Long aLong) {
                if (!isInView) return;
                if (aLong == 1) // 1 =>  controls confirm clicked
                    controlsConfirmClicked = true;
                else if (aLong == 2)  // 2 => alarms confirm clicked
                    alarmConfirmClicked = true;
                if (controlsConfirmClicked && alarmConfirmClicked) {
                    // Enable start button
                    controlsConfirmClicked = false;
                    alarmConfirmClicked = false;
                    startVentilation.setEnabled(true);
                    startVentilation.setBackgroundTintList(null);
                }
            }

            @Override
            public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                e.printStackTrace();
            }

            @Override
            public void onComplete() {
            }
        };

        startVentilation.setOnClickListener((v) -> {
            StaticStore.restrictedCommunicationDueToStandby = false;
            SendPacket sp = new SendPacket();
            sp.writeDefaultSTRTPacketValues();
            sp.sendToDevice();
            getActivity().findViewById(R.id.mainChart).setVisibility(View.VISIBLE);
            getActivity().findViewById(R.id.standbyFragmentContainer).setVisibility(View.GONE);
            setIsInView(false);
            Observable.just(startVentilation).subscribe(ControlsFragment.revertStandbyClickObserver);
            startVentilation.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#62727b")));
            startVentilation.setEnabled(false);
        });
        view.findViewById(R.id.sleepButton).setOnClickListener(MainActivity.sleepButtonListener);
        return view;
    }

    @Override
    public void onDestroy() {
        if (disposables != null)
            if (!disposables.isDisposed())
                disposables.dispose();
        super.onDestroy();
    }
}
