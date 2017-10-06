package com.gopaysense.psdata.fragment;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.gopaysense.psdata.LoanApplicationActivity;
import com.gopaysense.psdata.R;
import com.gopaysense.psdata.core.SMSProvider;
import com.gopaysense.psdata.core.ServerUtil;
import com.gopaysense.psdata.core.Utils;
import com.gopaysense.psdata.models.SMS;
import com.gopaysense.psdata.models.UserContactFeature;
import com.gopaysense.psdata.nao.ResponseCode;
import com.gopaysense.psdata.nao.ResultNAO;

import java.lang.reflect.Type;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SMSFragment.OnProcessingSMS} interface
 * to handle interaction events.
 * Use the {@link SMSFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SMSFragment extends Fragment {

    private static final String TAG = "SMSFragment";
    private static final int PERMISSION_REQUEST_SMS = 1001;

    final String[] SMS_PERMISSION_GROUP = {Manifest.permission.READ_SMS};

    private String name;
    private String userId;

    private UploadSMSTask task = null;
    private TextView tvDesc;
    private Button btnNext = null;
    private Button btnRetry = null;
    private ProgressBar progressBar = null;

    private OnProcessingSMS mListener;

    public SMSFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param name Parameter 1.
     * @param userId Parameter 2.
     * @return A new instance of fragment SMSFragment.
     */
    public static SMSFragment newInstance(FragmentManager fragmentManager, String name, String userId) {
        SMSFragment fragment = (SMSFragment) fragmentManager
                .findFragmentByTag(SMSFragment.TAG);
        if (fragment == null) {
            fragment = new SMSFragment();
            fragment.setRetainInstance(true);
            Bundle args = new Bundle();
            args.putString(LoanApplicationActivity.InboundParam.USER_NAME, name);
            args.putString(LoanApplicationActivity.InboundParam.USER_ID, userId);
            fragment.setArguments(args);
            fragmentManager.beginTransaction()
                    .replace(R.id.container, fragment)
                    .addToBackStack(SMSFragment.TAG)
                    .commit();
        }
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            name = getArguments().getString(LoanApplicationActivity.InboundParam.USER_NAME);
            userId = getArguments().getString(LoanApplicationActivity.InboundParam.USER_ID);
        }
    }

    private void checkPermissionAndProcess() {

        if (!Utils.hasPermission(getContext(), SMS_PERMISSION_GROUP)) {
            Log.i(LoanApplicationActivity.LOG_TAG, "We don't have Permission");
            askPermission(PERMISSION_REQUEST_SMS, SMS_PERMISSION_GROUP);
        } else {
            Log.i(LoanApplicationActivity.LOG_TAG, "We have Permission");
            processApplicationAfterPermissionGrant();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sms, container, false);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(ResponseCode code, String applicationId, String message) {
        if (mListener != null) {
            mListener.onFinish(code, applicationId, message);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnProcessingSMS) {
            mListener = (OnProcessingSMS) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnNext = view.findViewById(R.id.btn_next);
        btnRetry = view.findViewById(R.id.btn_retry);
        progressBar = view.findViewById(R.id.pb);
        tvDesc = view.findViewById(R.id.tv_desc);

        tvDesc.setText("Processing SMS");
        btnNext.setAlpha(0.5f);
        btnNext.setEnabled(false);

        btnRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissionAndProcess();
            }
        });

        checkPermissionAndProcess();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        progressBar = null;
        if (task != null) {
            task.cancel(true);
            task = null;
        }
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnProcessingSMS {
        void onFinish(ResponseCode result, String applicationId, String message);
    }

    public void finish(ResponseCode result, String applicationId, String message) {
        if (mListener != null) {
            mListener.onFinish(result, applicationId, message);
        }
    }

    private void processApplicationAfterPermissionGrant() {
        task = new UploadSMSTask();
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private class UploadSMSTask extends AsyncTask<String, Integer, ResultNAO> {

        @Override
        protected void onPreExecute() {
            //Show ProgressBar
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected ResultNAO doInBackground(String[] params) {
            try {
                SMSProvider smsProvider = new SMSProvider(getContext());
                //List<SMS> smses = smsProvider.conversations();
                List<SMS> smses = smsProvider.compile();

                Log.d(LoanApplicationActivity.LOG_TAG, "SMS - " + smses.size());

                for (SMS sms : smses) {
                    Log.d(LoanApplicationActivity.LOG_TAG, sms.log());
                }

                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                Type type = new TypeToken<List<SMS>>() {}.getType();
                String json = gson.toJson(smses, type);
//
//                ServerUtil util = new ServerUtil();
//                ServerUtil.ServerResponse response = util.post("", json);
                return new ResultNAO(ResponseCode.SUCCESS, null);
            } catch (Exception e) {
                Log.e(LoanApplicationActivity.LOG_TAG, "Issue while connecting to server " + e.getMessage(), e);
                return new ResultNAO(ResponseCode.CONNECTION_ISSUE, "Server is not reachable");
            }
        }

        @Override
        protected void onPostExecute(final ResultNAO result) {
            //Stop ProgressBar
            progressBar.setVisibility(View.GONE);
            if (result == null) return;
            //Get Processing ID
            final String processId = "XXXYYY";
            if (result.getCode() == ResponseCode.SUCCESS) {
                tvDesc.setText("SMS successfully processed. Please click on Process button to continue.");
                btnNext.setAlpha(1.0f);
                btnNext.setEnabled(true);
                btnRetry.setVisibility(View.GONE);
                btnNext.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mListener.onFinish(ResponseCode.SUCCESS, processId, result.getMessage());
                    }
                });
            } else {
                tvDesc.setText("SMS Processing Failed. Please retry.");
                btnRetry.setVisibility(View.VISIBLE);
            }
        }
    }

    private void askPermission(int requestCode, String... permissions) {
        requestPermissions(permissions, requestCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_SMS: {
                // If request is cancelled, the result arrays are empty.
                boolean permissionGranted = Utils.hasPermissionGranted(permissions, grantResults);
                if (permissionGranted) {
                    Log.i(LoanApplicationActivity.LOG_TAG, "SMS Permission Granted");
                    processApplicationAfterPermissionGrant();
                    return;
                }

                Log.i(LoanApplicationActivity.LOG_TAG, "SMS Permission Denied");
                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                        Manifest.permission.READ_SMS)) {
                    //Show permission explanation dialog...
                    AlertDialog.Builder builder
                            = new AlertDialog.Builder(getContext());
                    builder.setTitle("Alert")
                            .setMessage("Read SMS permission is required to process your loan application. Please grant access to proceed further.")
                            .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    askPermission(PERMISSION_REQUEST_SMS, SMS_PERMISSION_GROUP);
                                }
                            })
                            .setNegativeButton("No, thanks", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    finish(ResponseCode.USER_EXIT, null, "Please grant Read SMS permission to proceed further.");
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                } else {
                    //Never ask again selected, or device policy prohibits the app from having that permission.
                    //So, disable that feature, or fall back to another situation...
                    finish(ResponseCode.USER_EXIT, null, "Please go to Settings->Permission and grant Read SMS permission to proceed further.");
                }

                return;
            }
        }
    }
}