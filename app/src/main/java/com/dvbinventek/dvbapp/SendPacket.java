package com.dvbinventek.dvbapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class SendPacket {

//    short com_start; 					// COM_START; - Fixed pattern                                       0
//    short mode; 						// from 13...                                                       1
//    short setpap;                       //                                                                  2
//    short ipap;                         //                                                                  3
//    short epap;                         //                                                                  4
//    short vset;                         //                                                                  5
//    short frate; 				// in milli-seconds                                                         6
//    short ie; 				// in milli-seconds                                                             7
//    short tidalVolumeInspirationTime; 	// tidal volume raise time                                          8
//    short breathingTimeThreshold; 		// breathing time threshold to change modes (ms)                    9
//    short viTrigger; 					// trigger for inspiratory airflow (* 100)                          10
//    short viSetAirflow; 						// gain for expiratory airflow (* 100)                      11
//    short viDifferenceThreshold; 		// threshold to check if patient is breathing (* 100)               12
//    short zeroAirflowThreshold; 		// threshold to check if need to switch to emergency mode (* 100)   13
//    short modeChangeEmergency; 			// preferred mode change during emergency - (* 100)                 14
//    short maxFlowLpm; 					// max flow                                                         15
//    short minFlowLpm; 					// min flow                                                         16
//    short fio2; 						// fio2 set value (*100)                                            17
//    short iRamp;                     //                                                                     18
//    short patientTriggerFlow;			// simulated trigger flow                                           19
//    short patientTriggerTime;			// simulated trigger time                                           20
//    short reserved2;                                                                //                      21
//    short reserved3;                                                                //                      22
//    short reserved4;                                                                //                      23
//    short reserved5;                                                                //                      24
//    short resetModeIter;				// reset mode after preset number of iterations                     25
//    short resetModeIterIdx;				// mode iteration index, if>0, reset to required number             26
//    short loopOffsetDelay;				// add an offset delay after every loop in ms                       27
//    short reserved6[] = new short[10];                                                  //                  28-37
//    short warnings[] = new short[20];					// warnings                     //                  38-57
//    short inspExpHold;                          //                                                          58
//    short reserved7[]  = new short[40];                                                 //                  59-98
//    short com_stop; // COM_STOP;                                                             99


//    char[4] comStart; // 0
//    byte mode; // 4
//    byte fio2; // 5
//    short vtSet; // 6
//    byte pInsp; // 8
//    byte pLimit; // 9
//    byte peep; // 10
//    byte ps; // 11
//    float flowTrig; // 12
//    float rate; // 16
//    float I; // 20
//    float E; // 24
//    byte[3] reserved1; // 28
//    int modeChangeDuringApnea; // 31
//    float alarmMinMv; // 35
//    float alarmMaxMv; // 39
//    short alarmMinVt; // 43
//    short alarmMaxVt; // 45
//    float alarmMinPressure; // 47
//    float alarmMaxPressure; // 51
//    float alarmMaxPressureLimit; // 55
//    byte alarmMinRate; // 59
//    byte alarmMaxRate; // 60
//    byte alarmMaxApnea; // 61
//    byte pressureUnit; // 62
//    char[52] patientDetails; // 63
//    byte inspExpHold; // 115
//    byte sighHold; // 116
//    byte alarmSilence; // 117
//    byte alarmVolume; // 118
//    byte[52] reserved; // 119
//    byte[60] reservedDebug; // 171
//    short int pwm1InitWidth; // 231
//    short int pwm3InitWidth; // 233
//    short int pwm5InitWidth; // 235
//    short int pwm11InitWidth; // 237
//    byte[40] pidContants; // 239
//    char[4] comStop; // 279

    public static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    public static final char[] STRT = "STRT".toCharArray();
    public static final char[] STOP = "STOP".toCharArray();
    public static final char[] STBY = "STBY".toCharArray();
    public static final char[] SLFT = "SLFT".toCharArray();
    public static final char[] CLBF = "CLBF".toCharArray();
    public static final char[] RNTM = "RNTM".toCharArray();
    public static final char[] ALRM = "ALRM".toCharArray();
    public static final String STR_STRT = "STRT";
    public static final String STR_STOP = "STOP";
    public static final String STR_STBY = "STBY";
    public static final String STR_SLFT = "SLFT";
    public static final String STR_CLBF = "CLBF";
    public static final String STR_RNTM = "RNTM";
    public static final String STR_ALRM = "ALRM";
    public static final int TYPE_STRT = 1;
    public static final int TYPE_STOP = 2;
    public static final int TYPE_STBY = 3;
    public static final int TYPE_SLFT = 4;
    public static final int TYPE_CLBF = 5;
    public static final int TYPE_RNTM = 6;
    public static final int TYPE_ALRM = 7;

    public SharedPreferences sharedPreferences;
    public SharedPreferences.Editor editor;
    public Context context;
    ByteBuffer packet;
    UsbService service;

    public SendPacket() {
        this.service = StaticStore.service.get();
        packet = ByteBuffer.wrap(new byte[280]).order(ByteOrder.LITTLE_ENDIAN);// insert com_start and com_end
    }

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    public void writeInfo(byte b, int position) {
        if (packet != null)
            packet.put(position, b);
    }

    public void writeInfo(char[] b, int position) {
        if (packet != null)
            for (int i = 0; i < b.length; i++) {
                packet.put(position + i, (byte) b[i]);
            }
    }

    public void writeInfo(int b, int position) {
        if (packet != null)
            packet.putInt(position, b);
    }

    public void writeInfo(float b, int position) {
        if (packet != null)
            packet.putFloat(position, b);
    }

    public void writeInfo(short b, int position) {
        if (packet != null)
            packet.putShort(position, b);
    }

    public String getString() {
        return Arrays.toString(packet.array());
    }



    public boolean sendToDevice() {
//        if(packet == null){
//            Log.d("SENT_PACKET_ERR", "Could not send packet, packet is null");
//            return;
//        }
        if (StaticStore.restrictedCommunicationDueToStandby) return false;
        Log.d("SENT_PACKET_STRING", getString());
        if (service != null) {
            service.write(packet.array());
            return true;
        } else {
            Log.d("SENT_PACKET_ERR", "Could not send packet, service is null");
        }
        return false;
    }

    public void writeDefaultSTRTPacketValues() {
        //controls values
        writeInfo(SendPacket.STRT, 0);
        writeInfo(SendPacket.STRT, 276);
        writeInfo((byte) StaticStore.modeSelectedShort, 4);
        writeInfo((byte) (StaticStore.packet_fio2), 5);
        writeInfo(StaticStore.packet_vt, 6);
        writeInfo((byte) (StaticStore.packet_pinsp), 8);
        writeInfo((byte) (StaticStore.packet_peep), 10);
        writeInfo((byte) (StaticStore.packet_ps), 11);
        writeInfo(StaticStore.packet_flowTrig, 12);
        writeInfo(StaticStore.packet_rtotal, 16);
        writeInfo(((int) StaticStore.packet_ie / 100) / 10.0f, 20); // i
        writeInfo(((int) StaticStore.packet_ie % 100) / 10.0f, 24); // e
        writeInfo(StaticStore.packet_plimit, 9);
        writeInfo((byte) 27, 31);

        //alarm values
        writeInfo((byte) StaticStore.AlarmLimits.apnea, 58);
        writeInfo(StaticStore.AlarmLimits.minVolMin, 32);
        writeInfo(StaticStore.AlarmLimits.minVolMax, 36);
        writeInfo(StaticStore.AlarmLimits.vtMin, 40);
        writeInfo(StaticStore.AlarmLimits.vtMax, 42);
        writeInfo(StaticStore.AlarmLimits.pMin, 44);
        writeInfo(StaticStore.AlarmLimits.pMax, 48);
        writeInfo(StaticStore.AlarmLimits.rateMin, 56);
        writeInfo(StaticStore.AlarmLimits.rateMax, 57);
    }

    public void writeDefaultSTRTPacketValues(char[] header) {
        //controls values
        writeInfo(header, 0);
        writeInfo(header, 276);
        writeInfo((byte) StaticStore.modeSelectedShort, 4);
        writeInfo((byte) (StaticStore.packet_fio2), 5);
        writeInfo(StaticStore.packet_vt, 6);
        writeInfo((byte) (StaticStore.packet_pinsp), 8);
        writeInfo((byte) (StaticStore.packet_peep), 10);
        writeInfo((byte) (StaticStore.packet_ps), 11);
        writeInfo(StaticStore.packet_flowTrig, 12);
        writeInfo(StaticStore.packet_rtotal, 16);
        writeInfo(((int) StaticStore.packet_ie / 100) / 10.0f, 20); // i
        writeInfo(((int) StaticStore.packet_ie % 100) / 10.0f, 24); // e
        writeInfo(StaticStore.packet_plimit, 9);
        writeInfo((byte) 27, 31);

        //alarm values
        writeInfo((byte) StaticStore.AlarmLimits.apnea, 58);
        writeInfo(StaticStore.AlarmLimits.minVolMin, 32);
        writeInfo(StaticStore.AlarmLimits.minVolMax, 36);
        writeInfo(StaticStore.AlarmLimits.vtMin, 40);
        writeInfo(StaticStore.AlarmLimits.vtMax, 42);
        writeInfo(StaticStore.AlarmLimits.pMin, 44);
        writeInfo(StaticStore.AlarmLimits.pMax, 48);
        writeInfo(StaticStore.AlarmLimits.rateMin, 56);
        writeInfo(StaticStore.AlarmLimits.rateMax, 57);
    }
}
