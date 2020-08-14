package com.dvbinventek.dvbapp.dialogs;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.dvbinventek.dvbapp.R;


public class DialogViewPatientDetails extends LinearLayout {

    EditText name, age, height, weight;

    public DialogViewPatientDetails(Context context) {
        super(context);
        name = findViewById(R.id.patient_name);
        age = findViewById(R.id.patient_age);
        height = findViewById(R.id.patient_height);
        weight = findViewById(R.id.patient_weight);
        init(null);
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
