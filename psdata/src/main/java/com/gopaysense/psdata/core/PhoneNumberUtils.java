package com.gopaysense.psdata.core;

import android.os.Build;

/**
 * Created by Vikash Singh on 5/10/17.
 */

public class PhoneNumberUtils extends android.telephony.PhoneNumberUtils {

    public static String format(String phoneNumber) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return normalizeNumber(PhoneNumberUtils.formatNumberToE164(phoneNumber, "IN"));
        } else {
            //TODO Need to implement our own
            return normalizeNumber(PhoneNumberUtils.formatNumber(phoneNumber));
        }
    }

    public static String normalizeNumber(String phoneNumber) {
        if (phoneNumber == null)
            return "";
        StringBuilder sb = new StringBuilder();
        int len = phoneNumber.length();
        for (int i = 0; i < len; i++) {
            char c = phoneNumber.charAt(i);
            int digit = Character.digit(c, 10);
            if (digit != -1) {
                sb.append(digit);
            } else if (i == 0 && c == '+') {
                sb.append(c);
            }
//            else if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
//                return normalizeNumber(PhoneNumberUtils.convertKeypadLettersToDigits(phoneNumber));
//            }
        }
        return sb.toString();
    }

}
