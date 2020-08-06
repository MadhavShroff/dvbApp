package com.dvbinventek.dvbapp.customViews;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Spanned;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.dvbinventek.dvbapp.R;

import java.lang.ref.WeakReference;

public class ControlRow extends LinearLayout {

    WeakReference<LinearLayout> mRow;
    WeakReference<TextView> mLabel, mUnit, mCurrent;
    WeakReference<Button> mEdit;

    public ControlRow(Context context) {
        super(context);
        init(null);
    }

    public ControlRow(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public ControlRow(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    public ControlRow(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    public void setLabel(Spanned s) {
        mLabel.get().setText(s, TextView.BufferType.SPANNABLE);
    }

    public void setLabel(String s) {
        mLabel.get().setText(s, TextView.BufferType.SPANNABLE);
    }

    public void setUnit(Spanned s) {
        mUnit.get().setText(s, TextView.BufferType.SPANNABLE);
    }

    public void setUnit(String s) {
        mUnit.get().setText(s, TextView.BufferType.SPANNABLE);
    }

    public void setCurrent(String s) {
        mCurrent.get().setText(s, TextView.BufferType.SPANNABLE);
    }

    public void init(@Nullable AttributeSet set) {
        inflate(getContext(), R.layout.control_row_view, this);
        mRow = new WeakReference<>(findViewById(R.id.control_row));
        mCurrent = new WeakReference<>(findViewById(R.id.control_current));
        mUnit = new WeakReference<>(findViewById(R.id.control_unit));
        mLabel = new WeakReference<>(findViewById(R.id.control_label));
        mEdit = new WeakReference<>(findViewById(R.id.controls_edit_button));

        if (set == null) return;
        String current;
        String unit;
        String label;

        TypedArray typedArray = getContext().obtainStyledAttributes(set, R.styleable.ControlRow);
        try {
            //            min = typedArray.getString(R.styleable.CustomTextView_minValue);
            //            max = typedArray.getString(R.styleable.CustomTextView_maxValue);
            label = typedArray.getString(R.styleable.ControlRow_controlLabel);
            current = typedArray.getString(R.styleable.ControlRow_current);
            unit = typedArray.getString(R.styleable.ControlRow_controlUnit);
        } finally {
            typedArray.recycle();
        }
        mCurrent.get().setText(current);
        mUnit.get().setText(unit);
        mLabel.get().setText(label);
    }
}
