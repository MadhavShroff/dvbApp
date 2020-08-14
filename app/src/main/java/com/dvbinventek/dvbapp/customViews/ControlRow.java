package com.dvbinventek.dvbapp.customViews;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.text.Spanned;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.dvbinventek.dvbapp.R;

import java.lang.ref.WeakReference;

public class ControlRow extends LinearLayout {

    WeakReference<LinearLayout> mRow;
    WeakReference<TextView> mLabel, mUnit, mCurrent;
    public final int white = Color.parseColor("#ffffff");
    public final int black = Color.parseColor("#000000");
    WeakReference<ImageButton> mEdit;


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

    public void setButtonClickListener(View.OnClickListener listener) {
        mEdit.get().setOnClickListener(listener);
    }

    public void highlight(boolean yellow) {
        if (yellow) {
            mRow.get().setBackgroundColor(Color.parseColor("#ffd600"));
            mLabel.get().setTextColor(black);
            mUnit.get().setTextColor(black);
            mCurrent.get().setTextColor(black);
        } else {
            mRow.get().setBackgroundColor(0);
            mLabel.get().setTextColor(white);
            mUnit.get().setTextColor(white);
            mCurrent.get().setTextColor(white);
        }
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
