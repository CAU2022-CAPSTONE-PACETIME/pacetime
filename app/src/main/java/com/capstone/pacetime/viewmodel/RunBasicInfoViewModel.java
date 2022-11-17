package com.capstone.pacetime.viewmodel;

import androidx.databinding.Bindable;
import androidx.databinding.ObservableField;
import androidx.databinding.library.baseAdapters.BR;

import com.capstone.pacetime.RunInfo;

import java.util.Locale;

public class RunBasicInfoViewModel extends RunInfoViewModel {
    private String distanceStr;
    private String paceStr;
    private String runningTimeStr;

    public RunBasicInfoViewModel() {
        this.distanceStr = distanceFormatter(0);
        this.paceStr = paceFormatter(0);
        this.runningTimeStr = runningTimeFormatter(0);
    }

    private String distanceFormatter(float distance){
        int n = (int)distance;
        int d = (int)(distance * 100) % 100;
        return n + "." + d + "km";
    }

    private String paceFormatter(long pace){
        return String.format(Locale.getDefault(), "%02d'%02d\"", pace / 60, pace % 60);
    }

    private String runningTimeFormatter(long time){
        return String.format(Locale.getDefault(), "%02d:%02d", time / 60, time % 60);
    }

    public RunBasicInfoViewModel(RunInfo info){
        distanceStr = distanceFormatter(info.getDistance());
        paceStr = paceFormatter(info.getPace());
        runningTimeStr = runningTimeFormatter(info.getRunningTime());
    }

    @Bindable
    public String getDistanceStr() {
        return distanceStr;
    }

    public void setDistanceStr(float distance) {
        this.distanceStr = distanceFormatter(distance);
        notifyPropertyChanged(BR.distanceStr);
    }
    @Bindable
    public String getPaceStr() {
        return paceStr;
    }

    public void setPaceStr(long pace) {
        this.paceStr = paceFormatter(pace);
        notifyPropertyChanged(BR.paceStr);
    }
    @Bindable
    public String getRunningTimeStr() {
        return runningTimeStr;
    }

    public void setRunningTimeStr(long runningTime) {
        this.runningTimeStr = runningTimeFormatter(runningTime);
        notifyPropertyChanged(BR.runningTimeStr);
    }



}
