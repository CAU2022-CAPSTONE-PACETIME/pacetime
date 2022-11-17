package com.capstone.pacetime.data;

public class Step {
    private final int count;
    private final long timestamp;

    public Step(){
        count = 0;
        timestamp = 0;
    }

    public Step(int count, long timestamp){
        this.count = count;
        this.timestamp = timestamp;
    }

    public int getCount(){
        return count;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
