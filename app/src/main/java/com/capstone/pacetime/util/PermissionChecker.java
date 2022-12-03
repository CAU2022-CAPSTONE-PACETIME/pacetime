package com.capstone.pacetime.util;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;
import android.view.WindowManager;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;

import com.capstone.pacetime.BuildConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PermissionChecker {
    private static final String TAG = "PERMISSION_CHECKER";
    public static boolean checkPermissions(AppCompatActivity activity, String[] permissions){

        List<String> deniedPermissions = new ArrayList<>();

        for(String permission: permissions){
            if(activity.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED){
                deniedPermissions.add(permission);
                Log.d(TAG, "DENIED: " + permission);
            }
        }

        if(deniedPermissions.isEmpty()){
            return true;
        }
        else{
            ActivityResultLauncher<String[]> permissionLauncher = activity.registerForActivityResult(
                    new ActivityResultContracts.RequestMultiplePermissions(),
                    result -> {
                        for(String permission: deniedPermissions){
                            if(Boolean.FALSE.equals(result.get(permission))){
                                activity.finish();
                            }
                        }
                    }
            );

            permissionLauncher.launch(deniedPermissions.toArray(new String[]{}));
        }

        return false;
    }

    public static boolean checkPermission(AppCompatActivity activity, String permission, String content){
        if (activity.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED){
            ActivityResultLauncher<String> permissionLauncher;
            permissionLauncher = activity.registerForActivityResult(new ActivityResultContracts.RequestPermission(), (isGranted) -> {
                if (!isGranted) {
                    new AlertDialog.Builder(activity.getApplicationContext())
                            .setTitle(content + " 권한")
                            .setMessage("앱을 사용하시려면, "+ content +" 권한을 허용해 주세요.")
                            .setPositiveButton("확인", (DialogInterface dialog, int which) -> {
                                        Intent intent = new Intent();
                                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                        Uri uri = Uri.fromParts("package",
                                                BuildConfig.APPLICATION_ID, null);
                                        intent.setData(uri);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        activity.startActivity(intent);
                                    }
                            )
                            .create()
                            .show();
                }
            });
            permissionLauncher.launch(permission);
            return false;
        }

        return true;
    }
}
