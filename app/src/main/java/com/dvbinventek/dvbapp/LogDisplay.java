package com.dvbinventek.dvbapp;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

import cdflynn.android.library.checkview.CheckView;

public class LogDisplay extends AppCompatActivity {

    HistoricalDataRecyclerViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_display);
        Button back = findViewById(R.id.back);
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
        pb.setVisibility(View.GONE);
        save.setOnClickListener(v -> {
            cv.uncheck();
            pb.setVisibility(View.VISIBLE);
            final Handler handler = new Handler();
            try {
                writeDataToFile();
                save.setClickable(false);
                handler.postDelayed(() -> {
                    pb.setVisibility(View.GONE);
                    cv.check();
                    save.setClickable(true);
                }, 5000);
            } catch (IOException e) {
                handler.postDelayed(() -> {
                    pb.setVisibility(View.GONE);
                }, 5000);
                Log.d("MSG", "Error in saving data to file");
                e.printStackTrace();
                final String s = (String) save.getText();
                save.setText(R.string.err);
                new Handler().postDelayed(() -> {
                    save.setText(s);
                }, 5000);
            }
        });
        RecyclerView recyclerView = findViewById(R.id.table_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HistoricalDataRecyclerViewAdapter(StaticStore.Data);
        recyclerView.setAdapter(adapter);
    }

    public void writeDataToFile() throws IOException {
        Log.d("MSG", "Generating File to store, num rows: " + StaticStore.Data.size());
        String fileName = DateFormat.format("MM-dd-yyyyy-h-mm-ss-aa", System.currentTimeMillis()).toString() + ".csv";
        Path path = Paths.get(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).toString(), fileName);
        Files.createFile(path);
        FileWriter writer = new FileWriter(path.toString());
        String s = "date, pinsp, set-pinsp, ppeak, peep, set-peep, mvTotal, vt, set-vt, rtotal, set-rtotal, fio2, set-fio2, pmean, mvSpont, ie, set-ie, cStat, warnings\n";
        writer.append(s);
        synchronized (StaticStore.Data) {
            for (HashMap<String, String> i : StaticStore.Data) {
                s = "";
                s = s + i.get("date") + ", ";
                s = s + i.get("pinsp") + ", ";
                s = s + i.get("set-pinsp") + ", ";
                s = s + i.get("ppeak") + ", ";
                s = s + i.get("peep") + ", ";
                s = s + i.get("set-peep") + ", ";
                s = s + i.get("mvTotal") + ", ";
                s = s + i.get("vt") + ", ";
                s = s + i.get("set-vt") + ", ";
                s = s + i.get("rtotal") + ", ";
                s = s + i.get("set-rtotal") + ", ";
                s = s + i.get("fio2") + ", ";
                s = s + i.get("set-fio2") + ", ";
                s = s + i.get("pmean") + ", ";
                s = s + i.get("mvSpont") + ", ";
                s = s + i.get("ie") + ", ";
                s = s + i.get("set-ie") + ", ";
                s = s + i.get("cStat") + ", ";
                s = s + i.get("warning") + "\n";
                writer.append(s);
            }
            writer.flush();
            writer.close();
        }
        Log.d("MSG", "Saved to " + path);
    }
}