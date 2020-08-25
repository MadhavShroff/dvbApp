package com.dvbinventek.dvbapp.customViews;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Html;
import android.text.Spanned;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.dvbinventek.dvbapp.R;


public class MonitoringTextView extends LinearLayout {
    public MonitoringTextView(Context context) {
        super(context);
        init(null);
    }

    public void setUnit(String s) {
        setText(R.id.unit, s);
    }

    public void setUnit(Spanned s) {
        setText(R.id.unit, s);
    }

    public MonitoringTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public MonitoringTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    public MonitoringTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    void init(@Nullable AttributeSet set) {
        inflate(getContext(), R.layout.monitoring_text_view, this);
        boolean removeBottom;
        TextView Name = findViewById(R.id.name);
        TextView Value = findViewById(R.id.value);
        TextView Unit = findViewById(R.id.unit);

        if (set == null) return;

        String value, name, unit;
        TypedArray typedArray = getContext().obtainStyledAttributes(set, R.styleable.MonitoringTextView);
        try {
            //            min = typedArray.getString(R.styleable.CustomTextView_minValue);
            //            max = typedArray.getString(R.styleable.CustomTextView_maxValue);
            value = typedArray.getString(R.styleable.MonitoringTextView_monitValue);
            name = typedArray.getString(R.styleable.MonitoringTextView_monitName);
            unit = typedArray.getString(R.styleable.MonitoringTextView_monitUnit);
            removeBottom = typedArray.getBoolean(R.styleable.MonitoringTextView_removeLower, false);
        } finally {
            typedArray.recycle();
        }
        if (removeBottom) {
            Unit.setHeight(0);
            Name.setHeight(Name.getHeight() * 2);
            invalidate();
        }
        Value.setText(value);
        Name.setText(name);
        Unit.setText(unit);
    }

    public void setText(int id, Spanned s) {
        ((TextView) findViewById(id)).setText(s);
    }

    public void setText(int id, String s) {
        ((TextView) findViewById(id)).setText(s);
    }

    public void setSubText(String htmlSource) {
        setText(R.id.name, Html.fromHtml(htmlSource));
    }

    public double round1(float x) {
        return Math.round(x * 10) / 10.0;
    }

    public double round2(float x) {
        return Math.round(x * 100) / 100.0;
    }

    public void setValue(float s, int decimalPrecision) {
        switch (decimalPrecision) {
            case 0:
                setText(R.id.value, Integer.toString((int) s));
                break;
            case 1:
                setText(R.id.value, Double.toString(round1(s)));
                break;
            case 2:
                setText(R.id.value, Double.toString(round2(s)));
                break;
            default:
                setText(R.id.value, Float.toString(s));
                break;
        }
    }

    public void setValue(String s) {
        setText(R.id.value, s);
    }

    public void setValue(int s) {
        setText(R.id.value, Integer.toString(s));
    }
}
