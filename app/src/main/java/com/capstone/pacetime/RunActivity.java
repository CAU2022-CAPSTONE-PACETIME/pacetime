package com.capstone.pacetime;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Parcelable;
import android.util.Log;

import com.capstone.pacetime.command.RunDetailInfoUpdateCommand;
import com.capstone.pacetime.command.RunInfoUpdateCommand;
import com.capstone.pacetime.databinding.ActivityRunBinding;
import com.capstone.pacetime.receiver.GPSReceiver;
import com.capstone.pacetime.viewmodel.RunDetailInfoViewModel;
import com.google.android.gms.location.CurrentLocationRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.tasks.OnTokenCanceledListener;

import java.util.Arrays;

public class RunActivity extends AppCompatActivity {
    private static final String TAG = "RunActivity";
    private ActivityRunBinding binding;
    private RunDetailInfoViewModel viewModel;
    private RunInfoUpdateCommand command;
    private RunningManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_run);

        viewModel = new RunDetailInfoViewModel();
        command = new RunDetailInfoUpdateCommand();

        command.setViewModel(viewModel);
        binding.setDetailRunInfo(viewModel);

        for(String[] permissions: RunningManager.getPermissionSets()){
            Log.d(TAG, "Permissions: " + Arrays.toString(permissions));

            if(!PermissionChecker.checkPermissions(this, permissions)){
                Log.d(TAG, "Permissions Denied");
            }
        }

        RunInfo runInfo = new RunInfo();
        runInfo.setCommand(command);

        manager = new RunningManager(this, runInfo);
    }

    @Override
    protected void onStart() {
        super.onStart();
        manager.start();
    }

    @Override
    protected void onPause() {
        super.onPause();

        manager.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Destroy");
    }
}