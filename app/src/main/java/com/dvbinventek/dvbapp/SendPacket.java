package com.dvbinventek.dvbapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.Arrays;

public class SendPacket {

    public static final int PACKET_LENGTH = 100;

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


    //    char[4] comStart;
//    byte mode;
//    byte fio2;
//    short vtSet;
//    byte pInsp;
//    byte pLimit;
//    byte peep;
//    byte ps;
//    float flowTrig;
//    float rate;
//    float I;
//    float E;
//    byte[3] reserved1;
//    int modeChangeDuringApnea;
//    float alarmMinMv;
//    float alarmMaxMv;
//    short alarmMinVt;
//    short alarmMaxVt;
//    float alarmMinPressure;
//    float alarmMaxPressure;
//    float alarmMaxPressureLimit;
//    byte alarmMinRate;
//    byte alarmMaxRate;
//    byte alarmMaxApnea;
//    byte pressureUnit;
//    char[52] patientDetails;
//    byte inspExpHold;
//    byte sighHold;
//    byte alarmSilence;
//    byte alarmVolume;
//    byte[52] reserved;
//    byte[60] reservedDebug;
//    short int pwm1InitWidth;
//    short int pwm3InitWidth;
//    short int pwm5InitWidth;
//    short int pwm11InitWidth;
//    byte[40] pidContants;
//    char[4] comStop;
    public static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    public SharedPreferences sharedPreferences;
    public SharedPreferences.Editor editor;
    public Context context;
    short[] packet;
    UsbService service;

    public SendPacket() {
        this.service = StaticStore.service.get();
        packet = new short[100];
        writeInfo((short) 170, 0);
        writeInfo((short) 170, 99); // insert com_start and com_end
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

    public String getLastSentPacket() {
        return sharedPreferences.getString("packetStore", "");
    }

    public void setLastSentPacket(String s) {
        editor.putString("packetStore", s);
        editor.commit();
    }

    public int getLength() {
        return PACKET_LENGTH;
    }

    public void writeInfo(short b, int position) {
        if (packet != null)
            packet[position] = b;
    }

    public String getString() {
        return bytesToHex(shortToByte(packet));
    }

    public byte[] shortToByte(short[] input) {
        int short_index, byte_index;
        int iterations = input.length;
        byte[] buffer = new byte[input.length * 2];
        short_index = byte_index = 0;
        for (/*NOP*/; short_index != iterations; /*NOP*/) {
            buffer[byte_index] = (byte) (input[short_index] & 0x00FF);
            buffer[byte_index + 1] = (byte) ((input[short_index] & 0xFF00) >> 8);
            ++short_index;
            byte_index += 2;
        }
        return buffer;
    }

    public boolean sendToDevice() {
//        if(packet == null){
//            Log.d("SENT_PACKET_ERR", "Could not send packet, packet is null");
//            return;
//        }
        Log.d("SENT_PACKET", Arrays.toString(packet));
        Log.d("SENT_PACKET_STRING", getString());
        if (service != null) {
            service.write(shortToByte(packet));
            return true;
        } else {
            Log.d("SENT_PACKET_ERR", "Could not send packet, service is null");
        }
        return false;
    }

}
