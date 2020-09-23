package com.dvbinventek.dvbapp.dialogs;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.dvbinventek.dvbapp.R;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.TextInputEditText;


public class DialogViewPatientDetails extends LinearLayout {

    public DialogViewPatientDetails(Context context) {
        super(context);
        init(null);
    }

    public void setText(int id, String s) {
        TextView tv = findViewById(id);
        if (s.equals("0")) tv.setText("");
        tv.setText(s);
    }

    public String getText(int id) {
        TextView tv = findViewById(id);
        return tv.getText().toString();
    }

    public double round1(double x) {
        return Math.round(x * 10) / 10.0;
    }

    public double round0(double x) {
        return Math.round(x);
    }

    public void setVals(String name, String age, String height, String ibw, String room, String bed) {
        ((TextInputEditText) findViewById(R.id.patient_name)).setText(name);
        ((TextInputEditText) findViewById(R.id.patient_age)).setText(age);
        ((TextInputEditText) findViewById(R.id.patient_height_cm)).setText(height);
        ((TextInputEditText) findViewById(R.id.patient_weight)).setText(ibw);
        ((TextInputEditText) findViewById(R.id.patient_room)).setText(room);
        ((TextInputEditText) findViewById(R.id.patient_bed)).setText(bed);
        ((MaterialButtonToggleGroup) findViewById(R.id.toggleGroupMassUnit)).addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            float fl = 0.0f;
            String s;
            switch (checkedId) {
                case R.id.kilograms:
                    if (!isChecked) break;
                    ((TextView) findViewById(R.id.massUnit)).setText(R.string.kg);
                    s = ((TextView) findViewById(R.id.patient_weight)).getText().toString();
                    try {
                        fl = Float.parseFloat(s);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    s = "" + round1(0.45372051 * fl);
                    setText(R.id.patient_weight, s);
                    break;
                case R.id.pounds:
                    if (!isChecked) break;
                    ((TextView) findViewById(R.id.massUnit)).setText(R.string.lbs);
                    s = ((TextView) findViewById(R.id.patient_weight)).getText().toString();
                    try {
                        fl = Float.parseFloat(s);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    s = "" + round1(2.203999 * fl);
                    setText(R.id.patient_weight, s);
                    break;
            }
        });
        ((MaterialButtonToggleGroup) findViewById(R.id.toggleGroupPatientLengthUnit)).addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            double inches;
            switch (checkedId) {
                case R.id.inches:           // Convert inches to cm
                    if (!isChecked) break;
                    findViewById(R.id.heightCentimeters).setVisibility(GONE);
                    findViewById(R.id.heightInches).setVisibility(VISIBLE);
                    float cm = 0;
                    try {
                        cm = Float.parseFloat(getText(R.id.patient_height_cm));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    inches = round0(cm * 0.39370079);
                    setText(R.id.patient_height_ft, "" + ((int) inches / 12));
                    setText(R.id.patient_height_in, "" + ((int) inches % 12));
                    break;
                case R.id.centimeter:           // Convert inches to cm
                    if (!isChecked) break;
                    findViewById(R.id.heightCentimeters).setVisibility(VISIBLE);
                    findViewById(R.id.heightInches).setVisibility(GONE);
                    int inch = 0;
                    try {
                        inch = Integer.parseInt(getText(R.id.patient_height_ft)) * 12 + Integer.parseInt(getText(R.id.patient_height_in));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    setText(R.id.patient_height_cm, "" + ((float) inch * 2.54f));
                    break;
            }
        });
    }

    public String getHeightText() {
        switch (((MaterialButtonToggleGroup) findViewById(R.id.toggleGroupPatientLengthUnit)).getCheckedButtonId()) {
            case R.id.inches:
                return ((TextView) findViewById(R.id.patient_height_ft)).getText() + " ft " + ((TextView) findViewById(R.id.patient_height_in)).getText() + " in";
            case R.id.centimeter:
                return ((TextView) findViewById(R.id.patient_height_cm)).getText() + " cm";
        }
        return "";
    }

    public String getWeightText() {
        switch (((MaterialButtonToggleGroup) findViewById(R.id.toggleGroupMassUnit)).getCheckedButtonId()) {
            case R.id.kilograms:
                return ((TextView) findViewById(R.id.patient_weight)).getText() + " kg";
            case R.id.pounds:
                return ((TextView) findViewById(R.id.patient_weight)).getText() + " lbs";
        }
        return "";
    }

    public DialogViewPatientDetails(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public DialogViewPatientDetails(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    public DialogViewPatientDetails(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    void init(@Nullable AttributeSet set) {
        inflate(getContext(), R.layout.dialog_view_patient_details, this);
    }
}
