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
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.ProgressIndicator;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import cdflynn.android.library.checkview.CheckView;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.PublishSubject;

import static com.dvbinventek.dvbapp.MainActivity.PACKET_LENGTH;

public class LaunchActivity extends AppCompatActivity {

    public static final PublishSubject<byte[]> packetSubject = PublishSubject.create();
    public DevicePolicyManager mDevicePolicyManager;
    public PackageManager mPackageManager;
    public ComponentName mAdminComponentName;
    ScheduledExecutorService tpe = new ScheduledThreadPoolExecutor(1);
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
    public int a; // Countdown
    public CompositeDisposable disposables = new CompositeDisposable();
    public byte[] packet = {};
    public int progress;
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
    Observable<Long> packetEmitter = Observable.empty();
    boolean isChecked1 = false;
    boolean isChecked2 = false;
    boolean isChecked3 = false;
    boolean isChecked4 = false;
    boolean isChecked5 = false;
    boolean isChecked6 = false;
    boolean isStartClicked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        a = 30;
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
        progress = 0;

        //Setup Start, skip buttons, countdown text
        setupButtons();

        StaticStore.LaunchVars.calibrationError = 0;
        StaticStore.LaunchVars.calibrationStatus = 0;

        packetSubject.subscribe(new Observer<byte[]>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
                disposables.add(d);
            }

            @Override
            public void onNext(byte @NonNull [] bytes) {
                if (packet.length == 0)
                    packet = Arrays.copyOf(bytes, bytes.length);
                else if (packet.length < PACKET_LENGTH)
                    packet = joinArrays(packet, bytes);
                if (packet.length == PACKET_LENGTH) {
                    new ReceivePacket(packet, true);
                    updateProgressBar();
                    Log.d("PACKET_LAUNCH", Arrays.toString(packet));
                    packet = new byte[]{};
                } else if (packet.length > PACKET_LENGTH) {
                    Log.d("PACKET_DROPPED", Arrays.toString(packet));
                    packet = new byte[]{};
                }
            }

            @Override
            public void onError(@NonNull Throwable e) {
                e.printStackTrace();
            }

            @Override
            public void onComplete() {
            }
        });
        packetEmitter.subscribe();
    }

    public void updateProgressBar() {
        int err = StaticStore.LaunchVars.calibrationError;
        int status = StaticStore.LaunchVars.calibrationStatus;
        Log.d("PROGRESS", "Error:" + Integer.toBinaryString(StaticStore.LaunchVars.calibrationError) + " Status:" + Integer.toBinaryString(StaticStore.LaunchVars.calibrationStatus));
        if (status > 0 && isStartClicked && status < 64) {
            for (int i = 0; i < 6; i++)
                if (((status & 1 << i) >> i) == 1)
                    checkIt(i);
            for (int i = 0; i < 24; i++) {
                if (((err & 1 << i) >> i) == 1) {
                    showErrors(i);
                }
            }
        }
    }

    public void showErrors(int i) {
        if (i >= 0 && i < 4) {
            setVisibility(R.id.check1, View.GONE);
            setVisibility(R.id.progress_bar1, View.GONE);
            setVisibility(R.id.cross1, View.VISIBLE);
        } else if (i >= 4 && i < 8) {
            setVisibility(R.id.check2, View.GONE);
            setVisibility(R.id.progress_bar2, View.GONE);
            setVisibility(R.id.cross2, View.VISIBLE);
        } else if (i >= 8 && i < 12) {
            setVisibility(R.id.check3, View.GONE);
            setVisibility(R.id.progress_bar3, View.GONE);
            setVisibility(R.id.cross3, View.VISIBLE);
        } else if (i >= 12 && i < 16) {
            setVisibility(R.id.check4, View.GONE);
            setVisibility(R.id.progress_bar4, View.GONE);
            setVisibility(R.id.cross4, View.VISIBLE);
        } else if (i >= 16 && i < 20) {
            setVisibility(R.id.check5, View.GONE);
            setVisibility(R.id.progress_bar5, View.GONE);
            setVisibility(R.id.cross5, View.VISIBLE);
        } else if (i >= 20 && i < 24) {
            setVisibility(R.id.check6, View.GONE);
            setVisibility(R.id.progress_bar6, View.GONE);
            setVisibility(R.id.cross6, View.VISIBLE);
        }
        switch (i) {
            case 0:
                setVisibility(R.id.sft_error0x1, View.VISIBLE);
                break;
            case 1:
                setVisibility(R.id.sft_error0x2, View.VISIBLE);
                break;
            case 2:
                setVisibility(R.id.sft_error0x4, View.VISIBLE);
                break;
            case 3:
                setVisibility(R.id.sft_error0x8, View.VISIBLE);
                break;
            case 4:
                setVisibility(R.id.sft_error0x10, View.VISIBLE);
                break;
            case 5:
                setVisibility(R.id.sft_error0x20, View.VISIBLE);
                break;
            case 6:
                setVisibility(R.id.sft_error0x40, View.VISIBLE);
                break;
            case 8:
                setVisibility(R.id.sft_error0x100, View.VISIBLE);
                break;
            case 12:
                setVisibility(R.id.sft_error0x1000, View.VISIBLE);
                break;
            case 20:
                setVisibility(R.id.sft_error0x100000, View.VISIBLE);
                break;
            case 21:
                setVisibility(R.id.sft_error0x200000, View.VISIBLE);
                break;
        }
    }

    public void updateProgressBar(int i) {
        progress += i;
        ((TextView) findViewById(R.id.progressIndicatorText)).setText(progress + "%");
        ((ProgressIndicator) findViewById(R.id.progressIndicator)).setProgress(progress);
        if (progress == 100) {
            SendPacket sp = new SendPacket();
            sp.writeInfo(SendPacket.STOP, 0);
            sp.writeInfo(SendPacket.STOP, 276);
            findViewById(R.id.skip).setBackgroundColor(getResources().getColor(R.color.yellow));
            ((MaterialButton) findViewById(R.id.skip)).setText(R.string.next);
            findViewById(R.id.skip).setEnabled(true);
            Observable.interval(200, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.newThread()).take(10).subscribe(new Observer<Long>() {
                Disposable disposeAttempts;

                @Override
                public void onSubscribe(@NonNull Disposable d) {
                    sp.sendToDevice();
                    disposeAttempts = d;
                }

                @Override
                public void onNext(@NonNull Long aLong) {
                    if (StaticStore.Values.packetType != SendPacket.TYPE_STOP) {
                        Log.d("STANDBY", "trying again... " + StaticStore.Values.packetType);
                        sp.sendToDevice();
                    } else {
                        Log.d("STANDBY", "Got packet of type stop");
                        //restrict use of other buttons to send any packet to device
                        StaticStore.restrictedCommunicationDueToStandby = true;
                        //disable standby button
                        disposeAttempts.dispose();
                    }
                }

                @Override
                public void onError(@NonNull Throwable e) {
                    e.printStackTrace();
                }

                @Override
                public void onComplete() {
                    Toast.makeText(getApplicationContext(), "ERR: Cannot communicate with device", Toast.LENGTH_SHORT).show();
                    disposeAttempts.dispose();
                }
            });
        }
    }

    public void checkIt(int which) {
        switch (which) {
            case 0:
                setVisibility(R.id.progress_bar1, View.GONE);
                if (!isChecked1) {
                    ((CheckView) findViewById(R.id.check1)).setVisibility(View.VISIBLE);
                    ((CheckView) findViewById(R.id.check1)).check();
                    isChecked1 = true;
                    updateProgressBar(16);
                }
                break;
            case 1:
                setVisibility(R.id.progress_bar2, View.GONE);
                if (!isChecked2) {
                    ((CheckView) findViewById(R.id.check2)).setVisibility(View.VISIBLE);
                    ((CheckView) findViewById(R.id.check2)).check();
                    isChecked2 = true;
                    updateProgressBar(17);
                }
                break;
            case 2:
                setVisibility(R.id.progress_bar3, View.GONE);
                if (!isChecked3) {
                    ((CheckView) findViewById(R.id.check3)).setVisibility(View.VISIBLE);
                    ((CheckView) findViewById(R.id.check3)).check();
                    isChecked3 = true;
                    updateProgressBar(16);
                }
                break;
            case 3:
                setVisibility(R.id.progress_bar4, View.GONE);
                if (!isChecked4) {
                    ((CheckView) findViewById(R.id.check4)).setVisibility(View.VISIBLE);
                    ((CheckView) findViewById(R.id.check4)).check();
                    isChecked4 = true;
                    updateProgressBar(17);
                }
                break;
            case 4:
                setVisibility(R.id.progress_bar5, View.GONE);
                if (!isChecked5) {
                    ((CheckView) findViewById(R.id.check5)).setVisibility(View.VISIBLE);
                    ((CheckView) findViewById(R.id.check5)).check();
                    isChecked5 = true;
                    updateProgressBar(17);
                }
                break;
            case 5:
                setVisibility(R.id.progress_bar6, View.GONE);
                if (!isChecked6) {
                    ((CheckView) findViewById(R.id.check6)).setVisibility(View.VISIBLE);
                    ((CheckView) findViewById(R.id.check6)).check();
                    isChecked6 = true;
                    updateProgressBar(17);
                }
                break;
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

    public void setupButtons() {
        TextView start = findViewById(R.id.start);
        TextView skip = findViewById(R.id.skip);
        TextView countdown = findViewById(R.id.countdown);
        TextView progressText = findViewById(R.id.progressIndicatorText);
        ProgressIndicator progressCircle = findViewById(R.id.progressIndicator);
        progressText.setText("");
        progressCircle.setProgress(0);

        ((CheckView) findViewById(R.id.check1)).uncheck();
        ((CheckView) findViewById(R.id.check2)).uncheck();
        ((CheckView) findViewById(R.id.check3)).uncheck();
        ((CheckView) findViewById(R.id.check4)).uncheck();
        ((CheckView) findViewById(R.id.check5)).uncheck();
        ((CheckView) findViewById(R.id.check6)).uncheck();
        setVisibility(R.id.progress_bar1, View.GONE);
        setVisibility(R.id.progress_bar2, View.GONE);
        setVisibility(R.id.progress_bar3, View.GONE);
        setVisibility(R.id.progress_bar4, View.GONE);
        setVisibility(R.id.progress_bar5, View.GONE);
        setVisibility(R.id.progress_bar6, View.GONE);
        isChecked1 = false;
        isChecked2 = false;
        isChecked3 = false;
        isChecked4 = false;
        isChecked5 = false;
        isChecked6 = false;
        setVisibility(R.id.cross1, View.GONE);
        setVisibility(R.id.cross2, View.GONE);
        setVisibility(R.id.cross3, View.GONE);
        setVisibility(R.id.cross4, View.GONE);
        setVisibility(R.id.cross5, View.GONE);
        setVisibility(R.id.cross6, View.GONE);

        skip.setOnClickListener(v -> {
            tpe.shutdown();
            MediaPlayer mp = MediaPlayer.create(this, R.raw.button_press);
            mp.start();
            if (!MainActivity.mainActivityActive) {
                Log.d("MSG", "Skipped Self Test");
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
//                startMainActivityLockTaskMode();
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
            isChecked1 = false;
            isChecked2 = false;
            isChecked3 = false;
            isChecked4 = false;
            isChecked5 = false;
            isChecked6 = false;
            isStartClicked = true;
            ((CheckView) findViewById(R.id.check1)).uncheck();
            ((CheckView) findViewById(R.id.check2)).uncheck();
            ((CheckView) findViewById(R.id.check3)).uncheck();
            ((CheckView) findViewById(R.id.check4)).uncheck();
            ((CheckView) findViewById(R.id.check5)).uncheck();
            ((CheckView) findViewById(R.id.check6)).uncheck();
            Log.d("SELF_TEST", "Started Self Test");
            skip.setEnabled(false);
            tpe.shutdownNow();
            countdown.setVisibility(View.INVISIBLE);
            start.setEnabled(false);
            new Handler().postDelayed(() -> start.setEnabled(true), 10000);
            progressText.setText("0%");
            progressCircle.setProgress(2);
            setVisibility(R.id.progress_bar1, View.VISIBLE);
            setVisibility(R.id.progress_bar2, View.VISIBLE);
            setVisibility(R.id.progress_bar3, View.VISIBLE);
            setVisibility(R.id.progress_bar4, View.VISIBLE);
            setVisibility(R.id.progress_bar5, View.VISIBLE);
            setVisibility(R.id.progress_bar6, View.VISIBLE);
            StaticStore.restrictedCommunicationDueToStandby = false;
            SendPacket sp = new SendPacket();
            sp.writeInfo(SendPacket.SLFT, 0);
            sp.writeInfo(SendPacket.SLFT, 276);
            sp.sendToDevice();
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        tpe.shutdownNow();
    }

    public void setVisibility(int id, int vis) {
        findViewById(id).setVisibility(vis);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (disposables != null)
            if (!disposables.isDisposed())
                disposables.dispose();
    }

    void startMainActivityLockTaskMode() {
        if (MainActivity.mainActivityActive) return;
        if (mDevicePolicyManager.isDeviceOwnerApp(
                getApplicationContext().getPackageName())) {
            Intent lockIntent = new Intent(getApplicationContext(),
                    MainActivity.class);

            mPackageManager.setComponentEnabledSetting(new ComponentName(getApplicationContext(), MainActivity.class),
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP);
            startActivity(lockIntent);
            finish();
        } else {
            Log.d("MSG", "App not whitelisted as LockTask");
        }
    }

    public byte[] joinArrays(byte[] array1, byte[] array2) {
        int aLen = array1.length;
        int bLen = array2.length;
        byte[] result = new byte[aLen + bLen];
        System.arraycopy(array1, 0, result, 0, aLen);
        System.arraycopy(array2, 0, result, aLen, bLen);
        return result;
    }

    public class MyHandler extends Handler {
        public final WeakReference<LaunchActivity> mActivity;

        public MyHandler(LaunchActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UsbService.MESSAGE_FROM_SERIAL_PORT:
                    byte[] data = (byte[]) msg.obj;
                    packetSubject.onNext(data);
                    break;
                case UsbService.CTS_CHANGE:
                    Toast.makeText(mActivity.get(), "CTS_CHANGE", Toast.LENGTH_LONG).show();
                    break;
                case UsbService.DSR_CHANGE:
                    Toast.makeText(mActivity.get(), "DSR_CHANGE", Toast.LENGTH_LONG).show();
                    break;
            }
        }
    }
}