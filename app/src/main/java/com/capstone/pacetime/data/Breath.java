package com.capstone.pacetime.data;

import com.capstone.pacetime.data.enums.BreathState;

public class Breath {
    private final BreathState breathState;
    private final long timestamp;

    private float value;
    public float getValue(){
        return value;
    }
    public void setValue(float value){
        this.value = value;
    }
    public Breath(){
        breathState = BreathState.INHALE;
        timestamp = 0;
        value = 0.0f;
    }

    public Breath(BreathState state, long timestamp){
        this.breathState = state;
        this.timestamp = timestamp;
    }

    public BreathState getBreathState() {
        return breathState;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
