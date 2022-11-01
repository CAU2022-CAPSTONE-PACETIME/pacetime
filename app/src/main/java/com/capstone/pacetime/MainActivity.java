package com.capstone.pacetime;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.NumberPicker;
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

        breathPattern = new BreathPattern(1, 1);
        binding.setPattern(breathPattern);

        binding.pickerInhale.setMinValue(1);
        binding.pickerInhale.setMaxValue(9);

        binding.pickerExhale.setMinValue(1);
        binding.pickerExhale.setMaxValue(9);

        binding.pickerInhale.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                // Save the value in the number picker
                binding.getPattern().setInhale(newVal);
            }
        });

        binding.pickerExhale.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                // Save the value in the number picker
                binding.getPattern().setExhale(newVal);
            }
        });


        binding.switchBreath.setTextOff("Breath Off");
        binding.switchBreath.setTextOn("Breath On");
        binding.switchBreath.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    binding.pickerInhale.setVisibility(View.VISIBLE);
                    binding.pickerExhale.setVisibility(View.VISIBLE);
                    binding.textInhale.setVisibility(View.VISIBLE);
                    binding.textExhale.setVisibility(View.VISIBLE);
                }
                else{
                    binding.pickerInhale.setVisibility(View.INVISIBLE);
                    binding.pickerExhale.setVisibility(View.INVISIBLE);
                    binding.textInhale.setVisibility(View.INVISIBLE);
                    binding.textExhale.setVisibility(View.INVISIBLE);
                }
            }
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
}