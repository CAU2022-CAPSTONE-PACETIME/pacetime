package com.capstone.pacetime;

public class LayoutHistoryViewItem{
    private String runTime;
    private String runStartPlace;
    private String runDistance;
    private String runPace;
    private String runHour;
    private String isBreathUsed;

    public LayoutHistoryViewItem(){
        runTime ="";
        runStartPlace = "";
        runDistance = "";
        runPace = "";
        runHour = "";
        isBreathUsed = "";
    }

    public String getRunTime() {
        return runTime;
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

    public void setRunTime(String runTime) {
        this.runTime = runTime;
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
}
