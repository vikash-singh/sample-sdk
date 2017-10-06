package com.gopaysense.psdata;

import android.app.Activity;

/**
 * Created by Vikash Singh on /10/17.
 */

public class LoanApplicationMgr {


    public void launch(Activity activity, LoanApplicationCallback callback) {

        if (callback == null) {
            //TODO throw
            return; //TODO remove
        }

        String applicationId = null;
        callback.onFinished(applicationId);
    }

    public interface LoanApplicationCallback {

        public void onFinished(String applicationId);
    }
}
