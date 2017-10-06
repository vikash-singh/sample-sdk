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
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.gopaysense.psdata.LoanApplicationActivity;
import com.gopaysense.psdata.R;
import com.gopaysense.psdata.core.ContactsProvider;
import com.gopaysense.psdata.core.ServerUtil;
import com.gopaysense.psdata.core.TopContactsHelper;
import com.gopaysense.psdata.core.Utils;
import com.gopaysense.psdata.models.CallLog;
import com.gopaysense.psdata.models.UserContact;
import com.gopaysense.psdata.models.UserContactFeature;
import com.gopaysense.psdata.nao.ResponseCode;
import com.gopaysense.psdata.nao.ResultNAO;
import com.gopaysense.psdata.ui.ContactAdapter;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ContactFragment.OnProcessingContacts} interface
 * to handle interaction events.
 * Use the {@link ContactFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ContactFragment extends Fragment {

    private static final String TAG = "ContactFragment";
    private static final int PERMISSION_REQUEST_CALL_LOG = 1001;

    final String[] CALL_PERMISSION_GROUP = {Manifest.permission.READ_CALL_LOG,
            Manifest.permission.READ_CONTACTS};

    private String name;
    private String userId;

    private ListView listView;
    private TextView tvDesc = null;
    private Button btnNext = null;
    private Button btnRetry = null;
    private ProgressBar progressBar = null;

    private OnProcessingContacts mListener;
    private UploadCallLogTask task = null;

    public ContactFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param name Parameter 1.
     * @param userId Parameter 2.
     * @return A new instance of fragment ContactFragment.
     */
    public static ContactFragment newInstance(FragmentManager fragmentManager, String name, String userId) {
        ContactFragment fragment = (ContactFragment) fragmentManager
                .findFragmentByTag(ContactFragment.TAG);
        if (fragment == null) {
            fragment = new ContactFragment();
            fragment.setRetainInstance(true);
            Bundle args = new Bundle();
            args.putString(LoanApplicationActivity.InboundParam.USER_NAME, name);
            args.putString(LoanApplicationActivity.InboundParam.USER_ID, userId);
            fragment.setArguments(args);
            fragmentManager.beginTransaction()
                    .replace(R.id.container, fragment)
//                    .addToBackStack(ContactFragment.TAG)
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

        if (!Utils.hasPermission(getContext(), CALL_PERMISSION_GROUP)) {
            Log.i(LoanApplicationActivity.LOG_TAG, "We don't have Permission");
            askPermission(PERMISSION_REQUEST_CALL_LOG, CALL_PERMISSION_GROUP);
        } else {
            Log.i(LoanApplicationActivity.LOG_TAG, "We have Permission");
            processApplicationAfterPermissionGrant();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_contact, container, false);
    }

    public void finish(ResponseCode result, String applicationId, String message) {
        if (mListener != null) {
            mListener.afterProcessing(result, applicationId, message);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvDesc = view.findViewById(R.id.tv_desc);
        btnNext = view.findViewById(R.id.btn_next);
        btnRetry = view.findViewById(R.id.btn_retry);
        progressBar = view.findViewById(R.id.pb);
        listView = view.findViewById(android.R.id.list);

        tvDesc.setText("Processing Top Contacts");
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.afterProcessing(ResponseCode.SUCCESS, null, "TODO - A lot");
            }
        });
        btnNext.setEnabled(false);
        btnNext.setAlpha(0.5f);
        btnRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissionAndProcess();
            }
        });

        checkPermissionAndProcess();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnProcessingContacts) {
            mListener = (OnProcessingContacts) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
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

    @Override
    public void onSaveInstanceState(Bundle outState) {

    }

    public interface OnProcessingContacts {
        void afterProcessing(ResponseCode result, String applicationId, String message);
    }

    private void processApplicationAfterPermissionGrant() {
        task = new UploadCallLogTask();
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private class UploadCallLogTask extends AsyncTask<String, Integer, ResultNAO<List<UserContactFeature>>> {

        @Override
        protected void onPreExecute() {
            //Show ProgressBar
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected ResultNAO<List<UserContactFeature>> doInBackground(String[] params) {
            try {
                ContactsProvider contactsProvider = new ContactsProvider(getContext());
                List<CallLog> callLogs = contactsProvider.getCallDetails();
                List<UserContact> contacts = contactsProvider.getContacts(true);

                Log.d(LoanApplicationActivity.LOG_TAG, "Call Logs - " + callLogs.size());
                Log.d(LoanApplicationActivity.LOG_TAG, "Contacts - " + contacts.size());

                for (CallLog log : callLogs) {
                    Log.d(LoanApplicationActivity.LOG_TAG, log.log());
                }

                for (UserContact contact : contacts) {
                    Log.d(LoanApplicationActivity.LOG_TAG, contact.log());
                }

                TopContactsHelper helper = new TopContactsHelper();
                List<UserContactFeature> top50 = helper.process(contacts, callLogs);

                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                Type type = new TypeToken<List<UserContactFeature>>() {}.getType();
                String json = gson.toJson(top50, type);

//                ServerUtil util = new ServerUtil();
//                ServerUtil.ServerResponse response = util.post("", json);
                return new ResultNAO<>(ResponseCode.SUCCESS, null, top50);
            } catch (Exception e) {
                Log.e(LoanApplicationActivity.LOG_TAG, "Issue while connecting to server " + e.getMessage(), e);
                return new ResultNAO(ResponseCode.CONNECTION_ISSUE, "Server is not reachable");
            }
        }

        @Override
        protected void onPostExecute(ResultNAO<List<UserContactFeature>> result) {
            //Stop ProgressBar
            progressBar.setVisibility(View.GONE);
            if (result == null) return;

            if (result.getCode() == ResponseCode.SUCCESS) {
                //show list
                UserContactFeature[] arr = new UserContactFeature[result.getData().size()];
                result.getData().toArray(arr);
                listView.setAdapter(new ContactAdapter(getContext(), arr));
                listView.setVisibility(View.VISIBLE);
                tvDesc.setText("Now show list");
                tvDesc.setVisibility(View.GONE);
                btnNext.setAlpha(1.0f);
                btnNext.setEnabled(true);
                btnRetry.setVisibility(View.GONE);
            } else {
                tvDesc.setText("Top contacts processing failed, Please retry.");
                btnRetry.setVisibility(View.VISIBLE);
            }
            //Get Processing ID
        }
    }

    private void askPermission(int requestCode, String... permissions) {
        requestPermissions(permissions, requestCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CALL_LOG: {
                // If request is cancelled, the result arrays are empty.
                boolean permissionGranted = Utils.hasPermissionGranted(permissions, grantResults);
                if (permissionGranted) {
                    Log.i(LoanApplicationActivity.LOG_TAG, "CallLog Permission Granted");
                    processApplicationAfterPermissionGrant();
                    return;
                }

                Log.i(LoanApplicationActivity.LOG_TAG, "CallLog Permission Denied");
                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                        Manifest.permission.READ_CALL_LOG)
                        || ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                        Manifest.permission.READ_CONTACTS)) {
                    //Show permission explanation dialog...
                    AlertDialog.Builder builder
                            = new AlertDialog.Builder(getContext());
                    builder.setTitle("Alert")
                            .setMessage("Read Contact And Call Log permission is required to process your loan application. Please grant access to proceed further.")
                            .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    checkPermissionAndProcess();
                                }
                            })
                            .setNegativeButton("No, thanks", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    finish(ResponseCode.USER_EXIT, null, "Please grant Read Contact and Call Log Permission to proceed further.");
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                } else {
                    finish(ResponseCode.USER_EXIT, null, "Please go to Settings->Permission and grant Read Contact and Call Log Permission to proceed further.");
                    //Never ask again selected, or device policy prohibits the app from having that permission.
                    //So, disable that feature, or fall back to another situation...
                }

                return;
            }
        }
    }
}