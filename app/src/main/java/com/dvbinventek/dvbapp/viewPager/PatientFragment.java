package com.dvbinventek.dvbapp.viewPager;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.dvbinventek.dvbapp.R;
import com.dvbinventek.dvbapp.StaticStore;
import com.dvbinventek.dvbapp.dialogs.DialogViewPatientDetails;
import com.google.android.material.textfield.TextInputEditText;

import java.lang.ref.WeakReference;
import java.util.Objects;

public class PatientFragment extends Fragment {

    //TODO: add switch for kg to

    static int ui_flags =
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
    WeakReference<View> patientView;
    String weightUnitString = "kg";
    String heightUnitString = "cm";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_patient, container, false);
        patientView = new WeakReference<>(view);
        ImageView maleAdult = patientView.get().findViewById(R.id.maleAdult);
        ImageView femaleAdult = patientView.get().findViewById(R.id.femaleAdult);
        ImageView maleChild = patientView.get().findViewById(R.id.maleChild);
        ImageView femaleChild = patientView.get().findViewById(R.id.femaleChild);
        View.OnClickListener clickListener = (v) -> {
            setAllUnselected();
            setSelected(v);
        };
        maleAdult.setOnClickListener(clickListener);
        femaleAdult.setOnClickListener(clickListener);
        maleChild.setOnClickListener(clickListener);
        femaleChild.setOnClickListener(clickListener);
        ((TextView) patientView.get().findViewById(R.id.patient_view_name)).setText(StaticStore.PatientDetails.name);
        ((TextView) patientView.get().findViewById(R.id.patient_view_age)).setText(StaticStore.PatientDetails.age);
        ((TextView) patientView.get().findViewById(R.id.patient_view_height)).setText(StaticStore.PatientDetails.height + " cm");
        ((TextView) patientView.get().findViewById(R.id.patient_view_weight)).setText(StaticStore.PatientDetails.ibw + " kg");
        ((TextView) patientView.get().findViewById(R.id.roomNumber)).setText(StaticStore.PatientDetails.room);
        ((TextView) patientView.get().findViewById(R.id.bedNumber)).setText(StaticStore.PatientDetails.bed);

        Button change = patientView.get().findViewById(R.id.change);
        change.setOnClickListener(v -> {
            DialogViewPatientDetails pd = new DialogViewPatientDetails(getContext());
            pd.setVals(
                    StaticStore.PatientDetails.name,
                    StaticStore.PatientDetails.age,
                    StaticStore.PatientDetails.height,
                    StaticStore.PatientDetails.ibw,
                    StaticStore.PatientDetails.room,
                    StaticStore.PatientDetails.bed
            );
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext()).setCancelable(true).setView(pd);
            AlertDialog alert = builder.create();
            alert.show();
            pd.findViewById(R.id.dismissButton).setOnClickListener((vi) -> alert.dismiss());
            openKeyboard();
            alert.setOnDismissListener(dialog -> {
                Log.d("PATIENT_DIALOG", "onDismiss");
                getActivity().getWindow().getDecorView().setSystemUiVisibility(ui_flags);
                if (!((TextInputEditText) pd.findViewById(R.id.patient_name)).getText().toString().isEmpty())
                    ((TextView) patientView.get().findViewById(R.id.patient_view_name)).setText(((TextInputEditText) pd.findViewById(R.id.patient_name)).getText());
                if (!((TextInputEditText) pd.findViewById(R.id.patient_age)).getText().toString().isEmpty())
                    ((TextView) patientView.get().findViewById(R.id.patient_view_age)).setText(((TextInputEditText) pd.findViewById(R.id.patient_age)).getText());
                if (!((TextInputEditText) pd.findViewById(R.id.patient_weight)).getText().toString().isEmpty())
                    ((TextView) patientView.get().findViewById(R.id.patient_view_weight)).setText(pd.getWeightText());
                if (!((TextInputEditText) pd.findViewById(R.id.patient_height_cm)).getText().toString().isEmpty())
                    ((TextView) patientView.get().findViewById(R.id.patient_view_height)).setText(pd.getHeightText());
                if (!((TextInputEditText) pd.findViewById(R.id.patient_room)).getText().toString().isEmpty())
                    ((TextView) patientView.get().findViewById(R.id.roomNumber)).setText(((TextInputEditText) pd.findViewById(R.id.patient_room)).getText());
                if (!((TextInputEditText) pd.findViewById(R.id.patient_bed)).getText().toString().isEmpty())
                    ((TextView) patientView.get().findViewById(R.id.bedNumber)).setText(((TextInputEditText) pd.findViewById(R.id.patient_bed)).getText());
                StaticStore.PatientDetails.name = ((TextInputEditText) pd.findViewById(R.id.patient_name)).getText().toString();
                StaticStore.PatientDetails.age = ((TextInputEditText) pd.findViewById(R.id.patient_age)).getText().toString();
                StaticStore.PatientDetails.height = ((TextInputEditText) pd.findViewById(R.id.patient_height_cm)).getText().toString();
                StaticStore.PatientDetails.ibw = ((TextInputEditText) pd.findViewById(R.id.patient_weight)).getText().toString();
                StaticStore.PatientDetails.room = ((TextInputEditText) pd.findViewById(R.id.patient_room)).getText().toString();
                StaticStore.PatientDetails.bed = ((TextInputEditText) pd.findViewById(R.id.patient_bed)).getText().toString();
            });
        });
        return view;
    }

    public void openKeyboard() {
        InputMethodManager imm = (InputMethodManager) Objects.requireNonNull(getActivity()).getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(getActivity().getCurrentFocus(), InputMethodManager.SHOW_FORCED);
        }
    }

    public void setAllUnselected() {
        setBackgroundUnselected(R.id.maleAdult);
        setBackgroundUnselected(R.id.femaleAdult);
        setBackgroundUnselected(R.id.maleChild);
        setBackgroundUnselected(R.id.femaleChild);
    }

    public void setBackgroundUnselected(int id) {
        View v = patientView.get().findViewById(id);
        v.setBackgroundResource(0);
    }

    public void setSelected(View v) {
        v.setBackgroundResource(R.drawable.rounded_corner_blue);
    }
}
