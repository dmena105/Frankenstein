package com.frankenstein.frankenstein;

import android.*;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/**
 * Created by Timothyqiu on 2018-02-27.
 */

public class FrankensteinPermission {
    public static void checkPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(activity, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(activity, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.CAMERA}, 0);
            }
            if (ContextCompat.checkSelfPermission(activity, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(activity, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.CAMERA}, 0);
            }
        }
    }
}
