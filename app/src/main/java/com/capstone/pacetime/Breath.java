package com.capstone.pacetime;

public class Breath {
    private final BreathState breathState;
    private final long timestamp;

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
