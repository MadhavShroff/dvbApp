package com.dvbinventek.dvbapp.viewPager;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.Html;
import android.text.Spanned;
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
import com.dvbinventek.dvbapp.StandbyFragment;
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

    //TODO: Do not permit Max value to be lower than min value

    public static Set<String> resetList = new HashSet<>();
    public static List<Integer> highlightedList = new ArrayList<>();
    public static int countdown = 30;
    public static boolean countdownStarted = false;
    public final int white = Color.parseColor("#ffffff");
    public final int black = Color.parseColor("#000000");
    public final int lightGray = Color.parseColor("#909090");
    public WeakReference<View> alarmsView;
    public MediaPlayer mp;
    public CompositeDisposable disposables = new CompositeDisposable();
    public Disposable disposable;
    Observable<Long> observable = Observable.interval(0, 1000, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread());
    public static Observer<String> hpaObserver;
    public AlarmLimitsBottomSheet abs_p;
    Observer<Long> observer = new Observer<Long>() {
        @Override
        public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {
            disposable = d;
            disposables.add(d);
            setText(R.id.limits_saveChanges, alarmsView.get().getResources().getString(R.string.confirm_, 30));
        }

        @Override
        public void onNext(@io.reactivex.rxjava3.annotations.NonNull Long aLong) {
            setText(R.id.limits_saveChanges, alarmsView.get().getResources().getString(R.string.confirm_, 30 - aLong));
        }

        @Override
        public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
            e.printStackTrace();
        }

        @Override
        public void onComplete() {
            resetChanges();
            countdownStarted = false;
            setText(R.id.limits_saveChanges, alarmsView.get().getResources().getString(R.string.confirm));
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
        ((TextView) view.findViewById(R.id.minVolText)).setText(Html.fromHtml("MV<sub><small>total</small></sub>"));
        ((TextView) view.findViewById(R.id.p_unit)).setText(Html.fromHtml("cm H<sub><small>2</small></sub>O"));
        ((TextView) view.findViewById(R.id.vt_alarm_limits)).setText(Html.fromHtml("V<sub><small>t</small></sub>"));

        //Set Past Session Values
        setPastSessionValues();

        setOnClickListeners();

        return view;
    }

    public void setPastSessionValues() {
        StaticStore.AlarmLimits.new_minVolMax = StaticStore.AlarmLimits.minVolMax;
        StaticStore.AlarmLimits.new_minVolMin = StaticStore.AlarmLimits.minVolMin;
        StaticStore.AlarmLimits.new_rateMax = StaticStore.AlarmLimits.rateMax;
        StaticStore.AlarmLimits.new_rateMin = StaticStore.AlarmLimits.rateMin;
        StaticStore.AlarmLimits.new_vtMax = StaticStore.AlarmLimits.vtMax;
        StaticStore.AlarmLimits.new_vtMin = StaticStore.AlarmLimits.vtMin;
        StaticStore.AlarmLimits.new_pMax = StaticStore.AlarmLimits.pMax;
        StaticStore.AlarmLimits.new_pMin = StaticStore.AlarmLimits.pMin;
        StaticStore.AlarmLimits.new_apnea = StaticStore.AlarmLimits.apnea;

        setText(R.id.limits_minVolMax, String.valueOf(StaticStore.AlarmLimits.minVolMax == 0 ? "0" : StaticStore.AlarmLimits.minVolMax));
        setText(R.id.limits_minVolMin, String.valueOf(StaticStore.AlarmLimits.minVolMin == 0 ? "0" : StaticStore.AlarmLimits.minVolMin));
        setText(R.id.limits_ftotalMax, String.valueOf(StaticStore.AlarmLimits.rateMax == 0 ? "0" : StaticStore.AlarmLimits.rateMax));
        setText(R.id.limits_ftotalMin, String.valueOf(StaticStore.AlarmLimits.rateMin == 0 ? "0" : StaticStore.AlarmLimits.rateMin));
        setText(R.id.limits_vtMax, String.valueOf(StaticStore.AlarmLimits.vtMax == 0 ? "0" : StaticStore.AlarmLimits.vtMax));
        setText(R.id.limits_vtMin, String.valueOf(StaticStore.AlarmLimits.vtMin == 0 ? "0" : StaticStore.AlarmLimits.vtMin));
        setText(R.id.limits_pMax, String.valueOf(StaticStore.AlarmLimits.pMax == 0 ? "0" : StaticStore.AlarmLimits.pMax));
        setText(R.id.limits_pMin, String.valueOf(StaticStore.AlarmLimits.pMin == 0 ? "0" : StaticStore.AlarmLimits.pMin));
        setText(R.id.limits_apnea, String.valueOf(StaticStore.AlarmLimits.apnea == 0 ? "0" : StaticStore.AlarmLimits.apnea));

        //setup hpa Unit change observer
        hpaObserver = new Observer<String>() {
            @Override
            public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {
            }

            @Override
            public void onNext(@io.reactivex.rxjava3.annotations.NonNull String s) {
                if (s.equals("hpa")) {
                    setText(R.id.p_unit, "hPa");
                    abs_p.setSubHeading(Html.fromHtml("P (hPa)"));
                } else {
                    setText(R.id.p_unit, Html.fromHtml("cm H<small><sub>2</sub></small>O"));
                    abs_p.setSubHeading(Html.fromHtml("P (cm H<sub><small>2</small></sub>O)"));
                }
            }

            @Override
            public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                e.printStackTrace();
            }

            @Override
            public void onComplete() {
            }
        };
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (disposables != null)
            if (!disposables.isDisposed())
                disposables.dispose();
    }

    public void setOnClickListeners() {
        AlarmLimitsBottomSheet abs_minVol = new AlarmLimitsBottomSheet(getActivity(), Html.fromHtml("MV<sub><small>total</small></sub> (l)"), "minvol", "0 to 50", "0 to 50");
        AlarmLimitsBottomSheet abs_rate = new AlarmLimitsBottomSheet(getActivity(), Html.fromHtml("Rate (b/min)"), "rate", "0 to 70", "0 to 70");
        AlarmLimitsBottomSheet abs_vt = new AlarmLimitsBottomSheet(getActivity(), Html.fromHtml("Vt (ml)"), "vt", "0 to 3000", "0 to 3000");
        abs_p = new AlarmLimitsBottomSheet(getActivity(), Html.fromHtml("P (cm H<sub><small>2</small></sub>0)"), "p", "0 to 80", "0 to 80");
        AlarmLimitsBottomSheetApnea abs_apnea = new AlarmLimitsBottomSheetApnea(getActivity(), Html.fromHtml("Apnea Time (s)"), "5 to 60");

        ImageButton minVol = alarmsView.get().findViewById(R.id.limits_minvol_change);
        ImageButton ftotal = alarmsView.get().findViewById(R.id.limits_ftotal_change);
        ImageButton vt = alarmsView.get().findViewById(R.id.limits_vt_change);
        ImageButton p = alarmsView.get().findViewById(R.id.limits_p_change);
        ImageButton apnea = alarmsView.get().findViewById(R.id.limits_apnea_change);
        Button saveChanges = alarmsView.get().findViewById(R.id.limits_saveChanges);

        minVol.setOnClickListener(v -> abs_minVol.show(true));
        ftotal.setOnClickListener(v -> abs_rate.show(true));
        vt.setOnClickListener(v -> abs_vt.show(true));
        p.setOnClickListener(v -> abs_p.show(true));
        apnea.setOnClickListener(v -> abs_apnea.show(true));

        abs_minVol.setOnDismissListener(bottomSheet -> {
            String max = abs_minVol.getMax();
            String min = abs_minVol.getMin();
            if ((min.equals("") && max.equals("")) || !abs_minVol.isDone) return;
            setText(R.id.limits_minVolMin, min);
            setText(R.id.limits_minVolMax, max);
            float max_ = Float.parseFloat(max);
            float min_ = Float.parseFloat(min);
            if (StaticStore.AlarmLimits.new_minVolMax != max_ || StaticStore.AlarmLimits.new_minVolMin != min_) {
                abs_minVol.setHint();
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
            byte max_ = Byte.parseByte(max);
            byte min_ = Byte.parseByte(min);
            if (StaticStore.AlarmLimits.new_rateMax != max_ || StaticStore.AlarmLimits.new_rateMin != min_) {
                abs_rate.setHint();
                highlight(R.id.limits_ftotal, true);
                resetList.add("ftotal");
                try { // commit alarm limits
                    if (max.equals("-")) StaticStore.AlarmLimits.new_rateMax = 0;
                    else StaticStore.AlarmLimits.new_rateMax = max_;
                    if (min.equals("-")) StaticStore.AlarmLimits.new_rateMin = 0;
                    else StaticStore.AlarmLimits.new_rateMin = min_;
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
                abs_vt.setHint();
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
            float max_ = Float.parseFloat(max);
            float min_ = Float.parseFloat(min);
            if (StaticStore.AlarmLimits.new_pMax != max_ || StaticStore.AlarmLimits.new_pMin != min_) {
                abs_p.setHint();
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
                abs_apnea.setHint();
                highlight(R.id.limits_apnea_row, true);
                resetList.add("apnea");
                try {
                    if (apnea_.equals("-")) StaticStore.AlarmLimits.new_apnea = 0;
                    else StaticStore.AlarmLimits.new_apnea = value_;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Log.d("MSG", "packet value equals entered value");
            }
        });
        saveChanges.setOnClickListener(v -> {
            Observable.just(2L).subscribe(StandbyFragment.confirmButtonClickObserver);
            StaticStore.AlarmLimits.minVolMax = StaticStore.AlarmLimits.new_minVolMax;
            StaticStore.AlarmLimits.minVolMin = StaticStore.AlarmLimits.new_minVolMin;
            StaticStore.AlarmLimits.rateMax = StaticStore.AlarmLimits.new_rateMax;
            StaticStore.AlarmLimits.rateMin = StaticStore.AlarmLimits.new_rateMin;
            StaticStore.AlarmLimits.vtMax = StaticStore.AlarmLimits.new_vtMax;
            StaticStore.AlarmLimits.vtMin = StaticStore.AlarmLimits.new_vtMin;
            StaticStore.AlarmLimits.pMax = StaticStore.AlarmLimits.new_pMax;
            StaticStore.AlarmLimits.pMin = StaticStore.AlarmLimits.new_pMin;
            StaticStore.AlarmLimits.apnea = StaticStore.AlarmLimits.new_apnea;

            SharedPreferences.Editor editor = Objects.requireNonNull(getContext()).getSharedPreferences("dvbVentilator", Context.MODE_PRIVATE).edit();
            editor.putString("limits_minVolMax", "" + StaticStore.AlarmLimits.minVolMax);
            editor.putString("limits_minVolMin", "" + StaticStore.AlarmLimits.minVolMin);
            editor.putString("limits_fTotalMax", "" + StaticStore.AlarmLimits.rateMax);
            editor.putString("limits_fTotalMin", "" + StaticStore.AlarmLimits.rateMin);
            editor.putString("limits_vtMax", "" + StaticStore.AlarmLimits.vtMax);
            editor.putString("limits_vtMi", "" + StaticStore.AlarmLimits.vtMin);
            editor.putString("limits_pMax", "" + StaticStore.AlarmLimits.pMax);
            editor.putString("limits_pMin", "" + StaticStore.AlarmLimits.pMin);
            editor.putString("limits_apnea", "" + StaticStore.AlarmLimits.apnea);
            editor.apply();

            SendPacket sp = new SendPacket();
            sp.writeDefaultSTRTPacketValues(SendPacket.ALRM);
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
                    StaticStore.AlarmLimits.new_rateMin = StaticStore.AlarmLimits.rateMin;
                    StaticStore.AlarmLimits.new_rateMax = StaticStore.AlarmLimits.rateMax;
                    setText(R.id.limits_ftotalMin, String.valueOf(StaticStore.AlarmLimits.rateMin));
                    setText(R.id.limits_ftotalMax, String.valueOf(StaticStore.AlarmLimits.rateMax));
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
        Log.d("MSG", "resetChanges() : " + resetList);
        setText(R.id.limits_saveChanges, alarmsView.get().getResources().getString(R.string.confirm));
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
                        if (i == 0 && j == 1)
                            ((TextView) vi).setTextColor(lightGray); // set color of unit to light gray
                        else ((TextView) vi).setTextColor(white); // rest of the text to white

                    }
                }
            }
        }
    }

    public void setText(int id, String s) {
        TextView tv = alarmsView.get().findViewById(id);
        tv.setText(s);
    }

    public void setText(int id, Spanned s) {
        TextView tv = alarmsView.get().findViewById(id);
        tv.setText(s);
    }
}