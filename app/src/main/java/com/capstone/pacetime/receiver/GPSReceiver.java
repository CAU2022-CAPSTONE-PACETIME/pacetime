package com.capstone.pacetime.receiver;

import android.Manifest;
import android.annotation.SuppressLint;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.renderscript.RenderScript;

import androidx.annotation.NonNull;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnTokenCanceledListener;
import com.google.android.gms.tasks.Task;

import java.util.Timer;
import java.util.TimerTask;

public class GPSReceiver implements StartStopInterface{
    private final Handler dataHandler;
    private final Timer receiveTimer;
    private final TimerTask receiveTask;

    private final FusedLocationProviderClient client;

    public static String[] PERMISSIONS = new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
    };

    @SuppressLint("MissingPermission")
    public GPSReceiver(FusedLocationProviderClient client, Handler dataHandler) {
        this.client = client;
        this.dataHandler = dataHandler;
        this.receiveTimer = new Timer();
        this.receiveTask = new TimerTask() {
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
        };

    }
    @Override
    public void start(){
        receiveTimer.schedule(receiveTask, 0, 1000);
    }
    @Override
    public void stop(){
        receiveTask.cancel();
        receiveTimer.cancel();
        receiveTimer.purge();
    }
}
