package com.dvbinventek.dvbapp;

import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.lang.ref.WeakReference;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import cdflynn.android.library.checkview.CheckView;

import static com.dvbinventek.dvbapp.MainActivity.PACKET_LENGTH;

public class LaunchActivity extends AppCompatActivity {

    static String packet = "";
    public final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case UsbService.ACTION_USB_PERMISSION_GRANTED: // USB PERMISSION GRANTED
                    Toast.makeText(context, "Connected", Toast.LENGTH_LONG).show();
                    break;
                case UsbService.ACTION_USB_PERMISSION_NOT_GRANTED: // USB PERMISSION NOT GRANTED
                    Toast.makeText(context, "USB Permission not granted", Toast.LENGTH_LONG).show();
                    break;
                case UsbService.ACTION_NO_USB: // NO USB CONNECTED
                    Toast.makeText(context, "No USB connected", Toast.LENGTH_LONG).show();
                    break;
                case UsbService.ACTION_USB_DISCONNECTED: // USB DISCONNECTED
                    Toast.makeText(context, "USB disconnected", Toast.LENGTH_LONG).show();
                    break;
                case UsbService.ACTION_USB_NOT_SUPPORTED: // USB NOT SUPPORTED
                    Toast.makeText(context, "USB device not supported", Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };
    public UsbService usbService;
    public MyHandler mHandler;
    public final ServiceConnection usbConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            usbService = ((UsbService.UsbBinder) arg1).getService();
            usbService.setHandler(mHandler);
            StaticStore.service = new WeakReference<>(usbService);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            usbService = null;
        }
    };
    public ProgressBar pb;
    public CheckView cv;
    public Button start, skip;
    public int a;
    public DevicePolicyManager mDevicePolicyManager;
    public PackageManager mPackageManager;
    public ComponentName mAdminComponentName;
    TextView countdown;
    boolean gotCCPacket = false, gotDAPacketAgain = false;
    ScheduledExecutorService tpe = new ScheduledThreadPoolExecutor(1);
    ScheduledExecutorService ses = new ScheduledThreadPoolExecutor(1);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        a = 10;
        mHandler = new MyHandler(this);
        super.onCreate(savedInstanceState);
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        mDevicePolicyManager = (DevicePolicyManager)
                getSystemService(Context.DEVICE_POLICY_SERVICE);
        mAdminComponentName = DeviceAdminReceiver.getComponentName(this);
        mPackageManager = this.getPackageManager();

        setContentView(R.layout.activity_launch);
        start = findViewById(R.id.start);
        skip = findViewById(R.id.skip);
        countdown = findViewById(R.id.countdown);

        skip.setOnClickListener(v -> {
            tpe.shutdown();
            ses.shutdown();
            MediaPlayer mp = MediaPlayer.create(this, R.raw.button_press);
            mp.start();
            if (!MainActivity.mainActivityActive) {
                Log.d("MSG", "Skip button clicked");
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                finish();
            }
        });
        tpe.scheduleAtFixedRate(() -> {
            if (a == 1) {
                countdown.setText(R.string.auto_skip_1_sec);
                skip.callOnClick();
            } else {
                countdown.setText(getString(R.string.auto_skip_in_i_seconds, a));
            }
            a--;
        }, 1, 1, TimeUnit.SECONDS);
        start.setOnClickListener(v -> {
            //TODO: Write packet logic
//            tpe.shutdownNow();
//            countdown.setVisibility(View.INVISIBLE);
//            cv.uncheck();
//            start.setEnabled(false);
//            pb.setVisibility(View.VISIBLE);
//            SendPacket sp = new SendPacket();
//            sp.writeInfo((short)204, 0);
//            if(usbService != null) {
//                Log.d("SENT_PACKET", Arrays.toString(sp.packet));
//                usbService.write(shortToByte(sp.packet));
//            } else {
//                Log.d("SENT_PACKET_ERR", "Could not send packet, service is null");
//            }
//            ses.scheduleAtFixedRate(() -> {
//                Log.d("MSG", "Checking for calibration");
//                if(gotDAPacketAgain) {
//                    Log.d("MSG", "Calibrated!");
//                    skip.callOnClick();
//                }
//            }, 10, 1, TimeUnit.SECONDS);
        });
    }

    void startMainActivityLockTaskMode() {
        if (MainActivity.mainActivityActive) return;
        if (mDevicePolicyManager.isDeviceOwnerApp(
                getApplicationContext().getPackageName())) {
            Intent lockIntent = new Intent(getApplicationContext(),
                    MainActivity.class);

            mPackageManager.setComponentEnabledSetting(
                    new ComponentName(getApplicationContext(),
                            MainActivity.class),
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP);
            startActivity(lockIntent);
            finish();
        } else {
            Log.d("MSG", "App not whitelisted as LockTask");
        }
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

    @Override
    protected void onResume() {
        super.onResume();
        setFilters();
        startService(UsbService.class, usbConnection, null); // Start UsbService(if it was not started before) and Bind it
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mUsbReceiver);
        unbindService(usbConnection);

    }

    public void startService(Class<?> service, ServiceConnection serviceConnection, Bundle extras) {
        if (!UsbService.SERVICE_CONNECTED) {
            Intent startService = new Intent(this, service);
            if (extras != null && !extras.isEmpty()) {
                Set<String> keys = extras.keySet();
                for (String key : keys) {
                    String extra = extras.getString(key);
                    startService.putExtra(key, extra);
                }
            }
            startService(startService);
        }
        Intent bindingIntent = new Intent(this, service);
        bindService(bindingIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        StaticStore.service = new WeakReference<>(usbService);
    }

    public void setFilters() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbService.ACTION_USB_PERMISSION_GRANTED);
        filter.addAction(UsbService.ACTION_NO_USB);
        filter.addAction(UsbService.ACTION_USB_DISCONNECTED);
        filter.addAction(UsbService.ACTION_USB_NOT_SUPPORTED);
        filter.addAction(UsbService.ACTION_USB_PERMISSION_NOT_GRANTED);
        registerReceiver(mUsbReceiver, filter);
    }

    public void handleData(String d) {
        if (packet.length() == 0) {
            packet = d;
        } else if (packet.length() < PACKET_LENGTH) {
            packet = packet + d;
        } else if (packet.length() == PACKET_LENGTH) {
            Log.d("PACKET_LAUNCH", packet);
            if (packet.charAt(0) == 'C' && packet.charAt(1) == 'C') {
                gotCCPacket = true;
            }
            if (gotCCPacket) {
                if (packet.charAt(0) == 'D' && packet.charAt(1) == 'A' && gotCCPacket) {
                    cv.check();
                    gotDAPacketAgain = true;
                }
            }
            packet = "";
        } else {
            packet = "";
            Log.d("MSG", "DROPPED");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        tpe.shutdownNow();
        ses.shutdownNow();
    }

    public class MyHandler extends Handler {
        public final WeakReference<LaunchActivity> mActivity;
        public final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

        public MyHandler(LaunchActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UsbService.MESSAGE_FROM_SERIAL_PORT:
                    byte[] data = (byte[]) msg.obj;
                    handleData(bytesToHex(data));
                    break;
                case UsbService.CTS_CHANGE:
                    Toast.makeText(mActivity.get(), "CTS_CHANGE", Toast.LENGTH_LONG).show();
                    break;
                case UsbService.DSR_CHANGE:
                    Toast.makeText(mActivity.get(), "DSR_CHANGE", Toast.LENGTH_LONG).show();
                    break;
            }
        }

        public String bytesToHex(byte[] bytes) {
            char[] hexChars = new char[bytes.length * 2];
            for (int j = 0; j < bytes.length; j++) {
                int v = bytes[j] & 0xFF;
                hexChars[j * 2] = HEX_ARRAY[v >>> 4];
                hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
            }
            return new String(hexChars);
        }
    }
}