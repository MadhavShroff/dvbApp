package com.dvbinventek.dvbapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class UsbDisconnectedFragment extends Fragment {

    public static boolean isInView = false;
    public CompositeDisposable disposables = new CompositeDisposable();

    public static void setIsInView(boolean b) {
        isInView = b;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_usb_disconnected, container, false);
    }

    @Override
    public void onDestroy() {
        if (disposables != null)
            if (!disposables.isDisposed())
                disposables.dispose();
        super.onDestroy();
    }
}