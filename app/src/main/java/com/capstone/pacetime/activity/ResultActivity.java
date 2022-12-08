package com.capstone.pacetime.activity;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;

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
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ResultActivity extends AppCompatActivity implements OnMapReadyCallback, OnMapsSdkInitializedCallback {

    private ActivityResultBinding binding;
    private RunDetailInfoViewModel viewModel;
    private RunInfo info;
    private static RequestQueue requestQueue;
    private Handler uiHandler;
    private Object lock;

    private MapView mapView;
    private GoogleMap mMap;

    RunDataManager runDataManager;

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MapsInitializer.initialize(ResultActivity.this, MapsInitializer.Renderer.LATEST, ResultActivity.this);
//        MapsInitializer.initialize(this, MapsInitializer.Renderer.LATEST, this);
        runDataManager = RunDataManager.getInstance();

        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(getApplicationContext());
        }


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

//        uiHandler = new Handler(getMainLooper()) {
//            @Override
//            public void handleMessage(@NonNull Message msg) {git
//                super.handleMessage(msg);
//                if (msg.what == 1) {
//                }
//            }
//        };


        mapView = binding.mapViewResult;
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);



//        int i = 1;
//        lock = new Object();
//        synchronized (lock) {
//            while (mMap != null) {
//                try {
//                    lock.wait(50);
//                    Log.d("TTTTT", "" + i);
//                    i++;
//                } catch (InterruptedException e) {
//                    Log.d("LOCKERROR", e.toString());
//                }
//                if (mMap == null) {
//                    Log.d("TTTTT", "" + i);
//                    drawUserTrace(info.getTrace());
//                }
//            }
//        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        drawUserTrace(info.getTrace());

//        LatLng seoul = new LatLng(37.56, 126.97);
//
//        MarkerOptions markerOptions = new MarkerOptions();
//        markerOptions.position(seoul);
//        markerOptions.title("서울");
//        mMap.addMarker(markerOptions);
//
//        mMap.moveCamera((CameraUpdateFactory.newLatLngZoom(seoul, 10)));
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
}