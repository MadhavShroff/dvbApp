package com.dvbinventek.dvbapp;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ReceivePacket {
//
//        //Test info
////        StaticStore.Values.pPeak = 51.7f;
////        StaticStore.Monitoring.pMean = 22.5f;
////        StaticStore.Values.pMax = 51.7f;
////        StaticStore.packet_pip = 25.0f;
////        StaticStore.Values.pMin = 12.1f;
////        StaticStore.Values.pMin = 12.1f;
////        StaticStore.Values.pMin = 12.1f;
////        StaticStore.packet_cpap = 12f;
////        StaticStore.Values.vtMax = 400;
////        StaticStore.Values.vtMin = 399;
////        StaticStore.Values.viTotal = 400;
////        StaticStore.packet_vt = 400;
////        StaticStore.Values.fMax = 20.0f;
////        StaticStore.Values.fMin = 20.0f;
////        StaticStore.Values.bpmMeasured = 20.0f;
////        StaticStore.packet_ratef = 20.0f;
////        StaticStore.Values.fio2Max = 21f;
////        StaticStore.Values.fio2Min = 21f;
////        StaticStore.Values.fio2 = 21f;
////        StaticStore.packet_fio2 = 21;
////        StaticStore.Values.mode = getMode((short) 17);
////        StaticStore.Warnings.warningMode = a;

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
            case 17:
                return "VC-CMV";
            case 13:
                return "PC-CMV";
            case 18:
                return "VC-SIMV";
            case 15:
                return "PSV";
            case 14:
                return "PC-SIMV";
            case 19:
                return "PRVC";
            case 21:
                return "ACV";
            case 20:
                return "CPAP";
            case 16:
                return "BPAP";
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
        getWarningModeString(StaticStore.Warnings.warningHigh, StaticStore.Warnings.warningMed, StaticStore.Warnings.warningLow); // set current warnings
        //Set Monitoring values
    }

    public void initLaunch(ByteBuffer byteBuffer) {
        if (byteBuffer.limit() == 0) return;
        StaticStore.LaunchVars.calibrationStatus = byteBuffer.getInt(144);
        StaticStore.LaunchVars.calibrationError = byteBuffer.getInt(148);
    }

    public int getType(char c1, char c2, char c3, char c4) {
        char[] c = {c1, c2, c3, c4};
        String s = String.valueOf(c);
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
        int i_ = (int) i * 10;
        int e_ = (int) e * 10;
        return i_ * 100 + e_;
    }

    public void getWarningModeString(int c1, int c2, int c3) {
        StaticStore.Warnings.top2warnings[0] = "";
        StaticStore.Warnings.top2warnings[1] = "";
        StaticStore.Warnings.currentWarnings.clear();
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
