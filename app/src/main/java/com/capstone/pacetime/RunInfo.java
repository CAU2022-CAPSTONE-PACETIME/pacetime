package com.capstone.pacetime;

import android.location.Location;

import com.capstone.pacetime.command.RunInfoUpdateCommand;
import com.capstone.pacetime.data.Breath;
import com.capstone.pacetime.data.Step;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class RunInfo {
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;

    private List<Location> trace;
    private List<Breath> breathItems;
    private List<Step> stepCount;

    private float distance;
    private long runningTime;
    private long pace;

    private int cadence;

    private EnumSet<RunInfoUpdateFlag> flag;
    private RunInfoUpdateCommand command;

    public RunInfo(){
        startDateTime = LocalDateTime.now();
        distance = 0;
        runningTime = 0;
        pace = 0;
        cadence = 0;
        breathItems = new ArrayList<>();
        trace = new ArrayList<>();
        stepCount = new ArrayList<>();
        flag = null;
        command = null;
    }

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
        addFlag(RunInfoUpdateFlag.RUNNING_TIME);
        addFlag(RunInfoUpdateFlag.PACE);
    }

    public List<Location> getTrace() {
        return trace;
    }

    public void setTrace(List<Location> trace) {
        this.trace = trace;
        addFlag(RunInfoUpdateFlag.TRACE);
    }

    public void addTrace(Location pos){
        this.trace.add(pos);
        addFlag(RunInfoUpdateFlag.TRACE);
    }

    public List<Breath> getBreathItems() {
        return breathItems;
    }

    public void setBreathItems(List<Breath> breathItems) {
        this.breathItems = breathItems;
    }

    public void addBreathItem(Breath breath){
        this.breathItems.add(breath);
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
        addFlag(RunInfoUpdateFlag.DISTANCE);
    }

    public long getRunningTime() {
        return runningTime;
    }

    public void setRunningTime(long runningTime) {
        this.runningTime = runningTime;
        addFlag(RunInfoUpdateFlag.RUNNING_TIME);
    }

    public long getPace() {
        return pace;
    }

    public void setPace(long pace) {
        this.pace = pace;
        addFlag(RunInfoUpdateFlag.PACE);
    }

    public int getCadence() {
        return cadence;
    }

    public void setCadence(int cadence) {
        this.cadence = cadence;
        addFlag(RunInfoUpdateFlag.CADENCE);
    }

    public List<Step> getStepCount() {
        return stepCount;
    }

    public void addStepCount(Step stepCount) {
        this.stepCount.add(stepCount);
        addFlag(RunInfoUpdateFlag.STEP_COUNT);
    }

    public void update(){
        command.update(this);
    }

    public void setCommand(RunInfoUpdateCommand command){
        this.command = command;
    }

    private void addFlag(RunInfoUpdateFlag f){
        if(flag == null){
            flag = EnumSet.of(f);
        }
        else{
            if(!flag.contains(f)){
                flag.add(f);
            }
        }
    }
}
