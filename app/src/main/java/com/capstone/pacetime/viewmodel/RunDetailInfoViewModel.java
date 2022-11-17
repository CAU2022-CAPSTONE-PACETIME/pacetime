package com.capstone.pacetime.viewmodel;

import androidx.databinding.Bindable;
import androidx.databinding.library.baseAdapters.BR;

import com.capstone.pacetime.RunInfo;
import com.capstone.pacetime.data.Step;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Optional;

public class RunDetailInfoViewModel extends RunBasicInfoViewModel{
    private String cadenceStr;
    private String stepCountStr;
    private String startDateStr;
    private String isBreathUsedStr;
    private String startLocationStr;

    public RunDetailInfoViewModel() {
        super();
        cadenceStr = "0";
        stepCountStr = "0";
        startLocationStr = "Dokdo";
        // 시작 날짜 YYYY-MM-DD
        startDateStr = OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        // isBreathUsed
        isBreathUsedStr = "O";
    }

    public RunDetailInfoViewModel(RunInfo info){
        super(info);
        cadenceStr = String.valueOf(info.getCadence());

        stepCountStr = "-1";
        info.getStepCount().stream().max(new Comparator<Step>() {
            @Override
            public int compare(Step o1, Step o2) {
                if(o1.getCount() < o2.getCount()){
                    return 1;
                }else if(o1.getCount() > o2.getCount()){
                    return -1;
                }
                return 0;
            }
        }).ifPresent(step -> stepCountStr = "" + step.getCount());

        // 시작 장소
        startLocationStr = "Dokdo";
        // 시작 날짜 YYYY-MM-DD
        startDateStr = info.getStartDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        // isBreathUsed
        isBreathUsedStr = info.getIsBreathUsed() ? "O" : "X";
        //
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

    @Bindable
    public String getStartDateStr() {
        return startDateStr;
    }

    @Bindable
    public String getIsBreathUsedStr() {
        return isBreathUsedStr;
    }

    @Bindable
    public String getStartLocationStr() {
        return startLocationStr;
    }
}
