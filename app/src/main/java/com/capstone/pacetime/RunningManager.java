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
    private GPSReceiver gpsReceiver;
    private BreathReceiver breathReceiver;
    private StepCounter stepCounter;

    private RunInfo runInfo;
    private RunningState state;

    private Handler handler;
    private final HandlerThread thread ;

    public RunningManager(AppCompatActivity activity){
        thread = new HandlerThread("DataHandlerThread");

        SensorManager sensorManager = (SensorManager) activity.getApplicationContext().getSystemService(Context.SENSOR_SERVICE);

        gpsReceiver = new GPSReceiver(LocationServices.getFusedLocationProviderClient(activity), handler);
        breathReceiver = new BreathReceiver((AudioManager) activity.getApplicationContext().getSystemService(Context.AUDIO_SERVICE));
    }

    @Override
    public void start(){
        thread.start();
        handler = new Handler(thread.getLooper(), (@NonNull Message message) -> {
                Bundle data = message.getData();
                if(data == null){
                    return false;
                }
                if(data.keySet().contains("location")){
                    Location loc = data.getParcelable("location");

                    Log.i("RunningManager", "GPS: " + loc.toString());
                }
                if(data.keySet().contains("breath")){}
                if(data.keySet().contains("step")){}
                return true;
            }
        );
        gpsReceiver.start();
    }

    @Override
    public void stop() {
        gpsReceiver.stop();
    }

}
