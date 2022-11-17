package com.capstone.pacetime.command;

import com.capstone.pacetime.RealTimeRunInfo;
import com.capstone.pacetime.RunInfo;
import com.capstone.pacetime.viewmodel.RunInfoViewModel;

public abstract class RunInfoUpdateCommand {
    protected RunInfoViewModel viewModel;
    public abstract void update(RealTimeRunInfo info);
    public void setViewModel(RunInfoViewModel vm){
        this.viewModel = vm;
    }
}
