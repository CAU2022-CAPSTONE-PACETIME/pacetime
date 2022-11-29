package com.capstone.pacetime.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.View;

import com.capstone.pacetime.PermissionChecker;
import com.capstone.pacetime.R;
import com.capstone.pacetime.RealTimeRunInfo;
import com.capstone.pacetime.RunningManager;
import com.capstone.pacetime.RunningState;
import com.capstone.pacetime.command.RunDetailInfoUpdateCommand;
import com.capstone.pacetime.command.RunInfoUpdateCommand;
import com.capstone.pacetime.databinding.ActivityRunBinding;
import com.capstone.pacetime.receiver.GPSReceiver;
import com.capstone.pacetime.viewmodel.RunDetailInfoViewModel;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.OnMapsSdkInitializedCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

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
                if (msg.what == READY_RUN) { // READY -> RUN
                    runOnUiThread(() -> {
                        binding.includeDetailRunInfo.getRoot().setVisibility(View.GONE);
                        binding.buttonStop.setVisibility(View.INVISIBLE);
                        binding.constraintReady.setVisibility(View.GONE);
                        binding.constraintRun.setVisibility(View.VISIBLE);
                    });
                } else if (msg.what == RUN_PAUSE) { // RUN -> PAUSE
                    drawUserTrace(runInfo.getTrace());
                    runOnUiThread(() -> {
                        binding.includeDetailRunInfo.getRoot().setVisibility(View.VISIBLE);
                        binding.buttonStop.setVisibility(View.VISIBLE);
                    });
                } else if (msg.what == PAUSE_RUN) { // PAUSE -> RUN
                    runOnUiThread(() -> {
                        binding.includeDetailRunInfo.getRoot().setVisibility(View.GONE);
                        binding.buttonStop.setVisibility(View.INVISIBLE);
                    });
                } else { // PAUSE -> RUN

                }

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

        binding.buttonStop.setOnClickListener((view)->{
            if(manager.getState() == RunningState.STOP){
                manager.stop();
                uiHandler.sendEmptyMessage(PAUSE_STOP);
                Log.d(TAG, "State PAUSE -> STOP");
            }
        });

        mapView = binding.includeDetailRunInfo.mapView;
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }


    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
//        LatLng seoul = new LatLng(37.56, 126.97);
//
//        MarkerOptions markerOptions = new MarkerOptions();
//        markerOptions.position(seoul);
//        markerOptions.title("서울");
//        mMap.addMarker(markerOptions);

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

    private void drawUserTrace(List<Location> trace){
        ArrayList<LatLng> ll = new ArrayList<>();
        trace.forEach((Location location) -> {
                ll.add(new LatLng(location.getLatitude(), location.getLongitude()));
            }
        );

        PolylineOptions polyOptions = new PolylineOptions()
                .clickable(false)
                .addAll(ll);

        mMap.addPolyline(polyOptions);
        mMap.moveCamera((CameraUpdateFactory.newLatLngZoom(ll.get(ll.size()-1), 15)));
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