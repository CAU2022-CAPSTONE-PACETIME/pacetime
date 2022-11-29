package com.capstone.pacetime.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

//import com.capstone.pacetime.command.RunDetailInfoUpdateCommand;
import com.capstone.pacetime.R;
import com.capstone.pacetime.RunDataManager;
import com.capstone.pacetime.RunInfo;
import com.capstone.pacetime.command.RunInfoUpdateCommand;
import com.capstone.pacetime.data.Step;
import com.capstone.pacetime.databinding.ActivityResultBinding;
import com.capstone.pacetime.viewmodel.RunDetailInfoViewModel;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.OnMapsSdkInitializedCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.EnumSet;

public class ResultActivity extends AppCompatActivity implements OnMapReadyCallback, OnMapsSdkInitializedCallback {

    private ActivityResultBinding binding;
    private RunDetailInfoViewModel viewModel;
    private RunInfo info;

    private MapView mapView;
    private GoogleMap mMap;

    RunDataManager runDataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        runDataManager = RunDataManager.getInstance();

        MapsInitializer.initialize(this, MapsInitializer.Renderer.LATEST, this);


        info = runDataManager.firebaseToRunInfo(getIntent().getIntExtra("index", -1));

        binding = DataBindingUtil.setContentView(this, R.layout.activity_result);
        viewModel = new RunDetailInfoViewModel(info);
        binding.setDetailResultInfo(viewModel);

        mapView = binding.includeDetailRunInfoResult.mapView;
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
}