package com.capstone.pacetime.data;

import android.util.Log;
import androidx.databinding.ObservableInt;

public class BreathPattern {
    private static final String TAG = "BreathPattern";
    private final ObservableInt inhale;
    private final ObservableInt exhale;

    public BreathPattern(){
        inhale = new ObservableInt(1);
        exhale = new ObservableInt(1);
    }

    public BreathPattern(int inhaleNum, int exhaleNum){
        inhale = new ObservableInt(inhaleNum);
        exhale = new ObservableInt(exhaleNum);
    }

    public int getInhale(){
        return inhale.get();
    }

    public void setInhale(int val){
        Log.d(TAG, "Set Inhale: " + val);
        inhale.set(val);
    }

    public int getExhale(){
        return exhale.get();
    }

    public void setExhale(int val){
        Log.d(TAG, "Set Exhale: " + val);
        exhale.set(val);
    }
}
