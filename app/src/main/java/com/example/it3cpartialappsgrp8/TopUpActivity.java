package com.example.it3cpartialappsgrp8;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;


import com.braintreepayments.api.interfaces.HttpResponseCallback;
import com.braintreepayments.api.models.PaymentMethodNonce;
import com.braintreepayments.api.dropin.DropInResult;
import com.braintreepayments.api.dropin.DropInActivity;
import com.braintreepayments.api.dropin.DropInRequest;
import com.braintreepayments.api.internal.HttpClient;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.JsonObject;


import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;


public class TopUpActivity extends AppCompatActivity   {

    TextInputEditText topUpAmount;
    TextInputLayout topUpAmountLayout;
    JsonObject globalTransactionObject;
    Button paymentBtn;
    private ProgressDialog transactionDialog;
    //CHANGE ACCORDINGLY
    final String API_GET_TOKEN = "https://ffa-api.000webhostapp.com/braintree-php-6.11.2/main.php";
    final String API_CHECKOUT = "https://ffa-api.000webhostapp.com/braintree-php-6.11.2/checkout.php";
    private static final int REQUEST_CODE = 1234;
    String token, amount;
    HashMap<String, String> paramsHash;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_up);
        new getToken().execute();
        setupEditTexts();
        paymentBtn= findViewById(R.id.payment_button_container);
        paymentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String amountString = topUpAmount.getText().toString();
                if(!amountString.isEmpty()  || !amountString.trim().isEmpty()){
                    if(isAmountAboveMin(amountString)){
                        if(isAmountBelowMax(amountString)){
                            topUpAmountLayout.setError(null);
                            submitPayment();
                        }else{
                            topUpAmountLayout.setError("Maximum of 10,000 only!");
                        }
                    }
                    else{
                        topUpAmountLayout.setError("Minimum of 100 only!");
                    }
                }else{
                    topUpAmountLayout.setError("Can't be empty!");
                }


            }
        });
    }

    private Boolean isAmountAboveMin(String amountString) {
        double amount = Double.parseDouble(amountString);
        return amount > 100;
    }
    private Boolean isAmountBelowMax(String amountString) {
        double amount = Double.parseDouble(amountString);
        return amount < 10001;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult( requestCode,  resultCode, data);
        if(requestCode == REQUEST_CODE){
            if(resultCode == RESULT_OK){
                DropInResult result  = data.getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT);
                PaymentMethodNonce nonce = result.getPaymentMethodNonce();
                String strNonce = nonce.getNonce();


                if(!topUpAmount.getText().toString().isEmpty()){
                    amount = topUpAmount.getText().toString();
                    paramsHash = new HashMap<>();
                    paramsHash.put("amount",amount);
                    paramsHash.put("payment_method_nonce",strNonce);

                    sendPayments();
                }else{
                    Toast.makeText(TopUpActivity.this,"Please enter a valid amount", Toast.LENGTH_SHORT).show();
                }
            } else if (resultCode==RESULT_CANCELED) {
                Toast.makeText(TopUpActivity.this,"User Cancelled", Toast.LENGTH_SHORT).show();
            }else{
                Exception error=(Exception)data.getSerializableExtra(DropInActivity.EXTRA_ERROR);
                Toast.makeText(TopUpActivity.this,"Failed to communicate with Braintree, check your internet connection and try again.", Toast.LENGTH_SHORT).show();
                Log.d("Err",error.toString());
            }
        }
    }


    private void submitPayment(){
        String payValue= topUpAmount.getText().toString();
        if(!payValue.isEmpty())
        {
            DropInRequest dropInRequest=new DropInRequest().clientToken(token).collectDeviceData(true).disablePayPal();
            startActivityForResult(dropInRequest.getIntent(this),REQUEST_CODE);
        }
        else
            Toast.makeText(this, "Enter a valid amount for payment", Toast.LENGTH_SHORT).show();

    }

    private void sendPayments(){

        // disable inputs
        topUpAmount.setEnabled(false);
        paymentBtn.setEnabled(false);

        ProgressDialog dialog;
        dialog = new ProgressDialog(TopUpActivity.this, android.R.style.Theme_DeviceDefault_Dialog);
        dialog.setCancelable(false);
        dialog.setMessage("Please wait...");
        dialog.show();
        RequestQueue queue= Volley.newRequestQueue(TopUpActivity.this);
        StringRequest stringRequest=new StringRequest(Request.Method.POST, API_CHECKOUT,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                        Log.d("Response STRING", res);

                        // get only the json if an error occured
                        // Find the index of the first opening curly brace '{'
                        int startIndex = res.indexOf("{");

                        // Extract the JSON portion from the response
                        String response = res.substring(startIndex);
                        // Parse the JSON response
                        Gson gson = new Gson();
                        JsonObject jsonObject = gson.fromJson(response, JsonObject.class);

                        // Print the entire JSON response
                        String prettyJson = gson.toJson(jsonObject);
                        Log.d("Response", prettyJson);




                        if (jsonObject.has("success") && jsonObject.get("success").getAsBoolean()) {

//
                            Log.i("PAYMENT PROCESS STATUS", "Payment Success");
//

//                             Access specific fields from the JSON response
//                             Get the `id` from the `transaction` object
                            JsonObject transactionObject = jsonObject.getAsJsonObject("transaction");
                            String transactionId = transactionObject.get("id").getAsString();
                            String amount = transactionObject.get("amount").getAsString();
                            String currencyIsoCode = transactionObject.get("currencyIsoCode").getAsString();

                            //send transaction object to show
                            new RecordTransactionTask().execute(transactionObject);


                            //process
                            // Process the transaction details
                            Log.d("Transaction ID", transactionId);
                            Log.d("Amount", currencyIsoCode+ " "+amount);
                            dialog.dismiss();
                        }
                        else {
                            // enable inputs
                            topUpAmount.setEnabled(true);
                            paymentBtn.setEnabled(true);
                            dialog.dismiss();
                            Toast.makeText(TopUpActivity.this, "Payment Failed", Toast.LENGTH_SHORT).show();
                        }
                        Log.d("Response",response);

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("VolleyErr",error.toString());
                dialog.dismiss();
                // enable inputs
                topUpAmount.setEnabled(true);
                paymentBtn.setEnabled(true);
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                if(paramsHash==null)
                    return null;
                Map<String,String> params=new HashMap<>();
                for(String key:paramsHash.keySet())
                {
                    params.put(key,paramsHash.get(key));
                    Log.d("Params", key + " -> " + paramsHash.get(key));
                }
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params=new HashMap<>();
                params.put("Content-type","application/x-www-form-urlencoded");

                return params;
            }
        };
        RetryPolicy mRetryPolicy=new DefaultRetryPolicy(0,DefaultRetryPolicy.DEFAULT_MAX_RETRIES,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(mRetryPolicy);
        queue.add(stringRequest);
    }
    private class UpdateAccountTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            transactionDialog = new ProgressDialog(TopUpActivity.this);
            transactionDialog.setMessage("Updating account...");
            transactionDialog.setCancelable(false);
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            String accountId = strings[0];
            String amount = strings[1];

            CompletableFuture<Boolean> future = new CompletableFuture<>();
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference accountRef = database.getReference("users");
            accountRef.orderByChild("accountId").equalTo(accountId);

            accountRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        if(userSnapshot.child("accountId").getValue(String.class).equals(accountId) ){
                            // Get the current balance
                            Double currentBalance = userSnapshot.child("balance").getValue(Double.class);

                            // Update the balance property
                            Double newBalance = currentBalance + Double.parseDouble(amount); // Replace with the new balance calculation

                            // Create a map to update the balance property
                            Map<String, Object> updateData = new HashMap<>();
                            updateData.put("balance", newBalance);

                            // Update the balance property with a completion listener
                            userSnapshot.getRef().updateChildren(updateData, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                    if (databaseError == null) {
                                        // Update operation successful
                                        // You can add any additional code you want to execute upon success
                                        System.out.println("Balance update successful!");
                                        future.complete(true);
                                    } else {
                                        // Update operation failed
                                        // You can handle the failure or display an error message
                                        future.complete(false);
                                        System.err.println("Balance update failed: " + databaseError.getMessage());
                                    }
                                }
                            });
                            break;
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle any errors or cancellations
                }
            });

            try {
                return future.get(); // This will wait for the result (true or false)
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                return false; // Handle any exceptions, or you can rethrow them if needed
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            transactionDialog.dismiss();
            // Notify the listener about the success or failure
            if (success) {
                showReceipt(globalTransactionObject);
                Log.i("UPDATE ACCOUNT", "UPDATE BALANCE : SUCCESS");
            } else {
                Log.i("UPDATE ACCOUNT", "UPDATE BALANCE : FAILED");
            }
        }
    }


    private class RecordTransactionTask extends AsyncTask<JsonObject, Void, Boolean> {
        JsonObject transactionObject;
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Create an instance of the TransactionDetails class
        TransactionDetails transactionDetails = new TransactionDetails();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            transactionDialog = new ProgressDialog(TopUpActivity.this);
            transactionDialog.setMessage("Recording transaction...");
            Log.i("TRANSACTION", "Recording transaction...");
            transactionDialog.setCancelable(false);

        }

        @Override
        protected Boolean doInBackground(JsonObject... jsonObjects) {

            transactionObject = jsonObjects[0];
            //pass
            globalTransactionObject = transactionObject;

            // This method runs on a background thread
            JsonObject creditCard = transactionObject.getAsJsonObject("creditCard");
            JsonObject createdAt = transactionObject.getAsJsonObject("createdAt");

            String transactionId = transactionObject.get("id").getAsString();
            String amount = transactionObject.get("amount").getAsString();
//        String merchantAccountId = transactionObject.get("merchantAccountId").getAsString();
            String currencyIsoCode = transactionObject.get("currencyIsoCode").getAsString();
            String status_code = transactionObject.get("status").getAsString();
            String statusVal = "";

            //date
            String dateTime = createdAt.get("date").getAsString();


            //card details
            String cardType = creditCard.get("cardType").getAsString();
            String cardLast4 = creditCard.get("last4").getAsString();


            if(status_code.equals("submitted_for_settlement"))
                statusVal = "Processing";
            else statusVal = status_code;

            //get timestamp

            long createdAtTimestamp = DateUtils.convertDateTimeToTimestamp(dateTime);

//            Log.d("TIMESTAMP", "createdAtTimestamp: "+createdAtTimestamp);


// Set the values from the transactionObject
            transactionDetails.setUserId(userId);
            transactionDetails.setAccountId(getIntent().getStringExtra("accountId"));
            transactionDetails.setTransactionType("Top up");
            transactionDetails.setTransactionId(transactionObject.get("id").getAsString());
            transactionDetails.setAmount(transactionObject.get("amount").getAsString());
            transactionDetails.setCurrencyIsoCode(transactionObject.get("currencyIsoCode").getAsString());
            transactionDetails.setStatusCode(transactionObject.get("status").getAsString());
            transactionDetails.setCreatedAtTimestamp(createdAtTimestamp);
            transactionDetails.setCardType(creditCard.get("cardType").getAsString());
            transactionDetails.setCardLast4(creditCard.get("last4").getAsString());


            CompletableFuture<Boolean> future = new CompletableFuture<>();
// Pass the transactionDetails object to the Firebase Realtime Database
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference transactionRef = database.getReference("transactions");
            // Set the value with a completion listener
            transactionRef.child(transactionDetails.getUserId()).child(transactionDetails.getTransactionId()).setValue(transactionDetails, (databaseError, databaseReference) -> {
                if (databaseError == null) {
                    // Write operation was successful
                    Log.i("TRANSACTION", "Record successful");
                    future.complete(true);
                } else {
                    // Write operation failed
                    // You can handle the failure or display an error message
                    Log.d("TRANSACTION", "Transaction Record failed: " + databaseError.getMessage());
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
            // This method runs on the main UI thread after doInBackground() completes
            // Perform any UI updates or post-execution tasks here
            transactionDialog.dismiss();
            if(!success){
                Toast.makeText(TopUpActivity.this,"An error occurred, try again.", Toast.LENGTH_LONG).show();
            }else{
                //update account
                new UpdateAccountTask().execute(transactionDetails.getAccountId(),transactionDetails.getAmount());
                // Call the showReceipt() method here
            }


        }
    }


    public void navigateToDashboardActivity(View view) {
        Intent intent = new Intent(this, Dashboard.class);
        startActivity(intent);
        finish();
    }
    public void showReceipt(JsonObject transactionObject){

        ProgressDialog dialog  = new ProgressDialog(TopUpActivity.this);
        dialog.setMessage("Finishing...");
        dialog.setCancelable(false);
        dialog.show();

        JsonObject creditCard = transactionObject.getAsJsonObject("creditCard");
        JsonObject createdAt = transactionObject.getAsJsonObject("createdAt");

        String transactionId = transactionObject.get("id").getAsString();
        String amount = transactionObject.get("amount").getAsString();
//        String merchantAccountId = transactionObject.get("merchantAccountId").getAsString();
        String currencyIsoCode = transactionObject.get("currencyIsoCode").getAsString();
        String status_code = transactionObject.get("status").getAsString();
        String statusVal = "";

        //date
        String dateTime = createdAt.get("date").getAsString();
        String formattedDate = DateUtils.formatDateTimeToWordDate(dateTime);


        //card details
        String cardType = creditCard.get("cardType").getAsString();
        String cardLast4 = creditCard.get("last4").getAsString();


        if(status_code.equals("submitted_for_settlement"))
            statusVal = "Paid";
        else statusVal = status_code;


        // Create an intent to start the target activity
        Intent intent = new Intent(this, TopUpReceipt.class);

// Put the data you want to pass as extras in the intent
        intent.putExtra("transactionId", transactionId);
        intent.putExtra("amount", amount);
        intent.putExtra("currencyIsoCode", currencyIsoCode);
        intent.putExtra("statusVal", statusVal);
        intent.putExtra("dateTime", formattedDate);
        intent.putExtra("paymentMethod", "Credit Card");
        intent.putExtra("cardType", cardType);
        intent.putExtra("cardLast4", cardLast4);

// Add additional putExtra statements as needed

// Start the target activity
        dialog.dismiss();
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        finish();
        startActivity(intent);
    }

    private class getToken extends AsyncTask{
        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(TopUpActivity.this, android.R.style.Theme_DeviceDefault_Dialog);
            dialog.setCancelable(false);
            dialog.setMessage("Please wait...");
            dialog.show();
        }

        @Override
        protected Object doInBackground(Object[] objects) {

            HttpClient client = new HttpClient();
            client.get(API_GET_TOKEN, new HttpResponseCallback() {
                @Override
                public void success(String responseBody) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            token = responseBody;
                            Log.i("ResponseBody",token.toString());
                        }
                    });
                }

                @Override
                public void failure(Exception exception) {
                    Log.d("EDMT_ERROR", exception.toString());
                }
            });

            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            dialog.dismiss();
        }
    }



    public void setupEditTexts(){
        topUpAmount = findViewById(R.id.topUpAmount);
        topUpAmountLayout=findViewById(R.id.topUpAmountLayout);
    }







}