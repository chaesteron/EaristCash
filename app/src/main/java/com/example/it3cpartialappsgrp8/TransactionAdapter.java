package com.example.it3cpartialappsgrp8;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private Context context;
    private List<TransactionDetails> transactionList;

    private AdapterView.OnItemClickListener itemClickListener;

    public TransactionAdapter(Context context, List<TransactionDetails> transactionList) {
        this.context = context;
        this.transactionList = transactionList;
    }


    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.transaction_item, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        TransactionDetails transaction = transactionList.get(position);
        String amount = transaction.getAmount();
        String formattedAmount = "";
        String transactionType = transaction.getTransactionType();
        Log.d("transaction.getCreatedAtTimestamp()", "transaction.getCreatedAtTimestamp(): "+transaction.getCreatedAtTimestamp());
        String dateTime = DateUtils.convertTimestampToDateTime(transaction.getCreatedAtTimestamp());
        String formattedDate = DateUtils.formatDateTimeToWordDate(dateTime);
        switch (transactionType) {
            case "Send":
                formattedAmount = "-"+amount;
                break;

            case "Top up":

            case "Receive":
                formattedAmount = "+"+amount;
                break;
            // Add more cases for other transaction types if needed

            default:
                break;
        }
        holder.transactionTypeTextView.setText(transactionType);
        holder.amountTextView.setText(formattedAmount);
        holder.dateTextView.setText(formattedDate);

        // Set click listener for the item view
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (itemClickListener != null) {
                    itemClickListener.onItemClick(null, view, position, holder.getItemId());
                }
            }
        });
    }



    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        public TextView transactionTypeTextView, amountTextView, dateTextView;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            transactionTypeTextView = itemView.findViewById(R.id.transactionTypeTextView);
            amountTextView = itemView.findViewById(R.id.amountTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
        }
    }
    // Define an interface for the item click listener
    public interface OnItemClickListener {
        void onItemClick(TransactionDetails transaction);
    }
    public void setOnItemClickListener(AdapterView.OnItemClickListener listener) {
        this.itemClickListener = listener;
    }
}

