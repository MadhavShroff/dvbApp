package com.dvbinventek.dvbapp;

import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.UserManager;
import android.provider.Settings;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.dvbinventek.dvbapp.customViews.MainParamsView;
import com.dvbinventek.dvbapp.graphing.DimTracePaletteProvider;
import com.dvbinventek.dvbapp.graphing.RightAlignedOuterVerticallyStackedYAxisLayoutStrategy;
import com.dvbinventek.dvbapp.viewPager.ViewPagerFragmentAdapter;
import com.google.android.material.button.MaterialButton;
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

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.subjects.PublishSubject;

public class MainActivity extends AppCompatActivity {

    public static final int PACKET_LENGTH = 224;
    public static final PublishSubject<byte[]> packetSubject = PublishSubject.create();
    private static final int FIFO_CAPACITY = 40;
    public static UsbService usbService;
    //ViewPager vars
    public static ViewPager2 viewPager;
    static int ui_flags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
            View.SYSTEM_UI_FLAG_FULLSCREEN |
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
    public final XyDataSeries<Double, Double> pressureDataSeries = newDataSeries(FIFO_CAPACITY);
    public final XyDataSeries<Double, Double> pressureSweepDataSeries = newDataSeries(FIFO_CAPACITY);
    public final XyDataSeries<Double, Double> flowDataSeries = newDataSeries(FIFO_CAPACITY);
    public final XyDataSeries<Double, Double> flowSweepDataSeries = newDataSeries(FIFO_CAPACITY);
    public final XyDataSeries<Double, Double> volumeDataSeries = newDataSeries(FIFO_CAPACITY);
    public final XyDataSeries<Double, Double> volumeSweepDataSeries = newDataSeries(FIFO_CAPACITY);
    public final XyDataSeries<Double, Double> lastPressureSweepDataSeries = newDataSeries(1);
    public final XyDataSeries<Double, Double> lastFlowDataSeries = newDataSeries(1);
    public final XyDataSeries<Double, Double> lastVolumeDataSeries = newDataSeries(1);
    public final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
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
            }
        }
    };
    public UsbHandler mHandler = new UsbHandler(this);
    public final ServiceConnection usbConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            usbService = ((UsbService.UsbBinder) arg1).getService();
            usbService.setHandler(mHandler);
//            StaticStore.service = usbService;
//            connected = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            usbService = null;
//            connected = false;
        }
    };
    //chart vars
    public SciChartBuilder sciChartBuilder;
    public ISciChartSurface chart;
    public LinearLayout charts;
    public MaterialButton silence;
    public String packet = "";
    private CompositeDisposable disposables = new CompositeDisposable();
    //LockTask mode vars
    private DevicePolicyManager mDevicePolicyManager;
    private ComponentName mAdminComponentName;

    private static DoubleRange getMinMaxRange(DoubleValues values) {
        final DoubleRange range = new DoubleRange();
        SciListUtil.instance().minMax(values.getItemsArray(), 0, values.size(), range);
        range.growBy(0.1, 0.1);
        return range;
    }

    private XyDataSeries<Double, Double> newDataSeries(int fifoCapacity) {
        final XyDataSeries<Double, Double> ds = new XyDataSeries<>(Double.class, Double.class);
        ds.setFifoCapacity(fifoCapacity);
        ds.setAcceptsUnsortedData(false);
        return ds;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Set app theme, from splash screen
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);

        // Set decor view flags for fullscreen orientation, and to keep screen from sleeping
        setScreenFlags();

        //set layout
        setContentView(R.layout.activity_main);

        //set SharedPreferances values from past session
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

        //Setup ViewPager with listeners, referances, number of pages to bind (all 7)
        setupViewPager();
    }

    public void setSubscriptTextMainParams() {
        MainParamsView mpv = findViewById(R.id.pinsp);
        mpv.setPeepPip(); //Set first CustomTextView's MIN and MAX as Pinsp and Peep
        mpv.setLabel(Html.fromHtml("P<small><sub>insp</sub></small>"));
        mpv = findViewById(R.id.rate);
        mpv.setLabel(Html.fromHtml("R<small><sub>total</sub></small>"));
        mpv = findViewById(R.id.fio2);
        mpv.setLabel(Html.fromHtml("FiO<small><sub>2</sub></small>"));
    }

    public void setupViewPager() {
        //ViewPager
        viewPager = findViewById(R.id.view_pager);
        viewPager.setOffscreenPageLimit(6);
        viewPager.setAdapter(new ViewPagerFragmentAdapter(getSupportFragmentManager(), getLifecycle()));
//        viewPager.setAdapter(new ViewPagerAdapter(this, usbService, MainActivity.this, getSupportFragmentManager(), onStandbyClick));
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

    public void setSharefPrefs() {
        //Set controls info on app launch from memory
        SharedPreferences sharedPref = this.getSharedPreferences("dvbVentilator", Context.MODE_PRIVATE);
        StaticStore.modeSelected = sharedPref.getString("mode_selected", "-");
        StaticStore.packet_fio2 = Short.parseShort(sharedPref.getString("packet_fio2", "0"));
        StaticStore.packet_vt = Short.parseShort(sharedPref.getString("packet_vt", "0"));
        StaticStore.packet_vtrig = Float.parseFloat(sharedPref.getString("packet_vtrig", "0"));
        StaticStore.packet_pip = Float.parseFloat(sharedPref.getString("packet_pip", "0"));
        StaticStore.packet_cpap = Float.parseFloat(sharedPref.getString("packet_cpap", "0"));
        StaticStore.packet_ratef = Float.parseFloat(sharedPref.getString("packet_ratef", "0"));
        StaticStore.packet_tinsp = Float.parseFloat(sharedPref.getString("packet_tinsp", "0"));
        StaticStore.packet_ie = Short.parseShort(sharedPref.getString("packet_ie", "0"));
        StaticStore.modeSelectedShort = Short.parseShort(sharedPref.getString("mode_selected_short", "0"));
        StaticStore.packet_pmax = Float.parseFloat(sharedPref.getString("packet_pmax", "0"));
        StaticStore.packet_delps = Float.parseFloat(sharedPref.getString("packet_delps", "0"));
    }

    public void setupUsbDataReceiver() {
        //Create subscription to packetSubject(PublishSubject) to handle incoming data from packet
        disposables.add(packetSubject.subscribe(bytes -> {
            Log.d("PACKET_MAIN", Arrays.toString(bytes));
            //TODO: add handleData logic
        }));
    }

    public void setupCOSU() {
        mAdminComponentName = DeviceAdminReceiver.getComponentName(this);
        mDevicePolicyManager = (DevicePolicyManager) getSystemService(
                Context.DEVICE_POLICY_SERVICE);
        if (mDevicePolicyManager.isDeviceOwnerApp(getPackageName())) {
            setDefaultCosuPolicies(true);
        } else {
            Log.d("MSG", "Not Device owner, please set as owner via adb");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setFilters();
        // Start listening notifications from UsbService
        // Start UsbService(if it was not started before) and Bind it
        startService(UsbService.class, usbConnection, null);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        getWindow().getDecorView().setSystemUiVisibility(ui_flags);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposables.dispose();
    }

    @Override
    public void onPause() {
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
//        StaticStore.service = usbService;
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
            Log.d("PACKET_MAIN", packet);
//            new ProcessPacketAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, packet);
            packet = "";
        } else {
            packet = "";
            Log.d("MSG", "DROPPED");
        }
    }

    private void setUpMainChart() {
        try {
            SciChartSurface.setRuntimeLicenseKey("wtAosuCtN2onfU1Vva1WXGY8EJhcn2VvVs/K3fHohGNHodDaqXV3bXqI3k1n010ZtFZE5m4ogLrpOgJp7dGQyzmfzcLVSn+FY9cxDWkzbvxi5xeSAM1PfvqV8n6/u18LvcqWyOvmiDQ6pTjRWKRRH+BMcVgzrelrOTdGC7n4xhMvr2NCMLhnDO+mbT5oc06MvFtamLBV3oHjL+IZBJeoY0S1oed/2sV2gRpmLqysnrqVnKH1G0gnluuxtZsGXW+jG05K1Byh+5h/8eMd/JfJj216aCAZY1uEqDMv/g339IEylLuXkPVJYUMsChRl7GnLb7JX5xl3pIYhocxG+wfzdM05mwDErA5AAWV54NEJUBTWy4morZWwrpVYgQKA1SfUCYxaZvLUkojmR2BsitmrTolhbUwUkgn+7CWXUXWROHhZ+H7++zb015Wj5HkKc6P3/1A/3Nayh8Pno4Uft79pcLyLJakJ7dSXpQlO4Nsb3INwjT29M6iwK2eJClgC8U94mFulNwIJqvh30zgut+g3UdbW8kl5M1pyMr+Xto4OhNjd0HETlLr1o+h/AuJpQzSB+Q/GrAdVRUM=");
        } catch (Exception e) {
            e.printStackTrace();
        }
        final String pressureId = "pressureId";
        final String flowId = "flowId";
        final String volumeId = "volumeId";
        SciChartBuilder.init(this);
        sciChartBuilder = SciChartBuilder.instance();
        chart = new SciChartSurface(this);
        LinearLayout chartLayout = findViewById(R.id.mainChart);
        chartLayout.addView((View) chart, 0);

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

        UpdateSuspender.using(chart, () -> {
            Collections.addAll(chart.getAnnotations(),
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
            Collections.addAll(chart.getXAxes(), xAxis);
            Collections.addAll(chart.getYAxes(), yAxisPressure, yAxisFlow, yAxisVolume);
            Collections.addAll(chart.getRenderableSeries(),
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
            chart.setLayoutManager(new DefaultLayoutManager.Builder().setRightOuterAxesLayoutStrategy(new RightAlignedOuterVerticallyStackedYAxisLayoutStrategy()).build());
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
        setUserRestriction(UserManager.DISALLOW_SAFE_BOOT, active);
        setUserRestriction(UserManager.DISALLOW_FACTORY_RESET, active);
        setUserRestriction(UserManager.DISALLOW_ADD_USER, active);
        setUserRestriction(UserManager.DISALLOW_MOUNT_PHYSICAL_MEDIA, active);
        setUserRestriction(UserManager.DISALLOW_ADJUST_VOLUME, active);
        setUserRestriction(UserManager.DISALLOW_AIRPLANE_MODE, active);
        setUserRestriction(UserManager.DISALLOW_BLUETOOTH, active);
        setUserRestriction(UserManager.DISALLOW_APPS_CONTROL, active);
        setUserRestriction(UserManager.DISALLOW_CONFIG_CELL_BROADCASTS, active);
        setUserRestriction(UserManager.DISALLOW_CONFIG_SCREEN_TIMEOUT, active);
        setUserRestriction(UserManager.DISALLOW_CONFIG_TETHERING, active);
        setUserRestriction(UserManager.DISALLOW_CONFIG_MOBILE_NETWORKS, active);
        setUserRestriction(UserManager.DISALLOW_CREATE_WINDOWS, active);
        setUserRestriction(UserManager.DISALLOW_CONFIG_WIFI, active);
        setUserRestriction(UserManager.DISALLOW_DEBUGGING_FEATURES, active);
        setUserRestriction(UserManager.DISALLOW_FACTORY_RESET, active);
        setUserRestriction(UserManager.DISALLOW_DATA_ROAMING, active);
        setUserRestriction(UserManager.DISALLOW_INSTALL_APPS, active);
        setUserRestriction(UserManager.DISALLOW_FACTORY_RESET, active);
        setUserRestriction(UserManager.DISALLOW_SMS, active);
        setUserRestriction(UserManager.DISALLOW_NETWORK_RESET, active);
        setUserRestriction(UserManager.DISALLOW_USER_SWITCH, active);
        setUserRestriction(UserManager.DISALLOW_AMBIENT_DISPLAY, active);
        setUserRestriction(UserManager.DISALLOW_SYSTEM_ERROR_DIALOGS, active);
        setUserRestriction(UserManager.DISALLOW_UNINSTALL_APPS, active);

        // Disable keyguard and status bar
        mDevicePolicyManager.setKeyguardDisabled(mAdminComponentName, active);
        mDevicePolicyManager.setStatusBarDisabled(mAdminComponentName, active);

        // Enable STAY_ON_WHILE_PLUGGED_IN
        enableStayOnWhilePluggedIn(active);

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

    public static class UsbHandler extends Handler {
        public final WeakReference<MainActivity> mActivity;

        public UsbHandler(MainActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UsbService.MESSAGE_FROM_SERIAL_PORT:
                    packetSubject.onNext((byte[]) msg.obj);
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