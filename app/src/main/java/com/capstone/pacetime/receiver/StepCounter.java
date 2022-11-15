package com.capstone.pacetime.receiver;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;

import android.Manifest;
import com.capstone.pacetime.RunningDataType;
import com.capstone.pacetime.data.Step;

public class StepCounter implements ReceiverLifeCycleInterface {
    private Handler dataHandler;
    private final ReceiverLifeCycleInterface command;
    private final SensorEventListener sensorEventListener;

    private final Sensor counter;

    private int startStep = -1;

    private boolean paused = false;

    public static final String[] PERMISSIONS = {
            Manifest.permission.ACTIVITY_RECOGNITION
    };

    public void setDataHandler(Handler dataHandler){
        this.dataHandler = dataHandler;
    }
    public StepCounter(SensorManager sensorManager){
        this.dataHandler = null;

        this.counter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        this.sensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                if(paused)
                    return;
                if(dataHandler == null){
                    return;
                }
                Message msg = new Message();
                msg.arg1 = RunningDataType.STEP.ordinal();

                int step;
                if(startStep == -1){
                    step = startStep = (int)sensorEvent.values[0];
                }else{
                    step = (int)sensorEvent.values[0] - startStep;
                }

                msg.obj = new Step(step, System.currentTimeMillis());
                dataHandler.sendMessage(msg);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };

        this.command = new ReceiverLifeCycleInterface() {
            @Override
            public void start() {
                sensorManager.registerListener(sensorEventListener, counter, SensorManager.SENSOR_DELAY_NORMAL);
            }

            @Override
            public void stop() {
                sensorManager.unregisterListener(sensorEventListener, counter);
            }

            @Override
            public void pause() {
                paused = true;
            }

            @Override
            public void resume() {
                paused = false;
            }


        };
    }

    @Override
    public void start() {
        command.start();
    }

    @Override
    public void stop() {
        command.stop();
    }

    @Override
    public void pause() {
        command.pause();
    }

    @Override
    public void resume() {
        command.resume();
    }

}
