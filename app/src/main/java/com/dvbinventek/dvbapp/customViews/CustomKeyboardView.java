package com.dvbinventek.dvbapp.customViews;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.dvbinventek.dvbapp.R;

public class CustomKeyboardView extends LinearLayout {

    public KeyboardClickListener listener;

    public CustomKeyboardView(Context context) {
        super(context);
        init(null);
    }

    public CustomKeyboardView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public CustomKeyboardView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    public CustomKeyboardView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    void init(@Nullable AttributeSet set) {
        inflate(getContext(), R.layout.custom_keyboard, this);
        GridLayout gl = findViewById(R.id.keyboard_grid);
        OnClickListener keyListener = (v) -> {
            v.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FCDD03")));
            new Handler().postDelayed(() -> v.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#ffffff"))), 100);
            if (v instanceof TextView) {
                if (listener != null) listener.onTextChanged((String) ((TextView) v).getText());
            } else {
                if (listener != null) listener.onTextChanged("backspace");
            }
        };
        for (int i = 0; i < gl.getChildCount(); i++) {
            View v = gl.getChildAt(i);
            v.setOnClickListener(keyListener);
        }
    }

    public void setKeyboardClickListener(KeyboardClickListener clickListener) {
        this.listener = clickListener;
    }
}
