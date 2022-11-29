package com.capstone.pacetime.data;

import android.location.Location;
import android.util.Log;

import com.capstone.pacetime.command.RunInfoUpdateCommand;
import com.capstone.pacetime.data.Breath;
import com.capstone.pacetime.data.Step;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.TemporalField;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class RunInfo {
    private static final String TAG = "RUNINFO";
    protected OffsetDateTime startDateTime;
    protected OffsetDateTime endDateTime;
    protected List<Location> trace;
    protected List<Breath> breathItems;
    protected List<Step> stepCount;
    protected float distance;
    protected long runningTime;
    protected long pace;
    protected int cadence;
    protected boolean isBreathUsed;

    public RunInfo(){
        startDateTime = OffsetDateTime.now();
        endDateTime = OffsetDateTime.now();
        distance = 0;
        runningTime = 0;
        pace = 0;
        cadence = 0;
        breathItems = new ArrayList<>();
        trace = new ArrayList<>();
        stepCount = new ArrayList<>();
        isBreathUsed = true;
    }
    public RunInfo(boolean isBreathUsed){
        this();
        this.isBreathUsed = isBreathUsed;
    }
    public RunInfo(
            OffsetDateTime startDateTime,
            OffsetDateTime endDateTime,
            List<Location> trace,
            List<Breath> breathItems,
            List<Step> stepCount,
            float distance,
            long runningTime,
            long pace,
            int cadence,
            boolean isBreathUsed
    ){
        this();
        this.startDateTime   = startDateTime;
        this.endDateTime     = endDateTime ;
        this.distance        = distance    ;
        this.runningTime     = runningTime ;
        this.pace            = pace        ;
        this.cadence         = cadence     ;
        this.breathItems     = breathItems ;
        this.trace           = trace       ;
        this.stepCount       = stepCount   ;
        this.isBreathUsed    = isBreathUsed;
    }

    public boolean getIsBreathUsed(){
        return isBreathUsed;
    }
    public OffsetDateTime getStartDateTime() {
        return startDateTime;
    }
    public OffsetDateTime getEndDateTime() {
        return endDateTime;
    }
    public List<Location> getTrace() {
        return trace;
    }
    public List<Breath> getBreathItems() {
        return breathItems;
    }
    public float getDistance() {
        return distance;
    }
    public long getRunningTime() {
        return runningTime;
    }
    public long getPace() {
        return pace;
    }
    public int getCadence() {
        return cadence;
    }
    public List<Step> getStepCount() {
        return stepCount;
    }
}
