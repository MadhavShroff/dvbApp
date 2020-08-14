package com.dvbinventek.dvbapp;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class StaticStore {
    public static String modeSelected = "";
    public static short modeSelectedShort = 0;
    public static final List<HashMap<String, String>> Data = Collections.synchronizedList(new ArrayList<>());
    public static WeakReference<UsbService> service;

    static public short packet_fio2 = 0;
    static public short packet_vt = 0;
    static public short packet_ie = 0;
    static public float packet_pinsp = 0;
    static public float packet_vtrig = 0;
    static public float packet_peep = 0;
    static public float packet_ps = 0;
    static public float packet_rtotal = 0;
    static public float packet_tinsp = 0;
    static public float packet_plimit = 0;

    static public short new_packet_mode = 0;
    static public short new_packet_fio2 = 0;
    static public short new_packet_vt = 0;
    static public short new_packet_ie = 0;
    static public float new_packet_pinsp = 0;
    static public float new_packet_vtrig = 0;
    static public float new_packet_peep = 0;
    static public float new_packet_ps = 0;
    static public float new_packet_rtotal = 0;
    static public float new_packet_tinsp = 0;
    static public float new_packet_plimit = 0;

    //map (short mode) -> mode String
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

    //Controls values that cannot be exceeded
    public static class DeviceParameterLimits {
        // P limit
        public static float max_plimit = 60;
        // fio2
        public static short max_fio2 = 100;
        public static short min_fio2 = 21;
        // vt
        public static short max_vt = 1500;
        public static short min_vt = 50;
        // V trig / Flow Trig
        public static short max_vtrig = 20;
        public static short min_vtrig = 1;
        // PEEP / CPAP
        public static short max_cpap = 30;
        public static short min_cpap = 0;
        // PIP
        public static short max_pip = 60;
        public static short min_pip = 0;
        // PS / delps
        public static short max_delps = 45;
        public static short min_delps = 5;

        //TODO: refer to excel sheet
        public static short max_ratef = 60;
        public static short max_ie = 4040;
        public static float max_tinsp = 6f;
        public static float max_pinsp = 60f;

        public static short min_ratef = 4;
        public static short min_ie = 1010;
        public static float min_tinsp = 0.3f;
        public static float min_pinsp = 0f;
    }

    public static class Warnings {
        public static final List<String> currentWarnings = Collections.synchronizedList(new ArrayList<String>());
        public static final List<String> allWarnings = Collections.synchronizedList(new ArrayList<String>());
        public static int warningTypeCount = 8;
        public static String[] warnings = {
                "NO MAINS POWER",
                "PATIENT DISCONNECTED",
                "APNEA",
                "HIGH PRESSURE !!!",
                "HIGH PEEP !!",
                "TIDAL VOLUME LIMIT EXCEEDED",
                "BREATHING RATE LIMIT EXCEEDED",
                "OXYGEN DISCONNECTED",
        };
        public static String[] top2warnings = new String[2];
        public static int currentWarningsLength;
        public static short warningMode;
        public static String warningModeString;

        // In order of priority, List of Warning messages, warningMessages(i) => the message to be added to the warning string if the ith bit is set.
        // 0 is no warning
        // 1 is apnea ("Warning: Apnea")
        // 2 is ExpMinVol Limit Crossed
        // 3 is TidalVolume Limit Crossed
        // 4 is Pressure Limits Crossed
        // 5 is breathing rate crossed
        // 6 is patient disconnected
        // 7 mode Changed
    }

    // Values being used for MainParamsView values on the left of the screen.
    // These static values serve as the model of the model view controller.
    // The controller is the Observer to the USB packet. It observes for new packets and reacts to parse values.
    // Those parsed values are stored here so asynchronous functions can read and display these values
    public static class Values {
        public static float pMax;
        public static float veTotal;
        public static float viTotal;
        public static float pPeak;
        public static float pp;
        public static float pMin;
        public static float expMinVolMeasured;
        public static float vTidalFlow;
        public static float emvMax;
        public static float emvMin;
        public static float vtMin;
        public static float vtMax;
        public static float fMax;
        public static float fMin;
        public static float bpmMeasured;
        public static float fio2;
        public static float fio2Max;
        public static float fio2Min;
        public static float vFlow;
        public static short rSpont;
        public static String mode;
    }

    public static class Values2 {
        public static char[] comStart;        // 0 size 4
        public static byte mode;        // 4
        public static float graphPressure;        // 5
        public static float graphFlow;        // 9
        public static short graphVolume;        // 13
        public static float pInsp;        // 15
        public static float pPeak;        // 19
        public static float pMean;        // 23
        public static float peep;        // 27
        public static float peepMax;        // 31
        public static float peepMin;        // 35
        public static short vt;        // 39
        public static short vtMax;        // 41
        public static short vtMin;        // 43
        public static float rateTotal;        // 45
        public static float rateTotalMax;        // 49
        public static float rateTotalMin;        // 53
        public static byte fio2;        // 57
        public static byte fio2Max;        // 58
        public static byte fio2Min;        // 59
        public static short vti;        // 60
        public static short vte;        // 62
        public static float rateSpont;        // 64
        public static float mvTotal;        // 68
        public static float mvSpont;        // 72
        public static float tInsp;        // 76
        public static float tExp;        // 80
        public static float I;        // 84
        public static float E;        // 88
        public static short leakVolume;        // 92
        public static byte leakPercentage;        // 94
        public static float cStat;        // 95
        public static byte flowPeak;        // 99
        public static float pPlat;        // 100
        public static float autoPeep;        // 104
        public static byte breathingType;        // 108
        public static byte breathingPhase;        // 109
        public static byte[] reserved;        // 110 size 20
        public static float calibrationStatus;        // 130
        public static float calibrationError;        // 134
        public static float warningHigh;        // 138
        public static float warningMedium;        // 142
        public static float warningLow;        // 146
        public static byte sighHold;        // 150
        public static byte warningSilence;        // 151
        public static byte warningSync;        // 152
        public static byte batteryStatus;        // 153
        public static byte batteryPower;        // 154
        public static byte powerOffRequest;        // 155
        public static byte[] maintenenceDisplay;        // 156 size 24
        public static byte[] versionDisplay;        // 180 size 20
        public static short ts;        // 200
        public static short pi;        // 202
        public static short pe;        // 204
        public static short pb;        // 206
        public static short pev;        // 208
        public static short fi;        // 210
        public static short fe;        // 212
        public static short fo;        // 214
        public static short fTidal;        // 216
        public static short pwmWidthBlower;        // 218
        public static short pwmWidthInspiratoryValve;        // 220
        public static short pwmWidthInspiratoryValveExhale;        // 222
        public static short pwmWidthexhalationValve;        // 224
        public static short pwmWidthexhalationValveExhale;        // 226
        //        public static byte[] reservedDebug;		// 228 size 18
        public static char[] comStop;            // 246 size 4

    }

    public static class Monitoring {
        public static float pPlat;
        public static float pMean;
        public static float autoPeep;
        public static float vte;
        public static float mvSpont;
        public static int ie;
        public static int phaseColor = R.color.blue;
        public static String phase;
        public static float leakVol;
        public static float leakPercent;
        public static float rInsp;
        public static float cStat;
        public static float ti;
        public static float te;
        public static float flowPeak;
    }

    public static class AlarmLimits {
        public static short minVolMax = 20;
        public static short minVolMin = 1;
        public static short fTotalMax = 60;
        public static short fTotalMin = 4;
        public static short vtMax = 2000;
        public static short vtMin = 50;
        public static short pMax = 60;
        public static short pMin = 2;
        public static short apnea = 12;

        public static short new_minVolMax = 0;
        public static short new_minVolMin = 0;
        public static short new_fTotalMax = 0;
        public static short new_fTotalMin = 0;
        public static short new_vtMax = 0;
        public static short new_vtMin = 0;
        public static short new_pMax = 0;
        public static short new_pMin = 0;
        public static short new_apnea = 0;
    }
}
