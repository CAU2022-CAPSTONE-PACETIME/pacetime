package com.capstone.pacetime;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import com.capstone.pacetime.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    BreathPattern breathPattern;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setActivity(this);

        breathPattern = new BreathPattern();
        binding.setPattern(breathPattern);
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
}