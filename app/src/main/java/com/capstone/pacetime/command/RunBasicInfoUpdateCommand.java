package com.capstone.pacetime.command;

import com.capstone.pacetime.viewmodel.RunBasicInfoViewModel;
import com.capstone.pacetime.RunInfo;
import com.capstone.pacetime.RunInfoUpdateFlag;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;

public class RunBasicInfoUpdateCommand extends RunInfoUpdateCommand {
    @Override
    public void update(RunInfo info){
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
        ((RunBasicInfoViewModel)viewModel).setDistanceStr(String.valueOf(distance));
    }
    private void updatePace(long pace){
        ((RunBasicInfoViewModel)viewModel).setPaceStr(
                LocalDateTime.ofEpochSecond(
                        pace,
                        0,
                        ZoneOffset.of("Asia/Seoul")
                ).format(
                    DateTimeFormatter.ofPattern("mm'ss\"")
                )
        );
    }
    private void updateRunningTime(long time){
        ((RunBasicInfoViewModel)viewModel).setRunningTimeStr(
                LocalDateTime.ofEpochSecond(
                        time,
                        0,
                        ZoneOffset.of("Asia/Seoul")
                ).format(
                        DateTimeFormatter.ofPattern("mm:ss")
                )
        );
    }
}
