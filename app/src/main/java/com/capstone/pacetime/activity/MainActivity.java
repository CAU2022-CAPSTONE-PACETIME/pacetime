package com.capstone.pacetime.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.NumberPicker;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.capstone.pacetime.BuildConfig;
import com.capstone.pacetime.util.BluetoothHelper;
import com.capstone.pacetime.data.BreathPattern;
import com.capstone.pacetime.R;
import com.capstone.pacetime.util.PermissionChecker;
import com.capstone.pacetime.util.RunDataManager;
import com.capstone.pacetime.databinding.ActivityMainBinding;
import com.capstone.pacetime.receiver.GPSReceiver;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    BreathPattern breathPattern;
    static RequestQueue requestQueue;

    private int checkLoadingCount = 0;
    private boolean isStartClicked = false;
    private Intent startIntent;

    private GPSReceiver gps;
    private Handler handler;
    private HandlerThread thread;
    private final Object lock = new Object();
    private Handler loadHandler;
    private HandlerThread loadHandlerThread;

    RunDataManager runDataManager;
    BluetoothHelper bluetoothHelper;
    MutableLiveData<String> whichDevice = new MutableLiveData<>(null);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);

        PermissionChecker.checkPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION, "위치");

        startIntent = new Intent(this, RunActivity.class);

        runDataManager = RunDataManager.getInstance();

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setActivity(this);

        loadHandlerThread = new HandlerThread("DO loading task");
        loadHandlerThread.start();
        loadHandler = new Handler(loadHandlerThread.getLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
            }
        };


        thread = new HandlerThread("UpdateLocationThread");
        thread.start();
        handler = new Handler(thread.getLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                Bundle data = msg.getData();
                if (data == null) {
                    Log.v("ISSUCCESS", "no");
                    return;
                }
                if (data.keySet().contains("location")) {
                    Location loc = data.getParcelable("location");

                    currentCityCall(loc.getLatitude(), loc.getLongitude());
                    Log.i("Main_Activity", "GPS: " + loc);


//                        if (binding.switchBreath.isChecked()) {
//                            startIntent.putExtra("Inhale", breathPattern.getInhale());
//                            startIntent.putExtra("Exhale", breathPattern.getExhale());
//                        } else {
//                            startIntent.putExtra("Inhale", 0);
//                            startIntent.putExtra("Exhale", 0);
//                        }


                }
                if (data.keySet().contains("city")) {
                    String city = data.getString("city");
                    StringTokenizer st1 = new StringTokenizer(city, ", ");
                    ArrayList<String> pstr = new ArrayList<String>();
                    while(st1.hasMoreTokens()){
                        pstr.add(st1.nextToken());
                    }
                    currentWeatherCall(pstr.get(2));
                    StringBuilder cityBuilder = new StringBuilder();
                    if(!isStartClicked){
                        cityBuilder.append(pstr.get(2)).append(", ").append(pstr.get(3));
                        binding.textPlace.setText(cityBuilder.toString());
                    } else {
                        isStartClicked = false;
                        cityBuilder.append(pstr.get(2)).append("\n").append(pstr.get(3));
                        startIntent.putExtra("cityAddr", cityBuilder.toString());

                        if (binding.switchToggleBreath.isChecked()) {
                            startIntent.putExtra("Inhale", breathPattern.getInhale());
                            startIntent.putExtra("Exhale", breathPattern.getExhale());
                        } else {
                            startIntent.putExtra("Inhale", 0);
                            startIntent.putExtra("Exhale", 0);
                        }

                        startActivity(startIntent);
                    }
                }

            }
        };


        gps = new GPSReceiver(LocationServices.getFusedLocationProviderClient(this),
                (LocationManager) getSystemService(Context.LOCATION_SERVICE));
        gps.setDataHandler(handler);
        gps.getLocation();
        binding.viewWeatherFrame.setOnClickListener((view) -> {
            gps.getLocation();
        });
        binding.buttonStartRunning.setOnClickListener((view) -> {
            isStartClicked = true;
            gps.getLocation();
        });

        Runnable loadRunnable = new Runnable() {
            @Override
            public void run() {
                runDataManager.allFirebaseToRunInfos();
                Log.d("MAIN_ACTIVITY", "runInfos size first: " + runDataManager.getRunInfos().size());
                while (runDataManager.getIsLoading()) {
                    synchronized (lock) {
                        try {
                            checkLoadingCount++;
                            Log.d("MAIN_ACTIVITY", "loading..." + checkLoadingCount);
                            lock.wait(50);
                            Log.d("MAIN_ACTIVITY", "runInfos size: " + runDataManager.getRunInfos().size());
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };
        loadHandler.post(loadRunnable);

        bluetoothHelper = new BluetoothHelper(this);

        if (requestQueue == null) {
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


//        binding.switchBreath.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                isBreathSwitchOn(isChecked);
//            }
//        });

        binding.switchToggleBreath.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isBreathSwitchOn(isChecked);
            }
        });

        whichDevice.observe(this, new Observer<String>() {
            @Override
            public void onChanged(String nameDevice) {
                if (nameDevice != null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
//                            binding.textBluetooth.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#000080")));
                            binding.textBluetooth.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#8FB339")));
                            binding.textBluetooth.setText(nameDevice);
                            binding.getPattern().setInhale(1);
                            binding.getPattern().setExhale(1);
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
//                            binding.textBluetooth.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F00000")));
                            binding.textBluetooth.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#999999")));
                            binding.textBluetooth.setText("Device\nUnconnected");
                            binding.textInhale.setTextColor(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
                            binding.textExhale.setTextColor(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
                            binding.pickerInhale.setValue(1);
                            binding.pickerExhale.setValue(1);
                        }
                    });
                }
            }
        });
    }

    public void onHistoryClick(View v){
        Intent intent = new Intent(this, HistoryActivity.class);
        startActivity(intent);
    }

    private void currentCityCall(double lat, double lon) {
        String url = "https://maps.googleapis.com/maps/api/geocode/json?latlng=" +
                lat + "," +
                lon +
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

                    Bundle data = new Bundle();
                    data.putString("city", cityAddr);
                    Message msg = new Message();

                    msg.setData(data);

                    handler.sendMessage(msg);
                    Log.d("CITYWHERE", "city: "+ cityAddr);

                    Log.d("StartLocation: ", cityAddr);
//                    viewModel.setStartLocationStr(cityAddr);
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

//
//        String url = "http://api.openweathermap.org/geo/1.0/reverse?"
//                +"lat=" + lat
//                +"&lon="+ lon
//                +"&limit=1&appid=00969f33984829a2faf341274fe44028";

//        Log.v("WEATHERCALL","CityAddress: " + cityAddress);
//        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
//            @Override
//            public void onResponse(String response) {
//                try{
//                    JSONArray jsonArray = new JSONArray(response);
//                    JSONObject cityObj = jsonArray.getJSONObject(0);
//
//                    String city = cityObj.getString("name");
////                    cityBuilder.append(city);
//
//                    Bundle data = new Bundle();
//                    data.putString("city", city);
//                    Message msg = new Message();
//
//                    msg.setData(data);
//
//                    handler.sendMessage(msg);
//                    Log.d("CITYWHERE", "city: "+city);
//
//                }catch (JSONException e){
//                    e.printStackTrace();
//                }
//            }
//        }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                Log.v("WEATHERCALLFAIL2", "weather call failed!");
//            }
//        });
//
//        request.setShouldCache(false);
//        requestQueue.add(request);
    }

    private void currentWeatherCall(String city){
        String urlCity = "http://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=" + BuildConfig.WEATHER_API_KEY;

        Log.d("CITYWHERE", "city1: " + city);

        StringRequest requestCity = new StringRequest(Request.Method.GET, urlCity, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    JSONObject jsonObject = new JSONObject(response);
                    String city = jsonObject.getString("name");

                    JSONArray weatherJson = jsonObject.getJSONArray("weather");
                    JSONObject weatherObj = weatherJson.getJSONObject(0);

//                    String weather = weatherObj.getString("description");
                    String weatherIconSource = "i" + weatherObj.getString("icon");
                    Log.d("MAIN_ACTIVITY", "weather resource = " + weatherIconSource);
                    int weatherId = getResources().getIdentifier("com.capstone.pacetime:drawable/" + weatherIconSource, null, null);
                    Log.d("MAIN_ACTIVITY", "weather id = " + weatherId);

                    JSONObject tempK = new JSONObject(jsonObject.getString("main"));

                    double tempDo = (Math.round((tempK.getDouble("temp")-273.15)*10)/10.0);
                    String tempInC = tempDo +  "°C";

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            binding.textPlaceHolder.setVisibility(View.INVISIBLE);
                            binding.textPlaceHolder.setText("");
//                            binding.textPlace.setText(city);
//                            binding.textWeather.setText(weather);
                            binding.textWeatherHolder.setText("");
                            binding.imageWeather.setImageResource(weatherId);
//                            binding.textTemperatureHolder.setVisibility(View.INVISIBLE);
                            binding.textTemperatureHolder.setText("");
//                            binding.imageWeather.setImageResource(R.drawable.i01d);
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
        });

        requestCity.setShouldCache(false);
        requestQueue.add(requestCity);
    }

    public void onDeviceChanged(boolean isHeadset){
        if(isHeadset){
//            ConstraintLayout.LayoutParams constraintLayoutParams = (ConstraintLayout.LayoutParams) binding.imageNoBreath.getLayoutParams();
//            constraintLayoutParams.rightMargin = constraintLayoutParams.getMarginEnd();
//            binding.imageNoBreath.setLayoutParams(constraintLayoutParams);
            whichDevice.setValue(bluetoothHelper.getMyDeviceName());
//            binding.switchBreath.setVisibility(View.VISIBLE);
            binding.switchToggleBreath.setEnabled(true);
        }
        else{
//            binding.switchBreath.setVisibility(View.GONE);
//            ConstraintLayout.LayoutParams constraintLayoutParams = (ConstraintLayout.LayoutParams) binding.imageNoBreath.getLayoutParams();
//            constraintLayoutParams.rightMargin = constraintLayoutParams.getMarginEnd() / (-2);
//            binding.imageNoBreath.setLayoutParams(constraintLayoutParams);
//            binding.switchBreath.setChecked(false);
            binding.switchToggleBreath.setEnabled(false);
            binding.switchToggleBreath.setChecked(false);
            isBreathSwitchOn(false);
            whichDevice.setValue(null);
        }
    }

    public void isBreathSwitchOn(boolean isOn){
        if(isOn){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                    binding.pickerInhale.setVisibility(View.VISIBLE);
//                    binding.pickerExhale.setVisibility(View.VISIBLE);
                    binding.pickerInhale.setEnabled(true);
                    binding.pickerExhale.setEnabled(true);
                    binding.textInhale.setTextColor(ColorStateList.valueOf(Color.parseColor("#53B036")));
                    binding.textExhale.setTextColor(ColorStateList.valueOf(Color.parseColor("#53B036")));
//                    binding.textInhale.setVisibility(View.VISIBLE);
//                    binding.textExhale.setVisibility(View.VISIBLE);
//                    binding.imageBreath.setVisibility(View.VISIBLE);
//                    binding.imageNoBreath.setVisibility(View.INVISIBLE);
                }
            });
        }
        else{
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                    binding.pickerInhale.setVisibility(View.INVISIBLE);
//                    binding.pickerExhale.setVisibility(View.INVISIBLE);
                    binding.pickerInhale.setEnabled(false);
                    binding.pickerExhale.setEnabled(false);
                    binding.textInhale.setTextColor(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
                    binding.textExhale.setTextColor(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
//                    binding.textInhale.setVisibility(View.INVISIBLE);
//                    binding.textExhale.setVisibility(View.INVISIBLE);
//                    binding.imageBreath.setVisibility(View.INVISIBLE);
//                    binding.imageNoBreath.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        runDataManager.destory();
    }
}