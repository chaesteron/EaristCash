package com.example.it3cpartialappsgrp8;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class SendReceiptActivity extends AppCompatActivity {

    TextView receiverName, amount,transactionId,receiverId,sentStatus,transactionCreatedAt;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_receipt);

        setupTextViews();

        Intent intent = getIntent();

        if (intent != null) {
            Bundle extras = intent.getExtras();
            if (extras != null) {

                String formattedAmount = extras.getString("currencyIsoCode") + " "+extras.getString("amount");
                amount.setText(formattedAmount);
                sentStatus.setText(extras.getString("statusCode"));
                transactionId.setText(extras.getString("transactionId"));
                transactionCreatedAt.setText(extras.getString("dateTime"));
                receiverId.setText(extras.getString("receiverId"));

                //encrypt name
                String name = extras.getString("receiverName");
                StringBuilder encryptedName = new StringBuilder();

                String[] words = name.split("\\s+"); // Split the name into individual words

                for (String word : words) {
                    if (word.length() <= 2) {
                        // If the word has 2 or fewer characters, keep the word as is
                        encryptedName.append(word).append(" ");
                    } else {
                        // Replace the middle characters of the word with asterisks
                        encryptedName.append(word.charAt(0)); // Append the first character of the word
                        for (int i = 1; i < word.length() - 1; i++) {
                            encryptedName.append("*");
                        }
                        encryptedName.append(word.charAt(word.length() - 1)); // Append the last character of the word
                        encryptedName.append(" ");
                    }
                }
                receiverName.setText(encryptedName.toString());


            }
        }
    }
    public void navigateToDashboardActivity(View view) {
        Intent intent = new Intent(this, Dashboard.class);
        startActivity(intent);
    }
    public void setupTextViews(){
        receiverName = findViewById(R.id.receiverName);
        amount = findViewById(R.id.amount);
        transactionId = findViewById(R.id.transactionId);
        receiverId = findViewById(R.id.receiverId);
        sentStatus = findViewById(R.id.sentStatus);
        transactionCreatedAt = findViewById(R.id.transactionCreatedAt);

    }
}