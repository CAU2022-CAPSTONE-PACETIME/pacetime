package com.capstone.pacetime.receiver;

import android.Manifest;
import android.annotation.SuppressLint;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;

import java.util.Timer;
import java.util.TimerTask;

public class GPSReceiver implements StartStopInterface{
    private Handler dataHandler;
    private final Timer receiveTimer;
    private TimerTask receiveTask;

    private final FusedLocationProviderClient client;

    public static String[] PERMISSIONS = new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
//                Manifest.permission.ACCESS_BACKGROUND_LOCATION
    };

    public GPSReceiver(FusedLocationProviderClient client) {
        this.client = client;
        this.dataHandler = null;
        this.receiveTimer = new Timer();
    }

    public void setDataHandler(Handler dataHandler){
        this.dataHandler = dataHandler;
    }


    @Override
    public void start(){
        this.receiveTask = new ReceiveTask();
        receiveTimer.schedule(receiveTask, 0, 1000);
        Log.i("GPSReceiver", "STARTED");
    }
    @Override
    public void stop(){
        if(receiveTask != null){
            receiveTask.cancel();
            receiveTask = null;
        }

        Log.i("GPSReceiver", "STOPPED");
    }

    public void getLocation(){
        this.receiveTask = new ReceiveTask();
        receiveTimer.schedule(receiveTask, 0);
    }

    private class ReceiveTask extends TimerTask{
        @SuppressLint("MissingPermission")
        @Override
        public void run() {
            CancellationTokenSource receiverCts = new CancellationTokenSource();
            client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, receiverCts.getToken())
                    .addOnSuccessListener(
                            (location) -> {
                                if (location != null) {
                                    Bundle loc = new Bundle();
                                    loc.putParcelable("location", location);
                                    Message message = new Message();
                                    message.setData(loc);
                                    dataHandler.sendMessage(message);
                                }
                            }
                    );
        }
    }

}
