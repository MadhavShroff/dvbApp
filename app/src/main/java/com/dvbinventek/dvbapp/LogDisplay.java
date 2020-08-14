package com.dvbinventek.dvbapp;

import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;

import androidx.appcompat.app.AppCompatActivity;

import cdflynn.android.library.checkview.CheckView;

public class LogDisplay extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_display);
        Button back = findViewById(R.id.back);
        TableLayout tl = findViewById(R.id.table);
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
        back.setOnClickListener(v -> {
//            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            onBackPressed();
        });
        ProgressBar pb = findViewById(R.id.progress_bar);
        Button save = findViewById(R.id.saveData);
        CheckView cv = findViewById(R.id.check);
        FrameLayout f = findViewById(R.id.frame);
        pb.setVisibility(View.GONE);
        save.setOnClickListener(v -> {
            Log.d("MSG", "Reached click Listener");
            cv.uncheck();
            f.setLayoutParams(new LinearLayout.LayoutParams(80, ViewGroup.LayoutParams.MATCH_PARENT));
            pb.setVisibility(View.VISIBLE);
            final Handler handler = new Handler();
            handler.postDelayed(() -> {
                f.setLayoutParams(new LinearLayout.LayoutParams(130, ViewGroup.LayoutParams.MATCH_PARENT));
                pb.setVisibility(View.GONE);
                cv.check();
            }, 2000);

        });
        new GenerateTableRowsAsyncTask(getApplicationContext(), tl).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}