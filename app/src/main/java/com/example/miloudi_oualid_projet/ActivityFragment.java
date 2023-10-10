package com.example.miloudi_oualid_projet;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;

import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.tasks.Task;


public class ActivityFragment extends Fragment  implements SensorEventListener {



    BroadcastReceiver broadcastReceiver;

    private TextView txtActivity, dist;
    static TextView speed , averagespeed;
    private ActivityRecognitionClient mActivityRecognitionClient;
    private PendingIntent mPendingIntent;
    private Intent mIntentService;
    private SensorManager sManager;
    private Sensor stepSensor;
    private long steps = 0;

    public ActivityFragment() {
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_activity, container, false);


        mActivityRecognitionClient = new ActivityRecognitionClient(getContext());
        mIntentService = new Intent(getContext(), DetectedActivities.class);
        mPendingIntent = PendingIntent.getService(getContext(), 1, mIntentService, PendingIntent.FLAG_UPDATE_CURRENT);
        txtActivity = view.findViewById(R.id.txt_activity);
        dist = view.findViewById(R.id.distance);
        speed = view.findViewById(R.id.speed);
        averagespeed=view.findViewById(R.id.avgspeed);




        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("activity_intent")) {
                    int type = intent.getIntExtra("type", -1);
                    int confidence = intent.getIntExtra("confidence", 0);
                    handleUserActivity(type, confidence);
                }
            }
        };

        startTracking();
         this.sManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
         this.stepSensor =(Sensor) sManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        return view;

    }
    private void handleUserActivity(int type, int confidence) {
        String label = getString(R.string.activity_unknown);

        switch (type) {
            case DetectedActivity.IN_VEHICLE: {
                label = getString(R.string.activity_in_vehicle);

                break;
            }
            case DetectedActivity.ON_BICYCLE: {
                label = getString(R.string.activity_on_bicycle);
                break;
            }
            case DetectedActivity.ON_FOOT: {
                label = getString(R.string.activity_on_foot);
                break;
            }
            case DetectedActivity.RUNNING: {
                label = getString(R.string.activity_running);
                break;
            }
            case DetectedActivity.STILL: {
                label = getString(R.string.activity_still);
                break;
            }
            case DetectedActivity.TILTING: {
                label = getString(R.string.activity_tilting);
                break;
            }
            case DetectedActivity.WALKING: {
                label = getString(R.string.activity_walking);
                break;
            }
            case DetectedActivity.UNKNOWN: {
                label = getString(R.string.activity_unknown);
                break;
            }
        }


        if (confidence > 60) {
            txtActivity.setText("Activité : "+label);

        }

    }

    @Override
    public void onResume() {
        super.onResume();
        sManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_FASTEST);

        LocalBroadcastManager.getInstance(getContext()).registerReceiver(broadcastReceiver,
                new IntentFilter("activity_intent"));
    }

    @Override
    public void onPause() {
        super.onPause();
        sManager.unregisterListener(this, stepSensor);

        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(broadcastReceiver);
    }
    private void startTracking() {
        @SuppressLint("MissingPermission") Task<Void> task = mActivityRecognitionClient.requestActivityUpdates(
                0,
                mPendingIntent);


    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor sensor = sensorEvent.sensor;
        float[] values = sensorEvent.values;
        int value = -1;

        if (values.length > 0) {
            value = (int) values[0];
        }


        if (sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
            steps++;
        }
        dist.setText("Distance : "+getDistanceRun(steps)+"km");

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
    public float getDistanceRun(long steps){
        float distance = (float)(steps*78)/(float)100000;
        return distance;
    }
}