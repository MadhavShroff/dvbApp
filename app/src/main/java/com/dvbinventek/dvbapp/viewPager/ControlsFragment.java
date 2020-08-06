package com.dvbinventek.dvbapp.viewPager;

import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.dvbinventek.dvbapp.R;
import com.dvbinventek.dvbapp.StaticStore;
import com.dvbinventek.dvbapp.bottomSheets.MenuControlsBottomSheet;
import com.dvbinventek.dvbapp.customViews.ControlRow;

import java.lang.ref.WeakReference;
import java.util.Objects;

public class ControlsFragment extends Fragment {

    public MenuControlsBottomSheet modeBottomSheet;
    WeakReference<View> controlsView;
    WeakReference<ControlRow> fio2, vt, pinsp, plimit, peep, ps, vtrig, tinsp, ie, rtotal;
    int countdown = 30;

    ControlsFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.controls_fragment, container, false);
        controlsView = new WeakReference<>(view);
        fio2 = new WeakReference<>(view.findViewById(R.id.controls_fio2));
        vt = new WeakReference<>(view.findViewById(R.id.controls_vt));
        pinsp = new WeakReference<>(view.findViewById(R.id.controls_pinsp));
        plimit = new WeakReference<>(view.findViewById(R.id.controls_plimit));
        peep = new WeakReference<>(view.findViewById(R.id.controls_peep));
        ps = new WeakReference<>(view.findViewById(R.id.controls_ps));
        vtrig = new WeakReference<>(view.findViewById(R.id.controls_vtrig));
        tinsp = new WeakReference<>(view.findViewById(R.id.controls_tinsp));
        ie = new WeakReference<>(view.findViewById(R.id.controls_ie));
        rtotal = new WeakReference<>(view.findViewById(R.id.controls_rtotal));

        //Set subscript text for units and control labels
        plimit.get().setUnit(Html.fromHtml("cm H<small><sub>2</sub></small>O"));
        ps.get().setUnit(Html.fromHtml("cm H<small><sub>2</sub></small>O"));
        pinsp.get().setUnit(Html.fromHtml("cm H<small><sub>2</sub></small>O"));
        peep.get().setUnit(Html.fromHtml("cm H<small><sub>2</sub></small>O"));
        fio2.get().setLabel(Html.fromHtml("FiO<small><sub>2</sub></small>"));
        vtrig.get().setLabel(Html.fromHtml("Flow<small><sub>trig</sub></small>"));
        tinsp.get().setLabel(Html.fromHtml("T<small><sub>insp</sub></small>"));
        pinsp.get().setLabel(Html.fromHtml("P<small><sub>insp</sub></small>"));

        ImageButton mode = view.findViewById(R.id.controls_mode_change);
        modeBottomSheet = new MenuControlsBottomSheet(Objects.requireNonNull(getActivity()));
        mode.setOnClickListener(v -> {
            modeBottomSheet.show(true);
            modeBottomSheet.setOnDismissListener(bottomSheet1 -> {
//                if(StaticStore.modeSelectedShort != new_mode) {
//                    highlight(R.id.controls_mode, true);
//                    resetList.add("mode");
//                    setText(R.id.controls_mode_current, StaticStore.modeSelected);
//                } else {
//                    revertRowColor(R.id.controls_mode);
//                    if(s!= null && !s.isUnsubscribed()) s.unsubscribe();
//                    warningText.setText("");
//                    StaticStore.modeSelected = this.new_modeString;
//                    StaticStore.modeSelectedShort = this.new_mode;
//                    setText(R.id.controls_mode_current, StaticStore.modeSelected);
//                    countdownStarted = false;i
//                }
                setControlsForMode();
            });
        });
        return view;
    }

    public void setText(int id, String s) {
        TextView tv = controlsView.get().findViewById(id);
        tv.setText(s);
    }

    void setControlsForMode() {
        switch (StaticStore.modeSelected) {
            case "VC-CMV":
                setAllVisible();
                visibility(R.id.controls_pinsp, View.GONE);
                visibility(R.id.controls_vtrig, View.GONE);
                visibility(R.id.controls_ps, View.GONE);
                visibility(R.id.controls_plimit, View.GONE);
                break;
            case "PC-CMV":
                setAllVisible();
                visibility(R.id.controls_vt, View.GONE);
                visibility(R.id.controls_vtrig, View.GONE);
                visibility(R.id.controls_ps, View.GONE);
                visibility(R.id.controls_plimit, View.GONE);
                break;
            case "VC-SIMV":
                setAllVisible();
                visibility(R.id.controls_pinsp, View.GONE);
                visibility(R.id.controls_plimit, View.GONE);
                break;
            case "PRVC":
                setAllVisible();
                visibility(R.id.controls_pinsp, View.GONE);
                break;
            case "ACV":
                setAllVisible();
                visibility(R.id.controls_ps, View.GONE);
                visibility(R.id.controls_pinsp, View.GONE);
                visibility(R.id.controls_plimit, View.GONE);
                break;
            case "PSV":
                setAllVisible();
                visibility(R.id.controls_ps, View.GONE);
                visibility(R.id.controls_vt, View.GONE);
                visibility(R.id.controls_tinsp, View.GONE);
                visibility(R.id.controls_rtotal, View.GONE);
                visibility(R.id.controls_ie, View.GONE);
                visibility(R.id.controls_plimit, View.GONE);
                break;
            case "PC-SIMV":
                setAllVisible();
                visibility(R.id.controls_vt, View.GONE);
                visibility(R.id.controls_plimit, View.GONE);
                break;
            case "CPAP":
                setAllVisible();
                visibility(R.id.controls_pinsp, View.GONE);
                visibility(R.id.controls_ps, View.GONE);
                visibility(R.id.controls_vt, View.GONE);
                visibility(R.id.controls_vtrig, View.GONE);
                visibility(R.id.controls_rtotal, View.GONE);
                visibility(R.id.controls_tinsp, View.GONE);
                visibility(R.id.controls_ie, View.GONE);
                visibility(R.id.controls_plimit, View.GONE);
                break;
            case "BPAP":
                setAllVisible();
                visibility(R.id.controls_ps, View.GONE);
                visibility(R.id.controls_tinsp, View.GONE);
                visibility(R.id.controls_vt, View.GONE);
                visibility(R.id.controls_rtotal, View.GONE);
                visibility(R.id.controls_ie, View.GONE);
                visibility(R.id.controls_plimit, View.GONE);
                break;
        }
    }

    public void visibility(int id, int visibility) {
        try {
            View v = controlsView.get().findViewById(id);
            v.setVisibility(visibility);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setAllVisible() {
        visibility(R.id.controls_fio2, View.VISIBLE);
        visibility(R.id.controls_vt, View.VISIBLE);
        visibility(R.id.controls_vtrig, View.VISIBLE);
        visibility(R.id.controls_pinsp, View.VISIBLE);
        visibility(R.id.controls_peep, View.VISIBLE);
        visibility(R.id.controls_ps, View.VISIBLE);
        visibility(R.id.controls_tinsp, View.VISIBLE);
        visibility(R.id.controls_rtotal, View.VISIBLE);
        visibility(R.id.controls_ie, View.VISIBLE);
        visibility(R.id.controls_plimit, View.VISIBLE);
    }
}
