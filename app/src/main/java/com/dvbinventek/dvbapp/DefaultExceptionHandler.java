package com.dvbinventek.dvbapp;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

public class DefaultExceptionHandler implements Thread.UncaughtExceptionHandler {

    Activity activity;

    public DefaultExceptionHandler(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        Log.d("MSG", "Default Exception Handler Invoked");
        try {
            ex.printStackTrace();
            ex.getCause().printStackTrace();

            Intent intent = new Intent(activity, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK
                    | Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.finish();
            activity.startActivity(intent);
            System.exit(2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}