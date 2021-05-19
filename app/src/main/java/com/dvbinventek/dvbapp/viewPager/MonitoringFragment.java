package com.dvbinventek.dvbapp.viewPager;

import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.dvbinventek.dvbapp.R;
import com.dvbinventek.dvbapp.StaticStore;
import com.dvbinventek.dvbapp.customViews.MonitoringTextView;

import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MonitoringFragment extends Fragment {

    public static Disposable disposable;
    public static Observer<Long> observer;
    public static Observable<Long> observable = Observable.interval(0, 1, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread());
    WeakReference<View> monitoringView;
    public static Observer<String> hpaObserver;

    public static void startObserving() {
        observable.subscribe(observer);
    }

    public static void stopObserving() {
        if (disposable != null)
            if (!disposable.isDisposed())
                disposable.dispose();
    }

    public static String getIE(int ie) {
        String s = "";
        int i = ie / 1000;
        int id = (ie / 100) % 10;
        int e = (ie / 10) % 10;
        int ed = ie % 10;
//        if (id == 0 && ed == 0)
//            s = i + ":" + e;
//        else if (id == 0)
//            s = i + ":" + e + "." + ed;
//        else if (ed == 0)
//            s = i + "." + id + ":" + e;
//        else  // Write IE in Monitoring as i.id : e.ed always irrespective of if decimal is 0
            s = i + "." + id + ":" + e + "." + ed;
        return s;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_monitoring, container, false);
        monitoringView = new WeakReference<>(view);
        MonitoringTextView mvTotal = monitoringView.get().findViewById(R.id.monit_mvTotal);
        MonitoringTextView mvSpont = monitoringView.get().findViewById(R.id.monit_mvSpont);
        MonitoringTextView Rspont = monitoringView.get().findViewById(R.id.monit_Rspont);
        MonitoringTextView ie = monitoringView.get().findViewById(R.id.monit_ie);
        MonitoringTextView ti = monitoringView.get().findViewById(R.id.monit_ti);
        MonitoringTextView te = monitoringView.get().findViewById(R.id.monit_te);
        MonitoringTextView phase = monitoringView.get().findViewById(R.id.monit_phase);
        MonitoringTextView leakVol = monitoringView.get().findViewById(R.id.monit_leakVol);
        MonitoringTextView leakPercent = monitoringView.get().findViewById(R.id.monit_leakPercent);
        MonitoringTextView cStat = monitoringView.get().findViewById(R.id.monit_cStat);
        MonitoringTextView flowPeak = monitoringView.get().findViewById(R.id.monit_flowPeak);
        mvTotal.setSubText("MV<sub><small>total</small></sub>");
        mvSpont.setSubText("MV<sub><small>spont</small></sub>");
        Rspont.setSubText("R<sub><small>spont</small></sub>");
        ti.setSubText("T<sub><small>insp</small></sub>");
        te.setSubText("T<sub><small>exp</small></sub>");
        cStat.setSubText("C<sub><small>stat</small></sub>");
        cStat.setUnit(Html.fromHtml("ml/cm H<sub><small>2</small></sub>O"));
        flowPeak.setSubText("Flow<sub><small>peak</small></sub>");
        hpaObserver = new Observer<String>() {
            @Override
            public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {
            }

            @Override
            public void onNext(@io.reactivex.rxjava3.annotations.NonNull String s) {
                if (s.equals("hpa")) cStat.setUnit("ml/hPa");
                else cStat.setUnit(Html.fromHtml("ml/cm H<sub><small>2</small></sub>O"));
            }

            @Override
            public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                e.printStackTrace();
            }

            @Override
            public void onComplete() {
            }
        };

        observer = new Observer<Long>() {
            @Override
            public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {
                disposable = d;
            }

            @Override
            public void onNext(@io.reactivex.rxjava3.annotations.NonNull Long aLong) {
                mvSpont.setValue(StaticStore.Monitoring.mvSpont, 1);
                phase.setValue(StaticStore.Monitoring.phase);
                ie.setValue(getIE(StaticStore.Monitoring.ie));
                leakVol.setValue(StaticStore.Monitoring.leakVol, 0);
                leakPercent.setValue(StaticStore.Monitoring.leakPercent, 0);
                cStat.setValue(StaticStore.Monitoring.cStat, 1);
                ti.setValue(StaticStore.Monitoring.ti, 2);
                te.setValue(StaticStore.Monitoring.te, 2);
                flowPeak.setValue(StaticStore.Monitoring.flowPeak, 1);
                mvTotal.setValue(StaticStore.Monitoring.mvTotal, 1);
                Rspont.setValue(StaticStore.Values.rSpont, 0);
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

    @Override
    public void onDestroy() {
        if (disposable != null)
            if (!disposable.isDisposed())
                disposable.dispose();
        super.onDestroy();
    }
}