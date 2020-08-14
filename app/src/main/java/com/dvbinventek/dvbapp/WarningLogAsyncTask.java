package com.dvbinventek.dvbapp;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;

public class WarningLogAsyncTask extends AsyncTask<Void, Void, Void> {

    public static final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    public WeakReference<View> view;
    public String writeToEvents;

    public WarningLogAsyncTask(View view) {
        this.view = new WeakReference<>(view);
        writeToEvents = "";
    }

    @Override
    protected Void doInBackground(Void... voids) {
        if (StaticStore.Warnings.currentWarnings.size() == 0) return null;
        this.writeToEvents = sdf.format(new Date()) + ": ";
        boolean x = true;
        synchronized (StaticStore.Warnings.currentWarnings) {
            for (String s : StaticStore.Warnings.currentWarnings) {
                if (x) {
                    this.writeToEvents += s;
                    x = false;
                } else this.writeToEvents += ", " + s;
            }
        }
        synchronized (StaticStore.Warnings.allWarnings) {
            StaticStore.Warnings.allWarnings.add(this.writeToEvents);
        }
        this.writeToEvents += "\n\n";
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (StaticStore.Warnings.currentWarnings.size() == 0 || this.writeToEvents == null) return;
        TextView et = view.get().findViewById(R.id.limitsText);
        try {
            et.append(this.writeToEvents);
        } catch (NullPointerException e) {
            Log.d("MSG", "Crashed due to NPE thrown at WarningLogAsyncTask. et is null");
            e.printStackTrace();
        }
    }
}
