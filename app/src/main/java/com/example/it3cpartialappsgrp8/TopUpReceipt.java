package com.example.it3cpartialappsgrp8;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class TopUpReceipt extends AppCompatActivity {
    TextView merchantName, amount,transactionId,paymentMethod,paymentStatus,transactionCreatedAt;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topupreceipt);

        setupTextViews();

        Intent intent = getIntent();

        if (intent != null) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                merchantName.setText("EARISTCash");
                String formattedAmount = extras.getString("currencyIsoCode") + " "+extras.getString("amount");
                amount.setText(formattedAmount);
                String formattedPaymentMethod = extras.getString("paymentMethod") + "\n" + extras.getString("cardType") + " | Ending with " +extras.getString("cardLast4");
                paymentMethod.setText(formattedPaymentMethod);
                paymentStatus.setText(extras.getString("statusVal"));
                transactionId.setText(extras.getString("transactionId"));
                transactionCreatedAt.setText(extras.getString("dateTime"));
            }
        }
    }
    public void navigateToDashboardActivity(View view) {
        Intent intent = new Intent(this, Dashboard.class);
        startActivity(intent);
    }
    public void setupTextViews(){
        merchantName = findViewById(R.id.merchantName);
        amount = findViewById(R.id.amount);
        transactionId = findViewById(R.id.transactionId);
        paymentMethod = findViewById(R.id.paymentMethod);
        paymentStatus = findViewById(R.id.paymentStatus);
        transactionCreatedAt = findViewById(R.id.transactionCreatedAt);

    }
}