package com.dvbinventek.dvbapp;

import android.graphics.drawable.Drawable;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.dvbinventek.dvbapp.customViews.MainParamsView;
import com.dvbinventek.dvbapp.viewPager.ToolsFragment;
import com.google.android.material.button.MaterialButton;
import com.scichart.charting.model.dataSeries.XyDataSeries;
import com.scichart.charting.visuals.ISciChartSurface;
import com.scichart.core.framework.UpdateSuspender;

import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ProcessPacket {

    public static XyDataSeries<Double, Double> pressureDataSeries;
    public static XyDataSeries<Double, Double> pressureSweepDataSeries;
    public static XyDataSeries<Double, Double> flowDataSeries;
    public static XyDataSeries<Double, Double> flowSweepDataSeries;
    public static XyDataSeries<Double, Double> volumeDataSeries;
    public static XyDataSeries<Double, Double> volumeSweepDataSeries;
    public static XyDataSeries<Double, Double> lastPressureSweepDataSeries;
    public static XyDataSeries<Double, Double> lastFlowDataSeries;
    public static XyDataSeries<Double, Double> lastVolumeDataSeries;
    public static XyDataSeries<Double, Double> FPDataSeries;
    public static XyDataSeries<Double, Double> FVDataSeries;
    public static XyDataSeries<Double, Double> PVDataSeries;
    public static Drawable silenceAlarm, silencedAlarm, RRRedMode, RRRedAlarm, RRHolderRed, RRYellowMode, RRYellowAlarm, RRHolderYellow, RRGreen, RRHolder;
    public static WeakReference<ISciChartSurface> chart, FPchart, FVchart, PVchart;
    public static Double ppA, ppB, vFlowA, vFlowB, vtfA, vtfB, pp, vFlow, vtf;
    public static WeakReference<MainParamsView> tv1, tv2, tv3, tv4, tv5;
    public static WeakReference<TextView> alarm, modeBox, inspExp, sigh, spont;
    public static WeakReference<MaterialButton> silence;
    public static WeakReference<FrameLayout> viewPagerHolder;
    public static int blackColor, whiteColor, yellowColor;
    public static Disposable highDisposable, mediumDisposable;
    public static double pastx = 0;
    public static long start = 0;
    public static double x = 0;
    public static boolean isATrace = false;
    public final ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) modeBox.get().getLayoutParams();
    public boolean showPip, showCpap, showVt, showRatef, showFio2, showFlowRate;
    public static boolean isAlarmSet = false;

    ProcessPacket(byte[] bytes) {
        MainActivity.disposables.add(Observable.fromCallable(() -> {
            new ReceivePacket(bytes);
            pp = round2(StaticStore.Values.graphPressure);
            vFlow = round2(StaticStore.Values.graphFlow);
            vtf = round2(StaticStore.Values.graphVolume);
//      Log.d("CHART_WRITE", "pp:" + pp + "\t\tflow:" + vFlow + "\t\tvolume:" + vtf + "\t\tx:" + x);
            if (start == 0) {
                start = System.currentTimeMillis();
                x = 0;
            } else {
                pastx = x;
                long now = System.currentTimeMillis();
                x = x + (now - start) / 1000.0;
                start = now;
            }
            if (x > 10) {
                isATrace = !isATrace;
                x = 0;
            } else {
                if (pastx > x) {
                    return null;
                }
            }
            if (isATrace) {
                ppA = pp;
                vFlowA = vFlow;
                vtfA = vtf;
                ppB = Double.NaN;
                vFlowB = Double.NaN;
                vtfB = Double.NaN;
            } else {
                ppB = pp;
                vFlowB = vFlow;
                vtfB = vtf;
                ppA = Double.NaN;
                vFlowA = Double.NaN;
                vtfA = Double.NaN;
            }
            int mode = StaticStore.modeSelectedShort;
            // boolean values representing which boxes show the "Set: " value based on mode selected
            showPip = (mode >= 13 && mode < 17); // 13, 14, 15, 16
            showCpap = mode != 22; // all modes except 22
            showVt = (mode == 17 || mode == 18 || mode == 19 || mode == 21);
            showRatef = (mode != 15 && mode != 20 && mode != 16);
            showFio2 = true; // all values
//            showFlowRate = true;
            return true;
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(next -> {
                    if (StaticStore.Values.mode.equals("HFO")) { // if mode is HFO, blank out all other values
                        tv1.get().setMaxMinValue("", "", "", "");
                        tv2.get().setMaxMinValue("", "", "", "");
                        tv3.get().setMaxMinValue("", "", "", "");
                        tv4.get().setMaxMinValue("", "", "" + round1(StaticStore.Values.rateMeasured), "Set: " + StaticStore.packet_flowRate);
                        tv5.get().setMaxMinValue("", "", "" + StaticStore.Values.fio2, "Set: " + StaticStore.packet_fio2);
                    } else {
                        tv1.get().setMaxMinValue(round1(StaticStore.Values.pPeak), round1(StaticStore.Values.pMean), round1(StaticStore.Values.pInsp), showPip ? "Set: " + StaticStore.packet_pinsp : "");
                        tv2.get().setMaxMinValue(round1(StaticStore.Values.peepMax), round1(StaticStore.Values.peepMin), round1(StaticStore.Values.peep), showCpap ? "Set: " + StaticStore.packet_peep : "");
                        tv3.get().setMaxMinValue("" + (int) StaticStore.Values.vtMax, "" + (int) StaticStore.Values.vtMin, "" + (int) StaticStore.Values.vt, showVt ? "Set: " + StaticStore.packet_vt : "");
                        tv4.get().setMaxMinValue(round1(StaticStore.Values.rateMax), round1(StaticStore.Values.rateMin), round1(StaticStore.Values.rateMeasured), "Set: " + StaticStore.packet_rtotal);
                        tv5.get().setMaxMinValue(round1(StaticStore.Values.fio2Max), round1(StaticStore.Values.fio2Min), round1(StaticStore.Values.fio2), showFio2 ? "Set: " + StaticStore.packet_fio2 : "");
                    }
                    //modeBox.get().setText(StaticStore.modeSelected); // for displaying mode entered
                    if (!modeBox.get().getText().equals(StaticStore.Values.mode)) { // onModeChange :
                        modeBox.get().setText(StaticStore.Values.mode); // for displaying a new mode received from packet
                        if (StaticStore.Values.mode.equals("HFO")) { // Mode changed to HFO2
                            tv4.get().setUnit("(lpm)");
                            tv4.get().setLabel(Html.fromHtml("Flow Rate"));
                        } else { // Mode changed to not HFO2
                            tv4.get().setUnit("(b/min)");
                            tv4.get().setLabel(Html.fromHtml("R<sub><small>total</small></sub>"));
                        }
                    }
                    setStatusText();
                    if (MainActivity.graphShownRef == R.id.mainChart)
                        UpdateSuspender.using(chart.get(), () -> {
                            try {
                                pressureDataSeries.append(x, ppA);
                                pressureSweepDataSeries.append(x, ppB);
                                flowDataSeries.append(x, vFlowA);
                                flowSweepDataSeries.append(x, vFlowB);
                                volumeDataSeries.append(x, vtfA);
                                volumeSweepDataSeries.append(x, vtfB);
                                lastPressureSweepDataSeries.append(x, pp);
                                lastFlowDataSeries.append(x, vFlow);
                                lastVolumeDataSeries.append(x, vtf);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                    if (MainActivity.graphShownRef == R.id.FPChart)
                        UpdateSuspender.using(FPchart.get(), () -> {
                            try {
                                FPDataSeries.append(vFlow, pp);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                    if (MainActivity.graphShownRef == R.id.PVChart)
                        UpdateSuspender.using(PVchart.get(), () -> {
                            try {
                                PVDataSeries.append(pp, vtf);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                    if (MainActivity.graphShownRef == R.id.FVChart)
                        UpdateSuspender.using(FVchart.get(), () -> {
                            try {
                                FVDataSeries.append(vFlow, vtf);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                }));
        setWarning();
        setWarningFlash();
        showPowerOffMessage();
    }

    public String round1(float x) {
        return "" + Math.round(x * 10) / 10.0;
    }

    public double round2(double x) {
        return Math.round(x * 100) / 100.0;
    }

    public void setWarningFlash() {
        if (StaticStore.Warnings.warningSync != StaticStore.Warnings.warningSyncState) {
            tryToDispose(highDisposable);
            tryToDispose(mediumDisposable);
            switch (StaticStore.Warnings.warningSync) {
                case StaticStore.Warnings.HIGH: // RED FLASHING
                    Observable.interval(750, TimeUnit.MILLISECONDS).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<Long>() {
                        @Override
                        public void onSubscribe(@NonNull Disposable d) {
                            highDisposable = d;
                        }
                        @Override
                        public void onNext(@NonNull Long aLong) {
                            if (aLong % 2 == 1) setAlarmColorsRed();
                            else resetAlarmColors();
                        }
                        @Override
                        public void onError(@NonNull Throwable e) {
                            e.printStackTrace();
                        }
                        @Override
                        public void onComplete() {
                            resetAlarmColors();
                        }
                    });
                    break;
                case StaticStore.Warnings.MEDIUM: // YELLOW FLASHING
                    Observable.interval(750, TimeUnit.MILLISECONDS).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<Long>() {
                        @Override
                        public void onSubscribe(@NonNull Disposable d) {
                            mediumDisposable = d;
                        }
                        @Override
                        public void onNext(@NonNull Long aLong) {
                            if (aLong % 2 == 1) setAlarmColorsYellow();
                            else resetAlarmColors();
                        }
                        @Override
                        public void onError(@NonNull Throwable e) {
                            e.printStackTrace();
                        }
                        @Override
                        public void onComplete() {
                            resetAlarmColors();
                        }
                    });
                    break;
                case StaticStore.Warnings.LOW: // YELLOW SOLID
                    setAlarmColorsYellow();
                    break;
                case 0: // no alarm
                    tryToDispose(highDisposable);
                    tryToDispose(mediumDisposable);
                    resetAlarmColors();
            }
            StaticStore.Warnings.warningSyncState = StaticStore.Warnings.warningSync;
        }
    }

    public void tryToDispose(Disposable d) {
        if (d != null) if (!d.isDisposed()) d.dispose();
    }

    public void setAlarmColorsRed() {
        modeBox.get().setBackground(RRRedMode);
        viewPagerHolder.get().setBackground(RRHolderRed);
        tv1.get().setAlarmColors(R.color.Red1, R.color.Red2, R.color.HospitalRed, MainParamsView.RED);
        tv2.get().setAlarmColors(R.color.Red1, R.color.Red2, R.color.HospitalRed, MainParamsView.RED);
        tv3.get().setAlarmColors(R.color.Red1, R.color.Red2, R.color.HospitalRed, MainParamsView.RED);
        tv4.get().setAlarmColors(R.color.Red1, R.color.Red2, R.color.HospitalRed, MainParamsView.RED);
        tv5.get().setAlarmColors(R.color.Red1, R.color.Red2, R.color.HospitalRed, MainParamsView.RED);
        isAlarmSet = true;
    }

    public void setAlarmColorsYellow() {
        alarm.get().setBackground(RRYellowAlarm);
        alarm.get().setTextColor(blackColor);
        modeBox.get().setBackground(RRYellowMode);
        modeBox.get().setTextColor(blackColor);
        viewPagerHolder.get().setBackground(RRHolderYellow);
        tv1.get().setAlarmColors(R.color.yellow1, R.color.yellow2, R.color.yellow3, MainParamsView.YELLOW);
        tv2.get().setAlarmColors(R.color.yellow1, R.color.yellow2, R.color.yellow3, MainParamsView.YELLOW);
        tv3.get().setAlarmColors(R.color.yellow1, R.color.yellow2, R.color.yellow3, MainParamsView.YELLOW);
        tv4.get().setAlarmColors(R.color.yellow1, R.color.yellow2, R.color.yellow3, MainParamsView.YELLOW);
        tv5.get().setAlarmColors(R.color.yellow1, R.color.yellow2, R.color.yellow3, MainParamsView.YELLOW);
        isAlarmSet = true;
    }

    public void resetAlarmColors() {
//        tabLayout.setBackgroundColor(getColor(R.color.gray1));
        alarm.get().setBackground(RRRedAlarm);
        alarm.get().setTextColor(whiteColor);
        modeBox.get().setBackground(RRGreen);
        modeBox.get().setTextColor(yellowColor);
        viewPagerHolder.get().setBackground(RRHolder);
        tv1.get().resetAlarmColors();
        tv2.get().resetAlarmColors();
        tv3.get().resetAlarmColors();
        tv4.get().resetAlarmColors();
        tv5.get().resetAlarmColors();
        isAlarmSet = false;
    }

    public void showPowerOffMessage() {
        if (StaticStore.Values.shutdownPress != StaticStore.Values.shutdownPressState) {
            Log.d("POWER OFF", "Got StaticStore.Values.shutdownPress = " + StaticStore.Values.shutdownPress);
            Observable.just((long) StaticStore.Values.shutdownPress).subscribe(MainActivity.shutdownClickObserver);
            StaticStore.Values.shutdownPressState = StaticStore.Values.shutdownPress;
        }
    }

    public void setStatusText() {
        if (StaticStore.Values.breathingType == 1) {
            if (spont.get().getVisibility() == View.INVISIBLE)
                spont.get().setVisibility(View.VISIBLE);
        } else {
            if (spont.get().getVisibility() == View.VISIBLE)
                spont.get().setVisibility(View.INVISIBLE);
        }
        if (!inspExp.get().getText().equals(StaticStore.Monitoring.phase)) {
            inspExp.get().setText(StaticStore.Monitoring.phase);
            inspExp.get().setTextColor(inspExp.get().getResources().getColor(StaticStore.Monitoring.phaseColor));
        }
        //sigh indicator set values after check state, to avoid overwrite computation
        switch (StaticStore.MainActivityValues.sighHold) {
            case 0:
                if (StaticStore.MainActivityValues.sighState != StaticStore.MainActivityValues.SIGH_HIDDEN) {
                    sigh.get().setVisibility(View.INVISIBLE);
                    StaticStore.MainActivityValues.sighState = StaticStore.MainActivityValues.SIGH_HIDDEN;
                    Observable.just(R.color.yellow).subscribe(ToolsFragment.sighObserver);
                }
                break;
            case 1:
                if (StaticStore.MainActivityValues.sighState != StaticStore.MainActivityValues.SIGH_SHOWN) {
                    sigh.get().setVisibility(View.VISIBLE);
                    sigh.get().setTextColor(sigh.get().getResources().getColor(R.color.orange));
                    StaticStore.MainActivityValues.sighState = StaticStore.MainActivityValues.SIGH_SHOWN;
                    Observable.just(R.color.orange).subscribe(ToolsFragment.sighObserver);
                }
                break;
            case 2:
                if (StaticStore.MainActivityValues.sighState != StaticStore.MainActivityValues.SIGH_BREATH) {
                    sigh.get().setVisibility(View.VISIBLE);
                    sigh.get().setTextColor(sigh.get().getResources().getColor(R.color.yellow));
                    StaticStore.MainActivityValues.sighState = StaticStore.MainActivityValues.SIGH_BREATH;
                }
                break;
        }

        //silence button set text after check state, to avoid overwrite computation
        if (StaticStore.MainActivityValues.warningSilence == 0) {
            if (StaticStore.MainActivityValues.silenceState != StaticStore.MainActivityValues.SILENCED) {
                silence.get().setIcon(silenceAlarm);
                silence.get().setText(R.string.silence_alarm);
                StaticStore.MainActivityValues.silenceState = StaticStore.MainActivityValues.SILENCED;
            }
        } else {
            silence.get().setIcon(silencedAlarm);
            silence.get().setText(silence.get().getResources().getString(R.string.silenced, StaticStore.MainActivityValues.warningSilence));
            StaticStore.MainActivityValues.silenceState = StaticStore.MainActivityValues.UNSILENCED;
        }
    }

    String s;
    public void setWarning() {
        if (StaticStore.Warnings.currentWarnings.size() == 0) {
            if (alarm.get().getAlpha() == 0.99f) return;
            alarm.get().setVisibility(View.GONE);
            layoutParams.height = 155;
            modeBox.get().setLayoutParams(layoutParams);
            modeBox.get().setTextSize(58);
            alarm.get().setAlpha(0.99f);
        } else {
            if (StaticStore.Values.mode.equals("HFO")) {                 // If mode is
                if (StaticStore.Warnings.top2warnings[0].equals("MV LOW"))
                    s = "LOW FLOW RATE" + "\n";
                else if (StaticStore.Warnings.top2warnings[0].equals("MV HIGH"))
                    s = "HIGH FLOW RATE" + "\n";
                else s = StaticStore.Warnings.top2warnings[0] + "\n";

                if (StaticStore.Warnings.top2warnings[1].equals("MV LOW"))
                    s = "LOW FLOW RATE";
                else if (StaticStore.Warnings.top2warnings[1].equals("MV HIGH"))
                    s = "HIGH FLOW RATE";
                else s = StaticStore.Warnings.top2warnings[1];
            } else {
                s = StaticStore.Warnings.top2warnings[0] + "\n" + StaticStore.Warnings.top2warnings[1];
            }
            if (alarm.get().getAlpha() == 0.99f || !alarm.get().getText().equals(s)) {
                alarm.get().setText(s);
                layoutParams.height = 45;
                modeBox.get().setLayoutParams(layoutParams);
                modeBox.get().setTextSize(30);
                alarm.get().setVisibility(View.VISIBLE);
                alarm.get().setAlpha(1.0f);
                modeBox.get().setTextSize(30);
            }
        }
    }
}
