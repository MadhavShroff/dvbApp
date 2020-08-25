package com.dvbinventek.dvbapp;

import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import java.lang.ref.WeakReference;

public class UsbHandler extends Handler {
    public final WeakReference<MainActivity> mActivity;

    public UsbHandler(MainActivity activity) {
        mActivity = new WeakReference<>(activity);
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case UsbService.MESSAGE_FROM_SERIAL_PORT:
//                    packetSubject.onNext((byte[]) msg.obj);
                MainActivity.handleData((byte[]) msg.obj);
                break;
            case UsbService.CTS_CHANGE:
                Toast.makeText(mActivity.get(), "CTS_CHANGE", Toast.LENGTH_LONG).show();
                break;
            case UsbService.DSR_CHANGE:
                Toast.makeText(mActivity.get(), "DSR_CHANGE", Toast.LENGTH_LONG).show();
                break;
        }
    }
}