package com.dvbinventek.dvbapp.viewPager;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.dvbinventek.dvbapp.LogDisplay;
import com.dvbinventek.dvbapp.R;
import com.dvbinventek.dvbapp.WarningLogAsyncTask;

import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class EventsFragment extends Fragment {

    public static Observable<? extends Long> observable = Observable.interval(1000, 2700, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread());
    public static Observer<Long> observer;
    public static Disposable disposable;
    WeakReference<View> eventsView;

    public static void startObserving() {
        observable.subscribe(observer);
    }

    public static void stopObserving() {
        if (disposable != null)
            if (!disposable.isDisposed())
                disposable.dispose();
    }

    @Override
    public void onDestroy() {
        if (disposable != null && !disposable.isDisposed())
            disposable.dispose();
        super.onDestroy();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_events, container, false);
        eventsView = new WeakReference<>(view);
        eventsView.get().findViewById(R.id.logs_display_button).setOnClickListener(v -> Objects.requireNonNull(getContext()).startActivity(new Intent(getContext(), LogDisplay.class)));
        observer = new Observer<Long>() {
            @Override
            public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {
                disposable = d;
            }

            @Override
            public void onNext(Long o) {
                new WarningLogAsyncTask(eventsView.get()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }

            @Override
            public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
            }

            @Override
            public void onComplete() {
            }
        };
        startObserving();
        return view;
    }

}