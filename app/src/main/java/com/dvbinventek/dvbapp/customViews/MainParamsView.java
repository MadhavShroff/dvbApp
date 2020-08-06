package com.dvbinventek.dvbapp.customViews;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.Html;
import android.text.Spanned;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.dvbinventek.dvbapp.R;

public class MainParamsView extends LinearLayout {

    public static final int RED = 1, YELLOW = 2;
    public static final int BUFFER_SIZE = 6;
    public static final char[] buffer = new char[BUFFER_SIZE];
    public static final Paint paint = new Paint();
    public static int padding, textSize;
    public TextView Min, Max, Value, Label, Unit, minText, maxText, setValue;
    public ConstraintLayout constraintLayout;

    public MainParamsView(Context context) {
        super(context);
        init(null);
    }

    public MainParamsView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public MainParamsView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    public MainParamsView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    public double round1(float x) {
        return Math.round(x * 10) / 10.0;
    }

    public void setMaxMinValue(float max, float min, float value, String setval) {
        String maxs = "" + round1(max);
        String mins = "" + round1(min);
        boolean set = false;
        String values = "" + round1(value);
        if (!Value.getText().equals(values)) {
            Value.setText(values);
            set = true;
        }
        if (!Max.getText().equals(maxs)) {
            Max.setText(maxs);
            set = true;
        }
        if (!Min.getText().equals(mins)) {
            Min.setText(mins);
            set = true;
        }
        if (!setValue.getText().equals(setval)) {
            setValue.setText(getResources().getString(R.string.set, setval));
            set = true;
        }
        if (set) this.invalidate();
    }

    public void setMaxMinValueVIT(float max, float min, float value, String setval) {
        String maxs = "" + (int) max;
        String mins = "" + (int) min;
        boolean set = false;
        String values = "" + (int) value;
        if (!Value.getText().equals(values)) {
            Value.setText(values);
            set = true;
        }
        if (!Max.getText().equals(maxs)) {
            Max.setText(maxs);
            set = true;
        }
        if (!Min.getText().equals(mins)) {
            Min.setText(mins);
            set = true;
        }
        if (!setValue.getText().equals(setval)) {
            setValue.setText(getResources().getString(R.string.set, setval));
            set = true;
        }
        if (set) this.invalidate();
    }

    public void setLabel(Spanned s) {
        Label.setText(s);
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.drawText(buffer, 0, buffer.length, padding, padding, paint);
    }

    public void initPaint() {
        // Setting the paint
        padding = getPixels(TypedValue.COMPLEX_UNIT_DIP, padding);
        textSize = getPixels(TypedValue.COMPLEX_UNIT_SP, textSize);
        paint.setAntiAlias(true);
        paint.setColor(Color.DKGRAY);
        paint.setTextSize(textSize);
    }

    public void setText(StringBuilder source) {
        source.getChars(0, source.length(), buffer, 0);
    }

    private int getPixels(int unit, float size) {
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        return (int) TypedValue.applyDimension(unit, size, metrics);
    }

    void init(@Nullable AttributeSet set) {
        inflate(getContext(), R.layout.main_params_view, this);
        this.initPaint();
        Min = findViewById(R.id.min);
        Max = findViewById(R.id.max);
        Value = findViewById(R.id.value);
        Label = findViewById(R.id.label);
        Unit = findViewById(R.id.unit);
        minText = findViewById(R.id.minText);
        maxText = findViewById(R.id.maxText);
        constraintLayout = findViewById(R.id.constLayout);
        setValue = findViewById(R.id.setValue);

        if (set == null) return;
        String min;
        String max;
        String value;
        String label;
        String unit;

        TypedArray typedArray = getContext().obtainStyledAttributes(set, R.styleable.MainParamsView);
        try {
            //            min = typedArray.getString(R.styleable.CustomTextView_minValue);
            //            max = typedArray.getString(R.styleable.CustomTextView_maxValue);
            min = typedArray.getString(R.styleable.MainParamsView_minValue);
            max = typedArray.getString(R.styleable.MainParamsView_maxValue);
            value = typedArray.getString(R.styleable.MainParamsView_value);
            label = typedArray.getString(R.styleable.MainParamsView_label);
            unit = typedArray.getString(R.styleable.MainParamsView_unit);
        } finally {
            typedArray.recycle();
        }

        Min.setText(min);
        Max.setText(max);
        Value.setText(value);
        Label.setText(label);
        Unit.setText(unit);
    }

    public void setPeepPip() {
        minText.setText(Html.fromHtml("P<sub>mean</sub>"));
        maxText.setText(Html.fromHtml("P<sub>peak</sub>"));
        maxText.setTextSize(12);
        minText.setTextSize(12);
    }

    public void setAlarmColors(int color1, int color2, int color3, int color) {
        if (color == RED) {
            constraintLayout.setBackgroundColor(getResources().getColor(color2));
            minText.setBackgroundColor(getResources().getColor(color3));
            Min.setBackgroundColor(getResources().getColor(color3));
            Max.setBackgroundColor(getResources().getColor(color1));
            maxText.setBackgroundColor(getResources().getColor(color1));
        } else if (color == YELLOW) {
            constraintLayout.setBackgroundColor(getResources().getColor(color2));
            minText.setBackgroundColor(getResources().getColor(color3));
            Min.setBackgroundColor(getResources().getColor(color3));
            Max.setBackgroundColor(getResources().getColor(color1));
            maxText.setBackgroundColor(getResources().getColor(color1));
            //Set text color
            minText.setTextColor(minText.getResources().getColor(R.color.black));
            Min.setTextColor(minText.getResources().getColor(R.color.black));
            Max.setTextColor(minText.getResources().getColor(R.color.black));
            maxText.setTextColor(minText.getResources().getColor(R.color.black));
            Value.setTextColor(minText.getResources().getColor(R.color.black));
            Unit.setTextColor(minText.getResources().getColor(R.color.black));
            Label.setTextColor(minText.getResources().getColor(R.color.black));
            setValue.setTextColor(minText.getResources().getColor(R.color.black));
        }
    }

    public void resetAlarmColors() {
        constraintLayout.setBackgroundColor(getResources().getColor(R.color.green1));
        minText.setBackgroundColor(getResources().getColor(R.color.green3));
        Min.setBackgroundColor(getResources().getColor(R.color.green3));
        Max.setBackgroundColor(getResources().getColor(R.color.green2));
        maxText.setBackgroundColor(getResources().getColor(R.color.green2));
        minText.setTextColor(minText.getResources().getColor(R.color.white));
        Min.setTextColor(minText.getResources().getColor(R.color.white));
        Max.setTextColor(minText.getResources().getColor(R.color.white));
        maxText.setTextColor(minText.getResources().getColor(R.color.white));
        Value.setTextColor(minText.getResources().getColor(R.color.white));
        Unit.setTextColor(minText.getResources().getColor(R.color.white));
        Label.setTextColor(minText.getResources().getColor(R.color.white));
        setValue.setTextColor(minText.getResources().getColor(R.color.white));
    }
}
