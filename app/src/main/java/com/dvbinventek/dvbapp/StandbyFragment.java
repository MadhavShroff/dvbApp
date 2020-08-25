package com.dvbinventek.dvbapp;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.dvbinventek.dvbapp.viewPager.ControlsFragment;

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
    public View.OnClickListener clickListener;

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

        clickListener = (v) -> {
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
        };
        startVentilation.setOnClickListener(clickListener);
//        longPressListener = new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                if (event.getAction() == MotionEvent.ACTION_DOWN) {
//                    v.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#ff5722")));
//                    Observable.interval(1, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread()).take(5).subscribe(new Observer<Long>() {
//                        @Override
//                        public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) { disposables.add(d); holdDisposable = d; }
//                        @Override
//                        public void onNext(@io.reactivex.rxjava3.annotations.NonNull Long aLong) {
//                            TextView tv = view.findViewById(R.id.pressAndHold);
//                            tv.setText(getString(R.string.press_and_hold_the_start_button_for_5_seconds, 5 - aLong-1));
//                            if(aLong == 4) onComplete();
//                        }
//                        @Override
//                        public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {e.printStackTrace(); }
//                        @SuppressLint("ClickableViewAccessibility")
//                        @Override
//                        public void onComplete() {
//                            //standby screen exit back to graphs
//                            ((TextView)view.findViewById(R.id.pressAndHold)).setText("");
//                            new Handler().postDelayed(() -> startVentilation.setOnClickListener(clickListener), 1000);
//                            startVentilation.setOnTouchListener(null);
//                            TextView tv = view.findViewById(R.id.pressAndHold);
//                            tv.setText("");
//                            v.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#1aab00")));
//                            StaticStore.restrictedCommunicationDueToStandby = false;
//                            SendPacket sp = new SendPacket();
//                            sp.writeDefaultSTRTPacketValues();
//                            sp.sendToDevice();
//                            getActivity().findViewById(R.id.mainChart).setVisibility(View.VISIBLE);
//                            getActivity().findViewById(R.id.standbyFragmentContainer).setVisibility(View.GONE);
//                            Observable.just(v).subscribe(ControlsFragment.revertStandbyClickObserver);
//                        }
//                    });
//                } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
//                    v.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#1aab00")));
//                    TextView tv = view.findViewById(R.id.pressAndHold);
//                    tv.setText(getString(R.string.press_and_hold_the_start_button_for_5_seconds, 5));
//                    if(holdDisposable!= null)
//                        if(!holdDisposable.isDisposed())
//                            holdDisposable.dispose();
//                }
//                return false;
//            }
//        };

//        clickListener = (v -> {
//            Log.d("STANDBY", "Clicked startVentilation");
//            v.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FCDD03")));
//            new Handler().postDelayed(() -> v.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#1aab00"))), 100);
//            startVentilation.setEnabled(false);
//            Observable.interval(0, 1, TimeUnit.SECONDS)
//                    .take(10)
//                    .subscribeOn(Schedulers.computation())
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribe(new Observer<Long>() {
//                        @SuppressLint("ClickableViewAccessibility")
//                        @Override
//                        public void onComplete() {
//                            Log.d("MSG", "onCompleted");
//                            startVentilation.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#1aab00")));
//                            TextView tv = view.findViewById(R.id.pressAndHold);
//                            tv.setText(getString(R.string.press_and_hold_the_start_button_for_5_seconds, 5));
//                            tv.setVisibility(View.VISIBLE);
//                            startVentilation.setEnabled(true);
//                            startVentilation.setOnClickListener(null);
//                            startVentilation.setOnTouchListener(longPressListener);
//                        }
//                        @Override
//                        public void onError(Throwable e) {
//                            e.printStackTrace();
//                        }
//                        @Override
//                        public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) { }
//                        @Override
//                        public void onNext(Long aLong) {
//                            if(aLong % 2 == 0) v.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#1aab00")));
//                            else v.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#ff5722")));
//                        }
//                    });
//        });
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
