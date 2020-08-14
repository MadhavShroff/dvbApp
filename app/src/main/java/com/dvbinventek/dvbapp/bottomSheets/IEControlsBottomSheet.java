package com.dvbinventek.dvbapp.bottomSheets;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;

import com.arthurivanets.bottomsheets.BaseBottomSheet;
import com.arthurivanets.bottomsheets.config.BaseConfig;
import com.arthurivanets.bottomsheets.config.Config;
import com.dvbinventek.dvbapp.R;
import com.dvbinventek.dvbapp.customViews.CustomKeyboardView;

import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;


public class IEControlsBottomSheet extends BaseBottomSheet {

    public boolean canceled = false;
    public Disposable subscription;
    EditText et1, et2, value;
    ImageButton done, cancel;
    Observable<Long> highlightObservable = Observable.interval(0, 500, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.newThread());
    OnClickListener selectEditTextListener = (v) -> {
        value.setBackgroundResource(R.drawable.rounded_corner_white);
        value = (EditText) v;
        if (subscription != null) if (!subscription.isDisposed()) subscription.dispose();
        highlightObservable
                .subscribe(new Observer<Long>() {
                    boolean highlighted = false;

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                    }

                    @Override
                    public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {
                        subscription = d;
                    }

                    @Override
                    public void onNext(Long aLong) {
                        if (!highlighted)
                            value.setBackgroundResource(R.drawable.rounded_corner_white_yellow_border);
                        else value.setBackgroundResource(R.drawable.rounded_corner_white);
                        highlighted = !highlighted;
                    }
                });
    };

    OnClickListener positiveListener = v -> {
        canceled = false;
        dismiss(true);
        setHint();
        canceled = true;
        v.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FCDD03")));
        new Handler().postDelayed(() -> v.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#ffffff"))), 100);
    };
    OnClickListener negativeListener = v -> {
        Log.d("MSG", "Negative Listener");
    };

    public IEControlsBottomSheet(@NonNull Activity hostActivity, @NonNull BaseConfig config) {
        super(hostActivity, config);
    }

    public IEControlsBottomSheet(@NonNull Activity hostActivity, String s) {
        this(hostActivity, new Config.Builder(hostActivity).build());
        done = findViewById(R.id.cbs_done);
        et1 = findViewById(R.id.cbs_inspet);
        et2 = findViewById(R.id.cbs_expet);
        cancel = findViewById(R.id.cbs_cancel);
        cancel.setOnClickListener(v -> {
            canceled = true;
            et1.setText("");
            et2.setText("");
            dismiss(true);
        });
        done.setOnClickListener(v -> {
            dismiss(true);
            setHint();
        });
        value = et1;
        et1.setOnClickListener(selectEditTextListener);
        et2.setOnClickListener(selectEditTextListener);
        CustomKeyboardView keyboard = findViewById(R.id.cbs_keyboard);
        keyboard.setKeyboardClickListener(digit -> {
            if (!digit.equals("backspace"))
                value.append(digit);
            else
                backspace();
            onNumberChanged();
        });
    }

    public boolean isInRange() {
        //TODO: IE range logic
        if (et1.getText().toString().isEmpty() || et2.getText().toString().isEmpty() || et1.getText().toString().equals(".") || et2.getText().toString().equals(".")) {
            return false;
        } else
            return !(Float.parseFloat(et1.getText().toString()) <= 0) && !(Float.parseFloat(et1.getText().toString()) > 4)
                    && !(Float.parseFloat(et2.getText().toString()) <= 0) && !(Float.parseFloat(et2.getText().toString()) > 4);
    }

    public void backspace() {
        if (value == null) return;
        String str = value.getText().toString();
        if (str.length() <= 0) return;
        str = str.substring(0, str.length() - 1);
        value.setText(str);
    }

    private void onNumberChanged() {
        if (value == null) return;
        if (value.getText().length() != 0 && isInRange()) {
            done.setOnClickListener(positiveListener);
            done.setEnabled(true);
            done.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.blue)));
        } else {
            done.setOnClickListener(negativeListener);
            done.setEnabled(false);
            done.setImageTintList(ColorStateList.valueOf(Color.parseColor("#afafaf")));
        }
    }

    public void clearValue() {
        et1.setText("");
        et2.setText("");
        et1.performClick();
    }

    public void setHint() {
        if (!et1.getText().toString().isEmpty()) et1.setHint(et1.getText());
        et1.setText("");
        if (!et2.getText().toString().isEmpty()) et2.setHint(et2.getText());
        et2.setText("");
    }

    public float getI() {
        float i;
        try {
            String s = et1.getText().toString();
            if (s.isEmpty()) s = et1.getHint().toString();
            if (s.isEmpty()) s = "1";
            i = Float.parseFloat(s);
        } catch (Exception e) {
            e.printStackTrace();
            i = 0;
        }
        return i;
    }

    public float getE() {
        float i;
        try {
            String s = et2.getText().toString();
            if (s.isEmpty()) s = et2.getHint().toString();
            if (s.isEmpty()) s = "1";
            i = Float.parseFloat(s);
        } catch (NullPointerException ex) {
            i = 0;
            Log.d("MSG", "Enetered wrong values");
        }
        return i;
    }

    @NonNull
    @Override
    protected View onCreateSheetContentView(@NonNull Context context) {
        return LayoutInflater.from(context).inflate(
                R.layout.bottomsheet_controls_ie,
                this,
                false
        );
    }
}
