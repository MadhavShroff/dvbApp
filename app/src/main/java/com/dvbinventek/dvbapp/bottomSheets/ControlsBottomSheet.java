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

public class ControlsBottomSheet extends BaseBottomSheet {

    public boolean canceled = true;
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
    }

    public ControlsBottomSheet(@NonNull Activity hostActivity, Spanned s, String type) {
        this(hostActivity, new Config.Builder(hostActivity).build());
        this.type = type;
        ImageButton done = findViewById(R.id.cbs_done);
        EditText value = findViewById(R.id.cbs_editText);
        ImageButton cancel = findViewById(R.id.cbs_cancel);
        TextView tv = findViewById(R.id.cbs_heading);
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
        setText(R.id.cbs_editText, "");
        onNumberChanged();
    }

    public void setSubText(String s) {
        TextView tv = findViewById(R.id.subText);
        tv.setText(s);
    }

    public void setSubText(Spanned s) {
        TextView tv = findViewById(R.id.subText);
        tv.setText(s);
    }

    public void setText(int id, String s) {
        TextView tv = findViewById(id);
        tv.setText(s);
    }

    public void setHeading(String s) {
        TextView tv = findViewById(R.id.cbs_heading);
        tv.setText(s);
    }

    public void setHeading(Spanned s) {
        TextView tv = findViewById(R.id.cbs_heading);
        tv.setText(s);
    }

    public void backspace() {
        String str = ((EditText) findViewById(R.id.cbs_editText)).getText().toString();
        if (str.length() <= 0) return;
        str = str.substring(0, str.length() - 1);
        ((EditText) findViewById(R.id.cbs_editText)).setText(str);
    }

    private void onNumberChanged() {
        ImageButton done = findViewById(R.id.cbs_done);
        if (((EditText) findViewById(R.id.cbs_editText)).getText().length() != 0 && isInRange()) {
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
        if (((EditText) findViewById(R.id.cbs_editText)).getText().toString().equals("."))
            return false;
        float f = Float.parseFloat(((EditText) findViewById(R.id.cbs_editText)).getText().toString());
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
        if (!((EditText) findViewById(R.id.cbs_editText)).getText().toString().isEmpty())
            ((EditText) findViewById(R.id.cbs_editText)).setHint(((EditText) findViewById(R.id.cbs_editText)).getText());
        else ((EditText) findViewById(R.id.cbs_editText)).setText("");
    }

    @Override
    public boolean requestFocus(int direction, Rect previouslyFocusedRect) {
        findViewById(R.id.cbs_editText).requestFocus();
        return true;
    }

    @Override
    protected boolean onRequestFocusInDescendants(int direction, Rect previouslyFocusedRect) {
        return super.onRequestFocusInDescendants(direction, previouslyFocusedRect);
    }

    public String getValue() {
        return String.valueOf(((EditText) findViewById(R.id.cbs_editText)).getText());
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
