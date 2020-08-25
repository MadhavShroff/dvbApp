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
        t.put("pinsp", String.valueOf(((int) StaticStore.Values.pInsp * 100) / 100f));
        t.put("set-pinsp", String.valueOf(((int) StaticStore.packet_pinsp * 100) / 100));
        t.put("peep", String.valueOf(((int) StaticStore.Values.peep * 100) / 100));
        t.put("set-peep", String.valueOf(((int) StaticStore.packet_peep * 100) / 100));
        t.put("ppeak", String.valueOf(((int) StaticStore.Values.pPeak * 100) / 100));
        t.put("pmean", String.valueOf(((int) StaticStore.Values.pMean * 100) / 100));
        t.put("vt", String.valueOf(((int) StaticStore.Values.graphVolume * 100) / 100));
        t.put("set-vt", String.valueOf(((int) StaticStore.packet_vt * 100) / 100));
        t.put("rtotal", String.valueOf(((int) StaticStore.Values.rateMeasured * 100) / 100));
        t.put("set-rtotal", String.valueOf(((int) StaticStore.packet_rtotal * 100) / 100));
        t.put("rspont", String.valueOf(((int) StaticStore.Values.rSpont * 100) / 100));
        t.put("mvTotal", String.valueOf(((int) StaticStore.Monitoring.mvTotal * 100) / 100));
        t.put("mvSpont", String.valueOf(((int) StaticStore.Monitoring.mvSpont * 100) / 100));
        t.put("fio2", String.valueOf(StaticStore.Values.fio2));
        t.put("set-fio2", String.valueOf(StaticStore.packet_fio2));
        t.put("ie", getIE(StaticStore.Monitoring.ie));
        t.put("set-ie", getIE(StaticStore.packet_ie));
        t.put("cStat", String.valueOf(((int) StaticStore.Monitoring.cStat * 100) / 100));
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
