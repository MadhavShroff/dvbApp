package com.dvbinventek.dvbapp.bottomSheets;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Handler;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.arthurivanets.bottomsheets.BaseBottomSheet;
import com.arthurivanets.bottomsheets.config.BaseConfig;
import com.arthurivanets.bottomsheets.config.Config;
import com.dvbinventek.dvbapp.R;
import com.dvbinventek.dvbapp.StaticStore;
import com.dvbinventek.dvbapp.customViews.CustomKeyboardView;

import java.lang.ref.WeakReference;

public class ControlsBottomSheet extends BaseBottomSheet {

    public boolean canceled = true;
    ImageButton done, cancel;
    EditText value;
    TextView tv;
    WeakReference<Activity> activity;
    String type;
    OnClickListener positiveListener = v -> {
        canceled = false;
        dismiss(true);
        setHint();
        canceled = true;
        v.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FCDD03")));
        new Handler().postDelayed(() -> v.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#ffffff"))), 100);
    };
    OnClickListener negativeListener = v -> {
        Log.d("MSG", "Negative Listener");
    };

    public ControlsBottomSheet(@NonNull Activity hostActivity, @NonNull BaseConfig config) {
        super(hostActivity, config);
        this.activity = new WeakReference<>(hostActivity);
    }

    public ControlsBottomSheet(@NonNull Activity hostActivity, Spanned s, String type) {
        this(hostActivity, new Config.Builder(hostActivity).build());
        this.type = type;
        done = findViewById(R.id.cbs_done);
        value = findViewById(R.id.cbs_editText);
        cancel = findViewById(R.id.cbs_cancel);
        tv = findViewById(R.id.cbs_heading);
        tv.setText(s);
        done.setImageTintList(ColorStateList.valueOf(Color.parseColor("#afafaf")));
        done.setOnClickListener(positiveListener);
        cancel.setOnClickListener(v -> {
            canceled = true;
            v.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FCDD03")));
            new Handler().postDelayed(() -> v.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#ffffff"))), 100);
            value.setText("");
            dismiss(true);
        });
        onNumberChanged();
        CustomKeyboardView keyboard = findViewById(R.id.cbs_keyboard);
        keyboard.setKeyboardClickListener(digit -> {
            if (!digit.equals("backspace"))
                value.append(digit);
            else
                backspace();
            onNumberChanged();
        });
    }

    public void clearValue() {
        value.setText("");
        onNumberChanged();
    }

    public void setSubText(String s) {
        tv = findViewById(R.id.subText);
        tv.setText(s);
    }

    public void setSubText(Spanned s) {
        tv = findViewById(R.id.subText);
        tv.setText(s);
    }

    public void backspace() {
        String str = value.getText().toString();
        if (str.length() <= 0) return;
        str = str.substring(0, str.length() - 1);
        value.setText(str);
    }

    private void onNumberChanged() {
        if (value.getText().length() != 0 && isInRange()) {
            done.setOnClickListener(positiveListener);
            done.setEnabled(true);
            done.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.blue)));
        } else {
            done.setOnClickListener(negativeListener);
            done.setEnabled(false);
            done.setImageTintList(ColorStateList.valueOf(Color.parseColor("#afafaf")));
        }
    }

    public boolean isInRange() {
        if (value.getText().toString().equals(".")) return false;
        float f = Float.parseFloat(value.getText().toString());
        switch (this.type) {
            case "fio2":
                return !(f > StaticStore.DeviceParameterLimits.max_fio2) && !(f < StaticStore.DeviceParameterLimits.min_fio2) && (((f * 10) % 10) == 0);
            case "tinsp":
                return !(f > StaticStore.DeviceParameterLimits.max_tinsp) && !(f < StaticStore.DeviceParameterLimits.min_tinsp);
            case "cpap":
                return !(f > StaticStore.DeviceParameterLimits.max_cpap) && !(f < StaticStore.DeviceParameterLimits.min_cpap) && (f <= StaticStore.new_packet_pinsp);
            case "pmax":
                return !((f < StaticStore.new_packet_peep + 1) || (f > StaticStore.DeviceParameterLimits.max_plimit));
            case "delps":
                return !(f > StaticStore.DeviceParameterLimits.max_delps) && !(f < StaticStore.DeviceParameterLimits.min_delps);
            case "pip":
                return !(f > StaticStore.DeviceParameterLimits.max_pip) && !(f < StaticStore.DeviceParameterLimits.min_pip) && (f >= StaticStore.new_packet_peep);
            case "ratef":
                return !(f > StaticStore.DeviceParameterLimits.max_ratef) && !(f < StaticStore.DeviceParameterLimits.min_ratef);
            case "vt":
                return !(f > StaticStore.DeviceParameterLimits.max_vt) && !(f < StaticStore.DeviceParameterLimits.min_vt) && !(((f * 10) % 10) > 0);
            case "vtrig":
                return !(f > StaticStore.DeviceParameterLimits.max_vtrig) && !(f < StaticStore.DeviceParameterLimits.min_vtrig);
        }
        return false;
    }

    public void setHint() {
        if (!value.getText().toString().isEmpty()) value.setHint(value.getText());
        else value.setText("");
    }

    @Override
    public boolean requestFocus(int direction, Rect previouslyFocusedRect) {
        value.requestFocus();
        return true;
    }

    @Override
    protected boolean onRequestFocusInDescendants(int direction, Rect previouslyFocusedRect) {
        return super.onRequestFocusInDescendants(direction, previouslyFocusedRect);
    }

    public String getValue() {
        return String.valueOf(value.getText());
    }

    @NonNull
    @Override
    protected View onCreateSheetContentView(@NonNull Context context) {
        return LayoutInflater.from(context).inflate(
                R.layout.bottomsheet_controls,
                this,
                false
        );
    }
}
