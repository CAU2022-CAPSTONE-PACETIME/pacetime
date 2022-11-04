package com.capstone.pacetime;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.capstone.pacetime.databinding.ActivityMainBinding;
import com.capstone.pacetime.receiver.GPSReceiver;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    BreathPattern breathPattern;
    static RequestQueue requestQueue;

    private GPSReceiver gps;
    private Handler handler;
    private HandlerThread thread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setActivity(this);

        if(requestQueue == null){
            requestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        breathPattern = new BreathPattern(1, 1);
        binding.setPattern(breathPattern);

        binding.pickerInhale.setMinValue(1);
        binding.pickerInhale.setMaxValue(9);

        binding.pickerExhale.setMinValue(1);
        binding.pickerExhale.setMaxValue(9);

        binding.pickerInhale.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                // Save the value in the number picker
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        binding.getPattern().setInhale(newVal);
                    }
                });
            }
        });

        binding.pickerExhale.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Save the value in the number picker
                        binding.getPattern().setExhale(newVal);
                    }
                });
            }
        });


//        binding.switchBreath.setTextOff("Breath Off");
//        binding.switchBreath.setTextOn("Breath On");
        binding.switchBreath.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            binding.pickerInhale.setVisibility(View.VISIBLE);
                            binding.pickerExhale.setVisibility(View.VISIBLE);
                            binding.textInhale.setVisibility(View.VISIBLE);
                            binding.textExhale.setVisibility(View.VISIBLE);
                            binding.imageBreath.setVisibility(View.VISIBLE);
                            binding.imageNoBreath.setVisibility(View.INVISIBLE);
                        }
                    });
                }
                else{
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            binding.pickerInhale.setVisibility(View.INVISIBLE);
                            binding.pickerExhale.setVisibility(View.INVISIBLE);
                            binding.textInhale.setVisibility(View.INVISIBLE);
                            binding.textExhale.setVisibility(View.INVISIBLE);
                            binding.imageBreath.setVisibility(View.INVISIBLE);
                            binding.imageNoBreath.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }
        });

        thread = new HandlerThread("UpdateLocationThread");
        thread.start();
        handler = new Handler(thread.getLooper()){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                Bundle data = msg.getData();
                if(data == null){
                    Log.v("ISSUCCESS","no");
                    return;
                }
                if(data.keySet().contains("location")){
                    Location loc = data.getParcelable("location");

                    currentCityCall(loc.getLatitude(), loc.getLongitude());
                    Log.i("MainActivity", "GPS: " + loc.toString());

                }
                if(data.keySet().contains("city")){
                    String city = data.getString("city");
                    currentWeatherCall(city);
                }

            }
        };

        gps = new GPSReceiver(LocationServices.getFusedLocationProviderClient(this), handler);
        binding.buttonRefresh.setOnClickListener((view)->{
            gps.getLocation();
        });
    }

    public void onStartClick(View v){
        Log.d("MainActivity", "" + v.getId());
        runOnUiThread(()->{
            Toast.makeText(this, "Start", Toast.LENGTH_SHORT).show();
        });
    }

    public void onHistoryClick(View v){
        Log.d("MainActivity", "" + v.getId());
        runOnUiThread(()->{
            Toast.makeText(this, "History", Toast.LENGTH_SHORT).show();
        });
    }

    private String currentCityCall(double lat, double lon) {
        StringBuilder cityBuilder = new StringBuilder();
        String url = "http://api.openweathermap.org/geo/1.0/reverse?"
                +"lat=" + lat
                +"&lon="+ lon
                +"&limit=1&appid=00969f33984829a2faf341274fe44028";

//        Log.v("WETHERCALL","CityAddress: " + cityAddress);
        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    JSONArray jsonArray = new JSONArray(response);
                    JSONObject cityObj = jsonArray.getJSONObject(0);

                    String city = cityObj.getString("name");
//                    cityBuilder.append(city);

                    Bundle data = new Bundle();
                    data.putString("city", city);

                    Message msg = new Message();

                    msg.setData(data);

                    handler.sendMessage(msg);
                    Log.d("CITYWHERE", "city: "+city);

                }catch (JSONException e){
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.v("WEATHERCALLFAIL2", "weather call failed!");
            }
        }) {
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();

                return params;
            }
        };

        request.setShouldCache(false);
        requestQueue.add(request);

        Log.d("CITYBUILDERWHERE", "city: "+cityBuilder.toString());
        return cityBuilder.toString();
    }

    private void currentWeatherCall(String city){
        String urlCity = "http://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=00969f33984829a2faf341274fe44028";

        Log.d("CITYWHERE", "city1: " + city);

        StringRequest requestCity = new StringRequest(Request.Method.GET, urlCity, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    JSONObject jsonObject = new JSONObject(response);
                    String city = jsonObject.getString("name");

                    JSONArray weatherJson = jsonObject.getJSONArray("weather");
                    JSONObject weatherObj = weatherJson.getJSONObject(0);

                    String weather = weatherObj.getString("description");

                    JSONObject tempK = new JSONObject(jsonObject.getString("main"));

                    double tempDo = (Math.round((tempK.getDouble("temp")-273.15)*100)/100.0);
                    String tempInC = tempDo +  "°C";

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            binding.textPlace.setText(city);
                            binding.textWeather.setText(weather);
                            binding.textTemperature.setText(tempInC);
                        }
                    });

                }catch (JSONException e){
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.v("WEATHERCALLFAIL2", "weather call failed!");
            }
        }) {
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();

                return params;
            }
        };

        requestCity.setShouldCache(false);
        requestQueue.add(requestCity);
    }

}