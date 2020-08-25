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
    static public short packet_i = 0;
    static public short packet_e = 0;
    static public float packet_pinsp = 0;
    static public float packet_flowTrig = 0;
    static public float packet_peep = 0;
    static public float packet_ps = 0;
    static public float packet_rtotal = 0;
    static public float packet_tinsp = 0;
    static public byte packet_plimit = 0;

    static public short new_packet_mode = 0;
    static public short new_packet_fio2 = 0;
    static public short new_packet_vt = 0;
    static public short new_packet_ie = 0;
    static public float new_packet_pinsp = 0;
    static public float new_packet_flowTrig = 0;
    static public float new_packet_peep = 0;
    static public float new_packet_ps = 0;
    static public float new_packet_rtotal = 0;
    static public float new_packet_tinsp = 0;
    static public byte new_packet_plimit = 0;
    public static boolean restrictedCommunicationDueToStandby = false;

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

    public static class PatientDetails {
        public static String name = "Madhav Shroff";
        public static String age = "21";
        public static String height = "186 cm";
        public static String ibw = "78 kg";
        public static String room = "2";
        public static String bed = "6";
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
        public static final int HIGH = 3;
        public static final int MEDIUM = 2;
        public static final int LOW = 1;
        public static byte warningSync;
        public static byte warningSyncState;
        public static final List<String> currentWarnings = Collections.synchronizedList(new ArrayList<String>());
        public static final List<String> allWarnings = Collections.synchronizedList(new ArrayList<String>());
        public static int warningHighCount = 32;
        public static int warningMedCount = 7;
        public static int warningLowCount = 3;
        public static String[] warningsHigh = {
                "OPEN STATE",
                "APNEA (SAFE MODE)",
                "BATTERY SYSTEM ERROR",
                "BATTERY LOW POWER",
                "BATTERY TEMPERATURE HIGH",
                "BATTERY COMPLETE DISCHARGE",
                "PATIENT DISCONNECTED",
                "NO MAINS POWER",
                "PRESSURE HIGH",
                "PRESSURE LOW",
                "PRESSURE NOT RELEASED",
                "MV HIGH",
                "MV LOW",
                "O2 SUPPLY FAILED",
                "O2 SENSOR ERROR",
                "O2 SENSOR REPLACE",
                "O2 HIGH",
                "O2 LOW",
                "SYSTEM ERROR",
                "READ WRITE ERROR",
                "TECHNICAL FAULT - HIGH PRESSURE RELEASE",
                "TECHNICAL FAULT - PRESSURE SENSOR FAILURE",
                "TECHNICAL FAULT - FLOW SENSOR FAILURE",
                "TECHNICAL FAULT - BLOWER FAILURE",
                "TECHNICAL FAULT - VALVES FAILURE",
                "TBD",
                "TBD",
                "TBD",
                "TECHNICAL EVENT - BUZZER FAILED",
                "TBD",
                "TBD",
                "SELF TEST PRESSURE SENSOR FAILED"
        };
        public static String[] warningsMedium = {
                //medium
                "BATTERY LOW POWER",
                "RATE HIGH",
                "RATE LOW",
                "PEEP HIGH",
                "PEEP LOW",
                "TIDAL VOLUME HIGH",
                "TIDAL VOLUME LOW"
        };
        public static String[] warningsLow = {
                //low
                "BATTERY LOW POWER",
                "REPALCE BATTERY",
                "CHECK SETTINGS"
        };
        public static String[] top2warnings = new String[2];
        public static int currentWarningsLength;
        public static int warningHigh;
        public static int warningMed;
        public static int warningLow;
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
        public static int packetType;
        public static String mode;
        public static float graphPressure;
        public static float graphFlow;
        public static short graphVolume;
        public static float pInsp;
        public static float pMean;
        public static float pPeak;
        public static float vt;
        public static float peep;
        public static float peepMax;
        public static float peepMin;
        public static float vtMin;
        public static float vtMax;
        public static float rateMax;
        public static float rateMin;
        public static float rateMeasured;
        public static byte fio2;
        public static byte fio2Max;
        public static byte fio2Min;
        public static float rSpont;
        public static byte breathingType;
        public static byte shutdownPress;
        public static byte shutdownPressState;
    }

    public static class MainActivityValues {
        public static final byte SIGH_HIDDEN = 0;
        public static final byte SIGH_SHOWN = 1;
        public static final byte SIGH_BREATH = 2;
        public static final byte SILENCED = 0;
        public static final byte UNSILENCED = 1;
        public static final byte LOCKED = 2;
        public static final byte UNLOCKED = 1;
        public static byte sighHold;
        public static byte sighState = SIGH_HIDDEN;
        public static byte warningSilence;
        public static byte silenceState = UNSILENCED;
        public static byte lockState = UNLOCKED;
    }

    public static class LaunchVars {
        public static int calibrationStatus;
        public static int calibrationError;
    }

    public static class Monitoring {
        public static float pPlat;
        public static float autoPeep;
        public static float mvSpont;
        public static int ie;
        public static int phaseColor = R.color.insp;
        public static String phase;
        public static float leakVol;
        public static float leakPercent;
        public static float cStat;
        public static float ti;
        public static float te;
        public static int flowPeak;
        public static float mvTotal;

    }

    public static class AlarmLimits {
        public static float minVolMax = 60;
        public static float minVolMin = 0.5f;
        public static byte rateMax = 60;
        public static byte rateMin = 4;
        public static short vtMax = 1500;
        public static short vtMin = 50;
        public static float pMax = 60;
        public static float pMin = 0;
        public static short apnea = 12;

        public static float new_minVolMax = 60;
        public static float new_minVolMin = 0.5f;
        public static byte new_rateMax = 60;
        public static byte new_rateMin = 4;
        public static short new_vtMax = 1500;
        public static short new_vtMin = 50;
        public static float new_pMax = 60;
        public static float new_pMin = 0;
        public static short new_apnea = 12;
    }
}
