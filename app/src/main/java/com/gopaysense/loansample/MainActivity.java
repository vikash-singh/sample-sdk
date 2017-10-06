package com.gopaysense.loansample;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.gopaysense.psdata.LoanApplicationActivity;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PROCESS_LOAN_APPLICATION = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
    }

    public void initPD(View v) {
        launch();
    }

    private void launch() {
        Intent intent = new Intent(this, LoanApplicationActivity.class);
        intent.putExtra(LoanApplicationActivity.InboundParam.USER_ID, "ABC-01");
        intent.putExtra(LoanApplicationActivity.InboundParam.USER_NAME, "Vikash Singh");
        startActivityForResult(intent, REQUEST_CODE_PROCESS_LOAN_APPLICATION);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_PROCESS_LOAN_APPLICATION) {
            String message = null;
            if (data != null) {
                message = data.getStringExtra(LoanApplicationActivity.Result.MESSAGE);
            }

            if (resultCode == RESULT_CANCELED) {
                if (message != null && !message.isEmpty()) {
                    AlertDialog.Builder builder
                            = new AlertDialog.Builder(this);
                    builder.setTitle("Info")
                            .setMessage(message)
                            .setNeutralButton("Okay", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    //TODO
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
                return;
            }

            if (resultCode == RESULT_OK) {
                String applicationId = data.getStringExtra(LoanApplicationActivity.Result.APPLICATION_ID);
                AlertDialog.Builder builder
                        = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
                builder.setTitle("Congrats")
                        .setMessage("Congrats, We have received your loan request. " +
                                "Please note your application Id " + applicationId + " for future communication.")
                        .setNeutralButton("Okay", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                //TODO
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                return;
            }

            if (message == null) {
                message = "Sorry, We are not able to process your request at this moment. Please try again later.";
            }

            AlertDialog.Builder builder
                    = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
            builder.setTitle("Sorry")
                    .setMessage(message)
                    .setNeutralButton("Okay", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            //TODO
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
    }
}