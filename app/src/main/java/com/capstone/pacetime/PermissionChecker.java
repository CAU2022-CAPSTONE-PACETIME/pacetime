package com.capstone.pacetime;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PermissionChecker {

    public static boolean checkPermissions(AppCompatActivity activity, String[] permissions){

        List<String> abortedPermissions = new ArrayList<>();

        for(String permission: permissions) {
            if (activity.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                abortedPermissions.add(permission);
            }
        }
        if(!abortedPermissions.isEmpty()){
            ActivityResultLauncher<String[]> permissionLauncher;
            permissionLauncher = activity.registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
                    (Map<String, Boolean> result) -> new AlertDialog.Builder(activity.getApplicationContext())
                            .setMessage("앱을 사용 하기 위해 모든 권한을 허용 해야 합니다.")
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
                            .show()
                    );

            permissionLauncher.launch(abortedPermissions.toArray(new String[0]));
            return false;
        }
        return true;
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
