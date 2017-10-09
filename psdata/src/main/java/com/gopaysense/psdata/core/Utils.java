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

    static String normalizePhoneNumber(String number) {
        if (number == null || number.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        int len = number.length();

        for (int i = 0; i < len; i++) {
            char c = number.charAt(i);

            if (c == '*' || c == '#' ) {
                sb.append(c);
                continue;
            }

            //Take care of Locale Digit
            int digit = Character.digit(c, 10);

            //skip leading zeroes
            if (digit == 0 && sb.length() == 0) {
                continue;
            }

            if (digit != -1) {
                sb.append(digit);
            } else if (i == 0 && c == '+') {
                sb.append(c);
            }
        }

        //Prepend country code
        if (sb.charAt(0) != '+') {
            sb.insert(0, "+91");
        }

        return sb.toString();
    }



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
