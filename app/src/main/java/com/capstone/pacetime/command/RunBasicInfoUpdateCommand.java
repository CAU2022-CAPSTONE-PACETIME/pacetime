package com.capstone.pacetime.command;

import com.capstone.pacetime.data.RealTimeRunInfo;
import com.capstone.pacetime.viewmodel.RunBasicInfoViewModel;
import com.capstone.pacetime.data.enums.RunInfoUpdateFlag;

import java.util.EnumSet;

public class RunBasicInfoUpdateCommand extends RunInfoUpdateCommand {
    @Override
    public void update(RealTimeRunInfo info){
        EnumSet<RunInfoUpdateFlag> flag = info.getUpdateFlags();

        if(flag == null){
            return;
        }

        if(flag.contains(RunInfoUpdateFlag.DISTANCE)){
            updateDistance(info.getDistance());
        }
        if(flag.contains(RunInfoUpdateFlag.PACE)){
            updatePace(info.getPace());
        }
        if(flag.contains(RunInfoUpdateFlag.RUNNING_TIME)){
            updateRunningTime(info.getRunningTime());
        }
    }

    private void updateDistance(float distance){
        ((RunBasicInfoViewModel)viewModel).setDistanceStr(distance);
    }
    private void updatePace(long pace){
        ((RunBasicInfoViewModel)viewModel).setPaceStr(pace);
    }
    private void updateRunningTime(long time){
        ((RunBasicInfoViewModel)viewModel).setRunningTimeStr(time);
    }
}
