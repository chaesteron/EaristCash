package com.example.it3cpartialappsgrp8;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class SendActivity extends AppCompatActivity {
    Boolean viaQR;
    TextInputLayout receiverIdTextField, amountTextField;
    Button sendBtn;
    String  accountId, receiverId,receiverName;
    TransactionDetails transactionDetails = new TransactionDetails();
    Double balance, amount;

    ProgressDialog sendDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);

        setupLayout();

        viaQR = getIntent().getBooleanExtra("viaQR",false);
        accountId = getIntent().getStringExtra("accountId");
        balance = getIntent().getDoubleExtra("balance",0);

        //TODO MODIFY FOR QRCODE TRANSACTIONS
        //if send via qr code
        if(viaQR) {
            String receiverId = getIntent().getStringExtra("receiverId");
            receiverIdTextField.getEditText().setEnabled(false);
            receiverIdTextField.getEditText().setText(receiverId);
        }
        amountTextField.setHelperText("Balance: "+String.valueOf(balance));


        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateInput();
            }
        });

    }
    public void validateInput(){

        if(TextUtils.isEmpty(receiverIdTextField.getEditText().getText())){
            Toast.makeText(SendActivity.this,"Enter an account id for receiver", Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(amountTextField.getEditText().getText())){
            Toast.makeText(SendActivity.this,"Enter an amount", Toast.LENGTH_SHORT).show();
            return;
        }
        String strAmount = amountTextField.getEditText().getText().toString();
        amount = Double.parseDouble(strAmount);
        receiverId = receiverIdTextField.getEditText().getText().toString();

        Log.d("receiverId", "receiverId: "+receiverId+" amount: "+amount);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference accountRef = database.getReference("users");

        Query query = accountRef.orderByChild("accountId").equalTo(receiverId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        receiverName = userSnapshot.child("name").getValue(String.class);
                        Log.d("receiverName", "receiverName: "+receiverName);
                        break;
                    }
                    // Account with the specified accountId exists
                    //check if sufficient balance
                    if(amount <= balance){
                        new sendPointsTask().execute();
                    }else{
                        Toast.makeText(SendActivity.this,"Insufficient balance", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(SendActivity.this,"Invalid account id", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("databaseError", "onCancelled: "+databaseError.getMessage());
            }
        });

    }


    // Define an interface to handle the validation result
    interface OnValidationResultListener {
        void onValidationResult(boolean exists);
    }




    private class sendPointsTask extends AsyncTask<String, Void, Boolean> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            sendDialog = new ProgressDialog(SendActivity.this);
            sendDialog.setMessage("Please wait...");
            sendDialog.setCancelable(false);
            sendDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... strings) {


            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();


            Log.d("SEND", "sendPoints: "+amount);
            Log.d("SEND", "receiver: "+receiverId);

            transactionDetails.setUserId(userId);
            transactionDetails.setReceiverAccountId(receiverId);
            transactionDetails.setAccountId(getIntent().getStringExtra("accountId"));
            transactionDetails.setTransactionType("Send");
            transactionDetails.setAmount(String.valueOf(amount));
            transactionDetails.setCurrencyIsoCode("PHP");
            transactionDetails.setStatusCode("SUCCESSFUL");



// Pass the transactionDetails object to the Firebase Realtime Database
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference transactionRef = database.getReference("transactions");
            DatabaseReference newRef = transactionRef.push();
            String uniqueId = newRef.getKey();

            //set id and timestamp
            Map<String, String> SERVER_TIMESTAMP_FROM_DB;
            Log.d("ServerValue.TIMESTAMP", ServerValue.TIMESTAMP.toString());

//                String timestampString = createdAtMap.get("timestamp");
//                long timestampLong = 0;
//                try{
//                    timestampLong = Long.parseLong(timestampString);
//                }catch (NumberFormatException  e){
//                    Log.d("ParseException", e.getMessage());
//                }
            transactionDetails.setTransactionId(uniqueId);
            transactionDetails.setSERVER_TIMESTAMP(ServerValue.TIMESTAMP);

            CompletableFuture<Boolean> future = new CompletableFuture<>();
            //add to database
            transactionRef.child(transactionDetails.getUserId()).child(transactionDetails.getTransactionId()).setValue(transactionDetails)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {

                        @Override
                        public void onSuccess(Void aVoid) {

                            // Transaction saved successfully
                            transactionRef.child(transactionDetails.getUserId()).child(transactionDetails.getTransactionId()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {

                                        long server_TIMESTAMP = dataSnapshot.child("server_TIMESTAMP").getValue(Long.class);
                                        //set timestamp
                                        transactionDetails.setCreatedAtTimestamp(server_TIMESTAMP);
                                        transactionRef.child(transactionDetails.getUserId()).child(transactionDetails.getTransactionId()).child("createdAtTimestamp").setValue(transactionDetails.getCreatedAtTimestamp())
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {

                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Log.d("UPDATE TIMESTAMP", "SUCCESS ");
                                                        addReceiveTransactionForReceiver(transactionDetails.getReceiverAccountId());
                                                        future.complete(true);
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.d("UPDATE TIMESTAMP", "FAILED: "+e.getMessage());
                                                        future.complete(false);
                                                    }
                                                });
                                        System.out.println("Value of server_TIMESTAMP: " + server_TIMESTAMP);

                                    } else {
                                        future.complete(false);
                                        // Data does not exist at the specified location
                                        System.out.println("Transaction data not found.");
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    // Handle the error, if any
                                    System.err.println("DatabaseError: " + databaseError.getMessage());
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("onFailure", "onFailure: "+e.getMessage());
                            future.complete(false);
                        }
                    });
            // Wait for the CompletableFuture to complete and return the result
            try {
                return future.get(); // This will wait for the result (true or false)
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                return false; // Handle any exceptions, or you can rethrow them if needed
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            sendDialog.dismiss();
            if(success){
                new UpdateAccountTask().execute(receiverId,String.valueOf(amount),"add");
                new UpdateAccountTask().execute(accountId,String.valueOf(amount),"minus");
                showReceipt(transactionDetails);
            }else{
                Toast.makeText(SendActivity.this,"An error occurred", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void addReceiveTransactionForReceiver(String receiverId) {
        // Assuming you have the specific accountId you want to query for in a variable called targetAccountId

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        Query query = usersRef.orderByChild("accountId").equalTo(receiverId);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                            String userId = userSnapshot.getKey();
                            if(userId!=null){
                                Log.d("userId", "userId: "+userId);
                                //change the user id to id of receiver
                                transactionDetails.setUserId(userId);
                                transactionDetails.setTransactionType("Receive");
                                DatabaseReference transactionRef = FirebaseDatabase.getInstance().getReference("transactions");

                                transactionRef.child(transactionDetails.getUserId()).child(transactionDetails.getTransactionId()).setValue(transactionDetails);
                                // userId will be the user ID where the accountId matches targetAccountId
                                // You can do further processing with the userId here.
                                // If you expect only one match, you can break the loop here.
                                break;
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle any errors
            }
        });
    }

    private void showReceipt(TransactionDetails transactionDetails) {

        String receiverId = transactionDetails.getReceiverAccountId();
        String transactionId = transactionDetails.getTransactionId();
        String amount = transactionDetails.getAmount();
//        String merchantAccountId = transactionObject.get("merchantAccountId").getAsString();
        String currencyIsoCode = transactionDetails.getCurrencyIsoCode();
        String status_code = transactionDetails.getStatusCode();
        //date
        long createdAtTimestamp = transactionDetails.getCreatedAtTimestamp();

        String formattedDate = "";
        // Specify the input date format
        DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS", Locale.ENGLISH);

// Specify the desired output date format
        DateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy, hh:mm a", Locale.ENGLISH);
        outputFormat.setTimeZone(TimeZone.getTimeZone("Asia/Manila"));

        try {
            // Parse the input date string
            Date date = new Date(createdAtTimestamp);

            // Format the date to the desired output format
            formattedDate = outputFormat.format(date);

        } catch (Exception  e) {
            e.printStackTrace();
        }


        // Create an intent to start the target activity
        Intent intent = new Intent(this, SendReceiptActivity.class);

// Put the data you want to pass as extras in the intent
        intent.putExtra("transactionId", transactionId);
        intent.putExtra("amount", amount);
        intent.putExtra("currencyIsoCode", currencyIsoCode);
        intent.putExtra("statusCode", status_code);
        intent.putExtra("dateTime", formattedDate);
        intent.putExtra("receiverId", receiverId);
        intent.putExtra("receiverName", receiverName);


// Add additional putExtra statements as needed

// Start the target activity
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        finish();
        startActivity(intent);
    }

    private class UpdateAccountTask extends AsyncTask<String, Void, Void> {
        Double currentBalance, newBalance;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected Void doInBackground(String... strings) {
            String accountId = strings[0];
            String amount = strings[1];
            String action = strings[2];


            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference accountRef = database.getReference("users");

            Query query = accountRef.orderByChild("accountId").equalTo(accountId);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                            //added this condition to update the selected account only
                            if(userSnapshot.child("accountId").getValue(String.class).equals(accountId)){
                                Log.d("action", "onDataChange: "+action);
                                // Get the current balance
                                currentBalance = userSnapshot.child("balance").getValue(Double.class);
                                Log.d("TAG", "onDataChange: "+action);
                                if(action.equals("add")){
                                    newBalance = currentBalance + Double.parseDouble(amount) ;
                                    Log.d("ADD", "newBalance: "+newBalance);
                                }else{
                                    newBalance = currentBalance - Double.parseDouble(amount) ;
                                    Log.d("MINUS", "newBalance: "+newBalance);
                                }

                                userSnapshot.child("balance").getRef().setValue(newBalance);
                                break;
                            }
                        }
                    }
                }


                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle any errors or cancellations
                }
            });

            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
        }
    }

    public void navigateToDashboardActivity(View view){
        Intent intent = new Intent(this, Dashboard.class);
        startActivity(intent);
        finish();
    }
    public void setupLayout(){
        receiverIdTextField = findViewById(R.id.receiverIdTextField);
        amountTextField = findViewById(R.id.amountTextField);
        sendBtn = findViewById(R.id.sendBtn);
    }
    private long convertToTimestamp(String createdAt) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
            Date date = sdf.parse(createdAt);
            return date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }
}