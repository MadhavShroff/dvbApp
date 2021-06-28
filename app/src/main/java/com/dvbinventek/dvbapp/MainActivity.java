package com.dvbinventek.dvbapp;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.UserManager;
import android.provider.Settings;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.dvbinventek.dvbapp.customViews.MainParamsView;
import com.dvbinventek.dvbapp.graphing.DimTracePaletteProvider;
import com.dvbinventek.dvbapp.graphing.RightAlignedOuterVerticallyStackedYAxisLayoutStrategy;
import com.dvbinventek.dvbapp.viewPager.ControlsFragment;
import com.dvbinventek.dvbapp.viewPager.MonitoringFragment;
import com.dvbinventek.dvbapp.viewPager.SystemsFragment;
import com.dvbinventek.dvbapp.viewPager.ToolsFragment;
import com.dvbinventek.dvbapp.viewPager.ViewPagerFragmentAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.scichart.charting.layoutManagers.DefaultLayoutManager;
import com.scichart.charting.model.dataSeries.IDataSeries;
import com.scichart.charting.model.dataSeries.XyDataSeries;
import com.scichart.charting.visuals.ISciChartSurface;
import com.scichart.charting.visuals.SciChartSurface;
import com.scichart.charting.visuals.annotations.HorizontalLineAnnotation;
import com.scichart.charting.visuals.axes.AutoRange;
import com.scichart.charting.visuals.axes.NumericAxis;
import com.scichart.charting.visuals.pointmarkers.EllipsePointMarker;
import com.scichart.charting.visuals.renderableSeries.FastLineRenderableSeries;
import com.scichart.charting.visuals.renderableSeries.IRenderableSeries;
import com.scichart.core.framework.UpdateSuspender;
import com.scichart.core.model.DoubleValues;
import com.scichart.data.model.DoubleRange;
import com.scichart.data.model.SciListUtil;
import com.scichart.drawing.common.PenStyle;
import com.scichart.drawing.utility.ColorUtil;
import com.scichart.extensions.builders.SciChartBuilder;

import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.PublishSubject;

import static com.dvbinventek.dvbapp.ProcessPacket.silenceAlarm;
import static com.dvbinventek.dvbapp.ProcessPacket.silencedAlarm;
import static com.dvbinventek.dvbapp.StaticStore.Values.packetType;

public class MainActivity extends AppCompatActivity {

    //TODO: Make row list in historic data a recycler view
    //TODO: isInRange() in alarm limits

    //Tab Layout vars
    public static final int chartWidth = 665;
    public static final int viewPagerWidth = 352;
    public static final int PACKET_LENGTH = 300;

    //GraphView Screen Integer mapping
    public static final int GRAPHS = 1;
    public static final int STANDBY = 2;
    public static final int USB_DISCONNECTED = 3;
    public static final int USB_DISCONNECTED_POST_STANDBY = 4;

    public static WeakReference<LinearLayout> chartLayout; // 1
    public static WeakReference<LinearLayout> standbyLayout; // 2
    public static WeakReference<LinearLayout> usbDisconnectedLayout; // 3
    public static WeakReference<LinearLayout> usbDisconnectedPostLayout; // 4

    //For date format in data logs
    @SuppressLint("SimpleDateFormat")
    public static SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    //ViewPager vars
    static int ui_flags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
            View.SYSTEM_UI_FLAG_FULLSCREEN |
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
    public static final PublishSubject<byte[]> packetSubject = PublishSubject.create();
    public static final String dashes = "--";
    private static final int FIFO_CAPACITY = 100;
    public static boolean isSidebarShown = false;
    public static CompositeDisposable disposables = new CompositeDisposable();
    //Observe standby button in controls for a click
    public static Observer<View> standbyClickObserver;
    //setup shutdown alarm trigger
    public static Observer<Long> shutdownClickObserver;
    public static int callNumber = 0;
    static boolean mainActivityActive = false;
    //setup observer for hPa unit change
    public static Observer<String> hpaObserver;
    public static byte[] packet = {};

    public static DevicePolicyManager devicePolicyManager;
    public static ComponentName compName;

    public static View.OnClickListener sleepButtonListener = (v) -> {
        Log.d("MSG", "Clicked Sleep Button");
        if (devicePolicyManager.isAdminActive(compName)) {
            devicePolicyManager.lockNow();
        }
    };

    public static View.OnClickListener shutdownClickListener = (v) -> {
        Log.d("MSG", "Clicked Shutdown Button");
        Process chperm;
        try {
            chperm=Runtime.getRuntime().exec("su");
            DataOutputStream os =
                    new DataOutputStream(chperm.getOutputStream());
            os.writeBytes("am start -a com.android.internal.intent.action.REQUEST_SHUTDOWN\n");
            os.flush();
            chperm.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    };

    //chart vars
    public SciChartBuilder sciChartBuilder;
    public final XyDataSeries<Double, Double> pressureDataSeries = newDataSeries(FIFO_CAPACITY);
    public final XyDataSeries<Double, Double> pressureSweepDataSeries = newDataSeries(FIFO_CAPACITY);
    public final XyDataSeries<Double, Double> flowDataSeries = newDataSeries(FIFO_CAPACITY);
    public final XyDataSeries<Double, Double> flowSweepDataSeries = newDataSeries(FIFO_CAPACITY);
    public final XyDataSeries<Double, Double> volumeDataSeries = newDataSeries(FIFO_CAPACITY);
    public final XyDataSeries<Double, Double> volumeSweepDataSeries = newDataSeries(FIFO_CAPACITY);
    public final XyDataSeries<Double, Double> lastPressureSweepDataSeries = newDataSeries(1);
    public final XyDataSeries<Double, Double> lastFlowDataSeries = newDataSeries(1);
    public final XyDataSeries<Double, Double> lastVolumeDataSeries = newDataSeries(1);
    public static int graphShownRef = R.id.mainChart;
    public static UsbService usbService;
    public final XyDataSeries<Double, Double> FPDataSeries = newDataSeries(FIFO_CAPACITY / 2);
    public final XyDataSeries<Double, Double> FVDataSeries = newDataSeries(FIFO_CAPACITY / 2);
    public final XyDataSeries<Double, Double> PVDataSeries = newDataSeries(FIFO_CAPACITY / 2);
    public static Observer<String> usbStateObserver;
    public final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("BroadcastReceiver action", "" + Objects.requireNonNull(intent.getAction()));
            switch (Objects.requireNonNull(intent.getAction())) {
                case UsbService.ACTION_USB_PERMISSION_GRANTED: // USB PERMISSION GRANTED
                    Toast.makeText(context, "Connected to Ventilator", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_PERMISSION_NOT_GRANTED: // USB PERMISSION NOT GRANTED
                    Toast.makeText(context, "Permission not granted! Please enable USB Permissions", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_NO_USB: // NO USB CONNECTED
                    Toast.makeText(context, "No USB connected", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_DISCONNECTED: // USB DISCONNECTED
                    Toast.makeText(context, "USB disconnected", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_NOT_SUPPORTED: // USB NOT SUPPORTED
                    Toast.makeText(context, "USB device not supported", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_ATTACHED: // USB CONNECTED
                    Toast.makeText(context, "USB device Connected", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    //UsbService setup and vars
    public UsbHandler mHandler = new UsbHandler(this);
    public boolean usbConnected = false;
    public final ServiceConnection usbConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            usbService = ((UsbService.UsbBinder) arg1).getService();
            usbService.setHandler(mHandler);
            StaticStore.service = new WeakReference<>(usbService);
            usbConnected = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            usbService = null;
            usbConnected = false;
        }
    };

    //LockTask mode vars
    private DevicePolicyManager mDevicePolicyManager;
    private ComponentName mAdminComponentName;

    //LockButton vars
    public Disposable lockFlashDisposable;
    public ISciChartSurface mainChart, FPchart, FVchart, PVchart;

    public static String getIE(int ie) {
        String s = "";
        int i = ie / 1000;
        int id = (ie / 100) % 10;
        int e = (ie / 10) % 10;
        int ed = ie % 10;
        if (id == 0 && ed == 0) {
            s = i + ":" + e;
        } else if (id == 0 && ed != 0) {
            s = i + ":" + e + "." + ed;
        } else if (id != 0 && ed == 0) {
            s = i + "." + id + ":" + e;
        } else {
            s = i + "." + id + ":" + e + "." + ed;
        }
        return s;
    }

    private static DoubleRange getMinMaxRange(DoubleValues values) {
        final DoubleRange range = new DoubleRange();
        SciListUtil.instance().minMax(values.getItemsArray(), 0, values.size(), range);
        range.growBy(0.1, 0.1);
        return range;
    }

    public static void handleData(byte[] bytes) {
        if (packet.length == 0)
            packet = Arrays.copyOf(bytes, bytes.length);
        else if (packet.length < PACKET_LENGTH)
            packet = joinArrays(packet, bytes);
        if (packet.length == PACKET_LENGTH) {
            new ProcessPacket(packet);
//            Log.d("PACKET_MAIN", Arrays.toString(packet));
            packet = new byte[]{};
        } else if (packet.length > PACKET_LENGTH) {
            Log.d("PACKET_DROPPED", Arrays.toString(packet));
            packet = new byte[]{};
        }
    }

    public static byte[] joinArrays(byte[] array1, byte[] array2) {
        int aLen = array1.length;
        int bLen = array2.length;
        byte[] result = new byte[aLen + bLen];
        System.arraycopy(array1, 0, result, 0, aLen);
        System.arraycopy(array2, 0, result, aLen, bLen);
        return result;
    }

    public boolean isAdmin = false;
    public MediaPlayer alarmSound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Set app theme, from splash screen
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);

        // Set decor view flags for fullscreen orientation, and to keep screen from sleeping
        setScreenFlags();

        // Set layout
        setContentView(R.layout.activity_main);

        //set SharedPreferences values from past session
        setSharefPrefs();

        //setup USB data receiver
        setupUsbDataReceiver();

        //Set default handler for crashes/force close
        Thread.setDefaultUncaughtExceptionHandler(new DefaultExceptionHandler(this));

        //Setup COSU policies
        setupCOSU();

        //Setup chart and its layout
        setUpMainChart();

        //set subscript text to MainParamsView
        setSubscriptTextMainParams();

        //Setup ViewPager with listeners, references, number of pages to bind (all 7)
        setupViewPager();

        //Setup UsbStateEventChangeListener
        setupUsbStateChangeEvents();

        //Setup ProcessPacket References
        configureProcessPacketReferences();

        //Setup DataSnapshots at 3s intervals
        setupDataLogger();

        //Setup hPa Observer
        setupHpa();

        //Setup Lock screen
        setupLockScreenButton();

        //Set initial State
        setInitialState();

        //Setup standby fragment
        setupStandby();

        //Setup admin receiver and click listener for Sleep Button on Standby Page
        setupSleepButton();

        // Unset Device Policy Manager when running app in Development mode
        if (isAdmin) {
            DevicePolicyManager devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
            devicePolicyManager.clearDeviceOwnerApp(this.getPackageName());
        }
    }

    Disposable silenceDisposable;

    public void setupUsbStateChangeEvents() {
        standbyLayout = new WeakReference<>(findViewById(R.id.standbyFragmentContainer)); // 2
        usbDisconnectedLayout = new WeakReference<>(findViewById(R.id.usbDisconnectedFragmentContainer)); // 3
        usbDisconnectedPostLayout = new WeakReference<>(findViewById(R.id.usbDisconnectedPostStandbyFragmentContainer)); // 4
        usbStateObserver = new Observer<String>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
                disposables.add(d);
            }

            @Override
            public void onNext(@NonNull String o) {
                if (o.equals("disconnected")) {
                    handleUsbDisconnected();
                } else if (o.equals("connected")) {
                    handleUsbConnected();
                } else {
                    Log.d("INCONSISTENCY", "Unknown Event Received: " + o);
                }
            }

            @Override
            public void onError(@NonNull Throwable e) {
            }

            @Override
            public void onComplete() {
            }
        };
    }

    public void handleUsbConnected() {
        if (packetType == SendPacket.TYPE_STRT) {
            setGraphView(GRAPHS);
        } else {
            setGraphView(STANDBY);
        }
        if (alarmSound != null && alarmSound.isPlaying()) alarmSound.stop();
        getWindow().getDecorView().setSystemUiVisibility(ui_flags);
    }

    public void setupSleepButton() {
        devicePolicyManager = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        compName = new ComponentName(this, MyAdminReceiver.class);
        Log.d("ADMIN ENABLED", "" + devicePolicyManager.isAdminActive(compName));
    }

    public void setInitialState() {
        setGraphView(STANDBY);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mainActivityActive = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        mainActivityActive = false;
    }

    Observable<Long> flashLockButtonObservable = Observable.interval(500, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.newThread()).take(5);
    Observer<Long> flashLockButtonObserver = new Observer<Long>() {
        @Override
        public void onSubscribe(@NonNull Disposable d) {
            lockFlashDisposable = d;
            findViewById(R.id.lockScreen).setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FCDD03")));
        }

        @Override
        public void onNext(@NonNull Long aLong) {
            if (aLong % 2 == 0)
                findViewById(R.id.lockScreen).setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#00b686")));
            else
                findViewById(R.id.lockScreen).setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FCDD03")));
        }

        @Override
        public void onError(@NonNull Throwable e) {
            e.printStackTrace();
        }

        @Override
        public void onComplete() {
            findViewById(R.id.lockScreen).setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#00b686")));
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    public void setupLockScreenButton() {
        ImageButton lockButton = findViewById(R.id.lockScreen);
        Button viewPagerCover = findViewById(R.id.viewPagerCover);
        Button tabCover = findViewById(R.id.tabLayoutCover);
        Button chartsCover = findViewById(R.id.chartsCover);
        Button mainCover = findViewById(R.id.mainCover);
        View.OnClickListener flashButton = v -> {
            tryToDispose(lockFlashDisposable);
            flashLockButtonObservable.subscribe(flashLockButtonObserver);
            Toast.makeText(this, "Screen Locked", Toast.LENGTH_SHORT).show();
        };
        viewPagerCover.setOnClickListener(flashButton);
        tabCover.setOnClickListener(flashButton);
        chartsCover.setOnClickListener(flashButton);
        mainCover.setOnClickListener(flashButton);
        lockButton.setOnClickListener(v -> {
            if (StaticStore.MainActivityValues.lockState == StaticStore.MainActivityValues.UNLOCKED) { // Lock Screen
                tabCover.setVisibility(View.VISIBLE);
                viewPagerCover.setVisibility(View.VISIBLE);
                chartsCover.setVisibility(View.VISIBLE);
                mainCover.setVisibility(View.VISIBLE);
                lockButton.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.locked, null));
                StaticStore.MainActivityValues.lockState = StaticStore.MainActivityValues.LOCKED;
                Toast.makeText(this, "Screen Locked", Toast.LENGTH_LONG).show();
            } else {                                                                                   // Unlock Screen
                tabCover.setVisibility(View.GONE);
                viewPagerCover.setVisibility(View.GONE);
                chartsCover.setVisibility(View.GONE);
                mainCover.setVisibility(View.GONE);
                lockButton.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.unlocked, null));
                StaticStore.MainActivityValues.lockState = StaticStore.MainActivityValues.UNLOCKED;
                Toast.makeText(this, "Screen Unlocked", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void tryToDispose(Disposable d) {
        if (d != null) {
            if (!d.isDisposed()) {
                d.dispose();
            }
        }
    }

    public void setupHpa() {
        final String pressureId = "pressureId";
        hpaObserver = new Observer<String>() {
            @Override
            public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {
            }

            @Override
            public void onNext(@io.reactivex.rxjava3.annotations.NonNull String s) {
                if (s.equals("hpa")) {
                    ((MainParamsView) findViewById(R.id.pinsp)).setUnit("(hPa)");
                    ((MainParamsView) findViewById(R.id.peep)).setUnit("(hPa)");
                    UpdateSuspender.using(mainChart, () -> {
                        Collections.replaceAll(mainChart.getAnnotations(),
                                sciChartBuilder.newTextAnnotation().withXAxisId("XAxis").withYAxisId(pressureId).withY1(0d).withText(" Pressure (cm H2O)").withFontStyle(18, ColorUtil.White).build(),
                                sciChartBuilder.newTextAnnotation().withXAxisId("XAxis").withYAxisId(pressureId).withY1(0d).withText(" Pressure (hPa)").withFontStyle(18, ColorUtil.White).build());
                    });
                } else {
                    ((MainParamsView) findViewById(R.id.pinsp)).setUnit(Html.fromHtml("(cm H<small><sub>2</sub></small>O)"));
                    ((MainParamsView) findViewById(R.id.peep)).setUnit(Html.fromHtml("(cm H<small><sub>2</sub></small>O)"));
                    UpdateSuspender.using(mainChart, () -> {
                        Collections.replaceAll(mainChart.getAnnotations(),
                                sciChartBuilder.newTextAnnotation().withXAxisId("XAxis").withYAxisId(pressureId).withY1(0d).withText(" Pressure (hPa)").withFontStyle(18, ColorUtil.White).build(),
                                sciChartBuilder.newTextAnnotation().withXAxisId("XAxis").withYAxisId(pressureId).withY1(0d).withText(" Pressure (cm H2O)").withFontStyle(18, ColorUtil.White).build());
                    });
                }
            }

            @Override
            public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                e.printStackTrace();
            }

            @Override
            public void onComplete() {
            }
        };
    }

    // Ring Alarm on device if USB Disconnected, show a dialog box with options to shutdown or sleep (?) and sound alarm
    public void handleUsbDisconnected() {
        handleUsbConnected(); // first stop audio and remove dialog before starting and showing it again
        if (packetType == SendPacket.TYPE_STRT) {
            setGraphView(USB_DISCONNECTED);
            alarmSound = MediaPlayer.create(MainActivity.this, R.raw.alarm_sound);
            alarmSound.start();
            alarmSound.setLooping(true);
        } else {
            setGraphView(USB_DISCONNECTED_POST_STANDBY);
            if (alarmSound != null && alarmSound.isPlaying()) alarmSound.stop();
        }
    }

    public static Disposable standbyDisposable;

    public void setupStandby() {
        SendPacket sp = new SendPacket();
        sp.writeInfo(SendPacket.STOP, 0);
        sp.writeInfo(SendPacket.STOP, 276);
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        standbyClickObserver = new Observer<View>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
                disposables.add(d);
            }

            @Override
            public void onNext(View v) {
                Log.d("STANDBY", "Clicked Standby");
                AlertDialog alertDialogInner = builder
                        .setTitle("Are you REALLY sure you want to stop ventilation")
                        .setMessage("This action will stop providing ventilation to the patient. Ensure ...")
                        .setPositiveButton("Yes", (dialog, which1) -> {
                            // send packets continously every 200ms and when received packet has a STRT header, stop sending, and make the change
                            Observable.interval(1500, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.newThread()).take(5).subscribe(new Observer<Long>() {
                                @Override
                                public void onSubscribe(@NonNull Disposable d) {
                                    sp.sendToDevice();
                                    standbyDisposable = d;
                                }
                                @Override
                                public void onNext(@NonNull Long aLong) {
                                    if (packetType != SendPacket.TYPE_STOP) {
                                        Log.d("STANDBY", "trying again, got packet of type: " + packetType);
                                        sp.sendToDevice();
                                    } else {
                                        Log.d("STANDBY", "Got packet of type stop, entering standby");
                                        setGraphView(STANDBY);
                                        StandbyFragment.setIsInView(true);
                                        //restrict use of other buttons to send any packet to device
                                        StaticStore.restrictedCommunicationDueToStandby = true;
                                        //disable standby button
                                        v.setEnabled(false);
                                        v.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#2f2f2f")));
                                        ((Button) v).setTextColor(Color.parseColor("#515151"));
                                        standbyDisposable.dispose();
                                    }
                                }
                                @Override
                                public void onError(@NonNull Throwable e) {
                                    e.printStackTrace();
                                }

                                @Override
                                public void onComplete() {
                                    Toast.makeText(MainActivity.this, "ERR: Cannot communicate with device", Toast.LENGTH_SHORT).show();
                                    standbyDisposable.dispose();
                                }
                            });
                        }).setNegativeButton("No", (dialog_, which_) -> dialog_.dismiss())
                        .create();
                AlertDialog alertDialog = builder
                        .setTitle("Are you sure you want to stop ventilation")
                        .setMessage("This action will stop providing ventilation to the patient")
                        .setPositiveButton("Yes", (dialog, which) -> showDialogHideNav(alertDialogInner))
                        .setNegativeButton("No", (dialog_, which_) -> dialog_.dismiss())
                        .create();
                showDialogHideNav(alertDialog);
            }

            @Override
            public void onError(@NonNull Throwable e) {
                e.printStackTrace();
            }

            @Override
            public void onComplete() {
            }
        };
        shutdownClickObserver = new Observer<Long>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
                disposables.add(d);
            }

            @Override
            public void onNext(@NonNull Long aLong) {
                if (aLong == 1)
                    Toast.makeText(getBaseContext(), "External Standby button pressed. Long press to initiate standby", Toast.LENGTH_LONG).show();
                else if (aLong == 2)
                    Observable.just(ControlsFragment.stopVentilation.get()).subscribe(standbyClickObserver);
            }

            @Override
            public void onError(@NonNull Throwable e) {
                e.printStackTrace();
            }

            @Override
            public void onComplete() {
            }
        };
    }

    public void showDialogHideNav(AlertDialog alert) {
        alert.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        alert.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        alert.getWindow().getDecorView().setSystemUiVisibility(ui_flags);
        alert.show();
        alert.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
    }

    public void setGraphView(int which) {
        //Reset view
        chartLayout.get().setVisibility(View.GONE);
        standbyLayout.get().setVisibility(View.GONE);
        usbDisconnectedLayout.get().setVisibility(View.GONE);
        usbDisconnectedPostLayout.get().setVisibility(View.GONE);
        StandbyFragment.setIsInView(false);
        StaticStore.restrictedCommunicationDueToStandby = false;
        //make one visible
        switch (which) {
            case GRAPHS:
                chartLayout.get().setVisibility(View.VISIBLE);
                setupSilenceButton(1);
                break;
            case STANDBY:
                standbyLayout.get().setVisibility(View.VISIBLE);
                StandbyFragment.setIsInView(true);
                setupSilenceButton(1);
                StaticStore.restrictedCommunicationDueToStandby = true;
                break;
            case USB_DISCONNECTED:
                usbDisconnectedLayout.get().setVisibility(View.VISIBLE);
                setupSilenceButton(2);
                break;
            case USB_DISCONNECTED_POST_STANDBY:
                usbDisconnectedPostLayout.get().setVisibility(View.VISIBLE);
                setupSilenceButton(1);
        }
    }

    public void setupSilenceButton(int which) {
        MaterialButton silence = findViewById(R.id.snooze);
        tryToDispose(silenceDisposable);
        switch (which) {
            case 1:
                silence.setOnClickListener(v -> {
                    if (findViewById(R.id.chartsCover).getVisibility() == View.VISIBLE) { // Screen is locked
                        tryToDispose(lockFlashDisposable);
                        flashLockButtonObservable.subscribe(flashLockButtonObserver);
                        Toast.makeText(this, "Screen Locked", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (silence.getText().equals(getResources().getString(R.string.silence_alarm))) {
                        SendPacket sp = new SendPacket();
                        sp.writeInfo(SendPacket.RNTM, 0);
                        sp.writeInfo(SendPacket.RNTM, 276);
                        sp.writeInfo((byte) 2, 114);
                        sp.sendToDevice();
                    } else {
                        SendPacket sp = new SendPacket();
                        sp.writeInfo(SendPacket.RNTM, 0);
                        sp.writeInfo(SendPacket.RNTM, 276);
                        sp.writeInfo((byte) 1, 114);
                        sp.sendToDevice();
                    }
                });
                break;
            case 2:
                silence.setOnClickListener(v -> {
                    if (findViewById(R.id.chartsCover).getVisibility() == View.VISIBLE) { // Screen is locked
                        tryToDispose(lockFlashDisposable);
                        flashLockButtonObservable.subscribe(flashLockButtonObserver);
                        Toast.makeText(this, "Screen Locked", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (StaticStore.MainActivityValues.silenceState == StaticStore.MainActivityValues.SILENCED) {
                        Observable.interval(1, TimeUnit.SECONDS).take(60).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<Long>() {
                            @Override
                            public void onSubscribe(@NonNull Disposable d) {
                                silenceDisposable = d;
                                silence.setIcon(silencedAlarm);
                                if (alarmSound != null && alarmSound.isPlaying()) alarmSound.stop();
                                silence.setText(silence.getResources().getString(R.string.silenced, 60));
                                StaticStore.MainActivityValues.silenceState = StaticStore.MainActivityValues.UNSILENCED;
                            }

                            @Override
                            public void onNext(@NonNull Long aLong) {
                                silence.setText(silence.getResources().getString(R.string.silenced, 60 - aLong - 1));
                            }

                            @Override
                            public void onError(@NonNull Throwable e) {
                            }

                            @Override
                            public void onComplete() { // 60 seconds over
                                silence.setIcon(silenceAlarm);
                                silence.setText(R.string.silence_alarm);
                                if (alarmSound != null && !alarmSound.isPlaying()) {
                                    alarmSound = MediaPlayer.create(MainActivity.this, R.raw.alarm_sound);
                                    alarmSound.start();
                                    alarmSound.setLooping(true);
                                }
                                StaticStore.MainActivityValues.silenceState = StaticStore.MainActivityValues.SILENCED;
                            }
                        });
                    } else if (StaticStore.MainActivityValues.silenceState == StaticStore.MainActivityValues.UNSILENCED) {
                        tryToDispose(silenceDisposable);
                        silence.setIcon(silenceAlarm);
                        silence.setText(R.string.silence_alarm);
                        if (alarmSound != null && !alarmSound.isPlaying()) {
                            alarmSound = MediaPlayer.create(MainActivity.this, R.raw.alarm_sound);
                            alarmSound.start();
                            alarmSound.setLooping(true);
                        }
                        StaticStore.MainActivityValues.silenceState = StaticStore.MainActivityValues.SILENCED;
                    }
                });
                break;
        }
    }

    public void configureProcessPacketReferences() {
//        ProcessPacketAsyncTask.list = new XyDataSeries<Double, Double>[8];
        ProcessPacket.pressureDataSeries = pressureDataSeries;
        ProcessPacket.pressureSweepDataSeries = pressureSweepDataSeries;
        ProcessPacket.flowDataSeries = flowDataSeries;
        ProcessPacket.flowSweepDataSeries = flowSweepDataSeries;
        ProcessPacket.volumeDataSeries = volumeDataSeries;
        ProcessPacket.volumeSweepDataSeries = volumeSweepDataSeries;
        ProcessPacket.lastPressureSweepDataSeries = lastPressureSweepDataSeries;
        ProcessPacket.lastFlowDataSeries = lastFlowDataSeries;
        ProcessPacket.lastVolumeDataSeries = lastVolumeDataSeries;
        ProcessPacket.FPDataSeries = FPDataSeries;
        ProcessPacket.FVDataSeries = FVDataSeries;
        ProcessPacket.PVDataSeries = PVDataSeries;
        ProcessPacket.chart = new WeakReference<>(mainChart);
        ProcessPacket.FPchart = new WeakReference<>(FPchart);
        ProcessPacket.FVchart = new WeakReference<>(FVchart);
        ProcessPacket.PVchart = new WeakReference<>(PVchart);
        ProcessPacket.tv1 = new WeakReference<>(findViewById(R.id.pinsp));
        ProcessPacket.tv2 = new WeakReference<>(findViewById(R.id.peep));
        ProcessPacket.tv3 = new WeakReference<>(findViewById(R.id.vtf));
        ProcessPacket.tv4 = new WeakReference<>(findViewById(R.id.rate));
        ProcessPacket.tv5 = new WeakReference<>(findViewById(R.id.fio2));
        ProcessPacket.alarm = new WeakReference<>(findViewById(R.id.alarm));
        ProcessPacket.modeBox = new WeakReference<>(findViewById(R.id.modeBox));
        ProcessPacket.inspExp = new WeakReference<>(findViewById(R.id.mainInspExp));
        ProcessPacket.sigh = new WeakReference<>(findViewById(R.id.mainSigh));
        ProcessPacket.viewPagerHolder = new WeakReference<>(findViewById(R.id.viewPagerWrapper));
        silenceAlarm = ContextCompat.getDrawable(this, R.drawable.ic_silence_alarm);
        silencedAlarm = ContextCompat.getDrawable(this, R.drawable.ic_silenced);
        ProcessPacket.RRRedAlarm = ContextCompat.getDrawable(this, R.drawable.rounded_corner_red);
        ProcessPacket.RRRedMode = ContextCompat.getDrawable(this, R.drawable.rounded_corner_red);
        ProcessPacket.RRHolderRed = ContextCompat.getDrawable(this, R.drawable.rounded_corners_holder_red);
        ProcessPacket.RRYellowMode = ContextCompat.getDrawable(this, R.drawable.rounded_corner_yellow);
        ProcessPacket.RRYellowAlarm = ContextCompat.getDrawable(this, R.drawable.rounded_corner_yellow);
        ProcessPacket.RRHolderYellow = ContextCompat.getDrawable(this, R.drawable.rounded_corners_holder_yellow);
        ProcessPacket.RRGreen = ContextCompat.getDrawable(this, R.drawable.rounded_corner_green);
        ProcessPacket.blackColor = getColor(R.color.black);
        ProcessPacket.whiteColor = getColor(R.color.white);
        ProcessPacket.yellowColor = getColor(R.color.yellow);
        ProcessPacket.silence = new WeakReference<>(findViewById(R.id.snooze));
        ProcessPacket.spont = new WeakReference<>(findViewById(R.id.mainSpont));
    }

    public void setScreenFlags() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(ui_flags);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDevicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
    }

    public void setupViewPager() {
        //ViewPager setup
        ViewPager2 viewPager;
        viewPager = findViewById(R.id.viewPager);
        viewPager.setOffscreenPageLimit(6);
        viewPager.setAdapter(new ViewPagerFragmentAdapter(getSupportFragmentManager(), getLifecycle()));
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                if (position == 1) {
                    MonitoringFragment.startObserving();
                } else if (position == 3) {
                    ToolsFragment.startObserving();
                } else {
                    MonitoringFragment.stopObserving();
                    ToolsFragment.stopObserving();
                }
                if (position == 6) {
                    if (packetType == SendPacket.TYPE_STRT) { // If ventilation is going on
                        SystemsFragment.disableSelftest(true);
                        SystemsFragment.disableShutdown(true);
                    } else {                                             // If Ventilation is paused
                        SystemsFragment.disableSelftest(false);
                        SystemsFragment.disableShutdown(false);
                    }
                    SystemsFragment.setDetails();
                }
            }
        });

        // Tab Layout: TabSelectedListener and Mediator for attaching ViewPager2 to TabLayout
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        tabLayout.setBackgroundColor(getColor(R.color.gray1));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                showSidebar();
                tab.getIcon().setColorFilter(getResources().getColor(android.R.color.white), PorterDuff.Mode.SRC_IN);
                tab.view.setBackgroundColor(getColor(R.color.blue));
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                tab.getIcon().setColorFilter(getResources().getColor(android.R.color.darker_gray), PorterDuff.Mode.SRC_IN);
                tab.view.setBackgroundColor(getColor(R.color.dark_gray));
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                if (isSidebarShown) hideSidebar();
                else showSidebar();
            }
        });
        TabLayoutMediator tm = new TabLayoutMediator(tabLayout, viewPager, true, true, new TabLayoutMediator.TabConfigurationStrategy() {
            int[] icons = {R.drawable.controls_icon, R.drawable.ic_monitoring, R.drawable.ic_alarm, R.drawable.ic_tools, R.drawable.ic_patient, R.drawable.ic_events, R.drawable.ic_controls};
            String[] texts = {"Controls", "Monitoring", "Alarms", "Tools", "Patient", "Events", "System"};
            @Override
            public void onConfigureTab(@androidx.annotation.NonNull TabLayout.Tab tab, int position) {
                tab.setIcon(icons[position]);
                tab.setText(texts[position]);
            }
        });
        tm.attach();
    }

    public void hideSidebar() {// Hides sidebar, expands chart to cover screen
        FrameLayout viewPagerHolder = findViewById(R.id.viewPagerWrapper);
        viewPagerHolder.setVisibility(View.GONE);
        isSidebarShown = false;
    }

    public void setSubscriptTextMainParams() {
        MainParamsView mpv = findViewById(R.id.pinsp);
        mpv.setPeepPip(); //Set first CustomTextView's MIN and MAX as Pinsp and Peep
        mpv.setLabel(Html.fromHtml("P<small><sub>insp</sub></small>"));
        mpv = findViewById(R.id.rate);
        mpv.setLabel(Html.fromHtml("R<small><sub>total</sub></small>"));
        mpv = findViewById(R.id.fio2);
        mpv.setLabel(Html.fromHtml("FiO<small><sub>2</sub></small>"));
        mpv = findViewById(R.id.peep);
        mpv.setUnit(Html.fromHtml("(cm H<small><sub>2</sub></small>O)"));
    }

    public void showSidebar() {
        FrameLayout viewPagerHolder = findViewById(R.id.viewPagerWrapper);
        viewPagerHolder.setVisibility(View.VISIBLE);
        isSidebarShown = true;
    }

    public void setupCOSU() {
        mAdminComponentName = MyAdminReceiver.getComponentName(this);
        mDevicePolicyManager = (DevicePolicyManager) getSystemService(
                Context.DEVICE_POLICY_SERVICE);
        if (mDevicePolicyManager.isDeviceOwnerApp(getPackageName())) {
            setDefaultCosuPolicies(true);
        } else {
            Log.d("MSG", "Not Device owner, please set as owner via adb");
        }
    }

    public void setupDataLogger() {
        Observable.interval(10, 3, TimeUnit.SECONDS)
                .observeOn(Schedulers.newThread())
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        disposables.add(d);
                    }
                    @Override
                    public void onNext(@NonNull Long aLong) {
                        boolean isWarningString = false;
                        String warning = "";
                        callNumber++;
                        if (StaticStore.Warnings.currentWarnings.size() > 0) {
                            isWarningString = true;
                            boolean x = true;
                            synchronized (StaticStore.Warnings.currentWarnings) {
                                for (String s : StaticStore.Warnings.currentWarnings) {
                                    if (x) {
                                        warning += s;
                                        x = false;
                                    } else warning += ", " + s;
                                }
                            }
                        } else warning = dashes;
                        if (callNumber <= 20) { // 3s call
                            if (!isWarningString) {
                                return; //return if there is no warning, called every 3 seconds
                                // If there is a warning, a row is created every 3 seconds
                            }
                        } else callNumber = 0;
                        if (!(StaticStore.Data.size() == 0)) {
                            long y = ((new Date(StaticStore.Data.get(0).get("date")).getTime())); // time of earliest record
                            long z = ((new Date(df.format(new Date()))).getTime()); // time of latest record
                            if ((z - y) / 1000 > 259200) {                  // difference between earliest and latest is kept at 72 hours. (259200 seconds)
                                StaticStore.Data.remove(0); // Retain records for 72 hours, roll over old data
                            }
                        }
                        HashMap<String, String> t = new HashMap<>();
                        t.put("date", df.format(new Date()));
                        t.put("pinsp", String.valueOf(StaticStore.Values.pInsp));
                        t.put("set-pinsp", String.valueOf(StaticStore.packet_pinsp));
                        t.put("peep", String.valueOf(StaticStore.Values.peep));
                        t.put("set-peep", String.valueOf(StaticStore.packet_peep));
                        t.put("ppeak", String.valueOf(StaticStore.Values.graphPressure));
                        t.put("pmean", String.valueOf(StaticStore.Values.pMean));
                        t.put("vt", String.valueOf(StaticStore.Values.graphVolume));
                        t.put("set-vt", String.valueOf(StaticStore.packet_vt));
                        t.put("rtotal", String.valueOf(StaticStore.Values.rateMeasured));
                        t.put("set-rtotal", String.valueOf(StaticStore.packet_rtotal));
                        t.put("rspont", String.valueOf(StaticStore.Values.rSpont));
                        t.put("mvTotal", String.valueOf(StaticStore.Monitoring.mvTotal));
                        t.put("mvSpont", String.valueOf(StaticStore.Monitoring.mvSpont));
                        t.put("fio2", String.valueOf(StaticStore.Values.fio2));
                        t.put("set-fio2", String.valueOf(StaticStore.packet_fio2));
                        t.put("ie", getIE(StaticStore.Monitoring.ie));
                        t.put("set-ie", getIE(StaticStore.packet_ie));
                        t.put("cStat", String.valueOf(StaticStore.Monitoring.cStat));
                        t.put("warning", warning);
                        StaticStore.Data.add(t);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        e.printStackTrace();
                    }
                    @Override
                    public void onComplete() {
                        Log.d("INCONSISTENCY", "DataSnapshot Observable called onComplete(). Data Snapshots store for Log data not being stored Cannot be stopped");
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposables.dispose();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (alarmSound != null && alarmSound.isPlaying()) alarmSound.stop();
        ((MaterialButton) findViewById(R.id.snooze)).setIcon(silenceAlarm);
        ((MaterialButton) findViewById(R.id.snooze)).setText(R.string.silence_alarm);
        tryToDispose(silenceDisposable);
        unregisterReceiver(mUsbReceiver);
        unbindService(usbConnection);
    }

    public void setSharefPrefs() {

        //Set controls info on app launch from memory

        SharedPreferences sharedPref = this.getSharedPreferences("dvbVentilator", Context.MODE_PRIVATE);
        StaticStore.modeSelected = sharedPref.getString("mode_selected", StaticStore.DefaultValues.modeSelected_String);
        StaticStore.modeSelectedShort = Short.parseShort(sharedPref.getString("mode_selected_short", StaticStore.DefaultValues.modeSelected_Short));
        StaticStore.packet_fio2 = Short.parseShort(sharedPref.getString("packet_fio2", StaticStore.DefaultValues.fio2_Short));
        StaticStore.packet_vt = Short.parseShort(sharedPref.getString("packet_vt", StaticStore.DefaultValues.vt_Short));
        StaticStore.packet_flowTrig = Float.parseFloat(Objects.requireNonNull(sharedPref.getString("packet_vtrig", StaticStore.DefaultValues.flowTrig_Float)));
        StaticStore.packet_pinsp = Float.parseFloat(Objects.requireNonNull(sharedPref.getString("packet_pip", StaticStore.DefaultValues.pInsp_Float)));
        StaticStore.packet_peep = Float.parseFloat(Objects.requireNonNull(sharedPref.getString("packet_peep", StaticStore.DefaultValues.peep_Float)));
        StaticStore.packet_rtotal = Float.parseFloat(Objects.requireNonNull(sharedPref.getString("packet_ratef", StaticStore.DefaultValues.rTotal_Float)));
        StaticStore.packet_tinsp = Float.parseFloat(Objects.requireNonNull(sharedPref.getString("packet_tinsp", StaticStore.DefaultValues.tInsp_Float)));
        StaticStore.packet_ie = Short.parseShort(sharedPref.getString("packet_ie", StaticStore.DefaultValues.ie_Short));
        StaticStore.packet_plimit = (byte) Float.parseFloat(Objects.requireNonNull(sharedPref.getString("packet_pmax", StaticStore.DefaultValues.pLimit_Float)));
        StaticStore.packet_ps = Float.parseFloat(Objects.requireNonNull(sharedPref.getString("packet_ps", StaticStore.DefaultValues.ps_Float)));
        StaticStore.AlarmLimits.minVolMax = Float.parseFloat(sharedPref.getString("limits_minVolMax", StaticStore.DefaultValues.minVolMax_Float));
        StaticStore.AlarmLimits.minVolMin = Float.parseFloat(sharedPref.getString("limits_minVolMin", StaticStore.DefaultValues.minVolMin_Float));
        StaticStore.AlarmLimits.rateMax = (byte) Float.parseFloat(sharedPref.getString("limits_fTotalMax", StaticStore.DefaultValues.rateMax_Float));
        StaticStore.AlarmLimits.rateMin = (byte) Float.parseFloat(sharedPref.getString("limits_fTotalMin", StaticStore.DefaultValues.rateMin_Float));
        StaticStore.AlarmLimits.vtMax = Short.parseShort(sharedPref.getString("limits_vtMax", StaticStore.DefaultValues.vtMax_Short));
        StaticStore.AlarmLimits.vtMin = Short.parseShort(sharedPref.getString("limits_vtMi", StaticStore.DefaultValues.vtMin_Short));
        StaticStore.AlarmLimits.pMax = Float.parseFloat(sharedPref.getString("limits_pMax", StaticStore.DefaultValues.pMax_Float));
        StaticStore.AlarmLimits.pMin = Float.parseFloat(sharedPref.getString("limits_pMin", StaticStore.DefaultValues.pMin_Float));
        StaticStore.AlarmLimits.apnea = Short.parseShort(sharedPref.getString("limits_apnea", StaticStore.DefaultValues.apnea_Short));
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

    //Create subscription to packetSubject(PublishSubject) to handle incoming data from packet
    public void setupUsbDataReceiver() {
        disposables.add(packetSubject.subscribe(bytes -> {
            Log.d("BYTES " + bytes.length, Arrays.toString(bytes));
            if (packet.length == 0) {
                packet = Arrays.copyOf(bytes, bytes.length);
            } else if (packet.length < PACKET_LENGTH) {
                packet = joinArrays(packet, bytes);
            } else if (packet.length == PACKET_LENGTH) {
                new ProcessPacket(packet);
//                Log.d("PACKET_MAIN", Arrays.toString(packet));
                packet = new byte[]{};
            } else {
                Log.d("PACKET_DROPPED", Arrays.toString(packet));
                packet = new byte[]{};
            }
        }));
    }

    @Override
    public void onResume() {
        super.onResume();
        //if during activity startup the Usb state is disconnected, show the alarm
        setFilters();
        // Start listening notifications from UsbService
        // Start UsbService(if it was not started before) and Bind it
        new Handler().postDelayed(() -> startService(UsbService.class, usbConnection, null), 1000);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        getWindow().getDecorView().setSystemUiVisibility(ui_flags);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);

        Observable.timer(8, TimeUnit.SECONDS).take(1).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Long>() {
                    Disposable disp;

                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        disp = d;
                    }

                    @Override
                    public void onNext(@NonNull Long aLong) {
                        if (!UsbService.serialPortConnected) { // IF USB is disconnected in onResume
                            handleUsbDisconnected();
                        } else { // If USB is connected in onResume
                            handleUsbConnected();
                        }
                        Log.d("onResume", "" + packetType);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                    }

                    @Override
                    public void onComplete() {
                        disp.dispose();
                    }
                });
    }

    private HorizontalLineAnnotation generateBaseLines(String yAxisId) {
        return sciChartBuilder.newHorizontalLineAnnotation().withStroke(1, ColorUtil.White).withHorizontalGravity(Gravity.FILL_HORIZONTAL).withXAxisId("XAxis").withYAxisId(yAxisId).withY1(0d).build();
    }

    private NumericAxis generateYAxis(String id, DoubleRange visibleRange) {
        return sciChartBuilder.newNumericAxis().withAxisId(id).withVisibleRange(visibleRange).withAutoRangeMode(AutoRange.Never).withDrawMajorBands(false).withDrawMinorGridLines(true).withDrawMajorGridLines(true).build();
    }

    private FastLineRenderableSeries generateLineSeries(String yAxisId, IDataSeries ds, PenStyle strokeStyle) {
        FastLineRenderableSeries lineSeries = new FastLineRenderableSeries();
        lineSeries.setDataSeries(ds);
        lineSeries.setPaletteProvider(new DimTracePaletteProvider());
        lineSeries.setStrokeStyle(strokeStyle);
        lineSeries.setXAxisId("XAxis");
        lineSeries.setYAxisId(yAxisId);
        return lineSeries;
    }

    private IRenderableSeries generateScatterForLastAppendedPoint(String yAxisId, IDataSeries ds) {
        final EllipsePointMarker pm = sciChartBuilder.newPointMarker(new EllipsePointMarker())
                .withSize(4)
                .withFill(ColorUtil.White)
                .withStroke(ColorUtil.White, 1f)
                .build();

        return sciChartBuilder.newScatterSeries()
                .withDataSeries(ds)
                .withYAxisId(yAxisId)
                .withXAxisId("XAxis")
                .withPointMarker(pm)
                .build();
    }

    private void setDefaultCosuPolicies(boolean active) {
        // Set user restrictions
//        setUserRestriction(UserManager.DISALLOW_SAFE_BOOT, active);
//        setUserRestriction(UserManager.DISALLOW_FACTORY_RESET, active);
        setUserRestriction(UserManager.DISALLOW_ADD_USER, active);
//        setUserRestriction(UserManager.DISALLOW_MOUNT_PHYSICAL_MEDIA, active);
//        setUserRestriction(UserManager.DISALLOW_ADJUST_VOLUME, active);
//        setUserRestriction(UserManager.DISALLOW_AIRPLANE_MODE, active);
//        setUserRestriction(UserManager.DISALLOW_BLUETOOTH, active);
//        setUserRestriction(UserManager.DISALLOW_APPS_CONTROL, active);
//        setUserRestriction(UserManager.DISALLOW_CONFIG_CELL_BROADCASTS, active);
//        setUserRestriction(UserManager.DISALLOW_CONFIG_SCREEN_TIMEOUT, active);
//        setUserRestriction(UserManager.DISALLOW_CONFIG_TETHERING, active);
//        setUserRestriction(UserManager.DISALLOW_CONFIG_MOBILE_NETWORKS, active);
//        setUserRestriction(UserManager.DISALLOW_CREATE_WINDOWS, active);
//        setUserRestriction(UserManager.DISALLOW_CONFIG_WIFI, active);
//        setUserRestriction(UserManager.DISALLOW_FACTORY_RESET, active);
        setUserRestriction(UserManager.DISALLOW_DATA_ROAMING, active);
//        setUserRestriction(UserManager.DISALLOW_INSTALL_APPS, active);
//        setUserRestriction(UserManager.DISALLOW_SMS, active);
//        setUserRestriction(UserManager.DISALLOW_NETWORK_RESET, active);
//        setUserRestriction(UserManager.DISALLOW_USER_SWITCH, active);
//        setUserRestriction(UserManager.DISALLOW_AMBIENT_DISPLAY, active);
//        setUserRestriction(UserManager.DISALLOW_SYSTEM_ERROR_DIALOGS, active);
//        setUserRestriction(UserManager.DISALLOW_UNINSTALL_APPS, active);

        // Disable keyguard and status bar
        mDevicePolicyManager.setKeyguardDisabled(mAdminComponentName, active);
        mDevicePolicyManager.setStatusBarDisabled(mAdminComponentName, active);

        // Enable STAY_ON_WHILE_PLUGGED_IN
        enableStayOnWhilePluggedIn(active);
//        startLockTask();

        // set this Activity as a lock task package
        mDevicePolicyManager.setLockTaskPackages(mAdminComponentName,
                active ? new String[]{getPackageName()} : new String[]{});

        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_MAIN);
        intentFilter.addCategory(Intent.CATEGORY_HOME);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);

        if (active) {
            // set Cosu activity as home intent receiver so that it is started
            // on reboot
            mDevicePolicyManager.addPersistentPreferredActivity(
                    mAdminComponentName, intentFilter, new ComponentName(
                            getPackageName(), MainActivity.class.getName()));
        } else {
            mDevicePolicyManager.clearPackagePersistentPreferredActivities(
                    mAdminComponentName, getPackageName());
        }
    }

    private void setUserRestriction(String restriction, boolean disallow) {
        if (disallow) {
            mDevicePolicyManager.addUserRestriction(mAdminComponentName,
                    restriction);
        } else {
            mDevicePolicyManager.clearUserRestriction(mAdminComponentName,
                    restriction);
        }
    }

    private void enableStayOnWhilePluggedIn(boolean enabled) {
        if (enabled) {
            mDevicePolicyManager.setGlobalSetting(
                    mAdminComponentName,
                    Settings.Global.STAY_ON_WHILE_PLUGGED_IN,
                    Integer.toString(BatteryManager.BATTERY_PLUGGED_AC
                            | BatteryManager.BATTERY_PLUGGED_USB
                            | BatteryManager.BATTERY_PLUGGED_WIRELESS));
        } else {
            mDevicePolicyManager.setGlobalSetting(
                    mAdminComponentName,
                    Settings.Global.STAY_ON_WHILE_PLUGGED_IN,
                    "0"
            );
        }
    }

    private void setUpMainChart() {
        try {
            SciChartSurface.setRuntimeLicenseKey("mUZa8f4oGQKabCPwa9dbZa4NctRJtftTQTpUzsbvpo83qah4SLuOxCk5dhQ0ijY2GigaUHJC2tALcYaMJBtbRYQ0SmuSgskEVnBVrTO5dp3PjtNIBUY5+uCtURe4L4Ia58NpLAPdvP4GpTODH6D0tnzFn7OSyFBNXkW/uahwBYUl54kvJ7KnOTzpSRuFgmYUpQe94BKUZNRZv3cRny7KCep+oO1CMyPKsdUxepnObTYTC0SJiOP/Si3wQZqhGDDV3CjnGqtXu1clJgJ5JEBGCSesxirGMX4hblmdSSMXByc1ogmdwMzZGDIb69Ieb9wHBRefXS6vRDy0kiaHlhn3f1mTac7P5+5Q3obilPwru9xk6H7GpZW85hWZJi2oopNpkPwGtgYZXwfZLYEfWXnC7eP+AjIhh8H03OTdSgHD89CmcYn84Knf2zrkzKK9q2CTJA2zTg6Ht748aehShNFh2ipIpfDGZ0o0Whimqx19jLOnz0vd577qX6pwNt8=");
        } catch (Exception e) {
            e.printStackTrace();
        }
        final String pressureId = "pressureId";
        final String flowId = "flowId";
        final String volumeId = "volumeId";
        SciChartBuilder.init(this);
        sciChartBuilder = SciChartBuilder.instance();
        mainChart = new SciChartSurface(this);
        chartLayout = new WeakReference<>(findViewById(R.id.mainChart));
        chartLayout.get().addView((View) mainChart, 0);

        final NumericAxis xAxis = sciChartBuilder.newNumericAxis()
                .withVisibleRange(0, 10)
                .withAutoRangeMode(AutoRange.Never)
                .withAxisBandsFill(5)
                .withDrawMajorBands(true)
                .withAxisId("XAxis")
                .build();

        DoubleValues pressureRange = new DoubleValues();
        pressureRange.add(-10);
        pressureRange.add(65);
        DoubleValues flowRange = new DoubleValues();
        flowRange.add(-150);
        flowRange.add(+150);
        DoubleValues volumeRange = new DoubleValues();
        volumeRange.add(-500);
        volumeRange.add(1200);

        final NumericAxis yAxisPressure = generateYAxis(pressureId, getMinMaxRange(pressureRange));
        final NumericAxis yAxisFlow = generateYAxis(flowId, getMinMaxRange(flowRange));
        final NumericAxis yAxisVolume = generateYAxis(volumeId, getMinMaxRange(volumeRange));

        UpdateSuspender.using(mainChart, () -> {
            Collections.addAll(mainChart.getAnnotations(),
                    sciChartBuilder.newTextAnnotation()
                            .withXAxisId("XAxis")
                            .withYAxisId(pressureId)
                            .withY1(0d)
                            .withText(" Pressure (cm H2O)")
                            .withFontStyle(18, ColorUtil.White)
                            .build(),
                    generateBaseLines(pressureId),
                    sciChartBuilder.newTextAnnotation()
                            .withXAxisId("XAxis")
                            .withYAxisId(flowId)
                            .withY1(0d)
                            .withFontStyle(18, ColorUtil.White)
                            .withText(" Flow (lpm)")
                            .build(),
                    generateBaseLines(flowId),
                    sciChartBuilder.newTextAnnotation()
                            .withXAxisId("XAxis")
                            .withYAxisId(volumeId)
                            .withY1(0d)
                            .withFontStyle(18, ColorUtil.White)
                            .withText(" Volume (ml)")
                            .build(),
                    generateBaseLines(volumeId)
            );
            Collections.addAll(mainChart.getXAxes(), xAxis);
            Collections.addAll(mainChart.getYAxes(), yAxisPressure, yAxisFlow, yAxisVolume);
            Collections.addAll(mainChart.getRenderableSeries(),
                    MainActivity.this.generateLineSeries(pressureId, pressureDataSeries, sciChartBuilder.newPen().withColor(ColorUtil.argb(0xFF, 0x00, 0xFF, 0x00)).withAntiAliasing(true).withThickness(1.5f).build()),
                    MainActivity.this.generateLineSeries(pressureId, pressureSweepDataSeries, sciChartBuilder.newPen().withColor(ColorUtil.argb(0xFF, 0x00, 0xFF, 0x00)).withAntiAliasing(true).withThickness(1.5f).build()),
                    MainActivity.this.generateScatterForLastAppendedPoint(pressureId, lastPressureSweepDataSeries),
                    MainActivity.this.generateLineSeries(flowId, flowDataSeries, sciChartBuilder.newPen().withColor(ColorUtil.argb(0xFF, 0xFF, 0x66, 0x00)).withAntiAliasing(true).withThickness(1.5f).build()),
                    MainActivity.this.generateLineSeries(flowId, flowSweepDataSeries, sciChartBuilder.newPen().withColor(ColorUtil.argb(0xFF, 0xFF, 0x66, 0x00)).withAntiAliasing(true).withThickness(1.5f).build()),
                    MainActivity.this.generateScatterForLastAppendedPoint(flowId, lastFlowDataSeries),
                    MainActivity.this.generateLineSeries(volumeId, volumeDataSeries, sciChartBuilder.newPen().withColor(ColorUtil.argb(0xFF, 0xFF, 0xEA, 0x00)).withAntiAliasing(true).withThickness(1.5f).build()),
                    MainActivity.this.generateLineSeries(volumeId, volumeSweepDataSeries, sciChartBuilder.newPen().withColor(ColorUtil.argb(0xFF, 0xFF, 0xEA, 0x00)).withAntiAliasing(true).withThickness(1.5f).build()),
                    MainActivity.this.generateScatterForLastAppendedPoint(volumeId, lastVolumeDataSeries)
            );
            mainChart.setLayoutManager(new DefaultLayoutManager.Builder().setRightOuterAxesLayoutStrategy(new RightAlignedOuterVerticallyStackedYAxisLayoutStrategy()).build());
        });

        FPchart = new SciChartSurface(this);
        LinearLayout FP = findViewById(R.id.FPChart);
        FP.addView((View) FPchart, 0);
        UpdateSuspender.using(FPchart, () -> {
            Collections.addAll(
                    FPchart.getXAxes(),
                    sciChartBuilder.newNumericAxis()
                            .withVisibleRange(-50, 50)
                            .withAutoRangeMode(AutoRange.Never)
                            .withAxisBandsFill(5)
                            .withDrawMajorBands(true)
                            .withAxisId("XAxis")
                            .build()
            );
            DoubleValues range = new DoubleValues();
            range.add(-15);
            range.add(15);
            Collections.addAll(
                    FPchart.getYAxes(),
                    sciChartBuilder.newNumericAxis()
                            .withAxisId(pressureId)
                            .withVisibleRange(-5, 20)
                            .withAutoRangeMode(AutoRange.Never)
                            .withDrawMajorBands(false)
                            .withDrawMinorGridLines(true)
                            .withDrawMajorGridLines(true)
                            .build()
            );
            Collections.addAll(FPchart.getRenderableSeries(),
                    MainActivity.this.generateLineSeries(pressureId, FPDataSeries, sciChartBuilder.newPen().withColor(ColorUtil.argb(0xFF, 0x00, 0xFF, 0x00)).withAntiAliasing(true).withThickness(1.5f).build())
            );
        });
        PVchart = new SciChartSurface(this);
        LinearLayout PV = findViewById(R.id.PVChart);
        PV.addView((View) PVchart, 0);
        UpdateSuspender.using(PVchart, () -> {
            Collections.addAll(
                    PVchart.getXAxes(),
                    sciChartBuilder.newNumericAxis()
                            .withAutoRangeMode(AutoRange.Never)
                            .withVisibleRange(-5, 20)
                            .withAxisBandsFill(5)
                            .withDrawMajorBands(true)
                            .withAxisId("XAxis")
                            .build()
            );
            DoubleValues range = new DoubleValues();
            range.add(-100);
            range.add(500);
            Collections.addAll(
                    PVchart.getYAxes(),
                    sciChartBuilder.newNumericAxis()
                            .withAxisId(pressureId)
                            .withVisibleRange(-100, 600)
                            .withAutoRangeMode(AutoRange.Never)
                            .withDrawMajorBands(false)
                            .withDrawMinorGridLines(true)
                            .withDrawMajorGridLines(true)
                            .build()
            );
            Collections.addAll(PVchart.getRenderableSeries(),
                    MainActivity.this.generateLineSeries(pressureId, PVDataSeries, sciChartBuilder.newPen().withColor(ColorUtil.argb(0xFF, 0xFF, 0xEA, 0x00)).withAntiAliasing(true).withThickness(1.5f).build())
            );
        });
        FVchart = new SciChartSurface(this);
        LinearLayout FV = findViewById(R.id.FVChart);
        FV.addView((View) FVchart, 0);
        UpdateSuspender.using(FVchart, () -> {
            Collections.addAll(
                    FVchart.getXAxes(),
                    sciChartBuilder.newNumericAxis()
                            .withVisibleRange(-10, 10)
                            .withAutoRangeMode(AutoRange.Never)
                            .withAxisBandsFill(5)
                            .withDrawMajorBands(true)
                            .withAxisId("XAxis")
                            .build()
            );
            DoubleValues range = new DoubleValues();
            range.add(-100);
            range.add(500);
            Collections.addAll(
                    FVchart.getYAxes(),
                    sciChartBuilder.newNumericAxis()
                            .withAxisId(pressureId)
                            .withVisibleRange(-200, 350)
                            .withAutoRangeMode(AutoRange.Never)
                            .withDrawMajorBands(false)
                            .withDrawMinorGridLines(true)
                            .withDrawMajorGridLines(true)
                            .build()
            );
            Collections.addAll(FVchart.getRenderableSeries(),
                    MainActivity.this.generateLineSeries(pressureId, FVDataSeries, sciChartBuilder.newPen().withColor(ColorUtil.argb(0xFF, 0xFF, 0x66, 0x00)).withAntiAliasing(true).withThickness(1.5f).build())
            );
        });

//        MaterialButtonToggleGroup group = findViewById(R.id.toggleGroupMain);
//        group.addOnButtonCheckedListener((group1, checkedId, isChecked) -> {
//            findViewById(graphShownRef).setVisibility(View.GONE);
//            switch(checkedId) {
//                case R.id.FPbutton: findViewById(R.id.FPChart).setVisibility(View.VISIBLE); graphShownRef = R.id.FPChart; break;
//                case R.id.FVbutton: findViewById(R.id.FVChart).setVisibility(View.VISIBLE); graphShownRef = R.id.FVChart; break;
//                case R.id.PVbutton: findViewById(R.id.PVChart).setVisibility(View.VISIBLE); graphShownRef = R.id.PVChart; break;
//                case R.id.mainGraphsButton: findViewById(R.id.mainChart).setVisibility(View.VISIBLE); graphShownRef = R.id.mainChart; break;
//            }
//        });
    }

    private XyDataSeries<Double, Double> newDataSeries(int fifoCapacity) {
        final XyDataSeries<Double, Double> ds = new XyDataSeries<>(Double.class, Double.class);
        ds.setFifoCapacity(fifoCapacity);
        ds.setAcceptsUnsortedData(false);
        return ds;
    }
}