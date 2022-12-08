package com.capstone.pacetime.receiver;

import android.Manifest;
import android.annotation.SuppressLint;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.location.LocationRequest;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

public class GPSReceiver implements ReceiverLifeCycleInterface, LocationListener {
    private static final String TAG = "GPSReceiver";
    private Handler dataHandler;
    private final Timer receiveTimer;
    private TimerTask receiveTask;

    private boolean paused = false;

    private final FusedLocationProviderClient client;
    private final LocationManager manager;

    public static String[] PERMISSIONS = new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
//                Manifest.permission.ACCESS_BACKGROUND_LOCATION
    };

    @SuppressLint("MissingPermission")
    public GPSReceiver(FusedLocationProviderClient client, LocationManager locationManager) {
        this.client = client;
        this.dataHandler = null;
        this.receiveTimer = new Timer();
        this.manager = locationManager;
    }

    public void setDataHandler(Handler dataHandler){
        this.dataHandler = dataHandler;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void start() {
        paused = false;
        manager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER
                , 1000
                , 5
                , this
        );
    }

    @Override
    public void stop() {
        manager.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        Log.d(TAG, "OnLocationChanged");
        if (paused) {
            return;
        }
        if (dataHandler == null) {
            return;
        }
        Bundle loc = new Bundle();
        loc.putParcelable("location", location);
        Message message = new Message();
        message.setData(loc);
        dataHandler.sendMessage(message);

    }


//    @Override
//    public void start(){
//        this.receiveTask = new ReceiveTask();
//        receiveTimer.schedule(receiveTask, 0, 1000);
//        Log.i("GPSReceiver", "STARTED");
//    }
//    @Override
//    public void stop(){
//        if(receiveTask != null){
//            receiveTask.cancel();
//            receiveTask = null;
//        }
//
//        dataHandler = null;
//        Log.i("GPSReceiver", "STOPPED");
//    }

    @Override
    public void pause() {
        paused = true;
    }

    @Override
    public void resume() {
        paused = false;
    }

    @SuppressLint("MissingPermission")
    public void getLocation(){
        this.receiveTask = new ReceiveTask();
        receiveTimer.schedule(receiveTask, 0);
    }


    private class ReceiveTask extends TimerTask{
        @RequiresApi(api = Build.VERSION_CODES.S)
        @SuppressLint("MissingPermission")
        @Override
        public void run() {
            CancellationTokenSource receiverCts = new CancellationTokenSource();
            client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, receiverCts.getToken())
                    .addOnSuccessListener(
                            (location) -> {
                                if(paused){
                                    return;
                                }
                                if(dataHandler == null){
                                    return;
                                }
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
