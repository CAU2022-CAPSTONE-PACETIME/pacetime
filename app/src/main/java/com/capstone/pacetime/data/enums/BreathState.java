package com.capstone.pacetime.data.enums;

public enum BreathState {
    INHALE,
    NONE,
    EXHALE;

    BreathState(){ value = 0; }
    float value;
    public float getValue(){
        return value;
    }
    public void setValue(float value){
        this.value = value;
    }
}
