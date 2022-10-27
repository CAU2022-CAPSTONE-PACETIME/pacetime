package com.capstone.pacetime;

import androidx.databinding.Bindable;
import androidx.databinding.ObservableField;
import androidx.lifecycle.ViewModel;

public class RunBasicInfoViewModel extends RunInfoViewModel {
    private final ObservableField<String> distanceStr;
    private final ObservableField<String> paceStr;
    private final ObservableField<String> runningTimeStr;

    @Bindable
    public ObservableField<String> getDistanceStr() {
        return distanceStr;
    }

    public void setDistanceStr(String distanceStr) {
        this.distanceStr.set(distanceStr);
    }
    @Bindable
    public ObservableField<String> getPaceStr() {
        return paceStr;
    }

    public void setPaceStr(String paceStr) {
        this.paceStr.set(paceStr);
    }
    @Bindable
    public ObservableField<String> getRunningTimeStr() {
        return runningTimeStr;
    }

    public void setRunningTimeStr(String runningTimeStr) {
        this.runningTimeStr.set(runningTimeStr);
    }


    public RunBasicInfoViewModel() {
        this.distanceStr = new ObservableField<>();
        this.paceStr = new ObservableField<>();
        this.runningTimeStr = new ObservableField<>();

        distanceStr.set("0.0");
        paceStr.set("0\"0'");
        runningTimeStr.set("00:00");
    }
}
