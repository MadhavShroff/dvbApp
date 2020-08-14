package com.dvbinventek.dvbapp;

import java.nio.ByteBuffer;

public class ReceivePacket {

//    short[] packet;
//    short com_start; 				// COM_START                    0
//    short currentMode; 				// current 	mode of operation   1
//    // short reserved1;                                             2
//    short fio2; 			// 	oxygen mask flow or fio2            3 // Previously oxygenMaskFlow
//    short pp; 						// barometric pressure          4
//    short pi; 						// inspiratory pressure         5
//    short pe;  						// expiratory pressure          6
//    short pb; 						// blower pressure              7
//    short pev;  					// 	exhalation valve pressure   8
//    short vi;  						// inspiratory airflow          9
//    short ve;						// expiratory airflow           10
//    short vo;						// oxygen airflow               11
//    short vTidalFlow;  				// tidal airflow                12
//    short viMean; 					// inspiratory airflow mean     13
//    short veMean; 					// 	inspiratory airflow mean    14
//    short vFlow;					// measured 	leak in system  15
//    short ti; 						// inspiratory time             16
//    short te; 						//expiratory Time               17
//    short ts;    //Auto Machine Breathing start 	time in ms      18
//    short reserved2;                                             19
//    short reserved3;                                                 20
//    short reserved4;                                                 21
//    short warningMode;                           //                 22
//    short ~temperatureInspiratory~; -> Rspont 	//int with resolution 	of 0.1  23
//    short humidity; 				//int with resolution 	of 0.1  24
//    short breathingType; // 1 for human breathing, 0 for machine breathing   25
//    short breathingPhase; 			// 1 for inhalation, -1 for exhalation and 0 	for not breathing   26
//    short viTotal; 					// total volume 	during inspiratory phase    27
//    short veTotal;  				// total volume 	during expiratory phase     28
//    short reserved6 ;               //                              29
//    short reserved7 ;                               //              30
//    short bpmMeasured;  		// measured bpm                     31
//    short expMinVolMeasured; 	// 	minute volume measured          32
//    short pPeak;                                   //               33
//    short pMin;                                             //      34
//    short pMax;                  //                                 35
//    short emvMin;                //                                 36
//    short emvMax;                //                                 37
//    short vtMin;                //                                  38
//    short vtMax;                //                                  39
//    short fMin;                //                                   40
//    short fMax;                //                                   41
//    short fio2Min;                //                                42
//    short fio2Max;                //                                43
//    short inspExpSighHold;        //                                44
//    short pPlat;            //                                      45
//    short pMean;            //                                      46
//    short autoPeep;             //                                  47
//    short emvSpont;               //                                48
//    short ieMeasured;           //                                  49
//    short leakVolume;           //                                  50
//    short leakPercentage;       //                                  51
//    short rInsp;                //                                  52
//    short cStat;                //                                  53
//    short flowPeak;             //                                  54
//    short com_stop; 				// COM_STOP                     55

//    char[4] comStart;		// 0
//    byte mode;		// 4
//    float graphPressure;		// 5
//    float graphFlow;		// 9
//    short graphVolume;		// 13
//    float pInsp;		// 15
//    float pPeak;		// 19
//    float pMean;		// 23
//    float peep;		// 27
//    float peepMax;		// 31
//    float peepMin;		// 35
//    short vt;		// 39
//    short vtMax;		// 41
//    short vtMin;		// 43
//    float rateTotal;		// 45
//    float rateTotalMax;		// 49
//    float rateTotalMin;		// 53
//    byte fio2;		// 57
//    byte fio2Max;		// 58
//    byte fio2Min;		// 59
//    short vti;		// 60
//    short vte;		// 62
//    float rateSpont;		// 64
//    float mvTotal;		// 68
//    float mvSpont;		// 72
//    float tInsp;		// 76
//    float tExp;		// 80
//    float I;		// 84
//    float E;		// 88
//    short leakVolume;		// 92
//    byte leakPercentage;		// 94
//    float cStat;		// 95
//    byte flowPeak;		// 99
//    float pPlat;		// 100
//    float autoPeep;		// 104
//    byte breathingType;		// 108
//    byte breathingPhase;		// 109
//    byte[20] reserved;		// 110
//    float calibrationStatus;		// 130
//    float calibrationError;		// 134
//    float warningHigh;		// 138
//    float warningMedium;		// 142
//    float warningLow;		// 146
//    byte sighHold;		// 150
//    byte warningSilence;		// 151
//    byte warningSync;		// 152
//    byte batteryStatus;		// 153
//    byte batteryPower;		// 154
//    byte powerOffRequest;		// 155
//    byte[24] maintenenceDisplay;		// 156
//    byte[20] versionDisplay;		// 180
//    short ts;		// 200
//    short pi;		// 202
//    short pe;		// 204
//    short pb;		// 206
//    short pev;		// 208
//    short fi;		// 210
//    short fe;		// 212
//    short fo;		// 214
//    short fTidal;		// 216
//    short pwmWidthBlower;		// 218
//    short pwmWidthInspiratoryValve;		// 220
//    short pwmWidthInspiratoryValveExhale;		// 222
//    short pwmWidthexhalationValve;		// 224
//    short pwmWidthexhalationValveExhale;		// 226
//    byte[18] reservedDebug;		// 228
//    char[4] comStop			// 246


//    public void init(short[] packet) {
//        if(packet.length == 0) return;
////        com_start = packet[0];
//        StaticStore.Values.pPeak = Short.reverseBytes(packet[33])/100.0f;
//        StaticStore.Values.pp = Short.reverseBytes(packet[4])/100.0f;
//        StaticStore.Values.pMax = Short.reverseBytes(packet[35])/100.0f;
//        StaticStore.Values.pMin = Short.reverseBytes(packet[34])/100.0f;
//        StaticStore.Values.expMinVolMeasured = Short.reverseBytes(packet[32])/100.0f;
//        StaticStore.Values.vTidalFlow = Short.reverseBytes(packet[12]);
//        StaticStore.Values.emvMax = Short.reverseBytes(packet[37])/100.0f;
//        StaticStore.Values.emvMin = Short.reverseBytes(packet[36])/100.0f;
//        StaticStore.Values.vtMax = Short.reverseBytes(packet[39]);
//        StaticStore.Values.vtMin = Short.reverseBytes(packet[38]);
//        StaticStore.Values.bpmMeasured = Short.reverseBytes(packet[31])/100.0f;
//        StaticStore.Values.fMax = Short.reverseBytes(packet[41])/100.0f;
//        StaticStore.Values.fMin = Short.reverseBytes(packet[40])/100.0f;
//        StaticStore.Values.fio2 = (Short.reverseBytes(packet[3]))/100.0f;
//        StaticStore.Values.fio2Max = Short.reverseBytes(packet[43])/100.0f;
//        StaticStore.Values.fio2Min = Short.reverseBytes(packet[42])/100.0f;
//        StaticStore.Values.vFlow = Short.reverseBytes(packet[15])/100.0f;
//        StaticStore.Values.veTotal = Short.reverseBytes(packet[28]);
//        StaticStore.Values.viTotal = Short.reverseBytes(packet[27]);
//        StaticStore.Values.rSpont = (short) (Short.reverseBytes(packet[23])/100);
//        StaticStore.Values.mode = getMode(Short.reverseBytes(packet[1]));
//
//        //Set Warnings
//        StaticStore.Warnings.warningMode = Short.reverseBytes(packet[22]);
////        StaticStore.Warnings.warningMode = a;
//        getWarningModeString(StaticStore.Warnings.warningMode); // set current warnings
//
//        //Set Monitoring values
//        StaticStore.Monitoring.pPlat = Short.reverseBytes(packet[45])/100f;
//        StaticStore.Monitoring.pMean = Short.reverseBytes(packet[46])/100f;
//        StaticStore.Monitoring.autoPeep = Short.reverseBytes(packet[47])/100f;
//        StaticStore.Monitoring.vte = StaticStore.Values.vTidalFlow;
//        StaticStore.Monitoring.mvSpont = Short.reverseBytes(packet[48])/1000f;
//        StaticStore.Monitoring.ie = Short.reverseBytes(packet[49]);
//        if(Short.reverseBytes(packet[26]) == 1) {
//            StaticStore.Monitoring.phase = "Insp";
//            StaticStore.Monitoring.phaseColor = R.color.blue;
//        } else {
//            StaticStore.Monitoring.phase = "Exp";
//            StaticStore.Monitoring.phaseColor = R.color.Red1;
//        }
//        StaticStore.Monitoring.leakVol = Short.reverseBytes(packet[50]);
//        StaticStore.Monitoring.leakPercent = Short.reverseBytes(packet[51]);
//        StaticStore.Monitoring.rInsp = Short.reverseBytes(packet[52]);
//        StaticStore.Monitoring.cStat = Short.reverseBytes(packet[53]);
//        StaticStore.Monitoring.ti = Short.reverseBytes(packet[16])/1000.0f;
//        StaticStore.Monitoring.te = Short.reverseBytes(packet[17])/1000.0f;
//        StaticStore.Monitoring.flowPeak = Short.reverseBytes(packet[54])/100.0f;
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
//    }

    ReceivePacket(byte[] s) {
        try {
            init(ByteBuffer.wrap(s));
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


        //TODO: populate values from byte buffer
//        com_start = packet[0];

//        StaticStore.Values.pPeak = Short.reverseBytes(packet[33])/100.0f;
//        StaticStore.Values.pp = Short.reverseBytes(packet[4])/100.0f;
//        StaticStore.Values.pMax = Short.reverseBytes(packet[35])/100.0f;
//        StaticStore.Values.pMin = Short.reverseBytes(packet[34])/100.0f;
//        StaticStore.Values.expMinVolMeasured = Short.reverseBytes(packet[32])/100.0f;
//        StaticStore.Values.vTidalFlow = Short.reverseBytes(packet[12]);
//        StaticStore.Values.emvMax = Short.reverseBytes(packet[37])/100.0f;
//        StaticStore.Values.emvMin = Short.reverseBytes(packet[36])/100.0f;
//        StaticStore.Values.vtMax = Short.reverseBytes(packet[39]);
//        StaticStore.Values.vtMin = Short.reverseBytes(packet[38]);
//        StaticStore.Values.bpmMeasured = Short.reverseBytes(packet[31])/100.0f;
//        StaticStore.Values.fMax = Short.reverseBytes(packet[41])/100.0f;
//        StaticStore.Values.fMin = Short.reverseBytes(packet[40])/100.0f;
//        StaticStore.Values.fio2 = (Short.reverseBytes(packet[3]))/100.0f;
//        StaticStore.Values.fio2Max = Short.reverseBytes(packet[43])/100.0f;
//        StaticStore.Values.fio2Min = Short.reverseBytes(packet[42])/100.0f;
//        StaticStore.Values.vFlow = Short.reverseBytes(packet[15])/100.0f;
//        StaticStore.Values.veTotal = Short.reverseBytes(packet[28]);
//        StaticStore.Values.viTotal = Short.reverseBytes(packet[27]);
//        StaticStore.Values.rSpont = (short) (Short.reverseBytes(packet[23])/100);
//        StaticStore.Values.mode = getMode(Short.reverseBytes(packet[1]));
//
//        //Set Warnings
//        StaticStore.Warnings.warningMode = Short.reverseBytes(packet[22]);
////        StaticStore.Warnings.warningMode = a;
//        getWarningModeString(StaticStore.Warnings.warningMode); // set current warnings
//
//        //Set Monitoring values
//        StaticStore.Monitoring.pPlat = Short.reverseBytes(packet[45])/100f;
//        StaticStore.Monitoring.pMean = Short.reverseBytes(packet[46])/100f;
//        StaticStore.Monitoring.autoPeep = Short.reverseBytes(packet[47])/100f;
//        StaticStore.Monitoring.vte = StaticStore.Values.vTidalFlow;
//        StaticStore.Monitoring.mvSpont = Short.reverseBytes(packet[48])/1000f;
//        StaticStore.Monitoring.ie = Short.reverseBytes(packet[49]);
//        if(Short.reverseBytes(packet[26]) == 1) {
//            StaticStore.Monitoring.phase = "Insp";
//            StaticStore.Monitoring.phaseColor = R.color.blue;
//        } else {
//            StaticStore.Monitoring.phase = "Exp";
//            StaticStore.Monitoring.phaseColor = R.color.Red1;
//        }
//        StaticStore.Monitoring.leakVol = Short.reverseBytes(packet[50]);
//        StaticStore.Monitoring.leakPercent = Short.reverseBytes(packet[51]);
//        StaticStore.Monitoring.rInsp = Short.reverseBytes(packet[52]);
//        StaticStore.Monitoring.cStat = Short.reverseBytes(packet[53]);
//        StaticStore.Monitoring.ti = Short.reverseBytes(packet[16])/1000.0f;
//        StaticStore.Monitoring.te = Short.reverseBytes(packet[17])/1000.0f;
//        StaticStore.Monitoring.flowPeak = Short.reverseBytes(packet[54])/100.0f;
    }
//
//    ReceivePacket(String s) {
//        short[] packet = new short[s.length()/4];
//        try {
//            byte[] bytes = Hex.decodeHex(s.toCharArray());
//            ByteBuffer.wrap(bytes).asShortBuffer().get(packet);
//        } catch (DecoderException e) {
//            e.printStackTrace();
//        }
//        init(packet);
//    }

    public void getWarningModeString(short c) {
        StaticStore.Warnings.top2warnings[0] = "";
        StaticStore.Warnings.top2warnings[1] = "";
        StaticStore.Warnings.currentWarnings.clear();
        int num = 0;
        String ithWarning;
        for (int i = 0; i < StaticStore.Warnings.warningTypeCount; ++i) {
            if (((c & 1 << i) >> i) == 1) { //i the ith bit of the short is set
                StaticStore.Warnings.currentWarningsLength++;
                ithWarning = StaticStore.Warnings.warnings[i];
                StaticStore.Warnings.currentWarnings.add(ithWarning);
                if (num++ < 2) StaticStore.Warnings.top2warnings[num - 1] = ithWarning;
            }
        }
    }
}
