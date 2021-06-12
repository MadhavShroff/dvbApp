package com.dvbinventek.dvbapp.viewPager;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
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
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.dvbinventek.dvbapp.MainActivity;
import com.dvbinventek.dvbapp.R;
import com.dvbinventek.dvbapp.SendPacket;
import com.dvbinventek.dvbapp.StandbyFragment;
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
    public ControlsBottomSheet cbs_fio2, cbs_vt, cbs_vtrig, cbs_cpap, cbs_pip, cbs_delps, cbs_ratef, cbs_tinsp, cbs_pmax, cbs_flowRate;
    public IEControlsBottomSheet cbs_ie;
    public short new_mode;
    public String new_modeString;
    public MediaPlayer mp;
    public Disposable disposable;
    int countdown = 30;
    //TODO: Optimize lookups
    WeakReference<View> controlsView;
    public static WeakReference<Button> stopVentilation;
    WeakReference<ControlRow> fio2, vt, pinsp, plimit, peep, ps, vtrig, tinsp, ie, rtotal, flowRate;
    WeakReference<MenuControlsBottomSheet> modeBottomSheet;
    Observable<Long> observable = Observable.interval(0, 1, TimeUnit.SECONDS).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).take(60);
    public static Observer<View> revertStandbyClickObserver;
    public static Observer<String> hpaObserver;
    Observer<Long> confirmCountdownObserver = new Observer<Long>() {
        @Override
        public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {
            disposable = d;
            setText(R.id.send, controlsView.get().getResources().getString(R.string.confirm_, 30));
        }

        @Override
        public void onNext(@io.reactivex.rxjava3.annotations.NonNull Long aLong) {
            setText(R.id.send, controlsView.get().getResources().getString(R.string.confirm_, 30 - aLong));
        }

        @Override
        public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
            e.printStackTrace();
        }

        @Override
        public void onComplete() {
            resetChanges();
            countdownStarted = false;
            setText(R.id.send, controlsView.get().getResources().getString(R.string.confirm));
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
        flowRate = new WeakReference<>(view.findViewById(R.id.controls_flowRate));

        //Set subscript text for units and control labels and Default Controls
        setSubscriptAndPastSessionValues();

        //instantialize BottomSheet
        initializeBottomSheetDialogs();

        //Set initial state of standby button
        setInitialState(view);

        //sets control rows visible for only those views needed as per mode selected
        setControlsForMode();

        //Set click listeners for all buttons on controls screen
        setClickListeners();

        return view;
    }

    public void setInitialState(View v) {
        //disable standby button
        v.findViewById(R.id.stopVentilation).setEnabled(false);
        v.findViewById(R.id.stopVentilation).setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#2f2f2f")));
        ((Button) v.findViewById(R.id.stopVentilation)).setTextColor(Color.parseColor("#515151"));
    }

    public void initializeBottomSheetDialogs() {
        if (getActivity() == null) throw new NullPointerException();
        cbs_fio2 = new ControlsBottomSheet(getActivity(), Html.fromHtml("Set FiO<small><sub>2</sub></small> (%)"), "fio2");
        cbs_fio2.setSubText("21 to 100"); // no decimal
        cbs_vt = new ControlsBottomSheet(getActivity(), Html.fromHtml("Set V<small><sub>t</sub></small> (ml)"), "vt");
        cbs_vt.setSubText("50 to 2000");
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
        cbs_flowRate = new ControlsBottomSheet(getActivity(), Html.fromHtml("Set Flow<small><sub>rate</sub></small> (lpm)"), "flowrate");
        cbs_flowRate.setSubText(Html.fromHtml("1 to 80"));

        //setup hpa unit change listener
        hpaObserver = new Observer<String>() {
            @Override
            public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {
            }

            @Override
            public void onNext(@io.reactivex.rxjava3.annotations.NonNull String s) {
                if (s.equals("hpa")) {
                    plimit.get().setUnit("hPa");
                    pinsp.get().setUnit("hPa");
                    ps.get().setUnit("hPa");
                    peep.get().setUnit("hPa");
                    cbs_pmax.setHeading(Html.fromHtml("Set P<small><sub>limit</sub></small> (hPa)"));
                    cbs_pip.setHeading(Html.fromHtml("Set P<small><sub>insp</sub></small> (hPa)"));
                    cbs_delps.setHeading(Html.fromHtml("Set PS (hPa)"));
                    cbs_cpap.setHeading(Html.fromHtml("Set PEEP (hPa)"));
                } else {
                    plimit.get().setUnit(Html.fromHtml("cm H<small><sub>2</sub></small>O"));
                    pinsp.get().setUnit(Html.fromHtml("cm H<small><sub>2</sub></small>O"));
                    ps.get().setUnit(Html.fromHtml("cm H<small><sub>2</sub></small>O"));
                    peep.get().setUnit(Html.fromHtml("cm H<small><sub>2</sub></small>O"));
                    cbs_pmax.setHeading(Html.fromHtml("Set P<small><sub>limit</sub></small> (cm H<small><sub>2</sub></small>O)"));
                    cbs_pip.setHeading(Html.fromHtml("Set P<small><sub>insp</sub></small> (cm H<small><sub>2</sub></small>O)"));
                    cbs_delps.setHeading(Html.fromHtml("Set PS (cm H<small><sub>2</sub></small>O)"));
                    cbs_cpap.setHeading(Html.fromHtml("Set PEEP (cm H<small><sub>2</sub></small>O)"));
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

    Disposable disposable1;

    // Sets the behavior of how controls behave with one another,
    public void setClickListeners() {
        //Send button onClickListener
        controlsView.get().findViewById(R.id.send).setOnClickListener(v -> {
            Observable.just(1L).subscribe(StandbyFragment.confirmButtonClickObserver);
            setText(R.id.send, controlsView.get().getResources().getString(R.string.confirm));
            StaticStore.packet_fio2 = StaticStore.new_packet_fio2;
            StaticStore.packet_vt = StaticStore.new_packet_vt;
            StaticStore.packet_ie = StaticStore.new_packet_ie;
            StaticStore.packet_pinsp = StaticStore.new_packet_pinsp;
            StaticStore.packet_flowTrig = StaticStore.new_packet_flowTrig;
            StaticStore.packet_peep = StaticStore.new_packet_peep;
            StaticStore.packet_ps = StaticStore.new_packet_ps;
            StaticStore.packet_rtotal = StaticStore.new_packet_rtotal;
            StaticStore.packet_tinsp = StaticStore.new_packet_tinsp;
            StaticStore.packet_plimit = StaticStore.new_packet_plimit;
            StaticStore.packet_flowRate = StaticStore.new_packet_flowRate;
            new_mode = StaticStore.modeSelectedShort;
            new_modeString = StaticStore.modeSelected;

            SendPacket sp = new SendPacket();
            //write all values from StaticStore
            sp.writeDefaultSTRTPacketValues();
            // send constructed packet to device
            sp.sendToDevice();
            // add committed values to SharedPreferances, to use at startup
            SharedPreferences.Editor editor = Objects.requireNonNull(getContext()).getSharedPreferences("dvbVentilator", Context.MODE_PRIVATE).edit();
            editor.putString("packet_fio2", "" + StaticStore.packet_fio2);
            editor.putString("packet_vt", "" + StaticStore.packet_vt);
            editor.putString("packet_vtrig", "" + StaticStore.packet_flowTrig);
            editor.putString("packet_peep", "" + StaticStore.packet_peep);
            editor.putString("packet_pip", "" + StaticStore.packet_pinsp);
            editor.putString("packet_ps", "" + StaticStore.packet_ps);
            editor.putString("packet_ratef", "" + StaticStore.packet_rtotal);
            editor.putString("packet_ie", "" + StaticStore.packet_ie);
            editor.putString("packet_tinsp", "" + StaticStore.packet_tinsp);
            editor.putString("packet_pmax", "" + StaticStore.packet_plimit);
            editor.putString("packet_flowRate", "" + StaticStore.packet_flowRate);
            editor.putString("mode_selected", StaticStore.modeSelected);
            editor.putString("mode_selected_short", Short.toString(StaticStore.modeSelectedShort));
            // play sound and vibration
            mp.start();
            Vibrator myVib = (Vibrator) getContext().getSystemService(VIBRATOR_SERVICE);
            myVib.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
            editor.apply();
            tryToDispose(disposable);
            resetChanges();
            countdownStarted = false;
        });

        //Mode button onClickListener
        MenuControlsBottomSheet modeSheet = new MenuControlsBottomSheet(getActivity());
        modeBottomSheet = new WeakReference<>(modeSheet);
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

        controlsView.get().findViewById(R.id.controls_mode_change).setOnClickListener(v -> modeSheet.show(true));

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
                byte sh;
                sh = Byte.parseByte(s);
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
                        StaticStore.new_packet_rtotal = r;
                        rtotal.get().setCurrent(String.valueOf(r));
                    } else {
                        Log.d("MSG", "packet value equals entered value");
                    }
                }
                Log.d("MSG", "IE set: " + StaticStore.new_packet_ie);
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
        flowRate.get().setButtonClickListener(v -> {
            cbs_flowRate.show(true);
            cbs_flowRate.clearValue();
            cbs_flowRate.setOnDismissListener(bottomSheet -> {
                if (cbs_flowRate.canceled) return;
                String s = cbs_flowRate.getValue();
                float sh = Float.parseFloat(s);
                if (StaticStore.new_packet_flowRate != sh) {
                    highlight(R.id.controls_flowRate, true);
                    resetList.add("flowRate");
                    flowRate.get().setCurrent(s);
                    StaticStore.new_packet_flowRate = sh;
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
                if (StaticStore.new_packet_flowTrig != sh) {
                    highlight(R.id.controls_vtrig, true);
                    resetList.add("vtrig");
                    vtrig.get().setCurrent(s);
                    StaticStore.new_packet_flowTrig = sh;
                } else {
                    Log.d("MSG", "vtrig packet value equals entered value");
                }
            });
        });

        stopVentilation = new WeakReference<>(controlsView.get().findViewById(R.id.stopVentilation));
        stopVentilation.get().setOnClickListener(v -> Observable.just(v).subscribe(MainActivity.standbyClickObserver));
        ShapeableImageView up = controlsView.get().findViewById(R.id.swipeup);
        up.setOnClickListener(view -> {
            ScrollView sv = controlsView.get().findViewById(R.id.controls_scrollable);
            sv.smoothScrollTo(0, 500);
        });
        ScrollView sv = controlsView.get().findViewById(R.id.controls_scrollable);
        sv.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            int bottom = (sv.getChildAt(sv.getChildCount() - 1)).getHeight() - sv.getHeight() - scrollY;
            if (bottom == 0) {
                tryToDispose(disposable1);
                Observable.timer(10, TimeUnit.SECONDS).subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<Long>() {
                    @Override
                    public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {
                        disposable1 = d;
                    }

                    @Override
                    public void onNext(@io.reactivex.rxjava3.annotations.NonNull Long aLong) {
                        sv.smoothScrollTo(0, -500);
                    }

                    @Override
                    public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                    }

                    @Override
                    public void onComplete() {
                    }
                });
            }
        });
        revertStandbyClickObserver = new Observer<View>() {
            @Override
            public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {
            }

            @Override
            public void onNext(@io.reactivex.rxjava3.annotations.NonNull View view) {
                controlsView.get().findViewById(R.id.stopVentilation).setEnabled(true);
                controlsView.get().findViewById(R.id.stopVentilation).setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#ffffff")));
                ((Button) controlsView.get().findViewById(R.id.stopVentilation)).setTextColor(Color.parseColor("#000000"));
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

    public void setSubscriptAndPastSessionValues() {
        //Set subscript text for units and control labels and Default Controls
        //Set Static Store temp values to most recently comitted values
        StaticStore.new_packet_fio2 = StaticStore.packet_fio2;
        StaticStore.new_packet_vt = StaticStore.packet_vt;
        StaticStore.new_packet_ie = StaticStore.packet_ie;
        StaticStore.new_packet_pinsp = StaticStore.packet_pinsp;
        StaticStore.new_packet_flowTrig = StaticStore.packet_flowTrig;
        StaticStore.new_packet_peep = StaticStore.packet_peep;
        StaticStore.new_packet_ps = StaticStore.packet_ps;
        StaticStore.new_packet_rtotal = StaticStore.packet_rtotal;
        StaticStore.new_packet_tinsp = StaticStore.packet_tinsp;
        StaticStore.new_packet_plimit = StaticStore.packet_plimit;
        StaticStore.new_packet_flowRate = StaticStore.packet_flowRate;
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
        plimit.get().setLabel(Html.fromHtml("P<small><sub>limit</sub></small>"));
        flowRate.get().setLabel(Html.fromHtml("Flow<small><sub>rate</sub></small>"));
        vt.get().setLabel(Html.fromHtml("V<small><sub>t</sub></small>"));

        ((TextView) controlsView.get().findViewById(R.id.controls_mode_current)).setText(StaticStore.modeSelected == null ? "-" : StaticStore.modeSelected);
        fio2.get().setCurrent(String.valueOf(StaticStore.packet_fio2 == 0 ? "0" : StaticStore.packet_fio2));
        vt.get().setCurrent(String.valueOf(StaticStore.packet_vt == 0 ? "0" : StaticStore.packet_vt));
        vtrig.get().setCurrent(String.valueOf(StaticStore.packet_flowTrig == 0 ? "0" : StaticStore.packet_flowTrig));
        pinsp.get().setCurrent(String.valueOf(StaticStore.packet_pinsp == 0 ? "0" : StaticStore.packet_pinsp));
        peep.get().setCurrent(String.valueOf(StaticStore.packet_peep == 0 ? "0" : StaticStore.packet_peep));
        ps.get().setCurrent(String.valueOf(StaticStore.packet_ps == 0 ? "0" : StaticStore.packet_ps));
        rtotal.get().setCurrent(String.valueOf(StaticStore.packet_rtotal == 0 ? "0" : StaticStore.packet_rtotal));
        tinsp.get().setCurrent(String.valueOf(StaticStore.packet_tinsp == 0 ? "0" : StaticStore.packet_tinsp));
        ie.get().setCurrent(String.valueOf(StaticStore.packet_ie == 0 ? "-" : getIE(StaticStore.packet_ie)));
        plimit.get().setCurrent(String.valueOf(StaticStore.packet_plimit == 0 ? "0" : StaticStore.packet_plimit));
        flowRate.get().setCurrent(String.valueOf(StaticStore.packet_flowRate == 0 ? "0" : StaticStore.packet_flowRate));
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
                visibility(R.id.controls_flowRate, View.GONE);
                break;
            case "PC-CMV":
                setAllVisible();
                visibility(R.id.controls_vt, View.GONE);
                visibility(R.id.controls_vtrig, View.GONE);
                visibility(R.id.controls_ps, View.GONE);
                visibility(R.id.controls_plimit, View.GONE);
                visibility(R.id.controls_flowRate, View.GONE);
                break;
            case "VC-SIMV":
                setAllVisible();
                visibility(R.id.controls_pinsp, View.GONE);
                visibility(R.id.controls_plimit, View.GONE);
                visibility(R.id.controls_flowRate, View.GONE);
                break;
            case "PRVC":
                setAllVisible();
                visibility(R.id.controls_pinsp, View.GONE);
                visibility(R.id.controls_flowRate, View.GONE);
                break;
            case "ACV":
                setAllVisible();
                visibility(R.id.controls_ps, View.GONE);
                visibility(R.id.controls_pinsp, View.GONE);
                visibility(R.id.controls_plimit, View.GONE);
                visibility(R.id.controls_flowRate, View.GONE);
                break;
            case "PSV":
                setAllVisible();
                visibility(R.id.controls_ps, View.GONE);
                visibility(R.id.controls_vt, View.GONE);
                visibility(R.id.controls_tinsp, View.GONE);
                visibility(R.id.controls_rtotal, View.GONE);
                visibility(R.id.controls_ie, View.GONE);
                visibility(R.id.controls_plimit, View.GONE);
                visibility(R.id.controls_flowRate, View.GONE);
                break;
            case "PC-SIMV":
                setAllVisible();
                visibility(R.id.controls_vt, View.GONE);
                visibility(R.id.controls_plimit, View.GONE);
                visibility(R.id.controls_flowRate, View.GONE);
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
                visibility(R.id.controls_flowRate, View.GONE);
                break;
            case "BPAP":
                setAllVisible();
                visibility(R.id.controls_ps, View.GONE);
                visibility(R.id.controls_tinsp, View.GONE);
                visibility(R.id.controls_vt, View.GONE);
                visibility(R.id.controls_rtotal, View.GONE);
                visibility(R.id.controls_ie, View.GONE);
                visibility(R.id.controls_plimit, View.GONE);
                visibility(R.id.controls_flowRate, View.GONE);
                break;
            case "HFO":
                setAllVisible();
                visibility(R.id.controls_vt, View.GONE);
                visibility(R.id.controls_pinsp, View.GONE);
                visibility(R.id.controls_plimit, View.GONE);
                visibility(R.id.controls_peep, View.GONE);
                visibility(R.id.controls_ps, View.GONE);
                visibility(R.id.controls_vtrig, View.GONE);
                visibility(R.id.controls_rtotal, View.GONE);
                visibility(R.id.controls_tinsp, View.GONE);
                visibility(R.id.controls_ie, View.GONE);
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
        visibility(R.id.controls_flowRate, View.VISIBLE);
    }

    public void resetChanges() {
        for (int id : highlightedList) revertRowColor(id);
        highlightedList.clear();
        resetValues();
    }

    public void highlight(int id, boolean highlight) { // highlight row and reset timer to 30s
        if (highlight) {
            countdown = 30;
            makeRowYellow(id);
            highlightedList.add(id);
            if (countdownStarted) tryToDispose(disposable);
            observable.take(30).subscribe(confirmCountdownObserver);
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
                    StaticStore.new_packet_flowTrig = StaticStore.packet_flowTrig;
                    vtrig.get().setCurrent(String.valueOf(cleanNumber(StaticStore.packet_flowTrig)));
                    break;
                case "hfo2":
                    StaticStore.new_packet_flowRate = StaticStore.packet_flowRate;
                    flowRate.get().setCurrent(String.valueOf(cleanNumber(StaticStore.packet_flowRate)));
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
            ControlRow r = controlsView.get().findViewById(id);
            r.highlight(true);
        }
    }
}
