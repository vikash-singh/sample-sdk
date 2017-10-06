package com.gopaysense.psdata;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.gopaysense.psdata.fragment.ContactFragment;
import com.gopaysense.psdata.fragment.SMSFragment;
import com.gopaysense.psdata.nao.ResponseCode;

/**
 *
 */
public class LoanApplicationActivity extends AppCompatActivity
        implements ContactFragment.OnProcessingContacts, SMSFragment.OnProcessingSMS {
    public static final String LOG_TAG = "LOAN_DATA";

    public static final class InboundParam {
        public static final String USER_ID = "PS:INBOUND_EXTRA_USER_ID";
        public static final String USER_NAME = "PS:INBOUND_EXTRA_USER_NAME";
    }

    public static final class Result {
        public static final String CODE = "PS::RESULT::CODE";
        public static final String APPLICATION_ID = "PS::RESULT::APPLICATION_ID";
        public static final String MESSAGE = "PS::RESULT::MESSAGE";
    }

    String userId = null;
    String userName = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loan_application);

        if (null == savedInstanceState) {
            Intent intent = getIntent();
            userId = intent.getStringExtra(InboundParam.USER_ID);
            userName = intent.getStringExtra(InboundParam.USER_NAME);
            if (userId == null || userId.isEmpty()) {
                publishResult(ResponseCode.ERROR, null, "Application Error - User Id is not available");
                return;
            }
            if (userName == null) {
                userName = "Guest";
            }

            ContactFragment.newInstance(getSupportFragmentManager(), userName, userId);
        }
    }

    @Override
    public void afterProcessing(ResponseCode result, String applicationId, String message) {
        if (result == ResponseCode.SUCCESS) {
            SMSFragment.newInstance(getSupportFragmentManager(), userName, userId);
        } else {
            publishResult(result, applicationId, message);
        }
    }

    @Override
    public void onFinish(ResponseCode result, String applicationId, String message) {
        publishResult(result, applicationId, message);
    }

    private void publishResult(ResponseCode result, String applicationId, String message) {
        Intent intent = new Intent();
        intent.putExtra(Result.CODE, result.getCode());
        intent.putExtra(Result.APPLICATION_ID, applicationId);
        intent.putExtra(Result.MESSAGE, message);
        if (result == ResponseCode.USER_EXIT) {
            setResult(Activity.RESULT_CANCELED, intent);
        } else {
            setResult(Activity.RESULT_OK, intent);
        }
        finish();
    }
}
