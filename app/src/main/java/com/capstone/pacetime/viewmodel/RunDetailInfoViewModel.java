package com.capstone.pacetime.viewmodel;

import androidx.databinding.Bindable;
import androidx.databinding.ObservableField;

public class RunDetailInfoViewModel extends RunBasicInfoViewModel{
    private final ObservableField<String> cadenceStr;
    private final ObservableField<String> stepCountStr;

    public RunDetailInfoViewModel() {
        cadenceStr = new ObservableField<>();
        stepCountStr = new ObservableField<>();
    }

    @Bindable
    public ObservableField<String> getCadenceStr() {
        return cadenceStr;
    }

    public void setCadenceStr(String cadence){
        this.cadenceStr.set(cadence);
    }

    @Bindable
    public ObservableField<String> getStepCountStr() {
        return stepCountStr;
    }

    public void setStepCountStr(String stepCountStr){
        this.stepCountStr.set(stepCountStr);
    }
}
