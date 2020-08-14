package com.dvbinventek.dvbapp.bottomSheets;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Handler;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

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
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class AlarmLimitsBottomSheet extends BaseBottomSheet {

    public final EditText max = findViewById(R.id.albs_maxet);
    public final EditText min = findViewById(R.id.albs_minet);
    public final ImageButton done = findViewById(R.id.albs_done);
    public final ImageButton cancel = findViewById(R.id.albs_cancel);
    public final TextView heading = findViewById(R.id.albs_heading);
    public final TextView subHeading = findViewById(R.id.albs_subheading);
    public EditText value;
    public boolean isDone = false;
    public String type;
    public CompositeDisposable disposables = new CompositeDisposable();
    Observable<Long> highlightObservable = Observable.interval(0, 500, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.newThread());
    OnClickListener selectEditTextListener = (v) -> {
        value.setBackgroundResource(R.drawable.rounded_corner_white);
        value = (EditText) v;
        highlightObservable.subscribe(new Observer<Long>() {
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
                disposables.add(d);
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
        isDone = true;
        dismiss(true);
        setHint();
        isDone = false;
        v.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FCDD03")));
        new Handler().postDelayed(() -> v.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#ffffff"))), 100);
    };
    OnClickListener negativeListener = v -> {
        Log.d("MSG", "Negative Listener");
    };

    public AlarmLimitsBottomSheet(@NonNull Activity hostActivity, @NonNull BaseConfig config) {
        super(hostActivity, config);
    }

    public AlarmLimitsBottomSheet(@NonNull Activity hostActivity, Spanned h, String type) {
        this(hostActivity, new Config.Builder(hostActivity).build());
        this.type = type;
        heading.setText(R.string.setAlarmLimits);
        subHeading.setText(h);
        done.setImageTintList(ColorStateList.valueOf(Color.parseColor("#afafaf")));
        done.setOnClickListener(positiveListener);
        max.setOnClickListener(selectEditTextListener);
        min.setOnClickListener(selectEditTextListener);
        value = max;
        max.performClick();
        cancel.setOnClickListener(v -> {
            isDone = false;
            clear();
            v.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FCDD03")));
            new Handler().postDelayed(() -> v.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#ffffff"))), 100);
            dismiss(true);
        });
        done.setOnClickListener(v -> {
            isDone = true;
            dismiss(true);
            setHint();
            isDone = false;
        });
        onNumberChanged();
        CustomKeyboardView keyboard = findViewById(R.id.cbs_keyboard);
        keyboard.setKeyboardClickListener(digit -> {
            if (!digit.equals("backspace"))
                value.append(digit);
            else
                backspace();
            onNumberChanged();
        });
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

    public boolean isInRange() {
        String s = value.getText().toString();
        if (s.equals(".")) return false;
        short sh = Short.parseShort(s);
        switch (this.type) {

        }
        //TODO:
        return true;
    }

    public void clear() {
        max.setText("");
        min.setText("");
    }

    public String getMin() {
        if (min.getText().toString().isEmpty()) {
            if (min.getHint() == null)
                return "0";
            else
                return min.getHint().toString();
        } else {
            return min.getText().toString();
        }
    }

    public String getMax() {
        if (max.getText().toString().isEmpty()) {
            if (max.getHint() == null)
                return "0";
            else
                return max.getHint().toString();
        } else {
            return max.getText().toString();
        }
    }

    public void setHint() {
        if (!max.getText().toString().isEmpty()) max.setHint(max.getText());
        else max.setText("");
        if (!min.getText().toString().isEmpty()) min.setHint(min.getText());
        else min.setText("");
    }

    @Override
    public boolean requestFocus(int direction, Rect previouslyFocusedRect) {
        max.requestFocus();
        return true;
    }

    @NonNull
    @Override
    protected View onCreateSheetContentView(@NonNull Context context) {
        return LayoutInflater.from(context).inflate(R.layout.bottomsheet_alarmlimits, this, false
        );
    }
}
