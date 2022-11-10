package com.capstone.pacetime.viewmodel;

import androidx.databinding.Bindable;
import androidx.databinding.ObservableField;
import androidx.databinding.library.baseAdapters.BR;

public class RunDetailInfoViewModel extends RunBasicInfoViewModel{
    private String cadenceStr;
    private String stepCountStr;

    public RunDetailInfoViewModel() {
        super();
        cadenceStr = "0";
        stepCountStr = "0";
    }

    @Bindable
    public String getCadenceStr() {
        return cadenceStr;
    }

    public void setCadenceStr(String cadence){
        this.cadenceStr = cadence;
        notifyPropertyChanged(BR.cadenceStr);

    }

    @Bindable
    public String getStepCountStr() {
        return stepCountStr;
    }

    public void setStepCountStr(String stepCountStr){
        this.stepCountStr = stepCountStr;
        notifyPropertyChanged(BR.stepCountStr);
    }
}
