package com.dvbinventek.dvbapp.viewPager;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
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
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ToolsFragment extends Fragment {

    public static Disposable disposable;
    public static CompositeDisposable disposables = new CompositeDisposable();
    public static Observer<Long> observer;
    public static Observable<Long> observable = Observable.interval(0, 1, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread());
    WeakReference<View> toolsView;
    public static Observer<String> hpaObserver;
    public static Observer<Integer> sighObserver;

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
        MonitoringTextView autoPeep, pPlat;
        autoPeep = toolsView.get().findViewById(R.id.tools_autoPeep);
        pPlat = toolsView.get().findViewById(R.id.tools_pPlat);
        autoPeep.setUnit(Html.fromHtml("cm H<small><sub>2</sub></small>O"));
        pPlat.setUnit(Html.fromHtml("cm H<small><sub>2</sub></small>O"));
        toolsView.get().findViewById(R.id.inspHold).setOnTouchListener(new inspExpHoldOnTouchListener(1));
        toolsView.get().findViewById(R.id.expHold).setOnTouchListener(new inspExpHoldOnTouchListener(2));
        toolsView.get().findViewById(R.id.sigh).setOnClickListener(new sighOnClickListener());
        observer = new Observer<Long>() {
            @Override
            public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {
                disposable = d;
                disposables.add(d);
            }

            @Override
            public void onNext(@io.reactivex.rxjava3.annotations.NonNull Long aLong) {
                autoPeep.setValue(StaticStore.Monitoring.autoPeep, 1);
                pPlat.setValue(StaticStore.Monitoring.pPlat, 1);
            }

            @Override
            public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                e.printStackTrace();
            }

            @Override
            public void onComplete() {
            }
        };
        hpaObserver = new Observer<String>() {
            @Override
            public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {
                disposables.add(d);
            }

            @Override
            public void onNext(@io.reactivex.rxjava3.annotations.NonNull String s) {
                if (s.equals("hpa")) {
                    autoPeep.setUnit("hPa");
                    pPlat.setUnit("hPa");
                } else {
                    autoPeep.setUnit(Html.fromHtml("cm H<small><sub>2</sub></small>O"));
                    pPlat.setUnit(Html.fromHtml("cm H<small><sub>2</sub></small>O"));
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
        sighObserver = new Observer<Integer>() {
            @Override
            public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {
                disposables.add(d);
            }

            @Override
            public void onNext(@io.reactivex.rxjava3.annotations.NonNull Integer integer) {
                Log.d("SIGHOBS", "Called onNext of sighObserver");
                if (integer == R.color.yellow)
                    toolsView.get().findViewById(R.id.sigh).setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FCDD03"))); // yellow
                else if (integer == R.color.orange)
                    toolsView.get().findViewById(R.id.sigh).setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#ff5722"))); // orange
            }

            @Override
            public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
            }

            @Override
            public void onComplete() {
            }
        };
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

class inspExpHoldOnTouchListener implements View.OnTouchListener {
    int which;

    inspExpHoldOnTouchListener(int i) {
        this.which = i;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            v.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#ff5722")));
            SendPacket sp = new SendPacket();
            sp.writeInfo(SendPacket.RNTM, 0);
            sp.writeInfo(SendPacket.RNTM, 276);
            sp.writeInfo((byte) which, 112);
            sp.sendToDevice();
        } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
            v.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FCDD03")));
            SendPacket sp = new SendPacket();
            sp.writeInfo(SendPacket.RNTM, 0);
            sp.writeInfo(SendPacket.RNTM, 276);
            sp.sendToDevice();
        }
        v.performClick();
        return false;
    }
}

class sighOnClickListener implements View.OnClickListener {
    boolean isClicked = false;

    @Override
    public void onClick(View v) {
        v.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#ff5722")));
        new Handler().postDelayed(() -> v.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FCDD03"))), 120);
        SendPacket sp = new SendPacket();
        sp.writeInfo(SendPacket.RNTM, 0);
        sp.writeInfo(SendPacket.RNTM, 276);
        if (!isClicked) sp.writeInfo((byte) 2, 113);
        else sp.writeInfo((byte) 1, 113);
        sp.sendToDevice();
        isClicked = !isClicked;
    }
}
