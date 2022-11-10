package com.capstone.pacetime.viewmodel;

import androidx.databinding.Bindable;
import androidx.databinding.ObservableField;
import androidx.databinding.library.baseAdapters.BR;

public class RunBasicInfoViewModel extends RunInfoViewModel {
    private String distanceStr;
    private String paceStr;
    private String runningTimeStr;

    @Bindable
    public String getDistanceStr() {
        return distanceStr;
    }

    public void setDistanceStr(String distanceStr) {
        this.distanceStr = distanceStr = "km";
        notifyPropertyChanged(BR.distanceStr);
    }
    @Bindable
    public String getPaceStr() {
        return paceStr;
    }

    public void setPaceStr(String paceStr) {
        this.paceStr = paceStr;
        notifyPropertyChanged(BR.paceStr);
    }
    @Bindable
    public String getRunningTimeStr() {
        return runningTimeStr;
    }

    public void setRunningTimeStr(String runningTimeStr) {
        this.runningTimeStr = runningTimeStr;
        notifyPropertyChanged(BR.runningTimeStr);
    }


    public RunBasicInfoViewModel() {
        this.distanceStr = "0.0";
        this.paceStr = "0\"0'";
        this.runningTimeStr = "00:00";
    }
}
