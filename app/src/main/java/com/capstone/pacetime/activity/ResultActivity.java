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
import com.capstone.pacetime.RunningManager;
import com.capstone.pacetime.data.Breath;
import com.capstone.pacetime.data.BreathStability;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.ButtCap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.android.gms.maps.model.SquareCap;

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
    private boolean isBreathUsed;
    private List<BreathStability> listBreathStability;

    private MapView mapView;
    private GoogleMap mMap;

    RunDataManager runDataManager;

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MapsInitializer.initialize(ResultActivity.this, MapsInitializer.Renderer.LATEST, ResultActivity.this);
        runDataManager = RunDataManager.getInstance();

        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(getApplicationContext());
        }


        info = runDataManager.firebaseToRunInfo(getIntent().getIntExtra("index", -1));

        isBreathUsed = info.getIsBreathUsed();
        listBreathStability = RunningManager.BreathAnalyzer.getStabilityList(info);

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

        mapView = binding.mapViewResult;
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        if(isBreathUsed){
            drawUserBreathTrace(info.getTrace(), info.getBreathItems(), listBreathStability);
        } else {
            drawUserTrace(info.getTrace());
        }
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

        if(ll.isEmpty()){
            return;
        }

        MarkerOptions markerStart = new MarkerOptions();
        LatLng latLngStart = new LatLng(info.getTrace().get(0).getLatitude(), info.getTrace().get(0).getLongitude());
        markerStart.position(latLngStart);
        markerStart.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        mMap.addMarker(markerStart);

        int traceLastIndex = info.getTrace().size() - 1;

        MarkerOptions markerEnd = new MarkerOptions();
        LatLng latLngEnd = new LatLng(info.getTrace().get(traceLastIndex).getLatitude(), info.getTrace().get(traceLastIndex).getLongitude());
        markerEnd.position(latLngEnd);
        mMap.addMarker(markerEnd);

        PolylineOptions polyOptions = new PolylineOptions()
                .clickable(false)
                .addAll(ll);

        mMap.addPolyline(polyOptions);
        mMap.moveCamera((CameraUpdateFactory.newLatLngZoom(ll.get(ll.size()-1), 15)));
    }

    private void drawUserBreathTrace(List<Location> trace, List<Breath> breathList, List<BreathStability> breathStabilityList){
        MarkerOptions markerStart = new MarkerOptions();
        LatLng latLngStart = new LatLng(trace.get(0).getLatitude(), trace.get(0).getLongitude());
        markerStart.position(latLngStart);
        markerStart.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        mMap.addMarker(markerStart);

        int traceLastIndex = trace.size() - 1;

        MarkerOptions markerEnd = new MarkerOptions();
        LatLng latLngEnd = new LatLng(trace.get(traceLastIndex).getLatitude(), trace.get(traceLastIndex).getLongitude());
        markerEnd.position(latLngEnd);
        mMap.addMarker(markerEnd);


        List<LatLng> locList = new ArrayList<>();
        List<Integer> colorList = new ArrayList<>();
        int count = 0;

        for(int i = 0; i < trace.size() - 1; i++){
            for(int j = 0; j < breathStabilityList.size(); j++){
                if(j == breathStabilityList.size() - 1){
//                    if(breathList.get(breathList.size() - 1).getTimestamp() > trace.get(i + 1).getTime()) {
//                        break;
//                    } else if(breathList.get(breathList.size() - 1).getTimestamp() >= trace.get(i).getTime() && breathList.get(breathList.size() - 1).getTimestamp() <= trace.get(i + 1).getTime()){
//                        if(breathList.get(breathList.size() - 1).getTimestamp() - trace.get(i).getTime() <= trace.get(i + 1).getTime() - breathList.get(breathList.size() - 1).getTimestamp()){
//                            locList.add(new LatLng(trace.get(i).getLatitude(), trace.get(i).getLongitude()));
//                            colorList.add(decideColor(breathStabilityList.get(j).getValue()));
//                        } else {
//                            locList.add(new LatLng(trace.get(i + 1).getLatitude(), trace.get(i + 1).getLongitude()));
//                            colorList.add(decideColor(breathStabilityList.get(j).getValue()));
                            locList.add(new LatLng(trace.get(traceLastIndex).getLatitude(), trace.get(traceLastIndex).getLongitude()));
                            colorList.add(decideColor(breathStabilityList.get(j).getValue()));
//                        }
//                    }
                } else {
                    if(breathList.get(10 * j + 100).getTimestamp() > trace.get(i + 1).getTime()) {
                        break;
                    } else if(breathList.get(10 * j + 100).getTimestamp() >= trace.get(i).getTime() && breathList.get(10 * j + 100).getTimestamp() <= trace.get(i + 1).getTime()){
                        if(count == 0){
                            ArrayList<LatLng> ll = new ArrayList<>();
                            if(breathList.get(10 * j + 100).getTimestamp() - trace.get(i).getTime() <= trace.get(i + 1).getTime() - breathList.get(10 * j + 100).getTimestamp()){
                                trace.subList(0, i).forEach((Location location) -> ll.add(new LatLng(location.getLatitude(), location.getLongitude())));
                                locList.add(new LatLng(trace.get(i).getLatitude(), trace.get(i).getLongitude()));
                                colorList.add(decideColor(breathStabilityList.get(j).getValue()));
                            } else {
                                trace.subList(0, i + 1).forEach((Location location) -> ll.add(new LatLng(location.getLatitude(), location.getLongitude())));
                                locList.add(new LatLng(trace.get(i + 1).getLatitude(), trace.get(i + 1).getLongitude()));
                                colorList.add(decideColor(breathStabilityList.get(j).getValue()));
                            }

                            if(ll.isEmpty()){
                                return;
                            }

                            PolylineOptions polyOptions = new PolylineOptions()
                                    .clickable(false)
                                    .addAll(ll);

                            mMap.addPolyline(polyOptions);
                        } else {
                            if(breathList.get(10 * j + 100).getTimestamp() - trace.get(i).getTime() <= trace.get(i + 1).getTime() - breathList.get(10 * j + 100).getTimestamp()){
                                locList.add(new LatLng(trace.get(i).getLatitude(), trace.get(i).getLongitude()));
                                colorList.add(decideColor(breathStabilityList.get(j).getValue()));
                            } else {
                                locList.add(new LatLng(trace.get(i + 1).getLatitude(), trace.get(i + 1).getLongitude()));
                                colorList.add(decideColor(breathStabilityList.get(j).getValue()));
                            }
                        }
                        count++;
                    }
                }
            }
        }

        for(int i = 0; i < locList.size() - 1; i++){
            Log.d("LASTLOC", i + ": " + locList.get(i).latitude + ", " + locList.get(i).longitude);
            colorGradation(locList.get(i), locList.get(i + 1), colorList.get(i), colorList.get(i + 1));
        }

        for(int i = 0; i < colorList.size(); i++){
            Log.d("COLORLISTSIZE", "" + count);
            Log.d("COLORS", ""+Integer.toHexString(colorList.get(i)));
        }

        mMap.moveCamera((CameraUpdateFactory.newLatLngZoom(new LatLng(trace.get(traceLastIndex).getLatitude(), trace.get(traceLastIndex).getLongitude()), 15)));
    }

    private int decideColor(int value){
        if(value == 1){
            return 0xff00ff00; //초록
        } else if(value == 2){
            return 0xff40c000; //연두
        } else if(value == 3){
            return 0xff808000; //노랑
        } else if(value == 4){
            return 0xffc04000; //주황
        } else {
            return 0xffff0000; //빨강
        }
    }

    private void colorGradation(LatLng firstLatLng, LatLng secondLatLng, int firstColor, int secondColor){

        for(int j = 0; j < 5; j++){
            LatLng firstTempLatLng = new LatLng( ((double)(5 - j) / 5) * firstLatLng.latitude + ((double)j / 5) * secondLatLng.latitude,
                    ((double)(5 - j) / 5) * firstLatLng.longitude + ((double)j / 5) * secondLatLng.longitude);
            LatLng secondTempLatLng = new LatLng( ((double)(4 - j) / 5) * firstLatLng.latitude + ((double)(j + 1) / 5) * secondLatLng.latitude,
                    ((double)(4 - j) / 5) * firstLatLng.longitude + ((double)(j + 1) / 5) * secondLatLng.longitude);
            PolylineOptions polylineOptions = new PolylineOptions();
            polylineOptions.color((int) (((double)(4 - j) / 4) * firstColor + ((double)j / 4) * secondColor))
                    .add(firstTempLatLng)
                    .add(secondTempLatLng);
            mMap.addPolyline(polylineOptions);
        }

    }
}