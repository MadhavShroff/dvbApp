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
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.arthurivanets.bottomsheets.BaseBottomSheet;
import com.arthurivanets.bottomsheets.config.BaseConfig;
import com.arthurivanets.bottomsheets.config.Config;
import com.dvbinventek.dvbapp.R;
import com.dvbinventek.dvbapp.customViews.CustomKeyboardView;

public class AlarmLimitsBottomSheetApnea extends BaseBottomSheet {

    public final TextView heading = findViewById(R.id.albsap_heading);
    public final ImageButton done = findViewById(R.id.albsap_done);
    public final ImageButton cancel = findViewById(R.id.albsap_cancel);
    public final EditText value = findViewById(R.id.albsap_value);
    public final TextView subheading = findViewById(R.id.albsap_subheading);
    public boolean isDone = false;
    OnClickListener positiveListener = v -> {
        isDone = true;
        dismiss(true);
        setHint();
        isDone = false;
        v.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FCDD03")));
        new Handler().postDelayed(() -> v.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#ffffff"))), 100);
    };
    OnClickListener negativeListener = v -> {
        Log.d("MSG", "Negative Listener");
    };

    public AlarmLimitsBottomSheetApnea(@NonNull Activity hostActivity, @NonNull BaseConfig config) {
        super(hostActivity, config);
    }

    public AlarmLimitsBottomSheetApnea(@NonNull Activity hostActivity, Spanned s, String limit) {
        this(hostActivity, new Config.Builder(hostActivity).build());
        subheading.setText(s);
        ((TextView) findViewById(R.id.albsap_subText)).setText(limit);
        heading.setText(R.string.setAlarmLimits);
        cancel.setOnClickListener(v -> {
            isDone = false;
            value.setText("");
            dismiss(true);
        });
        done.setOnClickListener(v -> {
            isDone = true;
            dismiss(true);
            isDone = false;
            setHint();
        });
        value.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                isDone = true;
                dismiss(true);
                isDone = false;
            }
            setHint();
            return false;
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

    public boolean isInRange() {
        String s = value.getText().toString();
        if (s.equals(".")) return false;
        short sh = Short.parseShort(s);
        //TODO:
        return true;
    }

    public void backspace() {
        if (value == null) return;
        String str = value.getText().toString();
        if (str.length() <= 0) return;
        str = str.substring(0, str.length() - 1);
        value.setText(str);
    }

    private void onNumberChanged() {
        if (value == null) return;
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

    public void setHint() {
        if (!value.getText().toString().isEmpty()) value.setHint(value.getText());
        value.setText("");
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
        if (value.getText().toString().isEmpty()) {
            if (value.getHint() == null)
                return "-";
            else
                return value.getHint().toString();
        } else {
            return value.getText().toString();
        }
    }


    @NonNull
    @Override
    protected View onCreateSheetContentView(@NonNull Context context) {
        return LayoutInflater.from(context).inflate(
                R.layout.bottomsheet_alarmlimits_apnea,
                this,
                false
        );
    }
}
