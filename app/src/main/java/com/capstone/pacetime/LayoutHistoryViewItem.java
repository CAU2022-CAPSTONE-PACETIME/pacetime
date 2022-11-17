package com.capstone.pacetime;

import java.time.format.DateTimeFormatter;

public class LayoutHistoryViewItem{
    private String runDateTime;
    private String runStartPlace;
    private String runDistance;
    private String runPace;
    private String runHour;
    private String isBreathUsed;
    private int index;

    public LayoutHistoryViewItem(){
        runDateTime ="";
        runStartPlace = "";
        runDistance = "";
        runPace = "";
        runHour = "";
        isBreathUsed = "";
        index = 0;
    }

    public String getRunDateTime() {
        return runDateTime;
    }

    public String getRunStartPlace() {
        return runStartPlace;
    }

    public String getRunDistance() {
        return runDistance;
    }

    public String getRunPace() {
        return runPace;
    }

    public String getRunHour() {
        return runHour;
    }

    public String getIsBreathUsed() {
        return isBreathUsed;
    }

    public int getIndex() { return index; }

    public void setRunDateTime(String runDateTime) {
        this.runDateTime = runDateTime;
    }

    public void setRunStartPlace(String runStartPlace) {
        this.runStartPlace = runStartPlace;
    }

    public void setRunDistance(String runDistance) {
        this.runDistance = runDistance;
    }

    public void setRunPace(String runPace) {
        this.runPace = runPace;
    }

    public void setRunHour(String runHour) {
        this.runHour = runHour;
    }

    public void setIsBreathUsed(String isBreathUsed) {
        this.isBreathUsed = isBreathUsed;
    }

    public void setIndex(int index) { this.index = index; }

    public void setItem(RunInfo runInfo){
        this.runDateTime = runInfo.getStartDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        this.runStartPlace = runInfo.getTrace().get(0).toString();//임시로 toString
        this.runDistance = Float.toString(runInfo.getDistance());
        this.runPace = Long.toString(runInfo.getPace());
        this.runHour = Long.toString(runInfo.getRunningTime());
        if(runInfo.isBreathUsed()){
            this.isBreathUsed = "O";
        }else{
            this.isBreathUsed = "X";
        }

    }
}
