package com.dvbinventek.dvbapp;

import android.annotation.SuppressLint;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Objects;

public class ReceivePacket {

    @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat("ddMMyy");
    @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy");

    ReceivePacket(byte[] s) {
        try {
            init(ByteBuffer.wrap(s).order(ByteOrder.LITTLE_ENDIAN));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    ReceivePacket(byte[] s, boolean isLaunch) {
        try {
            initLaunch(ByteBuffer.wrap(s).order(ByteOrder.LITTLE_ENDIAN));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getMode(short s) {
        switch (s) {
            case 13:
                return "PC-CMV";
            case 14:
                return "PC-SIMV";
            case 15:
                return "PSV";
            case 16:
                return "BPAP";
            case 17:
                return "VC-CMV";
            case 18:
                return "VC-SIMV";
            case 19:
                return "PRVC";
            case 20:
                return "CPAP";
            case 21:
                return "ACV";
            case 22:
                return "HFO";
            default:
                return "--";
        }
    }

    public void init(ByteBuffer byteBuffer) {
        if (byteBuffer.limit() == 0) return;
        StaticStore.Values.packetType = getType((char) byteBuffer.get(0), (char) byteBuffer.get(1), (char) byteBuffer.get(2), (char) byteBuffer.get(3));
        StaticStore.Values.mode = getMode(byteBuffer.get(4));
        StaticStore.Values.graphPressure = byteBuffer.getFloat(8);
        StaticStore.Values.graphFlow = byteBuffer.getFloat(12);
        StaticStore.Values.graphVolume = byteBuffer.getShort(16);
        StaticStore.Values.pInsp = byteBuffer.getFloat(20);
        StaticStore.Values.pPeak = byteBuffer.getFloat(24);
        StaticStore.Values.pMean = byteBuffer.getFloat(28);
        StaticStore.Values.peep = byteBuffer.getFloat(32);
        StaticStore.Values.peepMax = byteBuffer.getFloat(36);
        StaticStore.Values.peepMin = byteBuffer.getFloat(40);
        StaticStore.Values.vt = byteBuffer.getShort(44);
        StaticStore.Values.vtMax = byteBuffer.getShort(46);
        StaticStore.Values.vtMin = byteBuffer.getShort(48);
        StaticStore.Values.rateMeasured = byteBuffer.getFloat(52);
        StaticStore.Values.rateMax = byteBuffer.getFloat(56);
        StaticStore.Values.rateMin = byteBuffer.getFloat(60);
        StaticStore.Values.fio2 = Byte.toUnsignedInt(byteBuffer.get(64));
        StaticStore.Values.fio2Max = Byte.toUnsignedInt(byteBuffer.get(65));
        StaticStore.Values.fio2Min = Byte.toUnsignedInt(byteBuffer.get(66));
        StaticStore.MainActivityValues.sighHold = byteBuffer.get(164);
        StaticStore.MainActivityValues.warningSilence = byteBuffer.get(165);
        StaticStore.Warnings.warningSync = byteBuffer.get(166);
        StaticStore.Monitoring.mvTotal = byteBuffer.getFloat(76);
        StaticStore.Monitoring.mvSpont = byteBuffer.getFloat(80);
        StaticStore.Values.rSpont = byteBuffer.getFloat(72);
        StaticStore.Values.breathingType = byteBuffer.get(120);
        StaticStore.Values.shutdownPress = byteBuffer.get(169);
        StaticStore.Monitoring.ie = concatIE(byteBuffer.getFloat(92), byteBuffer.getFloat(96));
        StaticStore.Monitoring.ti = byteBuffer.getFloat(84);
        StaticStore.Monitoring.te = byteBuffer.getFloat(88);
        if (byteBuffer.get(121) == 1) {
            StaticStore.Monitoring.phase = "Insp";
            StaticStore.Monitoring.phaseColor = R.color.insp;
        } else {
            StaticStore.Monitoring.phase = "Exp";
            StaticStore.Monitoring.phaseColor = R.color.exp;
        }
        StaticStore.Monitoring.leakVol = byteBuffer.getShort(100);
        StaticStore.Monitoring.leakPercent = byteBuffer.get(102);
        StaticStore.Monitoring.cStat = byteBuffer.getFloat(104);
        StaticStore.Monitoring.flowPeak = Byte.toUnsignedInt(byteBuffer.get(108));
        StaticStore.Monitoring.pPlat = byteBuffer.getFloat(112);
        StaticStore.Monitoring.autoPeep = byteBuffer.getFloat(116);
        //Set Warnings
        StaticStore.Warnings.warningHigh = byteBuffer.getInt(152);
        StaticStore.Warnings.warningMed = byteBuffer.getInt(156);
        StaticStore.Warnings.warningLow = byteBuffer.getInt(160);
        // Set current warnings
        getWarningModeString(StaticStore.Warnings.warningHigh, StaticStore.Warnings.warningMed, StaticStore.Warnings.warningLow);
        // Parse Systems Values
        getSystemsValues(byteBuffer);
    }

    public void getSystemsValues(ByteBuffer byteBuffer) {
        byte[] bytes = Arrays.copyOfRange(byteBuffer.array(), 172, 204);
        StaticStore.System.machineHours = String.valueOf(new char[]{(char) byteBuffer.get(172), (char) byteBuffer.get(173), (char) byteBuffer.get(174), (char) byteBuffer.get(175), (char) byteBuffer.get(176)});
        StaticStore.System.patientHours = String.valueOf(new char[]{(char) byteBuffer.get(177), (char) byteBuffer.get(178), (char) byteBuffer.get(179), (char) byteBuffer.get(180), (char) byteBuffer.get(181)});
        StaticStore.System.lastServiceDate = String.valueOf(new char[]{(char) byteBuffer.get(182), (char) byteBuffer.get(183), (char) byteBuffer.get(184), (char) byteBuffer.get(185), (char) byteBuffer.get(186), (char) byteBuffer.get(187)});
        StaticStore.System.lastServiceHrs = String.valueOf(new char[]{(char) byteBuffer.get(188), (char) byteBuffer.get(189), (char) byteBuffer.get(190), (char) byteBuffer.get(191), (char) byteBuffer.get(192)});
        StaticStore.System.nextServiceDate = String.valueOf(new char[]{(char) byteBuffer.get(193), (char) byteBuffer.get(194), (char) byteBuffer.get(195), (char) byteBuffer.get(196), (char) byteBuffer.get(197), (char) byteBuffer.get(198)});
        StaticStore.System.nextServiceHrs = String.valueOf(new char[]{(char) byteBuffer.get(199), (char) byteBuffer.get(200), (char) byteBuffer.get(201), (char) byteBuffer.get(202), (char) byteBuffer.get(203)});
        StaticStore.System.systemVersion = String.valueOf(new char[]{(char) byteBuffer.get(204), (char) byteBuffer.get(205), (char) byteBuffer.get(206), (char) byteBuffer.get(207), (char) byteBuffer.get(208), (char) byteBuffer.get(209), (char) byteBuffer.get(210), (char) byteBuffer.get(211), (char) byteBuffer.get(212)});
        try {
            StaticStore.System.lastServiceDate = formatter.format(Objects.requireNonNull(format.parse(StaticStore.System.lastServiceDate)));
            StaticStore.System.nextServiceDate = formatter.format(Objects.requireNonNull(format.parse(StaticStore.System.nextServiceDate)));
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public void initLaunch(ByteBuffer byteBuffer) {
        if (byteBuffer.limit() == 0) return;
        StaticStore.LaunchVars.calibrationStatus = byteBuffer.getInt(144);
        StaticStore.LaunchVars.calibrationError = byteBuffer.getInt(148);
    }

    public int getType(char c1, char c2, char c3, char c4) {
        char[] c = {c1, c2, c3, c4};
        String s = String.valueOf(c);
//        Log.d("PACKET_TYPE", s);
        switch (s) {
            case SendPacket.STR_STRT:
                return SendPacket.TYPE_STRT;
            case SendPacket.STR_STOP:
                return SendPacket.TYPE_STOP;
            case SendPacket.STR_STBY:
                return SendPacket.TYPE_STBY;
            case SendPacket.STR_SLFT:
                return SendPacket.TYPE_SLFT;
            case SendPacket.STR_CLBF:
                return SendPacket.TYPE_CLBF;
            case SendPacket.STR_RNTM:
                return SendPacket.TYPE_RNTM;
            case SendPacket.STR_ALRM:
                return SendPacket.TYPE_ALRM;
            default:
                return -1;
        }
    }

    public int concatIE(float i, float e) {
        int i_ = (int) (i * 10);
        int e_ = (int) (e * 10);
        return i_ * 100 + e_;
    }

    public void getWarningModeString(int c1, int c2, int c3) {
        StaticStore.Warnings.top2warnings[0] = "";
        StaticStore.Warnings.top2warnings[1] = "";
        StaticStore.Warnings.currentWarnings.clear();
        StaticStore.Warnings.currentWarningsLength = 0;
        int num = 0;
        String ithWarning;
        for (int i = 0; i < StaticStore.Warnings.warningHighCount; ++i) {
            if (((c1 & 1 << i) >> i) == 1) { //i the ith bit of the short is set
                StaticStore.Warnings.currentWarningsLength++;
                ithWarning = StaticStore.Warnings.warningsHigh[i];
                StaticStore.Warnings.currentWarnings.add(ithWarning);
                if (num++ < 2) StaticStore.Warnings.top2warnings[num - 1] = ithWarning;
            }
        }
        for (int i = 0; i < StaticStore.Warnings.warningMedCount; ++i) {
            if (((c2 & 1 << i) >> i) == 1) { //i the ith bit of the short is set
                StaticStore.Warnings.currentWarningsLength++;
                ithWarning = StaticStore.Warnings.warningsMedium[i];
                StaticStore.Warnings.currentWarnings.add(ithWarning);
                if (num++ < 2) StaticStore.Warnings.top2warnings[num - 1] = ithWarning;
            }
        }
        for (int i = 0; i < StaticStore.Warnings.warningLowCount; ++i) {
            if (((c3 & 1 << i) >> i) == 1) { //i the ith bit of the short is set
                StaticStore.Warnings.currentWarningsLength++;
                ithWarning = StaticStore.Warnings.warningsLow[i];
                StaticStore.Warnings.currentWarnings.add(ithWarning);
                if (num++ < 2) StaticStore.Warnings.top2warnings[num - 1] = ithWarning;
            }
        }
    }
}
