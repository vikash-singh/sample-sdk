package com.gopaysense.psdata.core;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;

/**
 * Created by Vikash Singh on 5/10/17.
 */

public class Utils {

    private static final String LOG_TAG = "LOAN_DATA";

    public static boolean hasPermission(Context context, String... permissions) {

        for (String permission : permissions) {
            int permissionCheck = ContextCompat.checkSelfPermission(context, permission);
            if (permissionCheck == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }

        return true;
    }

    public static boolean hasPermissionGranted(String permissions[], int[] grantResults) {
        boolean permissionGranted = true;
        if (grantResults.length > 0) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    permissionGranted = permissionGranted && true;
                } else {
                    Log.i(LOG_TAG, "Permission Denied - " + permissions[i]);
                    permissionGranted = false;
                }
            }
        } else {
            permissionGranted = false;
        }

        return permissionGranted;
    }
}
