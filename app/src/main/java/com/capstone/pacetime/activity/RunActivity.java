package com.capstone.pacetime.activity;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.View;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.capstone.pacetime.BuildConfig;
import com.capstone.pacetime.data.RunInfo;
import com.capstone.pacetime.util.PermissionChecker;
import com.capstone.pacetime.R;
import com.capstone.pacetime.data.RealTimeRunInfo;
import com.capstone.pacetime.RunningManager;
import com.capstone.pacetime.data.enums.RunningState;
import com.capstone.pacetime.command.RunDetailInfoUpdateCommand;
import com.capstone.pacetime.command.RunInfoUpdateCommand;
import com.capstone.pacetime.databinding.ActivityRunBinding;
import com.capstone.pacetime.util.RunDataManager;
import com.capstone.pacetime.util.RunInfoParser;
import com.capstone.pacetime.viewmodel.RunDetailInfoViewModel;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.OnMapsSdkInitializedCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;

public class RunActivity extends AppCompatActivity implements OnMapReadyCallback, OnMapsSdkInitializedCallback {
    private static final String TAG = "RunActivity";
    private ActivityRunBinding binding;
    private RunDetailInfoViewModel viewModel;
    private RunInfoUpdateCommand command;
    private RunningManager manager;
    static RequestQueue requestQueue;

    private HandlerThread runTriggerThread;
    private Handler runTriggerHandler, uiHandler;

    private final int READY_RUN = 0;
    private final int RUN_PAUSE = 1;
    private final int PAUSE_RUN = 2;
    private final int PAUSE_STOP = 3;

    private MapView mapView;
    private GoogleMap mMap;


    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
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

        if(!PermissionChecker.checkPermissions(
                this,
                RunningManager.getPermissionSets()
        )){
            finish();
        }

        int inhaleCnt = getIntent().getIntExtra("Inhale", 0);
        int exhaleCnt = getIntent().getIntExtra("Exhale", 0);

        RealTimeRunInfo runInfo;
        if(inhaleCnt == 0){
            runInfo = new RealTimeRunInfo(false, 0, 0);
        }
        else{
            runInfo = new RealTimeRunInfo(true, inhaleCnt, exhaleCnt);
        }
        runInfo.setCommand(command);

        String cityAddr = getIntent().getStringExtra("cityAddr");
        runInfo.setStartLocation(cityAddr);
//        runInfo.setStartLocation("aa");
        Log.d("RUNINFOCHECKCHECK", runInfo.getStartLocation());

        manager = new RunningManager(RunActivity.this, runInfo);
        manager.setState(RunningState.COUNT);

        runTriggerThread = new HandlerThread("RunTriggerThread");
        runTriggerThread.start();

        runTriggerHandler = new Handler(runTriggerThread.getLooper()){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                if(msg.what == 0){
                    if(manager != null)
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
                        binding.buttonRun.setSelected(true);
                    });
                } else if (msg.what == PAUSE_RUN) { // PAUSE -> RUN
                    runOnUiThread(() -> {
                        binding.includeDetailRunInfo.getRoot().setVisibility(View.GONE);
                        binding.buttonStop.setVisibility(View.INVISIBLE);
                        binding.buttonRun.setSelected(false);
                    });
                } else { // PAUSE -> STOP
                    manager.stop();
                    RunDataManager rdm = RunDataManager.getInstance();
                    rdm.runInfoToFirebase(new RunInfoParser(runInfo));

                    Intent resultIntent = new Intent(getApplicationContext(), ResultActivity.class);
                    resultIntent.putExtra("index", 0);

                    Bundle b = new Bundle();
                    b.putParcelableArrayList("bs", manager.getStabilities());
                    resultIntent.putExtra("bundle", b);

                    startActivity(resultIntent);
                    finish();
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
            if(manager.getState() == RunningState.PAUSE){
                manager.stop();
                uiHandler.sendEmptyMessage(PAUSE_STOP);
                Log.d(TAG, "State PAUSE -> STOP");
            }
        });

        mapView = binding.includeDetailRunInfo.mapView;
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        Timer timer = new Timer();
        timer.schedule(new ReadyTimerTask(3), 0, 1000);
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
        trace.forEach((Location location) -> ll.add(new LatLng(location.getLatitude(), location.getLongitude())));

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
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if(manager != null) {
            if(manager.getState() != RunningState.STOP){
                manager.stop();
                manager = null;
            }
        }
        if(runTriggerThread != null && runTriggerThread.isAlive()){
            runTriggerThread.interrupt();
        }
        Log.d(TAG, "Destroy");
        super.onDestroy();
    }
}