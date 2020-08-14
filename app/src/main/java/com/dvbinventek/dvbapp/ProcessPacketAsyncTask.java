package com.dvbinventek.dvbapp;

import android.view.View;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.dvbinventek.dvbapp.customViews.MainParamsView;
import com.scichart.charting.model.dataSeries.XyDataSeries;
import com.scichart.charting.visuals.ISciChartSurface;
import com.scichart.core.framework.UpdateSuspender;

import java.lang.ref.WeakReference;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ProcessPacketAsyncTask {

    public static XyDataSeries<Double, Double> pressureDataSeries;
    public static XyDataSeries<Double, Double> pressureSweepDataSeries;
    public static XyDataSeries<Double, Double> flowDataSeries;
    public static XyDataSeries<Double, Double> flowSweepDataSeries;
    public static XyDataSeries<Double, Double> volumeDataSeries;
    public static XyDataSeries<Double, Double> volumeSweepDataSeries;
    public static XyDataSeries<Double, Double> lastPressureSweepDataSeries;
    public static XyDataSeries<Double, Double> lastFlowDataSeries;
    public static XyDataSeries<Double, Double> lastVolumeDataSeries;
    public static WeakReference<ISciChartSurface> chart;
    public static Double ppA, ppB, vFlowA, vFlowB, vtfA, vtfB, pp, vFlow, vtf;
    public static WeakReference<MainParamsView> tv1, tv2, tv3, tv4, tv5;
    public static WeakReference<TextView> alarm, modeBox, inspExp;
    public static double pastx = 0;
    public static long start = 0;
    public static double x = 0;
    public static boolean isATrace = false;
    public final ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) modeBox.get().getLayoutParams();
    public boolean showPip, showCpap, showVt, showRatef, showFio2;

    ProcessPacketAsyncTask(byte[] bytes) {
        MainActivity.disposables.add(Observable.fromCallable(() -> {
            new ReceivePacket(bytes);
            pp = (double) StaticStore.Values.pp;
            vFlow = (double) StaticStore.Values.vFlow;
            vtf = (double) StaticStore.Values.vTidalFlow;
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
//        Log.d("MSG", "" + ppA + " " + ppB + " " + vFlowA + " " + vFlowB + " " + vtfA + " " + vtfB);
            int mode = StaticStore.modeSelectedShort;
            showPip = (mode >= 13 && mode <= 16); // 13, 14, 15, 16
            showCpap = true; // all values
            showVt = (mode == 17 || mode == 18 || mode == 19 || mode == 21);
            showRatef = (mode == 13 || mode == 17 || mode == 18);
            showFio2 = true; // all values
            return true;
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(next -> {
                    tv1.get().setMaxMinValue(StaticStore.Values.pPeak, StaticStore.Monitoring.pMean, StaticStore.Values.pMax, showPip ? "" + StaticStore.packet_pinsp : "");
                    tv2.get().setMaxMinValue(StaticStore.Values.pMin, StaticStore.Values.pMin, StaticStore.Values.pMin, showCpap ? "" + StaticStore.packet_peep : "");
                    tv3.get().setMaxMinValueVIT(StaticStore.Values.vtMax, StaticStore.Values.vtMin, StaticStore.Values.viTotal, showVt ? "" + StaticStore.packet_vt : "");
                    tv4.get().setMaxMinValue(StaticStore.Values.fMax, StaticStore.Values.fMin, StaticStore.Values.bpmMeasured, showRatef ? "" + StaticStore.packet_rtotal : "");
                    tv5.get().setMaxMinValue(StaticStore.Values.fio2Max, StaticStore.Values.fio2Min, StaticStore.Values.fio2, showFio2 ? "" + StaticStore.packet_fio2 : "");
//      modeBox.get().setText(StaticStore.modeSelected); // for displaying mode entered
                    modeBox.get().setText(StaticStore.Values.mode); // for displaying mode received from packet
                    inspExp.get().setText(StaticStore.Monitoring.phase);
                    inspExp.get().setTextColor(inspExp.get().getResources().getColor(StaticStore.Monitoring.phaseColor));
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
                    setWarning();
                }));
    }

    public void setWarning() {
        if (StaticStore.Warnings.currentWarnings.size() == 0) {
            if (alarm.get().getAlpha() == 0.99f) return;
            alarm.get().setVisibility(View.GONE);
            layoutParams.height = 155;
            modeBox.get().setLayoutParams(layoutParams);
            modeBox.get().setTextSize(58);
            alarm.get().setAlpha(0.99f);
        } else {
            String s = StaticStore.Warnings.top2warnings[0] + "\n" + StaticStore.Warnings.top2warnings[1];
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
