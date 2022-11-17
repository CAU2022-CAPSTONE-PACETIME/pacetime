package com.capstone.pacetime;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.View;

import com.capstone.pacetime.command.RunDetailInfoUpdateCommand;
import com.capstone.pacetime.command.RunInfoUpdateCommand;
import com.capstone.pacetime.databinding.ActivityRunBinding;
import com.capstone.pacetime.viewmodel.RunDetailInfoViewModel;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.OnMapsSdkInitializedCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

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

    private final int READY_RUN = 0;
    private final int RUN_PAUSE = 1;
    private final int PAUSE_RUN = 2;
    private final int PAUSE_STOP = 3;

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

        RealTimeRunInfo runInfo = new RealTimeRunInfo();
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
                    if (msg.what == READY_RUN) { // READY -> RUN
                        binding.includeDetailRunInfo.getRoot().setVisibility(View.GONE);
                        binding.buttonPause.setVisibility(View.INVISIBLE);
                        binding.constraintReady.setVisibility(View.GONE);
                        binding.constraintRun.setVisibility(View.VISIBLE);
                    }
                    else if (msg.what == RUN_PAUSE) { // RUN -> PAUSE
                        binding.includeDetailRunInfo.getRoot().setVisibility(View.VISIBLE);
                        binding.buttonPause.setVisibility(View.VISIBLE);
                    }
                    else if (msg.what == PAUSE_RUN) { // PAUSE -> RUN
                        binding.includeDetailRunInfo.getRoot().setVisibility(View.GONE);
                        binding.buttonPause.setVisibility(View.INVISIBLE);
                    }
                    else { // PAUSE -> RUN

                    }
                });
            }
        };

        binding.buttonRun.setOnClickListener((view)->{
            if(manager.getState() == RunningState.RUN){
                manager.pause();
                uiHandler.sendEmptyMessage(RUN_PAUSE);
                Log.d(TAG, "State RUN -> PAUSE");
            }else{
                manager.resume();
                uiHandler.sendEmptyMessage(PAUSE_RUN);
                Log.d(TAG, "State PAUSE -> RUN");
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
                uiHandler.sendEmptyMessage(READY_RUN);
                runTriggerHandler.sendEmptyMessage(READY_RUN);
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

        manager.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Destroy");
    }

}