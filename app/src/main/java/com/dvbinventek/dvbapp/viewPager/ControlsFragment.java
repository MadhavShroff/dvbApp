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
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.dvbinventek.dvbapp.MainActivity;
import com.dvbinventek.dvbapp.R;
import com.dvbinventek.dvbapp.SendPacket;
import com.dvbinventek.dvbapp.StaticStore;
import com.dvbinventek.dvbapp.bottomSheets.ControlsBottomSheet;
import com.dvbinventek.dvbapp.bottomSheets.IEControlsBottomSheet;
import com.dvbinventek.dvbapp.bottomSheets.MenuControlsBottomSheet;
import com.dvbinventek.dvbapp.customViews.ControlRow;
import com.google.android.material.imageview.ShapeableImageView;

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
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

import static android.content.Context.VIBRATOR_SERVICE;

public class ControlsFragment extends Fragment {
    public static boolean countdownStarted = false;
    public static Set<String> resetList = new HashSet<>(); // Set of all controls that need to be reset.
    public static List<Integer> highlightedList = new ArrayList<>();
    public final int white = Color.parseColor("#ffffff");
    public final int black = Color.parseColor("#000000");
    public ControlsBottomSheet cbs_fio2, cbs_vt, cbs_vtrig, cbs_cpap, cbs_pip, cbs_delps, cbs_ratef, cbs_tinsp, cbs_pmax;
    public IEControlsBottomSheet cbs_ie;
    public short new_mode;
    public String new_modeString;
    public MediaPlayer mp;
    public Disposable disposable;
    int countdown = 30;
    //TODO: Optimize lookups
    WeakReference<View> controlsView;
    WeakReference<ControlRow> fio2, vt, pinsp, plimit, peep, ps, vtrig, tinsp, ie, rtotal;
    WeakReference<MenuControlsBottomSheet> modeBottomSheet;
    Observable<Long> observable = Observable.interval(0, 1, TimeUnit.SECONDS).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).take(60);
    Observer<Long> observer = new Observer<Long>() {
        @Override
        public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {
            disposable = d;
            setText(R.id.highlight_text, controlsView.get().getResources().getString(R.string.warning_text, 30));
        }

        @Override
        public void onNext(@io.reactivex.rxjava3.annotations.NonNull Long aLong) {
            setText(R.id.highlight_text, controlsView.get().getResources().getString(R.string.warning_text, 30 - aLong));
        }

        @Override
        public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
            e.printStackTrace();
        }

        @Override
        public void onComplete() {
            resetChanges();
            countdownStarted = false;
        }
    };

    public String getIE(int ie) {
        String s = "";
        int i = ie / 1000;
        int id = (ie / 100) % 10;
        int e = (ie / 10) % 10;
        int ed = ie % 10;
        if (id == 0 && ed == 0) {
            s = i + ":" + e;
        } else if (id == 0 && ed != 0) {
            s = i + ":" + e + "." + ed;
        } else if (id != 0 && ed == 0) {
            s = i + "." + id + ":" + e;
        } else {
            s = i + "." + id + ":" + e + "." + ed;
        }
        return s;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        tryToDispose(disposable);
    }

    public void tryToDispose(Disposable disposable) {
        if (disposable != null)
            if (!disposable.isDisposed())
                disposable.dispose();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_controls, container, false);
        mp = MediaPlayer.create(getContext(), R.raw.button_press);
        Log.d("MSG", "ControlsFragment::onCreateView called");
        controlsView = new WeakReference<>(view);
        fio2 = new WeakReference<>(view.findViewById(R.id.controls_fio2));
        vt = new WeakReference<>(view.findViewById(R.id.controls_vt));
        pinsp = new WeakReference<>(view.findViewById(R.id.controls_pinsp));
        plimit = new WeakReference<>(view.findViewById(R.id.controls_plimit));
        peep = new WeakReference<>(view.findViewById(R.id.controls_peep));
        ps = new WeakReference<>(view.findViewById(R.id.controls_ps));
        vtrig = new WeakReference<>(view.findViewById(R.id.controls_vtrig));
        tinsp = new WeakReference<>(view.findViewById(R.id.controls_tinsp));
        ie = new WeakReference<>(view.findViewById(R.id.controls_ie));
        rtotal = new WeakReference<>(view.findViewById(R.id.controls_rtotal));

        //Set subscript text for units and control labels and Default Controls
        setSubscriptAndPastSessionValues();

        //instantialize BottomSheet
        initializeBottomSheetDialogs();

        //sets control rows visible for only those views needed as per mode selected
        setControlsForMode();

        //Set click listeners for all buttons on controls screen
        setClickListeners();

        return view;
    }

    public void initializeBottomSheetDialogs() {
        if (getActivity() == null) throw new NullPointerException();
        cbs_fio2 = new ControlsBottomSheet(getActivity(), Html.fromHtml("Set FiO<small><sub>2</sub></small> (%)"), "fio2");
        cbs_fio2.setSubText("21 to 100"); // no decimal
        cbs_vt = new ControlsBottomSheet(getActivity(), Html.fromHtml("Set V<small><sub>t</sub></small> (ml)"), "vt");
        cbs_vt.setSubText("50 to 1500");
        cbs_vtrig = new ControlsBottomSheet(getActivity(), Html.fromHtml("Set Flow<small><sub>trig</sub></small> (lpm)"), "vtrig");
        cbs_vtrig.setSubText("1(least effort) to 20(greatest effort)");
        cbs_cpap = new ControlsBottomSheet(getActivity(), Html.fromHtml("Set PEEP (cm H<small><sub>2</sub></small>O)"), "cpap");
        cbs_cpap.setSubText(Html.fromHtml("0 to 30 (>= P<small><sub>insp</sub></small>)"));
        cbs_pip = new ControlsBottomSheet(getActivity(), Html.fromHtml("Set P<small><sub>insp</sub></small> (cm H<small><sub>2</sub></small>O)"), "pip");
        cbs_pip.setSubText(Html.fromHtml("0 to 60 (>= PEEP)"));
        cbs_delps = new ControlsBottomSheet(getActivity(), Html.fromHtml("Set PS (cm H<small><sub>2</sub></small>O)"), "delps");
        cbs_delps.setSubText(Html.fromHtml("5 to 45"));
        cbs_ratef = new ControlsBottomSheet(getActivity(), Html.fromHtml("Set Rate (b/min)"), "ratef");
        cbs_ratef.setSubText("4 to 60 b/min");
        cbs_ie = new IEControlsBottomSheet(getActivity(), " 1 to 4 : 1 to 4");
        cbs_tinsp = new ControlsBottomSheet(getActivity(), Html.fromHtml("Set T<small><sub>insp</sub></small> (s)"), "tinsp");
        cbs_tinsp.setSubText("0.3 to 6s");
        cbs_pmax = new ControlsBottomSheet(getActivity(), Html.fromHtml("Set P<small><sub>limit</sub></small> (cm H<small><sub>2</sub></small>O)"), "pmax");
        cbs_pmax.setSubText(Html.fromHtml("PEEP+1 to 60"));

    }

    // Sets the behavior of how controls behave with one another,
    public void setClickListeners() {
        //Send button onClickListener
        Button send = controlsView.get().findViewById(R.id.send);
        send.setOnClickListener(v -> {
            StaticStore.packet_fio2 = StaticStore.new_packet_fio2;
            StaticStore.packet_vt = StaticStore.new_packet_vt;
            StaticStore.packet_ie = StaticStore.new_packet_ie;
            StaticStore.packet_pinsp = StaticStore.new_packet_pinsp;
            StaticStore.packet_vtrig = StaticStore.new_packet_vtrig;
            StaticStore.packet_peep = StaticStore.new_packet_peep;
            StaticStore.packet_ps = StaticStore.new_packet_ps;
            StaticStore.packet_rtotal = StaticStore.new_packet_rtotal;
            StaticStore.packet_tinsp = StaticStore.new_packet_tinsp;
            StaticStore.packet_plimit = StaticStore.new_packet_plimit;
            new_mode = StaticStore.modeSelectedShort;
            new_modeString = StaticStore.modeSelected;
            SendPacket sp = new SendPacket();
            sp.writeInfo((short) (StaticStore.packet_fio2 * 100), 17);
            sp.writeInfo(StaticStore.modeSelectedShort, 1);
            sp.writeInfo(StaticStore.packet_vt, 5);
            sp.writeInfo((short) (StaticStore.packet_vtrig * 100), 10);
            sp.writeInfo((short) (StaticStore.packet_peep * 100), 4);
            sp.writeInfo((short) (StaticStore.packet_pinsp * 100), 3);
            sp.writeInfo((short) (StaticStore.packet_ps * 100), 2);
            sp.writeInfo((short) (StaticStore.packet_rtotal * 100f), 6);
            sp.writeInfo(StaticStore.packet_ie, 7);
            //TODO: REMOVE
            sp.writeInfo((short) ((int) StaticStore.AlarmLimits.apnea * 1000), 39);
            sp.writeInfo((short) ((int) StaticStore.AlarmLimits.minVolMin * 100), 40);
            sp.writeInfo((short) ((int) StaticStore.AlarmLimits.minVolMax * 100), 41);
            sp.writeInfo(StaticStore.AlarmLimits.vtMin, 42);
            sp.writeInfo(StaticStore.AlarmLimits.vtMax, 43);
            sp.writeInfo((short) ((int) StaticStore.AlarmLimits.pMin * 100), 44);
            if (StaticStore.modeSelectedShort == 19) // in PRVC mode, send entered value, in all other modes, send value from alarm limits
                sp.writeInfo((short) ((int) StaticStore.packet_plimit * 100), 45);
            else
                sp.writeInfo((short) ((int) StaticStore.AlarmLimits.pMax * 100), 45);
            sp.writeInfo((short) ((int) StaticStore.AlarmLimits.fTotalMin * 100), 46);
            sp.writeInfo((short) ((int) StaticStore.AlarmLimits.fTotalMax * 100), 47);

            // add committed values to SharedPreferances, to use at startup
            SharedPreferences.Editor editor = Objects.requireNonNull(getContext()).getSharedPreferences("dvbVentilator", Context.MODE_PRIVATE).edit();
            editor.putString("packet_fio2", "" + StaticStore.packet_fio2);
            editor.putString("packet_vt", "" + StaticStore.packet_vt);
            editor.putString("packet_vtrig", "" + StaticStore.packet_vtrig);
            editor.putString("packet_peep", "" + StaticStore.packet_peep);
            editor.putString("packet_pip", "" + StaticStore.packet_pinsp);
            editor.putString("packet_ps", "" + StaticStore.packet_ps);
            editor.putString("packet_ratef", "" + StaticStore.packet_rtotal);
            editor.putString("packet_ie", "" + StaticStore.packet_ie);
            editor.putString("packet_tinsp", "" + StaticStore.packet_tinsp);
            editor.putString("packet_pmax", "" + StaticStore.packet_plimit);
            editor.putString("mode_selected", StaticStore.modeSelected);
            editor.putString("mode_selected_short", Short.toString(StaticStore.modeSelectedShort));
            // send constructed packet to device
            sp.sendToDevice();
            // play sound and vibration
            mp.start();
            Vibrator myVib = (Vibrator) getContext().getSystemService(VIBRATOR_SERVICE);
            myVib.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
            editor.apply();
            tryToDispose(disposable);
            resetChanges();
            countdownStarted = false;
        });

        WeakReference<MenuControlsBottomSheet> modeBottomSheet = new WeakReference<>(new MenuControlsBottomSheet(Objects.requireNonNull(getActivity())));
        //Mode button onClickListener
        ImageButton mode = controlsView.get().findViewById(R.id.controls_mode_change);
        mode.setOnClickListener(v -> {
            if (modeBottomSheet.get() == null) return;
            modeBottomSheet.get().show(true);
            modeBottomSheet.get().setOnDismissListener(bottomSheet1 -> {
                if (StaticStore.modeSelectedShort != new_mode) {
                    highlight(R.id.controls_mode, true);
                    resetList.add("mode");
                    setText(R.id.controls_mode_current, StaticStore.modeSelected);
                } else {
                    revertRowColor(R.id.controls_mode);
                    tryToDispose(disposable);
                    setText(R.id.highlight_text, "");
                    StaticStore.modeSelected = this.new_modeString;
                    StaticStore.modeSelectedShort = this.new_mode;
                    setText(R.id.controls_mode_current, StaticStore.modeSelected);
                    countdownStarted = false;
                }
                setControlsForMode();
            });
        });
        fio2.get().setButtonClickListener(v -> {
            cbs_fio2.show(true);
            cbs_fio2.clearValue();
            cbs_fio2.setOnDismissListener(bottomSheet -> {
                if (cbs_fio2.canceled) return;
                String s = cbs_fio2.getValue();
                short sh;
                sh = Short.parseShort(s);
                if (StaticStore.packet_fio2 != sh) {
                    highlight(R.id.controls_fio2, true);
                    resetList.add("fio2");
                    fio2.get().setCurrent(s);
                    StaticStore.new_packet_fio2 = sh;
                } else {
                    Log.d("MSG", "packet value equals entered value");
                }
            });
        });
        tinsp.get().setButtonClickListener(v -> {
            cbs_tinsp.show(true);
            cbs_tinsp.clearValue();
            cbs_tinsp.setOnDismissListener(bottomSheet -> {
                if (cbs_tinsp.canceled) {
                    return;
                }
                String s = cbs_tinsp.getValue();
                float ti;
                try {
                    ti = (float) round2(Float.parseFloat(s));
                } catch (Exception e) {
                    return;
                }
                if (StaticStore.packet_tinsp != ti) {
                    highlight(R.id.controls_tinsp, true);
                    resetList.add("tinsp");
                    tinsp.get().setCurrent(s);
                    StaticStore.new_packet_tinsp = ti;
                } else {
                    Log.d("MSG", "packet value equals entered value");
                }
                if (StaticStore.new_packet_ie != 0 && ti != 0f) {
                    float i = ((int) StaticStore.new_packet_ie / 100) / 10.0f;
                    float e = ((int) StaticStore.new_packet_ie % 100) / 10.0f;
                    float r = (float) round2(60.0f / (ti + ti * (e / i)));
                    if (StaticStore.new_packet_rtotal != r) {
                        highlight(R.id.controls_rtotal, true);
                        resetList.add("ratef");
                        StaticStore.new_packet_rtotal = (float) round2(r);
                        rtotal.get().setCurrent(String.valueOf(r));
                    } else {
                        Log.d("MSG", "packet value equals entered value");
                    }
                } else if (ti != 0 && StaticStore.new_packet_rtotal != 0) {
                    float texp = (60.0f / StaticStore.new_packet_rtotal) - ti;
                    short ie_ = (short) (1000 + (texp / ti) * 10);
                    if (StaticStore.new_packet_ie != ie_) {
                        highlight(R.id.controls_ie, true);
                        resetList.add("ie");
                        StaticStore.new_packet_ie = ie_;
                        ie.get().setCurrent(getIE(ie_));
                    } else {
                        Log.d("MSG", "packet value equals entered value");
                    }
                }
            });
        });
        peep.get().setButtonClickListener(v -> {
            cbs_cpap.show(true);
            cbs_cpap.clearValue();
            cbs_cpap.setOnDismissListener(bottomSheet -> {
                if (cbs_cpap.canceled) return;
                String s = cbs_cpap.getValue();
                float sh;
                sh = Float.parseFloat(s);
                if (StaticStore.packet_peep != sh) {
                    highlight(R.id.controls_peep, true);
                    resetList.add("cpap");
                    peep.get().setCurrent(String.valueOf(sh));
                    StaticStore.new_packet_peep = sh;
                } else {
                    Log.d("MSG", "packet value equals entered value");
                }
            });
        });
        plimit.get().setButtonClickListener(v -> {
            cbs_pmax.show(true);
            cbs_pmax.clearValue();
            cbs_pmax.setOnDismissListener(bottomSheet -> {
                if (cbs_pmax.canceled) return;
                String s = cbs_pmax.getValue();
                float sh;
                sh = Float.parseFloat(s);
                if (StaticStore.packet_plimit != sh) {
                    highlight(R.id.controls_plimit, true);
                    resetList.add("pmax");
                    plimit.get().setCurrent(s);
                    StaticStore.new_packet_plimit = sh;
                } else {
                    Log.d("MSG", "pmax packet value equals entered value");
                }
            });
        });
        ps.get().setButtonClickListener(v -> {
            cbs_delps.show(true);
            cbs_delps.clearValue();
            cbs_delps.setOnDismissListener(bottomSheet -> {
                if (cbs_delps.canceled) return;
                String s = cbs_delps.getValue();
                float sh;
                sh = Float.parseFloat(s);
                if (StaticStore.packet_ps != sh) {
                    highlight(R.id.controls_ps, true);
                    resetList.add("delps");
                    ps.get().setCurrent(s);
                    StaticStore.new_packet_ps = sh;
                } else {
                    Log.d("MSG", "delps packet value equals entered value");
                }
            });
        });
        ie.get().setButtonClickListener(v -> {
            cbs_ie.show(true);
            cbs_ie.clearValue();
            cbs_ie.setOnDismissListener(bottomSheet -> {
                if (cbs_ie.canceled) return;
                float i = cbs_ie.getI(), e = cbs_ie.getE();
                if (i == 0f || e == 0f) return;
                short ie_ = (short) (i * 1000 + e * 10);
                if (StaticStore.new_packet_ie != ie_) {
                    highlight(R.id.controls_ie, true);
                    resetList.add("ie");
                    StaticStore.new_packet_ie = ie_;
                    ie.get().setCurrent(getIE(ie_));
                } else {
                    Log.d("MSG", "ie packet value equals entered value");
                }
                if (StaticStore.new_packet_rtotal != 0 && i != 0 && e != 0) {
                    float ti = (float) round2((60.0f / StaticStore.new_packet_rtotal) * (i / (i + e)));
                    if (StaticStore.new_packet_tinsp != ti) {
                        highlight(R.id.controls_tinsp, true);
                        resetList.add("tinsp");
                        tinsp.get().setCurrent(String.valueOf(ti));
                        StaticStore.new_packet_tinsp = ti;
                    } else {
                        Log.d("MSG", "packet value equals entered value");
                    }
                } else if (StaticStore.new_packet_tinsp != 0 && i != 0 && e != 0) {
                    float r = (float) round2(60.0f / (StaticStore.new_packet_tinsp + StaticStore.new_packet_tinsp * (e / i)));
                    if (StaticStore.new_packet_rtotal != r) {
                        highlight(R.id.controls_rtotal, true);
                        resetList.add("ratef");
                        StaticStore.new_packet_rtotal = (float) r;
                        rtotal.get().setCurrent(String.valueOf(r));
                    } else {
                        Log.d("MSG", "packet value equals entered value");
                    }
                }
                Log.d("MSG", "IE set: " + StaticStore.packet_ie);
            });
        });
        rtotal.get().setButtonClickListener(v -> {
            cbs_ratef.show(true);
            cbs_ratef.clearValue();
            cbs_ratef.setOnDismissListener(bottomSheet -> {
                if (cbs_ratef.canceled) return;
                String s = cbs_ratef.getValue();
                float r;
                try {
                    r = (float) round2(Float.parseFloat(s));
                } catch (Exception e) {
                    return;
                }
                if (StaticStore.new_packet_rtotal != r) {
                    highlight(R.id.controls_rtotal, true);
                    resetList.add("ratef");
                    StaticStore.new_packet_rtotal = (float) round2(r);
                    rtotal.get().setCurrent(String.valueOf(r));
                } else {
                    Log.d("MSG", "packet value equals entered value");
                }
                if (StaticStore.new_packet_ie != 0 && r != 0) {
                    float i = (float) ((int) StaticStore.new_packet_ie / 100) / 10.0f;
                    float e = (StaticStore.new_packet_ie % 100) / 10.0f;
                    float ti = (float) round2((60.0f / r) * (i / (i + e)));
                    if (StaticStore.new_packet_tinsp != ti) {
                        highlight(R.id.controls_tinsp, true);
                        resetList.add("tinsp");
                        tinsp.get().setCurrent(String.valueOf(ti));
                        StaticStore.new_packet_tinsp = ti;
                    } else {
                        Log.d("MSG", "packet value equals entered value");
                    }
                } else if (StaticStore.new_packet_tinsp != 0 && r != 0) {
                    float texp = (60.0f / r) - StaticStore.new_packet_tinsp;
                    short ie_ = (short) (1000 + (texp / StaticStore.new_packet_tinsp) * 10);
                    if (StaticStore.new_packet_ie != ie_) {
                        highlight(R.id.controls_ie, true);
                        resetList.add("ie");
                        StaticStore.new_packet_ie = ie_;
                        ie.get().setCurrent(getIE(ie_));
                    } else {
                        Log.d("MSG", "packet value equals entered value");
                    }
                }
            });
        });
        pinsp.get().setButtonClickListener(v -> {
            cbs_pip.show(true);
            cbs_pip.clearValue();
            cbs_pip.setOnDismissListener(bottomSheet -> {
                if (cbs_pip.canceled) return;
                String s = cbs_pip.getValue();
                float sh = Float.parseFloat(s);
                if (StaticStore.new_packet_pinsp != sh) {
                    highlight(R.id.controls_pinsp, true);
                    resetList.add("pip");
                    pinsp.get().setCurrent(s);
                    StaticStore.new_packet_pinsp = sh;
                } else {
                    Log.d("MSG", "pip packet value equals entered value");
                }
            });
        });
        vt.get().setButtonClickListener(v -> {
            cbs_vt.show(true);
            cbs_vt.clearValue();
            cbs_vt.setOnDismissListener(bottomSheet -> {
                if (cbs_vt.canceled) return;
                String s = cbs_vt.getValue();
                short sh = Short.parseShort(s);
                if (StaticStore.new_packet_vt != sh) {
                    highlight(R.id.controls_vt, true);
                    resetList.add("vt");
                    vt.get().setCurrent(s);
                    StaticStore.new_packet_vt = sh;
                } else {
                    Log.d("MSG", "vt packet value equals entered value");
                }
            });
        });
        vtrig.get().setButtonClickListener(v -> {
            cbs_vtrig.show(true);
            cbs_vtrig.clearValue();
            cbs_vtrig.setOnDismissListener(bottomSheet -> {
                if (cbs_vtrig.canceled) return;
                String s = cbs_vtrig.getValue();
                float sh = Float.parseFloat(s);
                if (StaticStore.new_packet_vtrig != sh) {
                    highlight(R.id.controls_vtrig, true);
                    resetList.add("vtrig");
                    vtrig.get().setCurrent(s);
                    StaticStore.new_packet_vtrig = sh;
                } else {
                    Log.d("MSG", "vtrig packet value equals entered value");
                }
            });
        });

        Button stopVentilation = controlsView.get().findViewById(R.id.stopVentilation);
        stopVentilation.setOnClickListener(v -> {
            Observable.just(v).subscribe(MainActivity.standbyClickObserver);
        });
        ShapeableImageView up = controlsView.get().findViewById(R.id.swipeup);
        up.setOnClickListener(v -> {
            ScrollView sv = controlsView.get().findViewById(R.id.controls_scrollable);
            sv.smoothScrollTo(0, 500);
        });
    }

    public void setSubscriptAndPastSessionValues() {
        //Set subscript text for units and control labels and Default Controls
        //Set Static Store temp values to most recently comitted values
        StaticStore.new_packet_fio2 = StaticStore.packet_fio2;
        StaticStore.new_packet_vt = StaticStore.packet_vt;
        StaticStore.new_packet_ie = StaticStore.packet_ie;
        StaticStore.new_packet_pinsp = StaticStore.packet_pinsp;
        StaticStore.new_packet_vtrig = StaticStore.packet_vtrig;
        StaticStore.new_packet_peep = StaticStore.packet_peep;
        StaticStore.new_packet_ps = StaticStore.packet_ps;
        StaticStore.new_packet_rtotal = StaticStore.packet_rtotal;
        StaticStore.new_packet_tinsp = StaticStore.packet_tinsp;
        StaticStore.new_packet_plimit = StaticStore.packet_plimit;
        new_mode = StaticStore.modeSelectedShort;
        new_modeString = StaticStore.modeSelected;

        plimit.get().setUnit(Html.fromHtml("cm H<small><sub>2</sub></small>O"));
        ps.get().setUnit(Html.fromHtml("cm H<small><sub>2</sub></small>O"));
        pinsp.get().setUnit(Html.fromHtml("cm H<small><sub>2</sub></small>O"));
        peep.get().setUnit(Html.fromHtml("cm H<small><sub>2</sub></small>O"));
        fio2.get().setLabel(Html.fromHtml("FiO<small><sub>2</sub></small>"));
        vtrig.get().setLabel(Html.fromHtml("Flow<small><sub>trig</sub></small>"));
        tinsp.get().setLabel(Html.fromHtml("T<small><sub>insp</sub></small>"));
        pinsp.get().setLabel(Html.fromHtml("P<small><sub>insp</sub></small>"));

        TextView tv = controlsView.get().findViewById(R.id.controls_mode_current);
        tv.setText(StaticStore.modeSelected == null ? "-" : StaticStore.modeSelected);
        fio2.get().setCurrent(String.valueOf(StaticStore.packet_fio2 == 0 ? "0" : StaticStore.packet_fio2));
        vt.get().setCurrent(String.valueOf(StaticStore.packet_vt == 0 ? "0" : StaticStore.packet_vt));
        vtrig.get().setCurrent(String.valueOf(StaticStore.packet_vtrig == 0 ? "0" : StaticStore.packet_vtrig));
        pinsp.get().setCurrent(String.valueOf(StaticStore.packet_pinsp == 0 ? "0" : StaticStore.packet_pinsp));
        peep.get().setCurrent(String.valueOf(StaticStore.packet_peep == 0 ? "0" : StaticStore.packet_peep));
        ps.get().setCurrent(String.valueOf(StaticStore.packet_ps == 0 ? "0" : StaticStore.packet_ps));
        rtotal.get().setCurrent(String.valueOf(StaticStore.packet_rtotal == 0 ? "0" : StaticStore.packet_rtotal));
        tinsp.get().setCurrent(String.valueOf(StaticStore.packet_tinsp == 0 ? "0" : StaticStore.packet_tinsp));
        ie.get().setCurrent(String.valueOf(StaticStore.packet_ie == 0 ? "0" : getIE(StaticStore.packet_ie)));
        plimit.get().setCurrent(String.valueOf(StaticStore.packet_plimit == 0 ? "0" : StaticStore.packet_plimit));
        setControlsForMode();
    }
    void setControlsForMode() {
        switch (StaticStore.modeSelected) {
            case "VC-CMV":
                setAllVisible();
                visibility(R.id.controls_pinsp, View.GONE);
                visibility(R.id.controls_vtrig, View.GONE);
                visibility(R.id.controls_ps, View.GONE);
                visibility(R.id.controls_plimit, View.GONE);
                break;
            case "PC-CMV":
                setAllVisible();
                visibility(R.id.controls_vt, View.GONE);
                visibility(R.id.controls_vtrig, View.GONE);
                visibility(R.id.controls_ps, View.GONE);
                visibility(R.id.controls_plimit, View.GONE);
                break;
            case "VC-SIMV":
                setAllVisible();
                visibility(R.id.controls_pinsp, View.GONE);
                visibility(R.id.controls_plimit, View.GONE);
                break;
            case "PRVC":
                setAllVisible();
                visibility(R.id.controls_pinsp, View.GONE);
                break;
            case "ACV":
                setAllVisible();
                visibility(R.id.controls_ps, View.GONE);
                visibility(R.id.controls_pinsp, View.GONE);
                visibility(R.id.controls_plimit, View.GONE);
                break;
            case "PSV":
                setAllVisible();
                visibility(R.id.controls_ps, View.GONE);
                visibility(R.id.controls_vt, View.GONE);
                visibility(R.id.controls_tinsp, View.GONE);
                visibility(R.id.controls_rtotal, View.GONE);
                visibility(R.id.controls_ie, View.GONE);
                visibility(R.id.controls_plimit, View.GONE);
                break;
            case "PC-SIMV":
                setAllVisible();
                visibility(R.id.controls_vt, View.GONE);
                visibility(R.id.controls_plimit, View.GONE);
                break;
            case "CPAP":
                setAllVisible();
                visibility(R.id.controls_pinsp, View.GONE);
                visibility(R.id.controls_ps, View.GONE);
                visibility(R.id.controls_vt, View.GONE);
                visibility(R.id.controls_vtrig, View.GONE);
                visibility(R.id.controls_rtotal, View.GONE);
                visibility(R.id.controls_tinsp, View.GONE);
                visibility(R.id.controls_ie, View.GONE);
                visibility(R.id.controls_plimit, View.GONE);
                break;
            case "BPAP":
                setAllVisible();
                visibility(R.id.controls_ps, View.GONE);
                visibility(R.id.controls_tinsp, View.GONE);
                visibility(R.id.controls_vt, View.GONE);
                visibility(R.id.controls_rtotal, View.GONE);
                visibility(R.id.controls_ie, View.GONE);
                visibility(R.id.controls_plimit, View.GONE);
                break;
        }
    }
    public void visibility(int id, int visibility) {
        try {
            View v = controlsView.get().findViewById(id);
            v.setVisibility(visibility);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void setAllVisible() {
        visibility(R.id.controls_fio2, View.VISIBLE);
        visibility(R.id.controls_vt, View.VISIBLE);
        visibility(R.id.controls_vtrig, View.VISIBLE);
        visibility(R.id.controls_pinsp, View.VISIBLE);
        visibility(R.id.controls_peep, View.VISIBLE);
        visibility(R.id.controls_ps, View.VISIBLE);
        visibility(R.id.controls_tinsp, View.VISIBLE);
        visibility(R.id.controls_rtotal, View.VISIBLE);
        visibility(R.id.controls_ie, View.VISIBLE);
        visibility(R.id.controls_plimit, View.VISIBLE);
    }

    public void resetChanges() {
        Log.d("MSG", "resetChanges() called");
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

    public void revertRowColor(int id) {
        if (id == R.id.controls_mode) {
            LinearLayout ll = controlsView.get().findViewById(id);
            ll.setBackgroundColor(0);
            for (int i = 0; i < ll.getChildCount(); i++) {
                View v = ll.getChildAt(i);
                if (v instanceof TextView) {
                    ((TextView) v).setTextColor(white);
                }
            }
        } else {
            ControlRow r = controlsView.get().findViewById(id);
            r.highlight(false);
        }
    }

    public void setText(int id, String s) {
        TextView tv = controlsView.get().findViewById(id);
        tv.setText(s);
    }

    public String cleanNumber(float f) {
        f = (float) round2(f);
        if ((int) (f * 10) % 10 == 0) { // if there is a 0 decimal place
            return String.valueOf((int) f);
        }
        return String.valueOf(f);
    }

    public void resetValues() {
        TextView tv;
        for (String s : resetList) {
            switch (s) {
                case "fio2":
                    StaticStore.new_packet_fio2 = StaticStore.packet_fio2;
                    fio2.get().setCurrent(String.valueOf(StaticStore.packet_fio2));
                    break;
                case "tinsp":
                    StaticStore.new_packet_tinsp = StaticStore.packet_tinsp;
                    tinsp.get().setCurrent(String.valueOf(cleanNumber(StaticStore.packet_tinsp)));
                    break;
                case "ratef":
                    StaticStore.new_packet_rtotal = StaticStore.packet_rtotal;
                    rtotal.get().setCurrent(String.valueOf(cleanNumber(StaticStore.packet_rtotal)));
                    break;
                case "ie":
                    StaticStore.new_packet_ie = StaticStore.packet_ie;
                    ie.get().setCurrent(getIE(StaticStore.packet_ie));
                    break;
                case "cpap":
                    StaticStore.new_packet_peep = StaticStore.packet_peep;
                    peep.get().setCurrent(String.valueOf(cleanNumber(StaticStore.packet_peep)));
                    break;
                case "delps":
                    StaticStore.new_packet_ps = StaticStore.packet_ps;
                    ps.get().setCurrent(String.valueOf(cleanNumber(StaticStore.packet_ps)));
                    break;
                case "pmax":
                    StaticStore.new_packet_plimit = StaticStore.packet_plimit;
                    plimit.get().setCurrent(String.valueOf(cleanNumber(StaticStore.packet_plimit)));
                    break;
                case "pip":
                    StaticStore.new_packet_pinsp = StaticStore.packet_pinsp;
                    pinsp.get().setCurrent(String.valueOf(cleanNumber(StaticStore.packet_pinsp)));
                    break;
                case "vt":
                    StaticStore.new_packet_vt = StaticStore.packet_vt;
                    vt.get().setCurrent(String.valueOf(StaticStore.packet_vt));
                    break;
                case "vtrig":
                    StaticStore.new_packet_vtrig = StaticStore.packet_vtrig;
                    vtrig.get().setCurrent(String.valueOf(cleanNumber(StaticStore.packet_vtrig)));
                    break;
                case "mode":
                    StaticStore.modeSelected = this.new_modeString;
                    StaticStore.modeSelectedShort = this.new_mode;
                    setText(R.id.controls_mode_current, StaticStore.modeSelected);
                    modeBottomSheet.get().clickCorrectModeButton(StaticStore.modeSelectedShort);
                    setControlsForMode();
                    break;
            }
        }
        resetList.clear();
    }

    public double round2(float x) {
        return Math.round(x * 100) / 100.0;
    }

    public void makeRowYellow(int id) {
        if (id == R.id.controls_mode) {
            LinearLayout ll = controlsView.get().findViewById(id);
            ll.setBackgroundColor(Color.parseColor("#ffd600"));
            for (int i = 0; i < ll.getChildCount(); i++) {
                View v = ll.getChildAt(i);
                if (v instanceof TextView) {
                    ((TextView) v).setTextColor(black);
                }
            }
        } else {
            Log.d("MSG", "makeRowYellow -> else");
            ControlRow r = controlsView.get().findViewById(id);
            r.highlight(true);
        }
    }
}
