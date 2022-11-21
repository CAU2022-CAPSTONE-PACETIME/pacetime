package com.capstone.pacetime.command;

import com.capstone.pacetime.RealTimeRunInfo;
import com.capstone.pacetime.viewmodel.RunDetailInfoViewModel;
import com.capstone.pacetime.RunInfo;
import com.capstone.pacetime.RunInfoUpdateFlag;

import java.util.EnumSet;

public class RunDetailInfoUpdateCommand extends RunBasicInfoUpdateCommand{
    @Override
    public void update(RealTimeRunInfo info){
        super.update(info);
        EnumSet<RunInfoUpdateFlag> flag = info.getUpdateFlags();

        if(flag == null){
            return;
        }

        if(flag.contains(RunInfoUpdateFlag.STEP_COUNT)){
            updateStepCount(info.getStepCount().get(info.getStepCount().size()-1).getCount());
        }
        if(flag.contains(RunInfoUpdateFlag.CADENCE)){
            updateCadence(info.getCadence());
        }
    }

    private void updateStepCount(int count){
        ((RunDetailInfoViewModel)viewModel).setStepCountStr(
                String.valueOf(count)
        );
    }
    private void updateCadence(int cadence){
        ((RunDetailInfoViewModel)viewModel).setCadenceStr(
                String.valueOf(cadence)
        );
    }
}
