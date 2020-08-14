package com.dvbinventek.dvbapp.viewPager;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.dvbinventek.dvbapp.R;
import com.dvbinventek.dvbapp.dialogs.DialogViewPatientDetails;

import java.lang.ref.WeakReference;
import java.util.Objects;

public class PatientFragment extends Fragment {

    static int ui_flags =
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
    WeakReference<View> patientView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_patient, container, false);
        patientView = new WeakReference<>(view);
        ImageView maleAdult = patientView.get().findViewById(R.id.maleAdult);
        ImageView femaleAdult = patientView.get().findViewById(R.id.femaleAdult);
        ImageView maleChild = patientView.get().findViewById(R.id.maleChild);
        ImageView femaleChild = patientView.get().findViewById(R.id.femaleChild);
        Button change = patientView.get().findViewById(R.id.change);
        View.OnClickListener clickListener = (v) -> {
            setAllUnselected();
            setSelected(v);
        };
        maleAdult.setOnClickListener(clickListener);
        femaleAdult.setOnClickListener(clickListener);
        maleChild.setOnClickListener(clickListener);
        femaleChild.setOnClickListener(clickListener);


        change.setOnClickListener(v -> {
            final DialogViewPatientDetails pd = new DialogViewPatientDetails(getContext());
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext()).setCancelable(true).setTitle("Set Patient Details").setView(pd).setPositiveButton("Done", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    EditText name = pd.findViewById(R.id.patient_name);
                    EditText age = pd.findViewById(R.id.patient_age);
                    EditText weight = pd.findViewById(R.id.patient_weight);
                    EditText height = pd.findViewById(R.id.patient_height);
                    EditText room = pd.findViewById(R.id.patient_room);
                    EditText bed = pd.findViewById(R.id.patient_bed);

                    TextView namet = patientView.get().findViewById(R.id.patient_view_name);
                    TextView aget = patientView.get().findViewById(R.id.patient_view_age);
                    TextView weightt = patientView.get().findViewById(R.id.patient_view_weight);
                    TextView heightt = patientView.get().findViewById(R.id.patient_view_height);
                    TextView roomt = patientView.get().findViewById(R.id.roomNumber);
                    TextView bedt = patientView.get().findViewById(R.id.bedNumber);

                    if (!name.getText().toString().isEmpty()) namet.setText(name.getText());
                    if (!age.getText().toString().isEmpty()) aget.setText(age.getText());
                    if (!weight.getText().toString().isEmpty())
                        weightt.setText(weight.getText() + " kg");
                    if (!height.getText().toString().isEmpty())
                        heightt.setText(height.getText() + " cm");
                    if (!room.getText().toString().isEmpty()) roomt.setText(room.getText());
                    if (!bed.getText().toString().isEmpty()) bedt.setText(bed.getText());
                }
            });
            AlertDialog alert = builder.create();
            alert.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            alert.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
            alert.getWindow().getDecorView().setSystemUiVisibility(ui_flags);
            alert.show();
            openKeyboard();
            alert.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
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
