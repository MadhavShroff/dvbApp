package com.dvbinventek.dvbapp.viewPager;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.dvbinventek.dvbapp.R;
import com.dvbinventek.dvbapp.SendPacket;
import com.dvbinventek.dvbapp.StaticStore;
import com.dvbinventek.dvbapp.bottomSheets.AlarmLimitsBottomSheet;
import com.dvbinventek.dvbapp.bottomSheets.AlarmLimitsBottomSheetApnea;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

import static android.content.Context.VIBRATOR_SERVICE;

public class AlarmsFragment extends Fragment {

    public static Set<String> resetList = new HashSet<String>();
    public static List<Integer> highlightedList = new ArrayList<>();
    public static int countdown = 30;
    public static boolean countdownStarted = false;
    public final int white = Color.parseColor("#ffffff");
    public final int black = Color.parseColor("#000000");
    public WeakReference<View> alarmsView;
    public MediaPlayer mp;
    public CompositeDisposable disposables = new CompositeDisposable();
    public Disposable disposable;
    Observable<Long> observable = Observable.interval(0, 1000, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread());
    Observer<Long> observer = new Observer<Long>() {
        @Override
        public void onError(Throwable e) {
            e.printStackTrace();
        }

        @Override
        public void onComplete() {
            resetChanges();
            countdownStarted = false;
        }

        @Override
        public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {
            disposable = d;
            disposables.add(d);
        }

        @Override
        public void onNext(Long aLong) {
            setText(R.id.highlight_text, getResources().getString(R.string.warning_text, 30 - aLong));
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //TODO: Create custom row views for Alarm Rows
        View view = inflater.inflate(R.layout.fragment_alarm_limits, container, false);
        alarmsView = new WeakReference<>(view);
        mp = MediaPlayer.create(getContext(), R.raw.button_press);

        //Set subtext
        TextView minVolText = view.findViewById(R.id.minVolText);
        minVolText.setText(Html.fromHtml("MV<sub><small>total</small></sub> (l)"));

        //Set Past Session Values
        setPastSessionValues();

        setOnClickListeners();

        return view;
    }

    public void setPastSessionValues() {
        StaticStore.AlarmLimits.new_minVolMax = StaticStore.AlarmLimits.minVolMax;
        StaticStore.AlarmLimits.new_minVolMin = StaticStore.AlarmLimits.minVolMin;
        StaticStore.AlarmLimits.new_fTotalMax = StaticStore.AlarmLimits.fTotalMax;
        StaticStore.AlarmLimits.new_fTotalMin = StaticStore.AlarmLimits.fTotalMin;
        StaticStore.AlarmLimits.new_vtMax = StaticStore.AlarmLimits.vtMax;
        StaticStore.AlarmLimits.new_vtMin = StaticStore.AlarmLimits.vtMin;
        StaticStore.AlarmLimits.new_pMax = StaticStore.AlarmLimits.pMax;
        StaticStore.AlarmLimits.new_pMin = StaticStore.AlarmLimits.pMin;
        StaticStore.AlarmLimits.new_apnea = StaticStore.AlarmLimits.apnea;

        setText(R.id.limits_minVolMax, String.valueOf(StaticStore.AlarmLimits.minVolMax == 0 ? "0" : StaticStore.AlarmLimits.minVolMax));
        setText(R.id.limits_minVolMin, String.valueOf(StaticStore.AlarmLimits.minVolMin == 0 ? "0" : StaticStore.AlarmLimits.minVolMin));
        setText(R.id.limits_ftotalMax, String.valueOf(StaticStore.AlarmLimits.fTotalMax == 0 ? "0" : StaticStore.AlarmLimits.fTotalMax));
        setText(R.id.limits_ftotalMin, String.valueOf(StaticStore.AlarmLimits.fTotalMin == 0 ? "0" : StaticStore.AlarmLimits.fTotalMin));
        setText(R.id.limits_vtMax, String.valueOf(StaticStore.AlarmLimits.vtMax == 0 ? "0" : StaticStore.AlarmLimits.vtMax));
        setText(R.id.limits_vtMin, String.valueOf(StaticStore.AlarmLimits.vtMin == 0 ? "0" : StaticStore.AlarmLimits.vtMin));
        setText(R.id.limits_pMax, String.valueOf(StaticStore.AlarmLimits.pMax == 0 ? "0" : StaticStore.AlarmLimits.pMax));
        setText(R.id.limits_pMin, String.valueOf(StaticStore.AlarmLimits.pMin == 0 ? "0" : StaticStore.AlarmLimits.pMin));
        setText(R.id.limits_apnea, String.valueOf(StaticStore.AlarmLimits.apnea == 0 ? "0" : StaticStore.AlarmLimits.apnea));

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (disposables != null)
            if (!disposables.isDisposed())
                disposables.dispose();
    }

    public void setOnClickListeners() {
        AlarmLimitsBottomSheet abs_minVol = new AlarmLimitsBottomSheet(getActivity(), Html.fromHtml("MV<sub><small>total</small></sub> (l)"), "minvol");
        AlarmLimitsBottomSheet abs_rate = new AlarmLimitsBottomSheet(getActivity(), Html.fromHtml("Rate (b/min)"), "rate");
        AlarmLimitsBottomSheet abs_vt = new AlarmLimitsBottomSheet(getActivity(), Html.fromHtml("Vt (ml)"), "vt");
        AlarmLimitsBottomSheet abs_p = new AlarmLimitsBottomSheet(getActivity(), Html.fromHtml("P (cm H<sub><small>2</small></sub>0)"), "p");
        AlarmLimitsBottomSheetApnea abs_apnea = new AlarmLimitsBottomSheetApnea(getActivity(), Html.fromHtml("Apnea Time (s)"));

        ImageButton minVol = alarmsView.get().findViewById(R.id.limits_minvol_change);
        ImageButton ftotal = alarmsView.get().findViewById(R.id.limits_ftotal_change);
        ImageButton vt = alarmsView.get().findViewById(R.id.limits_vt_change);
        ImageButton p = alarmsView.get().findViewById(R.id.limits_p_change);
        ImageButton apnea = alarmsView.get().findViewById(R.id.limits_apnea_change);
        Button saveChanges = alarmsView.get().findViewById(R.id.limits_saveChanges);

        minVol.setOnClickListener(v -> {
            abs_minVol.show(true);
        });
        ftotal.setOnClickListener(v -> {
            abs_rate.show(true);
        });
        vt.setOnClickListener(v -> {
            abs_vt.show(true);
        });
        p.setOnClickListener(v -> {
            abs_p.show(true);
        });
        apnea.setOnClickListener(v -> {
            abs_apnea.show(true);
        });

        abs_minVol.setOnDismissListener(bottomSheet -> {
            String max = abs_minVol.getMax();
            String min = abs_minVol.getMin();
            if ((min.equals("") && max.equals("")) || !abs_minVol.isDone) return;
            setText(R.id.limits_minVolMin, min);
            setText(R.id.limits_minVolMax, max);
            short max_ = (short) (Integer.parseInt(max));
            short min_ = (short) (Integer.parseInt(min));
            if (StaticStore.AlarmLimits.new_minVolMax != max_ || StaticStore.AlarmLimits.new_minVolMin != min_) {
                highlight(R.id.limits_minvol, true);
                resetList.add("minvol");
                try { // commit alarm limits
                    if (max.equals("-")) StaticStore.AlarmLimits.new_minVolMax = 0;
                    else StaticStore.AlarmLimits.new_minVolMax = max_;
                    if (min.equals("-")) StaticStore.AlarmLimits.new_minVolMin = 0;
                    else StaticStore.AlarmLimits.new_minVolMin = min_;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Log.d("MSG", "packet value equals entered value");
            }
        });
        abs_rate.setOnDismissListener(bottomSheet -> {
            String max = abs_rate.getMax();
            String min = abs_rate.getMin();
            if ((min.equals("") && max.equals("")) || !abs_rate.isDone) return;
            setText(R.id.limits_ftotalMin, min);
            setText(R.id.limits_ftotalMax, max);
            short max_ = (short) (Integer.parseInt(max));
            short min_ = (short) (Integer.parseInt(min));
            if (StaticStore.AlarmLimits.new_fTotalMax != max_ || StaticStore.AlarmLimits.new_fTotalMin != min_) {
                highlight(R.id.limits_ftotal, true);
                resetList.add("ftotal");
                try { // commit alarm limits
                    if (max.equals("-")) StaticStore.AlarmLimits.new_fTotalMax = 0;
                    else StaticStore.AlarmLimits.new_fTotalMax = max_;
                    if (min.equals("-")) StaticStore.AlarmLimits.new_fTotalMin = 0;
                    else StaticStore.AlarmLimits.new_fTotalMin = min_;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Log.d("MSG", "packet value equals entered value");
            }
        });
        abs_vt.setOnDismissListener(bottomSheet -> {
            String max = abs_vt.getMax();
            String min = abs_vt.getMin();
            if ((min.equals("") && max.equals("")) || !abs_vt.isDone) return;
            setText(R.id.limits_vtMin, min);
            setText(R.id.limits_vtMax, max);
            short max_ = (short) (Integer.parseInt(max));
            short min_ = (short) (Integer.parseInt(min));
            if (StaticStore.AlarmLimits.new_vtMax != max_ || StaticStore.AlarmLimits.new_vtMin != min_) {
                highlight(R.id.limits_vt, true);
                resetList.add("vt");
                try {
                    if (max.equals("-")) StaticStore.AlarmLimits.new_vtMax = 0;
                    else StaticStore.AlarmLimits.new_vtMax = max_;
                    if (min.equals("-")) StaticStore.AlarmLimits.new_vtMin = 0;
                    else StaticStore.AlarmLimits.new_vtMin = min_;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Log.d("MSG", "packet value equals entered value");
            }
        });
        abs_p.setOnDismissListener(bottomSheet -> {
            String max = abs_p.getMax();
            String min = abs_p.getMin();
            if ((min.equals("") && max.equals("")) || !abs_p.isDone) return;
            setText(R.id.limits_pMin, min);
            setText(R.id.limits_pMax, max);
            short max_ = (short) (Integer.parseInt(max));
            short min_ = (short) (Integer.parseInt(min));
            if (StaticStore.AlarmLimits.new_pMax != max_ || StaticStore.AlarmLimits.new_pMin != min_) {
                highlight(R.id.limits_p, true);
                resetList.add("p");
                try {
                    if (max.equals("-")) StaticStore.AlarmLimits.new_pMax = 0;
                    else StaticStore.AlarmLimits.new_pMax = max_;
                    if (min.equals("-")) StaticStore.AlarmLimits.new_pMin = 0;
                    else StaticStore.AlarmLimits.new_pMin = min_;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Log.d("MSG", "packet value equals entered value");
            }
        });
        abs_apnea.setOnDismissListener(bottomSheet -> {
            String apnea_ = abs_apnea.getValue();
            if (apnea_.equals("") || !abs_apnea.isDone) return;
            setText(R.id.limits_apnea, apnea_);
            short value_ = (short) (Integer.parseInt(apnea_));
            if (StaticStore.AlarmLimits.new_apnea != value_) {
                highlight(R.id.limits_apnea, true);
                resetList.add("apnea");
                try {
                    if (apnea_.equals("-")) StaticStore.AlarmLimits.apnea = 0;
                    else StaticStore.AlarmLimits.apnea = value_;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Log.d("MSG", "packet value equals entered value");
            }
        });
        saveChanges.setOnClickListener(v -> {
            StaticStore.AlarmLimits.minVolMax = StaticStore.AlarmLimits.new_minVolMax;
            StaticStore.AlarmLimits.minVolMin = StaticStore.AlarmLimits.new_minVolMin;
            StaticStore.AlarmLimits.fTotalMax = StaticStore.AlarmLimits.new_fTotalMax;
            StaticStore.AlarmLimits.fTotalMin = StaticStore.AlarmLimits.new_fTotalMin;
            StaticStore.AlarmLimits.vtMax = StaticStore.AlarmLimits.new_vtMax;
            StaticStore.AlarmLimits.vtMin = StaticStore.AlarmLimits.new_vtMin;
            StaticStore.AlarmLimits.pMax = StaticStore.AlarmLimits.new_pMax;
            StaticStore.AlarmLimits.pMin = StaticStore.AlarmLimits.new_pMin;
            StaticStore.AlarmLimits.apnea = StaticStore.AlarmLimits.new_apnea;

            SharedPreferences.Editor editor = Objects.requireNonNull(getContext()).getSharedPreferences("dvbVentilator", Context.MODE_PRIVATE).edit();
            editor.putString("limits_minVolMax", "" + StaticStore.AlarmLimits.minVolMax);
            editor.putString("limits_minVolMin", "" + StaticStore.AlarmLimits.minVolMin);
            editor.putString("limits_fTotalMax", "" + StaticStore.AlarmLimits.fTotalMax);
            editor.putString("limits_fTotalMin", "" + StaticStore.AlarmLimits.fTotalMin);
            editor.putString("limits_vtMax", "" + StaticStore.AlarmLimits.vtMax);
            editor.putString("limits_vtMi", "" + StaticStore.AlarmLimits.vtMin);
            editor.putString("limits_pMax", "" + StaticStore.AlarmLimits.pMax);
            editor.putString("limits_pMin", "" + StaticStore.AlarmLimits.pMin);
            editor.putString("limits_apnea", "" + StaticStore.AlarmLimits.apnea);
            editor.apply();

            SendPacket sp = new SendPacket();
            sp.writeInfo((short) (StaticStore.packet_fio2 * 100), 17);
            sp.writeInfo(StaticStore.modeSelectedShort, 1);
            sp.writeInfo(StaticStore.packet_vt, 5);
            sp.writeInfo((short) (StaticStore.packet_vtrig * 100), 10);
            sp.writeInfo((short) (StaticStore.packet_peep * 100), 4);
            sp.writeInfo((short) (StaticStore.packet_pinsp * 100), 3);
            sp.writeInfo((short) (StaticStore.packet_ps * 100), 2);
            sp.writeInfo((short) (StaticStore.packet_rtotal * 100), 6);
            sp.writeInfo(StaticStore.packet_ie, 7);
            //alarm values
            sp.writeInfo((short) ((int) StaticStore.AlarmLimits.apnea * 1000), 39);
            sp.writeInfo((short) ((int) StaticStore.AlarmLimits.minVolMin * 100), 40);
            sp.writeInfo((short) ((int) StaticStore.AlarmLimits.minVolMax * 100), 41);
            sp.writeInfo(StaticStore.AlarmLimits.vtMin, 42);
            sp.writeInfo(StaticStore.AlarmLimits.vtMax, 43);
            sp.writeInfo((short) ((int) StaticStore.AlarmLimits.pMin * 100), 44);
            sp.writeInfo((short) ((int) StaticStore.AlarmLimits.pMax * 100), 45);
            sp.writeInfo((short) ((int) StaticStore.AlarmLimits.fTotalMin * 100), 46);
            sp.writeInfo((short) ((int) StaticStore.AlarmLimits.fTotalMax * 100), 47);
            if (sp.sendToDevice()) mp.start();
            Vibrator myVib = (Vibrator) getContext().getSystemService(VIBRATOR_SERVICE);
            myVib.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
            tryToDispose(disposable);
            resetChanges();
            countdownStarted = false;
        });
    }

    public void resetValues() {
        for (String s : resetList) {
            switch (s) {
                case "minvol":
                    StaticStore.AlarmLimits.new_minVolMin = StaticStore.AlarmLimits.minVolMin;
                    StaticStore.AlarmLimits.new_minVolMax = StaticStore.AlarmLimits.minVolMax;
                    setText(R.id.limits_minVolMin, String.valueOf(StaticStore.AlarmLimits.minVolMin));
                    setText(R.id.limits_minVolMax, String.valueOf(StaticStore.AlarmLimits.minVolMax));
                    break;
                case "ftotal":
                    StaticStore.AlarmLimits.new_fTotalMin = StaticStore.AlarmLimits.fTotalMin;
                    StaticStore.AlarmLimits.new_fTotalMax = StaticStore.AlarmLimits.fTotalMax;
                    setText(R.id.limits_ftotalMin, String.valueOf(StaticStore.AlarmLimits.fTotalMin));
                    setText(R.id.limits_ftotalMax, String.valueOf(StaticStore.AlarmLimits.fTotalMax));
                    break;
                case "vt":
                    StaticStore.AlarmLimits.new_vtMin = StaticStore.AlarmLimits.vtMin;
                    StaticStore.AlarmLimits.new_vtMax = StaticStore.AlarmLimits.vtMax;
                    setText(R.id.limits_vtMin, String.valueOf(StaticStore.AlarmLimits.vtMin));
                    setText(R.id.limits_vtMax, String.valueOf(StaticStore.AlarmLimits.vtMax));
                    break;
                case "p":
                    StaticStore.AlarmLimits.new_pMin = StaticStore.AlarmLimits.pMin;
                    StaticStore.AlarmLimits.new_pMax = StaticStore.AlarmLimits.pMax;
                    setText(R.id.limits_pMin, String.valueOf(StaticStore.AlarmLimits.pMin));
                    setText(R.id.limits_pMax, String.valueOf(StaticStore.AlarmLimits.pMax));
                    break;
                case "apnea":
                    StaticStore.AlarmLimits.new_apnea = StaticStore.AlarmLimits.apnea;
                    setText(R.id.limits_apnea, String.valueOf(StaticStore.AlarmLimits.apnea));
                    break;
            }
        }
        resetList.clear();
    }

    public void resetChanges() {
        Log.d("MSG", "resetChanges(): " + resetList);
        setText(R.id.highlight_text, "");
        for (int id : highlightedList) {
            revertRowColor(id);
        }
        highlightedList.clear();
        resetValues();
    }

    public void highlight(int id, boolean highlight) { // highlight row and reset timer to 30s
        if (highlight) {
            countdown = 30;
            makeRowYellow(id);
            highlightedList.add(id);
            if (countdownStarted) tryToDispose(disposable);
            observable.take(30).subscribe(observer);
            countdownStarted = true;
        }
    }

    void tryToDispose(Disposable d) {
        if (d != null)
            if (!d.isDisposed())
                d.dispose();
    }

    public void makeRowYellow(int id) {
        LinearLayout ll = alarmsView.get().findViewById(id);
        ll.setBackgroundColor(Color.parseColor("#ffd600"));
        for (int i = 0; i < ll.getChildCount(); i++) {
            View v = ll.getChildAt(i);
            if (v instanceof TextView) {
                ((TextView) v).setTextColor(black);
            } else if (v instanceof LinearLayout) {
                for (int j = 0; j < ((LinearLayout) v).getChildCount(); j++) {
                    View vi = ((LinearLayout) v).getChildAt(j);
                    if (vi instanceof TextView) {
                        ((TextView) vi).setTextColor(black);
                    }
                }
            }
        }
    }

    public void revertRowColor(int id) {
        LinearLayout ll = alarmsView.get().findViewById(id);
        ll.setBackgroundColor(0);
        for (int i = 0; i < ll.getChildCount(); i++) {
            View v = ll.getChildAt(i);
            if (v instanceof TextView) {
                ((TextView) v).setTextColor(white);
            } else if (v instanceof LinearLayout) {
                for (int j = 0; j < ((LinearLayout) v).getChildCount(); j++) {
                    View vi = ((LinearLayout) v).getChildAt(j);
                    if (vi instanceof TextView) {
                        Log.d("MSG", "");
                        ((TextView) vi).setTextColor(white);
                    }
                }
            }
        }
    }

    public void setText(int id, String s) {
        TextView tv = alarmsView.get().findViewById(id);
        tv.setText(s);
    }
}