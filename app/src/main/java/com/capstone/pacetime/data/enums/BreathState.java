package com.capstone.pacetime.data.enums;

public enum BreathState {
    INHALE,
    NONE,
    EXHALE;

    BreathState(){ value = 0; }
    private float value;
    public float getValue(){
        return value;
    }
    public BreathState setValue(float value){
        this.value = value;
        return this;
    }
}
