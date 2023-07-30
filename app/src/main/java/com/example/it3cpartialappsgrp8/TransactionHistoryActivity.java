package com.example.it3cpartialappsgrp8;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class TransactionHistoryActivity extends AppCompatActivity implements AdapterView.OnItemClickListener  {

    private RecyclerView recyclerView;
    private TransactionAdapter transactionAdapter;
    private List<TransactionDetails> transactionList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_history);



        recyclerView = findViewById(R.id.transactionRecyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        transactionList = new ArrayList<>();
        transactionAdapter = new TransactionAdapter(this, transactionList);
        recyclerView.setAdapter(transactionAdapter);

        transactionAdapter.setOnItemClickListener(this);
        loadTransactionHistory();
    }

    private void loadTransactionHistory() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference transactionRef = FirebaseDatabase.getInstance().getReference("transactions");

        Query query = transactionRef.child(userId).orderByChild("createdAtTimestamp").limitToLast(25);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                transactionList.clear();
                for (DataSnapshot transactionSnapshot : dataSnapshot.getChildren()) {
                    TransactionDetails transaction = transactionSnapshot.getValue(TransactionDetails.class);
                    // Add the transaction to the beginning of the list
                    transactionList.add(0,transaction);
                }
                transactionAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle any errors
            }
        });
    }
    // Implement the onItemClick method of the AdapterView.OnItemClickListener interface
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        TransactionDetails transaction = transactionList.get(position);
        // Show a dialog with transaction details
        showDialogWithTransactionDetails(transaction);
    }
    private void showDialogWithTransactionDetails(TransactionDetails transaction) {
        // Create and show a dialog with the transaction details


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        AlertDialog.Builder builder2 = new AlertDialog.Builder(this);

        // Use HTML formatting to set the title color

        String dateTime = DateUtils.formatDateTimeToWordDate(DateUtils.convertTimestampToDateTime(transaction.getCreatedAtTimestamp()));


            //        for receive transactions
            // Inflate the custom layout for the AlertDialog
            View customLayout = getLayoutInflater().inflate(R.layout.custom_transaction_dialog, null);

            // Find the UI elements in the custom layout and set their text with transaction details
            TextView descriptionTextView = customLayout.findViewById(R.id.descriptionTextView);
            String description = getDescription(transaction.getTransactionType(),transaction.getAccountId(),transaction.getReceiverAccountId());
            descriptionTextView.setText(description);

            TextView dateTimeTextView = customLayout.findViewById(R.id.dateTimeTextView);
            dateTimeTextView.setText(dateTime);

            TextView amountTextView = customLayout.findViewById(R.id.amountTextView);
            amountTextView.setText(formatAmount(transaction.getTransactionType(),transaction.getAmount()));

            TextView transactionIdTextView = customLayout.findViewById(R.id.transactionIdTextView);
            transactionIdTextView.setText(transaction.getTransactionId());

            builder.setView(customLayout)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .show();



    }
    public String formatAmount(String transactionType, String amount){
        String formattedAmount = "";
        switch (transactionType) {
            case "Send":
                formattedAmount= "-"+amount;
                break;

            case "Top up":
            case "Receive":
                formattedAmount= "+"+amount;
                break;
            // Add more cases for other transaction types if needed
            default:
                break;
        }
        return formattedAmount;
    }
    public String getDescription(String transactionType,String accountId ,String receiverId){
        String desc = "";
        switch (transactionType) {
            case "Send":
            case "Receive":
                desc= "Transfer from "+accountId+" to "+receiverId;
                break;

            case "Top up":
                desc= "Top up for "+accountId;
                break;

            // Add more cases for other transaction types if needed
            default:
                break;
        }
        return desc;
    }

}
