package com.dvbinventek.dvbapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;

public class HistoricalDataRecyclerViewAdapter extends RecyclerView.Adapter<HistoricalDataRecyclerViewAdapter.ViewHolder> {

    public static List<HashMap<String, String>> localDataSet = null;

    public HistoricalDataRecyclerViewAdapter(List<HashMap<String, String>> dataSet) {
        localDataSet = dataSet;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.row_view, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        viewHolder.setValues(localDataSet.get(position));
    }

    @Override
    public int getItemCount() {
        return localDataSet.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        WeakReference<View> thisView;

        public ViewHolder(View view) {
            super(view);
            thisView = new WeakReference<>(view);
        }

        public void setValues(HashMap<String, String> i) {
            ((TextView) thisView.get().findViewById(R.id.log_date)).setText(i.get("date"));
            ((TextView) thisView.get().findViewById(R.id.log_pinsp)).setText(thisView.get().getResources().getString(R.string.log_placeholder, i.get("pinsp"), i.get("set-pinsp")));
            ((TextView) thisView.get().findViewById(R.id.log_ppeak)).setText(i.get("ppeak"));
            ((TextView) thisView.get().findViewById(R.id.log_peep)).setText(thisView.get().getResources().getString(R.string.log_placeholder, i.get("peep"), i.get("set-peep")));
            ((TextView) thisView.get().findViewById(R.id.log_mvtotal)).setText(i.get("mvTotal"));
            ((TextView) thisView.get().findViewById(R.id.log_vt)).setText(thisView.get().getResources().getString(R.string.log_placeholder, i.get("vt"), i.get("set-vt")));
            ((TextView) thisView.get().findViewById(R.id.log_rate)).setText(thisView.get().getResources().getString(R.string.log_placeholder, i.get("rtotal"), i.get("set-rtotal")));
            ((TextView) thisView.get().findViewById(R.id.log_fio2)).setText(thisView.get().getResources().getString(R.string.log_placeholder, i.get("fio2"), i.get("set-fio2")));
            ((TextView) thisView.get().findViewById(R.id.log_pmean)).setText(i.get("pmean"));
            ((TextView) thisView.get().findViewById(R.id.log_mvspont)).setText(i.get("mvSpont"));
            ((TextView) thisView.get().findViewById(R.id.log_ieMeasured)).setText(thisView.get().getResources().getString(R.string.log_placeholder, i.get("ie"), i.get("set-ie")));
            ((TextView) thisView.get().findViewById(R.id.log_cstat)).setText(i.get("cStat"));
            ((TextView) thisView.get().findViewById(R.id.log_alarms)).setText(i.get("warning"));
        }
    }

}

