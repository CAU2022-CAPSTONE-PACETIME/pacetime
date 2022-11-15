package com.capstone.pacetime;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;

import com.capstone.pacetime.command.RunDetailInfoUpdateCommand;
import com.capstone.pacetime.command.RunInfoUpdateCommand;
import com.capstone.pacetime.databinding.ActivityRunBinding;
import com.capstone.pacetime.receiver.GPSReceiver;
import com.capstone.pacetime.viewmodel.RunDetailInfoViewModel;
import com.google.android.gms.location.CurrentLocationRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.OnMapsSdkInitializedCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.tasks.OnTokenCanceledListener;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class RunActivity extends AppCompatActivity implements OnMapReadyCallback, OnMapsSdkInitializedCallback {
    private static final String TAG = "RunActivity";
    private ActivityRunBinding binding;
    private RunDetailInfoViewModel viewModel;
    private RunInfoUpdateCommand command;
    private RunningManager manager;

    private HandlerThread runTriggerThread;
    private Handler runTriggerHandler, uiHandler;

    private final int READY2RUN     = 0;
    private final int RUN2PAUSE     = 1;
    private final int PAUSE2RUN     = 2;
    private final int PAUSE2STOP    = 3;

    private MapView mapView;
    private GoogleMap mMap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MapsInitializer.initialize(this, MapsInitializer.Renderer.LATEST, this);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_run);
        viewModel = new RunDetailInfoViewModel();
        command = new RunDetailInfoUpdateCommand();
        command.setViewModel(viewModel);
        binding.setDetailRunInfo(viewModel);
        binding.constraintReady.setVisibility(View.VISIBLE);
        binding.constraintRun.setVisibility(View.INVISIBLE);

        PermissionChecker.checkPermissions(
                this,
                RunningManager.getPermissionSets()
                );

        RunInfo runInfo = new RunInfo();
        runInfo.setCommand(command);

        manager = new RunningManager(this, runInfo);
        manager.setState(RunningState.COUNT);

        runTriggerThread = new HandlerThread("RunTriggerThread");
        runTriggerThread.start();

        runTriggerHandler = new Handler(runTriggerThread.getLooper()){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                if(msg.what == 0){
                    manager.start();
                    runTriggerThread.interrupt();
                    runTriggerHandler = null;
                }
            }
        };

        uiHandler = new Handler(getMainLooper()){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                runOnUiThread(() -> {
                    if (msg.what == READY2RUN) {
                        binding.includeDetailRunInfo.getRoot().setVisibility(View.GONE);
                        binding.buttonPause.setVisibility(View.INVISIBLE);
                        binding.constraintReady.setVisibility(View.GONE);
                        binding.constraintRun.setVisibility(View.VISIBLE);

                    } else if (msg.what == RUN2PAUSE) {
                        binding.includeDetailRunInfo.getRoot().setVisibility(View.VISIBLE);
                        binding.buttonPause.setVisibility(View.VISIBLE);
                    } else if (msg.what == PAUSE2RUN) {
                        binding.includeDetailRunInfo.getRoot().setVisibility(View.GONE);
                        binding.buttonPause.setVisibility(View.INVISIBLE);
                    }
                });
            }
        };

        binding.buttonRun.setOnClickListener((view)->{
            if(manager.getState() == RunningState.RUN){
                manager.setState(RunningState.PAUSE);
                uiHandler.sendEmptyMessage(PAUSE2RUN);
            }else{
                manager.setState(RunningState.RUN);
                uiHandler.sendEmptyMessage(RUN2PAUSE);
            }
        });

        mapView = binding.includeDetailRunInfo.mapView;
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }


    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        LatLng seoul = new LatLng(37.56, 126.97);

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(seoul);
        markerOptions.title("서울");
        mMap.addMarker(markerOptions);

        mMap.moveCamera((CameraUpdateFactory.newLatLngZoom(seoul, 10)));
    }

    @Override
    public void onMapsSdkInitialized(@NonNull MapsInitializer.Renderer renderer) {
        switch (renderer) {
            case LATEST:
                Log.d("MapsDemo", "The latest version of the renderer is used.");
                break;
            case LEGACY:
                Log.d("MapsDemo", "The legacy version of the renderer is used.");
                break;
        }
    }

    class ReadyTimerTask extends TimerTask{
        private int time;

        public ReadyTimerTask(int time){
            this.time = time;
        }

        @Override
        public void run() {
            runOnUiThread(()->binding.textviewReadyTimer.setText(String.valueOf(time)));
            time--;
            if(time < 0){
                uiHandler.sendEmptyMessage(READY2RUN);
                runTriggerHandler.sendEmptyMessage(READY2RUN);
                this.cancel();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        Timer timer = new Timer();
        timer.schedule(new ReadyTimerTask(3), 0, 1000);
    }

    @Override
    protected void onPause() {
        super.onPause();

        manager.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Destroy");
    }

}