package com.iovation.mobile.android.sample.sampleapp;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;

import com.iovation.mobile.android.FraudForceManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NativeActivity extends Activity {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    /**
     * Called when the activity is first created.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in onSaveInstanceState(Bundle). <b>Note: Otherwise it is null.</b>
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FraudForceManager.INSTANCE.refresh(getApplicationContext());

        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }

    public void printDevice(View target) {
        TextView bbResult = (TextView) findViewById(R.id.bbResult);
        bbResult.setText("");
        bbResult.setVisibility(View.INVISIBLE);
        TextView bbResultLabel = (TextView) findViewById(R.id.bbResultLabel);
        bbResultLabel.setText(R.string.printingWaitMsg);
        bbResultLabel.setVisibility(View.VISIBLE);

        executor.execute(() -> {
            String bb = FraudForceManager.INSTANCE.getBlackbox(getApplicationContext());
            mainHandler.post(() -> {
                TextView resultLabel = (TextView) findViewById(R.id.bbResultLabel);
                resultLabel.setText(R.string.bbResultLabel);
                resultLabel.setVisibility(View.VISIBLE);

                TextView result = (TextView) findViewById(R.id.bbResult);
                result.setText(bb);
                result.setVisibility(View.VISIBLE);
            });
        });
    }
}
