package com.capstone.pacetime;

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

import com.capstone.pacetime.receiver.BreathReceiver;
import com.capstone.pacetime.receiver.GPSReceiver;
import com.capstone.pacetime.receiver.StartStopInterface;
import com.capstone.pacetime.receiver.StepCounter;
import com.google.android.gms.location.LocationServices;

public class RunningManager implements StartStopInterface {
    private final GPSReceiver gpsReceiver;
    private final BreathReceiver breathReceiver;
    private final StepCounter stepCounter;

    private RunInfo runInfo;
    private RunningState state;

    private Handler handler;
    private final HandlerThread thread ;

    public RunningManager(AppCompatActivity activity){
        thread = new HandlerThread("DataHandlerThread");
        thread.start();

        SensorManager sensorManager = (SensorManager) activity.getApplicationContext().getSystemService(Context.SENSOR_SERVICE);

        gpsReceiver = new GPSReceiver(LocationServices.getFusedLocationProviderClient(activity), handler);
        breathReceiver = new BreathReceiver((AudioManager) activity.getApplicationContext().getSystemService(Context.AUDIO_SERVICE), handler);
        stepCounter = new StepCounter(sensorManager, handler);
    }

    @Override
    public void start(){
        handler = new Handler(thread.getLooper(), (@NonNull Message msg) -> {
                if(msg.arg1 == RunningDataType.BREATH.ordinal()){

                }
                else if(msg.arg1 == RunningDataType.LOCATION.ordinal()){
                    Bundle data = msg.getData();
                    if(data == null){
                        return false;
                    }
                    Location loc = data.getParcelable("location");

                    Log.i("RunningManager", "GPS: " + loc.toString());
                }else if(msg.arg1 == RunningDataType.STEP.ordinal()){
                    breathReceiver.doConvert(System.currentTimeMillis());
                }
                return true;
            }
        );
        gpsReceiver.start();
        breathReceiver.start();
        stepCounter.start();
    }

    @Override
    public void stop() {
        gpsReceiver.stop();
        breathReceiver.stop();
        stepCounter.stop();
    }

}
