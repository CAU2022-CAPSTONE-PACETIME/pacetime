package com.capstone.pacetime.receiver;

import android.Manifest;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.media.AudioManager;
import android.os.Build;

import androidx.annotation.RequiresApi;

public class BreathReceiver implements StartStopInterface, SensorEventListener {
    private AudioManager audioManager;
    private StartStopInterface command;

    public static String[] PERMISSIONS = new String[]{
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN
    };

    public BreathReceiver(AudioManager audioManager){
        this.audioManager = audioManager;
    }

    public void setCommand(StartStopInterface command){

        this.command = command;
    }

    @Override
    public void start() {
        command.start();
    }

    @Override
    public void stop() {

    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
