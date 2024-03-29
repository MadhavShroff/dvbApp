package com.dvbinventek.dvbapp.viewPager;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.dvbinventek.dvbapp.LaunchActivity;
import com.dvbinventek.dvbapp.MainActivity;
import com.dvbinventek.dvbapp.R;
import com.dvbinventek.dvbapp.SendPacket;
import com.dvbinventek.dvbapp.StaticStore;
import com.google.android.material.button.MaterialButtonToggleGroup;

import java.lang.ref.WeakReference;

import io.reactivex.rxjava3.core.Observable;

public class SystemsFragment extends Fragment {

    //TODO: Add handshake logic, only implement once tested with Ram.

    public static WeakReference<Button> selfTest;
    public static WeakReference<Button> shutdown;
    public static WeakReference<TextView> machineHours;
    public static WeakReference<TextView> patientHours;
    public static WeakReference<TextView> lastServiceDate;
    public static WeakReference<TextView> lastServiceHrs;
    public static WeakReference<TextView> nextServiceDate;
    public static WeakReference<TextView> nextServiceHrs;
    public static WeakReference<TextView> systemVersion;
    //    public static WeakReference<SwitchCompat> oxygenConcentratorSwitch;
    public static WeakReference<MaterialButtonToggleGroup> hpaSwitch;
//    public static CompositeDisposable disposables = new CompositeDisposable();

//    public static Observer<Boolean> O2StateObserver = new Observer<Boolean>() {
//        @Override
//        public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {disposables.add(d);}
//        @Override
//        public void onNext(@io.reactivex.rxjava3.annotations.NonNull Boolean isOn) {oxygenConcentratorSwitch.get().setChecked(isOn); }
//        @Override
//        public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {e.printStackTrace(); }
//        @Override
//        public void onComplete() {}
//    };
//    public static Observer<Boolean> hpaStateObserver = new Observer<Boolean>() { // changes state of hpa switch when invoked from ProcessPacket
//        @Override
//        public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {disposables.add(d);}
//        @Override
//        public void onNext(@io.reactivex.rxjava3.annotations.NonNull Boolean isOn) {
//            if (isOn) { // If hpa is turned on, check the hpa box, and change the unit across app using Observers
//                hpaSwitch.get().check(R.id.hPa);
//                Observable.just("hPa").subscribe(ToolsFragment.hpaObserver);
//                Observable.just("hpa").subscribe(AlarmsFragment.hpaObserver);
//                Observable.just("hpa").subscribe(MonitoringFragment.hpaObserver);
//                Observable.just("hpa").subscribe(ControlsFragment.hpaObserver);
//                Observable.just("hpa").subscribe(MainActivity.hpaObserver);
//            }
//            else {
//                hpaSwitch.get().check(R.id.cmH20);
//                Observable.just("cmh2o").subscribe(ToolsFragment.hpaObserver);
//                Observable.just("cmh2o").subscribe(AlarmsFragment.hpaObserver);
//                Observable.just("cmh2o").subscribe(MonitoringFragment.hpaObserver);
//                Observable.just("cmh2o").subscribe(ControlsFragment.hpaObserver);
//                Observable.just("cmh2o").subscribe(MainActivity.hpaObserver);
//            }
//        }
//        @Override
//        public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {e.printStackTrace(); }
//        @Override
//        public void onComplete() {}
//    };

    public static void disableSelftest(boolean b) { // Disables the self test button
        if (b) {
            selfTest.get().setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#2f2f2f")));
            selfTest.get().setTextColor(Color.parseColor("#515151"));
        } else {
            selfTest.get().setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#00b686")));
            selfTest.get().setTextColor(Color.parseColor("#000000"));
        }
        selfTest.get().setEnabled(!b);
    }

    public static void disableShutdown(boolean b) { // Disables the Shutdown button
        if (b) {
            shutdown.get().setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#2f2f2f")));
            shutdown.get().setTextColor(Color.parseColor("#515151"));
        } else {
            shutdown.get().setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#00b686")));
            shutdown.get().setTextColor(Color.parseColor("#000000"));
        }
        shutdown.get().setEnabled(!b);
    }

    public static void setDetails() {
        machineHours.get().setText(StaticStore.System.machineHours);
        patientHours.get().setText(StaticStore.System.patientHours);
        lastServiceDate.get().setText(StaticStore.System.lastServiceDate);
        lastServiceHrs.get().setText(StaticStore.System.lastServiceHrs);
        nextServiceDate.get().setText(StaticStore.System.nextServiceDate);
        nextServiceHrs.get().setText(StaticStore.System.nextServiceHrs);
        systemVersion.get().setText(StaticStore.System.systemVersion);
    }

    public void getWeakReferances(View v) {
        machineHours = new WeakReference<>(v.findViewById(R.id.machineHours));
        patientHours = new WeakReference<>(v.findViewById(R.id.patientHours));
        lastServiceDate = new WeakReference<>(v.findViewById(R.id.lastServiceDate));
        lastServiceHrs = new WeakReference<>(v.findViewById(R.id.lastServiceHrs));
        nextServiceDate = new WeakReference<>(v.findViewById(R.id.nextServiceDate));
        nextServiceHrs = new WeakReference<>(v.findViewById(R.id.nextServiceHrs));
        systemVersion = new WeakReference<>(v.findViewById(R.id.systemVersion));
//        oxygenConcentratorSwitch = new WeakReference<>(v.findViewById(R.id.o2switch));
        hpaSwitch = new WeakReference<>(v.findViewById(R.id.toggleGroup));
        selfTest = new WeakReference<>(v.findViewById(R.id.selfTest));
        shutdown = new WeakReference<>(v.findViewById(R.id.shutdown));
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_systems, container, false);
        view.findViewById(R.id.selfTest).setOnClickListener(v -> {
            startActivity(new Intent(v.getContext(), LaunchActivity.class));
        });
        getWeakReferances(view);
        view.findViewById(R.id.sleepDisplay).setOnClickListener(MainActivity.sleepButtonListener);
        view.findViewById(R.id.shutdown).setOnClickListener(MainActivity.shutdownClickListener);
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
//        ((MaterialButtonToggleGroup) view.findViewById(R.id.toggleGroup)).getCheckedButtonId();
//        ((MaterialButtonToggleGroup) view.findViewById(R.id.toggleGroup)).addOnButtonCheckedListener((group, checkedId, isChecked) -> {
//            Log.d("MSG", "Called " + isChecked + " " + (checkedId == R.id.hPa));
//            SendPacket sp = new SendPacket();
//            sp.writeInfo(SendPacket.RNTM, 0);
//            sp.writeInfo(SendPacket.RNTM, 276);
//            if (checkedId == R.id.hPa) {
//                if(oxygenConcentratorSwitch.get().isChecked()) { // hpa is on, O2 conc is on
//                    sp.writeInfo(3, 59);
//                } else { // hpa is on, o2 is off
//                    sp.writeInfo(2, 59);
//                }
//            } else {
//                if(oxygenConcentratorSwitch.get().isChecked()) { // hpa is off, O2 conc is on
//                    sp.writeInfo(1, 59);
//                } else { // hpa is off, o2 is off
//                    sp.writeInfo(0, 59);
//                }
//            }
//            sp.sendToDevice();
//        });
//        oxygenConcentratorSwitch.get().setOnCheckedChangeListener((buttonView, isChecked) -> {
//            SendPacket sp = new SendPacket();
//            sp.writeInfo(SendPacket.RNTM, 0);
//            sp.writeInfo(SendPacket.RNTM, 276);
//            if (isChecked) {
//                if(hpaSwitch.get().getCheckedButtonId() == R.id.hPa) { // hpa on o2 on
//                    sp.writeInfo(3, 59);
//                } else { // hpa off o2 on
//                    sp.writeInfo(1, 59);
//                }
//            } else {
//                if(hpaSwitch.get().getCheckedButtonId() == R.id.hPa) { // hpa on o2 off
//                    sp.writeInfo(2, 59);
//                } else { // hpa off o2 off
//                    sp.writeInfo(0, 59);
//                }
//            }
//            sp.sendToDevice();
//        });
        return view;
    }
//
//    @Override
//    public void onDestroyView() {
//        tryToDispose(disposables);
//        super.onDestroyView();
//    }
//
//    private void tryToDispose(Disposable d) {
//        if (d != null) {
//            if (!d.isDisposed()) {
//                d.dispose();
//            }
//        }
//    }
}
