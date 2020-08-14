package com.dvbinventek.dvbapp;

import android.app.Activity;
import android.os.AsyncTask;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class DataStoreAsyncTask extends AsyncTask<Void, Void, Void> {

    public static final String dashes = "--";
    static int callNumber = 0;
    WeakReference<Activity> activity;
    boolean isWarningString = false;
    String warning;

    public DataStoreAsyncTask(Activity mainActivity) {
        activity = new WeakReference<>(mainActivity);
        callNumber++;
        warning = "";
        if (StaticStore.Warnings.currentWarnings.size() > 0) {
            isWarningString = true;
            boolean x = true;
            synchronized (StaticStore.Warnings.currentWarnings) {
                for (String s : StaticStore.Warnings.currentWarnings) {
                    if (x) {
                        warning += s;
                        x = false;
                    } else warning += ", " + s;
                }
            }
        } else {
            isWarningString = false;
            warning = dashes;
        }
    }

    @Override
    protected Void doInBackground(Void... voids) {
        if (callNumber <= 20) { // 3s call
            if (!isWarningString)
                return null;            //return if there is no warning, called every 3 seconds
        } else callNumber = 0;
        if (StaticStore.Data.size() > 3000) {
            StaticStore.Data.remove(0);
        }
        HashMap<String, String> t = new HashMap<>();
        t.put("date", new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()));
        t.put("pinsp", String.valueOf(StaticStore.Values.pMax));
        t.put("set-pinsp", String.valueOf(StaticStore.packet_pinsp));
        t.put("peep", String.valueOf(StaticStore.Values.pMin));
        t.put("set-peep", String.valueOf(StaticStore.packet_peep));
        t.put("ppeak", String.valueOf(StaticStore.Values.pp));
        t.put("pmean", String.valueOf(StaticStore.Monitoring.pMean));
        t.put("vt", String.valueOf(StaticStore.Values.vTidalFlow));
        t.put("set-vt", String.valueOf(StaticStore.packet_vt));
        t.put("rtotal", String.valueOf(StaticStore.Values.bpmMeasured));
        t.put("set-rtotal", String.valueOf(StaticStore.packet_rtotal));
        t.put("rspont", String.valueOf(StaticStore.Values.rSpont));
        t.put("mvTotal", String.valueOf(StaticStore.Values.expMinVolMeasured));
        t.put("mvSpont", String.valueOf(StaticStore.Monitoring.mvSpont));
        t.put("fio2", String.valueOf(StaticStore.Values.fio2));
        t.put("set-fio2", String.valueOf(StaticStore.packet_fio2));
        t.put("ie", getIE(StaticStore.Monitoring.ie));
        t.put("set-ie", getIE(StaticStore.packet_ie));
        t.put("cStat", String.valueOf(StaticStore.Monitoring.cStat));
        t.put("warning", warning);
        StaticStore.Data.add(t);
        return null;
    }

    String getIE(int ie) {
        String s = "";
        int i = ie / 1000;
        int id = (ie / 100) % 10;
        int e = (ie / 10) % 10;
        int ed = ie % 10;
        if (id == 0 && ed == 0) {
            s = i + ":" + e;
        } else if (id == 0 && ed != 0) {
            s = i + ":" + e + "." + ed;
        } else if (id != 0 && ed == 0) {
            s = i + "." + id + ":" + e;
        } else {
            s = i + "." + id + ":" + e + "." + ed;
        }
        return s;
    }
}
