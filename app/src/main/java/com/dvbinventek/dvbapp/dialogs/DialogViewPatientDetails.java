package com.dvbinventek.dvbapp.dialogs;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.dvbinventek.dvbapp.R;
import com.google.android.material.textfield.TextInputEditText;


public class DialogViewPatientDetails extends LinearLayout {

    public DialogViewPatientDetails(Context context) {
        super(context);
        init(null);
    }

    public void setVals(String name, String age, String height, String ibw, String room, String bed) {
        ((TextInputEditText) findViewById(R.id.patient_name)).setText(name);
        ((TextInputEditText) findViewById(R.id.patient_age)).setText(age);
        ((TextInputEditText) findViewById(R.id.patient_height)).setText(height);
        ((TextInputEditText) findViewById(R.id.patient_weight)).setText(ibw);
        ((TextInputEditText) findViewById(R.id.patient_room)).setText(room);
        ((TextInputEditText) findViewById(R.id.patient_bed)).setText(bed);
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
