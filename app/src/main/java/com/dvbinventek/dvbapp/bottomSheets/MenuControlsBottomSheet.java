package com.dvbinventek.dvbapp.bottomSheets;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;

import com.arthurivanets.bottomsheets.BaseBottomSheet;
import com.arthurivanets.bottomsheets.config.BaseConfig;
import com.arthurivanets.bottomsheets.config.Config;
import com.dvbinventek.dvbapp.R;
import com.dvbinventek.dvbapp.StaticStore;

public class MenuControlsBottomSheet extends BaseBottomSheet {

    public final Button vccmv = findViewById(R.id.vccmv);
    public final Button vcsimv = findViewById(R.id.vcsimv);
    public final Button pccmv = findViewById(R.id.pccmv);
    public final Button pcsimv = findViewById(R.id.pcsimv);
    public final Button prvc = findViewById(R.id.prvc);
    public final Button psv = findViewById(R.id.psv);
    public final Button acv = findViewById(R.id.acv);
    public final Button cpap = findViewById(R.id.cpap);
    public final Button bpap = findViewById(R.id.bpap);
    public final Button hfo2 = findViewById(R.id.hfo2);
    View b;

    public MenuControlsBottomSheet(@NonNull Activity hostActivity) {
        this(hostActivity, new Config.Builder(hostActivity).build());
        vccmv.setOnClickListener(new ControlsChangeOnClickListener());
        pccmv.setOnClickListener(new ControlsChangeOnClickListener());
        vcsimv.setOnClickListener(new ControlsChangeOnClickListener());
        pccmv.setOnClickListener(new ControlsChangeOnClickListener());
        pcsimv.setOnClickListener(new ControlsChangeOnClickListener());
        prvc.setOnClickListener(new ControlsChangeOnClickListener());
        psv.setOnClickListener(new ControlsChangeOnClickListener());
        acv.setOnClickListener(new ControlsChangeOnClickListener());
        cpap.setOnClickListener(new ControlsChangeOnClickListener());
        bpap.setOnClickListener(new ControlsChangeOnClickListener());
        hfo2.setOnClickListener(new ControlsChangeOnClickListener());
    }

    public MenuControlsBottomSheet(@NonNull Activity hostActivity, @NonNull BaseConfig config) {
        super(hostActivity, config);
    }

    public void clickCorrectModeButton(short modeShort) {
        Log.d("ModeClick", "Clicked mode button " + modeShort);
        switch (modeShort) {
            case 17:
                vccmv.performClick();
                break;
            case 13:
                pccmv.performClick();
                break;
            case 18:
                vcsimv.performClick();
                break;
            case 15:
                psv.performClick();
                break;
            case 14:
                pcsimv.performClick();
                break;
            case 19:
                prvc.performClick();
                break;
            case 21:
                acv.performClick();
                break;
            case 20:
                cpap.performClick();
                break;
            case 16:
                bpap.performClick();
                break;
            case 22:
                hfo2.performClick();
                break;
            default:
                b.setAlpha(0.3f);
        }
    }

    @NonNull
    @Override
    public final View onCreateSheetContentView(@NonNull Context context) {
        return LayoutInflater.from(context).inflate(
                R.layout.dialog_view_mode,
                this,
                false
        );
    }

    class ControlsChangeOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (b == null) b = v;
            else {
                b.setAlpha(0.3f);
            }
            if (v.getAlpha() == 0.3f) {
                v.setAlpha(1f);
                b = v;
            }
            switch (v.getId()) {
                case R.id.vccmv:
                    StaticStore.modeSelected = "VC-CMV";
                    StaticStore.modeSelectedShort = 17;
                    break;
                case R.id.pccmv:
                    StaticStore.modeSelected = "PC-CMV";
                    StaticStore.modeSelectedShort = 13;
                    break;
                case R.id.vcsimv:
                    StaticStore.modeSelected = "VC-SIMV";
                    StaticStore.modeSelectedShort = 18;
                    break;
                case R.id.psv:
                    StaticStore.modeSelected = "PSV";
                    StaticStore.modeSelectedShort = 15;
                    break;
                case R.id.pcsimv:
                    StaticStore.modeSelected = "PC-SIMV";
                    StaticStore.modeSelectedShort = 14;
                    break;
                case R.id.prvc:
                    StaticStore.modeSelected = "PRVC";
                    StaticStore.modeSelectedShort = 19;
                    break;
                case R.id.acv:
                    StaticStore.modeSelected = "ACV";
                    StaticStore.modeSelectedShort = 21;
                    break;
                case R.id.cpap:
                    StaticStore.modeSelected = "CPAP";
                    StaticStore.modeSelectedShort = 20;
                    break;
                case R.id.bpap:
                    StaticStore.modeSelected = "BPAP";
                    StaticStore.modeSelectedShort = 16;
                    break;
                case R.id.hfo2:
                    StaticStore.modeSelected = "HFO2";
                    StaticStore.modeSelectedShort = 22;
                    break;
            }
            dismiss(true);
            Log.d("ModeSelected", StaticStore.modeSelected + " " + StaticStore.modeSelectedShort);
            // new mode received
        }
    }
}