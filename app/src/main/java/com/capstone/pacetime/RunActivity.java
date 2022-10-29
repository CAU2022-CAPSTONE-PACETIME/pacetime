package com.capstone.pacetime;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;

import com.capstone.pacetime.command.RunDetailInfoUpdateCommand;
import com.capstone.pacetime.command.RunInfoUpdateCommand;
import com.capstone.pacetime.databinding.ActivityRunBinding;
import com.capstone.pacetime.viewmodel.RunDetailInfoViewModel;

public class RunActivity extends AppCompatActivity {
    private ActivityRunBinding binding;
    private RunDetailInfoViewModel viewModel;
    private RunInfoUpdateCommand command;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_run);
        viewModel = new RunDetailInfoViewModel();
        command = new RunDetailInfoUpdateCommand();

        command.setViewModel(viewModel);
        binding.setDetailRunInfo(viewModel);

    }
}