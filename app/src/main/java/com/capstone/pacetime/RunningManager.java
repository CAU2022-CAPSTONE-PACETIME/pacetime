package com.capstone.pacetime;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.SensorManager;
import android.location.Location;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.capstone.pacetime.data.Breath;
import com.capstone.pacetime.data.Step;
import com.capstone.pacetime.receiver.BreathReceiver;
import com.capstone.pacetime.receiver.GPSReceiver;
import com.capstone.pacetime.receiver.StartStopInterface;
import com.capstone.pacetime.receiver.StepCounter;
import com.google.android.gms.location.LocationServices;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class RunningManager implements StartStopInterface {
    private final GPSReceiver gpsReceiver;
    private final BreathReceiver breathReceiver;
    private final StepCounter stepCounter;

    private UpdateTask updateTask;
    private final Timer updateTimer;

    private final RunInfo runInfo;
    private RunningState state;

    private Handler handler;
    private final HandlerThread thread ;

    class DataThread extends HandlerThread{

        public DataThread(String name) {
            super(name);
        }

        @Override
        public synchronized void start() {
            super.start();
            handler = new Handler(getLooper()) {
                @Override
                public void handleMessage(@NonNull Message msg) {
                    super.handleMessage(msg);
                    if(msg.arg1 == RunningDataType.BREATH.ordinal()){
                        runInfo.addBreathItem((Breath)msg.obj);
                    }
                    else if(msg.arg1 == RunningDataType.LOCATION.ordinal()){
                        Bundle data = msg.getData();
                        if(data == null){
                            return ;
                        }
                        Location loc = data.getParcelable("location");
                        Log.i("RunningManager", "GPS: " + loc.toString());

                        runInfo.addTrace(loc);

                    }else if(msg.arg1 == RunningDataType.STEP.ordinal()){
                        runInfo.addStepCount((Step)msg.obj);
                        Log.d("RunningManager", "Step: " + ((Step) msg.obj).getCount());

                        breathReceiver.doConvert(System.currentTimeMillis());
                    }
                }
            };

            gpsReceiver.setDataHandler(handler);
            breathReceiver.setDataHandler(handler);
            stepCounter.setDataHandler(handler);
        }
    }

    public RunningManager(AppCompatActivity activity, RunInfo runInfo){
        this.runInfo = runInfo;

        updateTimer = new Timer();

        thread = new DataThread("DataHandlerThread");

        SensorManager sensorManager = (SensorManager) activity.getApplicationContext().getSystemService(Context.SENSOR_SERVICE);

        gpsReceiver = new GPSReceiver(LocationServices.getFusedLocationProviderClient(activity));
        breathReceiver = new BreathReceiver((AudioManager) activity.getApplicationContext().getSystemService(Context.AUDIO_SERVICE));
        stepCounter = new StepCounter(sensorManager);
    }

    public void changeState(RunningState state){
        this.state = state;
    }


    private class UpdateTask extends TimerTask {
        @SuppressLint("MissingPermission")
        @Override
        public void run() {
            runInfo.update();
        }
    }

    @Override
    public void start(){
        thread.start();
        this.updateTask = new UpdateTask();
        updateTimer.schedule(updateTask, 1000, 1000);

        gpsReceiver.start();
        breathReceiver.start();
        stepCounter.start();
    }

    @Override
    public void stop() {
        if(updateTask != null){
            updateTask.cancel();
            updateTask = null;
        }
//        gpsReceiver.stop();
//        breathReceiver.stop();
        stepCounter.stop();
    }

    public static String[] getPermissionSets(){
        String[] ret = new String[
                BreathReceiver.PERMISSIONS.length +
                        GPSReceiver.PERMISSIONS.length +
                        StepCounter.PERMISSIONS.length
                ];

        int cnt = 0;
        for(String[] permissions : new String[][]{ BreathReceiver.PERMISSIONS, GPSReceiver.PERMISSIONS, StepCounter.PERMISSIONS }){
            for(String permission : permissions){
                ret[cnt] = permission;
                cnt++;
            }
        }

        return ret;
    }

}
