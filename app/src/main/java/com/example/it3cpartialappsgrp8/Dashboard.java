package com.example.it3cpartialappsgrp8;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import android.app.ProgressDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.NonNull;

public class Dashboard extends AppCompatActivity {

    private TextView userNameTextView, userNumberTextView, accountIDTextView, balanceTextView;
    private CardView topUp, history, sendBtn, generateQR, scanQR;
    private ImageView setting;
    private Double balance;
    private LinearLayout buttonsLayout;

    private String registeredUserName, registeredUserPhoneNumber, accountID, userId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        //hide first and wait for the user id

        setupLayout();
        // Retrieve user data from Firebase and update the UI
        getUserData();
        setupOnclickListeners();

//        on refresh
        SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Perform your refresh action here
                // For example, you can reload the activity or update the data
                getUserData();
                swipeRefreshLayout.setRefreshing(false);
            }
        });




    }

    private void getUserData() {

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    registeredUserName = dataSnapshot.child("name").getValue(String.class);
                    registeredUserPhoneNumber = dataSnapshot.child("phoneNumber").getValue(String.class);
                    accountID = dataSnapshot.child("accountId").getValue(String.class);
                    balance = dataSnapshot.child("balance").getValue(Double.class);

                    updateUI();
                    if(buttonsLayout.getVisibility() == View.INVISIBLE){
                        buttonsLayout.setVisibility(View.VISIBLE);
                        scanQR.setVisibility(View.VISIBLE);
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle any errors
            }
        });
    }
    private void openChangePasswordActivity() {
        Intent intent = new Intent(this, ChangePassword.class);
        startActivity(intent);
    }

    private void updateUI() {
        userNameTextView.setText(StringUtils.capitalize(registeredUserName));
        userNumberTextView.setText(registeredUserPhoneNumber);
        accountIDTextView.setText(accountID);
        balanceTextView.setText(String.format("%.2f", balance));
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void topUp() {
        Intent intent = new Intent(this, TopUpActivity.class);
        intent.putExtra("accountId", accountID);
        startActivity(intent);
    }

    private void send() {
        Intent intent = new Intent(this, SendActivity.class);
        intent.putExtra("balance", balance);
        intent.putExtra("accountId", accountID);
        intent.putExtra("viaQR", false);
        startActivity(intent);
    }
    private void generateQRCode() {
        Intent intent = new Intent(this, GenerateQrCode.class);
        intent.putExtra("accountId",accountID);
        startActivity(intent);
    }
    private void showTransactionHistory() {
        Intent intent = new Intent(this, TransactionHistoryActivity.class);
        startActivity(intent);
    }

    private void openSettings() {
        // Inflate the custom layout for the dialog
        View dialogView = LayoutInflater.from(this).inflate(R.layout.custom_setting_dialog, null);

        // Create the AlertDialog builder and set the custom view
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setView(dialogView);

        // Create the AlertDialog
        AlertDialog alertDialog = builder.create();

        // Find the buttons in the custom layout and set their click listeners
        Button changePasswordBtn = dialogView.findViewById(R.id.change_btn);
        Button logoutBtn = dialogView.findViewById(R.id.logout);

        changePasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openChangePasswordActivity();
                alertDialog.dismiss(); // Dismiss the dialog after handling the click
            }
        });

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
                alertDialog.dismiss(); // Dismiss the dialog after handling the click
            }
        });

        // Show the AlertDialog
        alertDialog.show();
    }


    private void scanCode()
    {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Volume up to flash on");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setCaptureActivity(CaptureAct.class);
        barLauncher.launch(options);
    }
    private void setupLayout() {
        buttonsLayout = findViewById(R.id.buttonsLayout);
        userNameTextView = findViewById(R.id.user_name);
        userNumberTextView = findViewById(R.id.user_number);
        accountIDTextView = findViewById(R.id.accountIDTextView);
        balanceTextView = findViewById(R.id.balanceTextView);
        generateQR = findViewById(R.id.dashboard_generateQrCode);
        topUp = findViewById(R.id.topUp);
        scanQR = findViewById(R.id.scanQR);
        sendBtn = findViewById(R.id.sendBtn);
        history = findViewById(R.id.dashboard_history);
        setting = findViewById(R.id.dash_setting);
    }
    private void setupOnclickListeners() {
        generateQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generateQRCode();
            }
        });

        scanQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanCode();
            }
        });

        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSettings();
            }
        });
        topUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                topUp();
            }
        });
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send();
            }
        });
        history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTransactionHistory();
            }
        });
    }
    ActivityResultLauncher<ScanOptions> barLauncher = registerForActivityResult(new ScanContract(), result->
    {
        ProgressDialog loading = new ProgressDialog(Dashboard.this); // Use the default style
        loading.setMessage("Loading..."); // Set the message to be displayed
        loading.setCancelable(false);
        loading.show();

        AlertDialog.Builder builder = new AlertDialog.Builder(Dashboard.this);
        builder.setTitle("Account Id not found");
        builder.setMessage(result.getContents());
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                dialogInterface.dismiss();

            }
        });

        if(result.getContents() !=null)
        {
            if(result.getContents().equals(accountID)){
                loading.dismiss();
                builder.setTitle("Sending to own accountId is NOT ALLOWED");
                builder.setMessage(result.getContents());
                builder.show();

            }else {
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference accountRef = database.getReference("users");

                Query query = accountRef.orderByChild("accountId").equalTo(result.getContents());
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        loading.dismiss();
                        if (dataSnapshot.exists()) {
                            Intent intent = new Intent(Dashboard.this, SendActivity.class);
                            intent.putExtra("balance", balance);
                            intent.putExtra("viaQR", true);
                            intent.putExtra("accountId", accountID);
                            intent.putExtra("receiverId", result.getContents());
                            startActivity(intent);
                        } else {
                            builder.show();
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        loading.dismiss();
                        builder.setTitle("Database Error");
                        builder.setMessage(databaseError.toString());
                        builder.show();

                    }
                });

            }
        }
        loading.dismiss();
    });
}
