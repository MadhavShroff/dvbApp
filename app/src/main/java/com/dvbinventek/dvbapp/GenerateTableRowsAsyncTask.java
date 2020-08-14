package com.dvbinventek.dvbapp;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GenerateTableRowsAsyncTask extends AsyncTask<Void, View, List<View>> {

    WeakReference<Context> context;
    WeakReference<TableLayout> tl;

    GenerateTableRowsAsyncTask(Context context, TableLayout tl) {
        this.context = new WeakReference<>(context);
        this.tl = new WeakReference<>(tl);
    }

    @Override
    protected List<View> doInBackground(Void... voids) {
        List<View> rows = new ArrayList<>();
        TextView tv;
        Log.d("MSG", "Generating Rows... " + StaticStore.Data.size());
        synchronized (StaticStore.Data) {
            for (HashMap<String, String> i : StaticStore.Data) {
                View v = View.inflate(context.get(), R.layout.row_view, null);
                tv = v.findViewById(R.id.log_date);
                tv.setText(i.get("date"));
                tv = v.findViewById(R.id.log_pinsp);
                tv.setText(tv.getResources().getString(R.string.log_placeholder, i.get("pinsp"), i.get("set-pinsp")));
                tv = v.findViewById(R.id.log_ppeak);
                tv.setText(i.get("ppeak"));
                tv = v.findViewById(R.id.log_peep);
                tv.setText(tv.getResources().getString(R.string.log_placeholder, i.get("peep"), i.get("set-peep")));
                tv = v.findViewById(R.id.log_mvtotal);
                tv.setText(i.get("mvTotal"));
                tv = v.findViewById(R.id.log_vt);
                tv.setText(tv.getResources().getString(R.string.log_placeholder, i.get("vt"), i.get("set-vt")));
                tv = v.findViewById(R.id.log_rate);
                tv.setText(tv.getResources().getString(R.string.log_placeholder, i.get("rtotal"), i.get("set-rtotal")));
                tv = v.findViewById(R.id.log_fio2);
                tv.setText(tv.getResources().getString(R.string.log_placeholder, i.get("fio2"), i.get("set-fio2")));
                tv = v.findViewById(R.id.log_pmean);
                tv.setText(i.get("pmean"));
                tv = v.findViewById(R.id.log_mvspont);
                tv.setText(i.get("mvSpont"));
                tv = v.findViewById(R.id.log_ieMeasured);
                tv.setText(tv.getResources().getString(R.string.log_placeholder, i.get("ie"), i.get("set-ie")));
                tv = v.findViewById(R.id.log_cstat);
                tv.setText(i.get("cStat"));
                tv = v.findViewById(R.id.log_alarms);
                tv.setText(i.get("warning"));
                publishProgress(v);
            }
        }
        return rows;
    }

    @Override
    protected void onProgressUpdate(View... values) {
        this.tl.get().addView(values[0]);
    }
}
