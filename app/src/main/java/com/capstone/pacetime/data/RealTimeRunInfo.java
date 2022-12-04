package com.capstone.pacetime.data;

import android.location.Location;

import com.capstone.pacetime.command.RunInfoUpdateCommand;
import com.capstone.pacetime.data.enums.RunInfoUpdateFlag;

import java.time.OffsetDateTime;
import java.util.EnumSet;
import java.util.List;

public class RealTimeRunInfo extends RunInfo {
    private OffsetDateTime lastDateTime;
    private EnumSet<RunInfoUpdateFlag> flag;
    private RunInfoUpdateCommand command;

    public RealTimeRunInfo(boolean isBreathUsed, int inhale, int exhale) {
        super(isBreathUsed, inhale, exhale);
        lastDateTime = getStartDateTime();
        flag = null;
        command = null;
    }
    public void setStepCount(List<Step> stepCount){
        this.stepCount = stepCount;
        addFlag(RunInfoUpdateFlag.TRACE);
    }
    public void setStartDateTime(OffsetDateTime startDateTime) {
        this.startDateTime = startDateTime;
        this.lastDateTime = this.startDateTime;
    }
    public void updateLastDateTime(){
        this.lastDateTime = endDateTime;
    }
    public EnumSet<RunInfoUpdateFlag> getUpdateFlags(){
        return flag;
    }
    public void setUpdateFlags(EnumSet<RunInfoUpdateFlag> flag){
        this.flag = flag;
    }
    public void setEndDateTime(OffsetDateTime endDateTime) {
        this.endDateTime = endDateTime;
        addFlag(RunInfoUpdateFlag.RUNNING_TIME);
        addFlag(RunInfoUpdateFlag.PACE);
    }
    public void setTrace(List<Location> trace) {
        this.trace = trace;
        addFlag(RunInfoUpdateFlag.TRACE);
    }
    public void addTrace(Location pos){
        this.trace.add(pos);
        addFlag(RunInfoUpdateFlag.TRACE);
    }
    public void setPace(long pace) {
        this.pace = pace;
        addFlag(RunInfoUpdateFlag.PACE);
    }
    public void setDistance(float distance) {
        this.distance = distance;
        addFlag(RunInfoUpdateFlag.DISTANCE);
    }
    public void setRunningTime(long runningTime) {
        this.runningTime = runningTime;
        addFlag(RunInfoUpdateFlag.RUNNING_TIME);
    }
    public void setBreathItems(List<Breath> breathItems) {
        this.breathItems = breathItems;
    }
    public void setCadence(int cadence) {
        this.cadence = cadence;
        addFlag(RunInfoUpdateFlag.CADENCE);
    }
    public void addStepCount(Step stepCount) {
        this.stepCount.add(stepCount);
        addFlag(RunInfoUpdateFlag.STEP_COUNT);
    }
    public void addBreathItem(Breath breath) {
        this.breathItems.add(breath);
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
    public void update(){
        long beforeRT = endDateTime.toEpochSecond() - lastDateTime.toEpochSecond();
        setEndDateTime(OffsetDateTime.now());
        long nowRT = endDateTime.toEpochSecond() - lastDateTime.toEpochSecond();

        setRunningTime(
                getRunningTime() + (nowRT - beforeRT)
        );

        setCadence(calculateCadence());
        setDistance(calculateDistance());
        setPace(calculatePace());

        command.update(this);
    }

    private int lastDistanceIdx = 0;

    private int calculateCadence(){
        if(runningTime == 0){
            return 0;
        }
        return (int) (stepCount.get(stepCount.size()-1).getCount() / runningTime);
    }

    private float calculateDistance(){
        float newDist = 0;
        for(; lastDistanceIdx < trace.size()-1; lastDistanceIdx++){
            newDist += trace.get(lastDistanceIdx).distanceTo(trace.get(lastDistanceIdx+1));
        }

        newDist /= 1000;

        return distance + newDist;
    }

    private long calculatePace(){
        if(distance <= 0.001){
            return 0;
        }
        return (long) (runningTime / distance);
    }

    public void setCommand(RunInfoUpdateCommand command){
        this.command = command;
    }
}
