package com.capstone.pacetime;

import android.util.Log;
import android.widget.EditText;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.databinding.ObservableInt;
import androidx.lifecycle.MutableLiveData;

import java.util.Objects;

public class BreathPattern {
    private static final String TAG = "BreathPattern";
    private final ObservableInt inhale;
    private final ObservableInt exhale;

    public BreathPattern(){
        inhale = new ObservableInt(0);
        exhale = new ObservableInt(0);
    }

    public CharSequence getInhale(){
        return "" + inhale.get();
    }

    public void setInhale(CharSequence val){
        Log.d(TAG, "Set Inhale: " + val);
        if(val.equals("")){
            return;
        }
        inhale.set(Integer.parseInt((String) val));
    }

    public CharSequence getExhale(){
        return "" + exhale.get();
    }

    public void setExhale(CharSequence val){
        Log.d(TAG, "Set Exhale: " + val);
        if(val.equals("")){
            return;
        }
        exhale.set(Integer.parseInt((String) val));
    }


}
