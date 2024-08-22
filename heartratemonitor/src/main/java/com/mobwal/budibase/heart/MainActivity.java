package com.mobwal.budibase.heart;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.android.material.snackbar.Snackbar;

import java.util.Map;

public class MainActivity extends AppCompatActivity
    implements View.OnClickListener, SensorEventListener, DataClient.OnDataChangedListener {

    public static final String TAG = "HEART_RATE";

    protected final ActivityResultLauncher<String[]> mPermissionResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), this::onSensorsPermission);

    private Sensor mHeartRateSensor;
    private SensorManager mSensorManager;
    private Button mManagerMonitor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mManagerMonitor = findViewById(R.id.manager_monitor);
        mManagerMonitor.setOnClickListener(this);

        Wearable.getDataClient(this).addListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();

        Wearable.getDataClient(this).removeListener(this);
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.manager_monitor) {
            if(mManagerMonitor.getText() == getString(R.string.start)) {
                permissionRequest();
            } else {
                stopMonitor();
            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_HEART_RATE) {
            String msg = " Value sensor: " + (int)sensorEvent.values[0];
            Log.d(TAG, msg);

            DataClient dataClient = Wearable.getDataClient(this);
            PutDataMapRequest putDataMapReq = PutDataMapRequest.create("/message").setUrgent();
            putDataMapReq.getDataMap().putInt(Sensor.STRING_TYPE_HEART_RATE, (int)sensorEvent.values[0]);
            PutDataRequest putDataReq = putDataMapReq.asPutDataRequest().setUrgent();
            Task<DataItem> putDataTask = dataClient.putDataItem(putDataReq);

            putDataTask.addOnSuccessListener(new OnSuccessListener<DataItem>() {
                @Override
                public void onSuccess(DataItem dataItem) {
                    Log.d(TAG, "sending :)");
                }
            });
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        Log.d(TAG, "onAccuracyChanged - accuracy: " + i);
    }

    private void permissionRequest() {
        if (checkSelfPermission(android.Manifest.permission.BODY_SENSORS) != PackageManager.PERMISSION_GRANTED) {
            mPermissionResultLauncher.launch(new String[] { android.Manifest.permission.BODY_SENSORS });
        }
        else{
            Log.d(TAG,"ALREADY GRANTED");

            initSensors();
            startMonitor();
        }
    }

    protected void onSensorsPermission(@NonNull Map<String, Boolean> result) {
        boolean areAllGranted = true;
        for (Boolean b : result.values()) {
            areAllGranted = areAllGranted && b;
        }

        if (areAllGranted) {
            initSensors();

            startMonitor();
        } else {
            Snackbar.make(findViewById(R.id.main), "Недостаточно прав для приложения", Snackbar.LENGTH_LONG).setAction("...", view -> {
                startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName())));
            }).show();
        }
    }


    private void startMonitor() {
        mManagerMonitor.setText(getString(R.string.stop));

        if (mSensorManager != null && mHeartRateSensor != null) {
            mSensorManager.registerListener(this, mHeartRateSensor, SensorManager.SENSOR_DELAY_FASTEST);
        }
    }

    private void stopMonitor() {
        mManagerMonitor.setText(getString(R.string.start));

        if (mSensorManager != null)
            mSensorManager.unregisterListener(this);
    }

    private void  initSensors() {
        mSensorManager = (SensorManager)this.getSystemService(Context.SENSOR_SERVICE);
        mHeartRateSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
    }

    @Override
    protected void onDestroy() {
        stopMonitor();

        super.onDestroy();
    }

    @Override
    public void onDataChanged(@NonNull DataEventBuffer dataEventBuffer) {
        for (DataEvent event : dataEventBuffer) {
            if (event.getType() == DataEvent.TYPE_DELETED) {
                Log.d(TAG, "DataItem deleted: " + event.getDataItem().getUri());
            } else if (event.getType() == DataEvent.TYPE_CHANGED) {
                Log.d(TAG, "DataItem changed: " + event.getDataItem().getUri());
            }
        }
    }
}