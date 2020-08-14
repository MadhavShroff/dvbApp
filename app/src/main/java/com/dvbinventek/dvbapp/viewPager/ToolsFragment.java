package com.dvbinventek.dvbapp.viewPager;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.dvbinventek.dvbapp.R;
import com.dvbinventek.dvbapp.SendPacket;
import com.dvbinventek.dvbapp.StaticStore;
import com.dvbinventek.dvbapp.customViews.MonitoringTextView;

import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ToolsFragment extends Fragment {

    public static Disposable disposable;
    public static Observer<Long> observer;
    public static Observable<Long> observable = Observable.interval(0, 1, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread());
    WeakReference<View> toolsView;

    public static void startObserving() {
        observable.subscribe(observer);
    }

    public static void stopObserving() {
        if (disposable != null)
            if (!disposable.isDisposed())
                disposable.dispose();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tools, container, false);
        toolsView = new WeakReference<>(view);
        toolsView.get().findViewById(R.id.inspHold).setOnTouchListener(new inspExpHoldOnTouchListener(1));
        toolsView.get().findViewById(R.id.expHold).setOnTouchListener(new inspExpHoldOnTouchListener(2));
        toolsView.get().findViewById(R.id.sigh).setOnTouchListener(new inspExpHoldOnTouchListener(3));

        observer = new Observer<Long>() {
            @Override
            public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {
                disposable = d;
            }

            @Override
            public void onNext(@io.reactivex.rxjava3.annotations.NonNull Long aLong) {
                ((MonitoringTextView) toolsView.get().findViewById(R.id.tools_autoPeep)).setValue(StaticStore.Monitoring.autoPeep, 1);
                ((MonitoringTextView) toolsView.get().findViewById(R.id.tools_pPlat)).setValue(StaticStore.Monitoring.pPlat, 1);
            }

            @Override
            public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                e.printStackTrace();
            }

            @Override
            public void onComplete() {
            }
        };
        return view;
    }

    static class inspExpHoldOnTouchListener implements View.OnTouchListener {
        int which;

        inspExpHoldOnTouchListener(int i) {
            this.which = i;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                v.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#ff5722")));
                SendPacket sp = new SendPacket();
                sp.writeInfo((short) 0, 0);
                sp.writeInfo((short) 0, 99);
                sp.writeInfo((short) which, 58);
                sp.sendToDevice();
            } else if (event.getAction() != 2) {
                v.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FCDD03")));
                SendPacket sp = new SendPacket();
                sp.writeInfo((short) 0, 0);
                sp.writeInfo((short) 0, 99);
                sp.sendToDevice();
                v.performClick();
            }
            return false;
        }
    }
}
