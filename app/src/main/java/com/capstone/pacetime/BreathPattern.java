package com.capstone.pacetime;

import androidx.databinding.Bindable;
import androidx.lifecycle.MutableLiveData;

import java.util.Objects;

public class BreathPattern {
    private MutableLiveData<Integer> inhale;

    private MutableLiveData<Integer> exhale;

    public BreathPattern(){
        inhale = new MutableLiveData<>(0);
        exhale = new MutableLiveData<>(0);
    }

    @Bindable
    public CharSequence getInhale(){
        return Objects.requireNonNull(inhale.getValue()).toString();
    }

    public void setInhale(CharSequence val){
        inhale.postValue(Integer.valueOf((String) val));
    }

    @Bindable
    public CharSequence getExhale(){
        return Objects.requireNonNull(exhale.getValue()).toString();
    }

    public void setExhale(CharSequence val){
        exhale.postValue(Integer.valueOf((String) val));
    }

}
