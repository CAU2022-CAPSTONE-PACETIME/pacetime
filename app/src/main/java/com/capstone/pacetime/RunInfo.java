package com.capstone.pacetime;

import android.location.Location;

import com.capstone.pacetime.command.RunInfoUpdateCommand;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;

public class RunInfo {
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;

    private List<Location> trace;
    private List<Breath> breathItems;

    private float distance;
    private long runningTime;
    private long pace;

    private int cadence;
    private int stepCount;

    private EnumSet<RunInfoUpdateFlag> flag;
    private RunInfoUpdateCommand command;


    public EnumSet<RunInfoUpdateFlag> getUpdateFlags(){
        return flag;
    }

    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(LocalDateTime startDateTime) {
        this.startDateTime = startDateTime;
    }

    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(LocalDateTime endDateTime) {
        this.endDateTime = endDateTime;
    }

    public List<Location> getTrace() {
        return trace;
    }

    public void setTrace(List<Location> trace) {
        this.trace = trace;
    }

    public List<Breath> getBreathItems() {
        return breathItems;
    }

    public void setBreathItems(List<Breath> breathItems) {
        this.breathItems = breathItems;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public long getRunningTime() {
        return runningTime;
    }

    public void setRunningTime(long runningTime) {
        this.runningTime = runningTime;
    }

    public long getPace() {
        return pace;
    }

    public void setPace(long pace) {
        this.pace = pace;
    }

    public int getCadence() {
        return cadence;
    }

    public void setCadence(int cadence) {
        this.cadence = cadence;
    }

    public int getStepCount() {
        return stepCount;
    }

    public void setStepCount(int stepCount) {
        this.stepCount = stepCount;
    }

    private void update(){
        command.update(this);
    }

    public void setCommand(RunInfoUpdateCommand command){
        this.command = command;
    }
}
