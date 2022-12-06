package com.capstone.pacetime.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;

//import com.capstone.pacetime.command.RunDetailInfoUpdateCommand;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.capstone.pacetime.BuildConfig;
import com.capstone.pacetime.R;
import com.capstone.pacetime.util.RunDataManager;
import com.capstone.pacetime.data.RunInfo;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.StringReader;

public class ResultActivity extends AppCompatActivity implements OnMapReadyCallback, OnMapsSdkInitializedCallback {

    private ActivityResultBinding binding;
    private RunDetailInfoViewModel viewModel;
    private RunInfo info;
    private static RequestQueue requestQueue;

    private MapView mapView;
    private GoogleMap mMap;

    RunDataManager runDataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        runDataManager = RunDataManager.getInstance();

        if(requestQueue == null){
            requestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        MapsInitializer.initialize(this, MapsInitializer.Renderer.LATEST, this);


        info = runDataManager.firebaseToRunInfo(getIntent().getIntExtra("index", -1));

        Location loc = info.getTrace().get(0);

        String url = "https://maps.googleapis.com/maps/api/geocode/json?latlng=" +
                loc.getLatitude() + "," +
                loc.getLongitude() +
                "&key=" + BuildConfig.GEOCODE_API_KEY;
        StringRequest req = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    Log.d("LOCATIONSTRING", response);
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray resultArray = jsonObject.getJSONArray("results");
                    JSONObject addrObj = resultArray.getJSONObject(0);
                    String cityAddr = addrObj.getString("formatted_address");

                    Log.d("StartLocation: ", cityAddr);
                    viewModel.setStartLocationStr(cityAddr);
                } catch (JSONException e) {
                    Log.d("LOCATIONFAIL1", "location fail 1");
                    Log.d("LOCATIONFAIL1", e.toString());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.v("LOCATIONFAIL2", "location call failed!");
                Log.v("LOCATIONFAIL", error.toString());
            }
        });
        req.setShouldCache(false);
        requestQueue.add(req);

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