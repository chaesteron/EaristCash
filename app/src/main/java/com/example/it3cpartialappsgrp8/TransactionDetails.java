package com.example.it3cpartialappsgrp8;

import java.util.Map;

public class TransactionDetails {
    private String transactionId;
    private String amount;
    private Object  SERVER_TIMESTAMP;
    private String currencyIsoCode;
    private String statusCode;
    private long createdAtTimestamp;
    private String cardType;
    private String cardLast4;

    public String getReceiverAccountId() {
        return receiverAccountId;
    }

    public void setReceiverAccountId(String receiverAccountId) {
        this.receiverAccountId = receiverAccountId;
    }

    private String receiverAccountId;

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    private String transactionType;
    private String accountId;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    private String userId;


    public TransactionDetails() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public TransactionDetails(String transactionId, String amount, String currencyIsoCode, String statusCode, long createdAtTimestamp, Object  SERVER_TIMESTAMP, String cardType, String cardLast4,String transactionType,String accountId,String userId, String receiverAccountId) {
        this.transactionId = transactionId;
        this.amount = amount;
        this.currencyIsoCode = currencyIsoCode;
        this.statusCode = statusCode;
        this.createdAtTimestamp = createdAtTimestamp;
        this.cardType = cardType;
        this.cardLast4 = cardLast4;
        this.accountId = accountId;
        this.transactionType = transactionType;
        this.userId = userId;
        this.receiverAccountId = receiverAccountId;
        this.SERVER_TIMESTAMP = SERVER_TIMESTAMP;
    }
    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getCurrencyIsoCode() {
        return currencyIsoCode;
    }

    public void setCurrencyIsoCode(String currencyIsoCode) {
        this.currencyIsoCode = currencyIsoCode;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public long getCreatedAtTimestamp() {
        return createdAtTimestamp;
    }

    public void setCreatedAtTimestamp(long createdAtTimestamp) {
        this.createdAtTimestamp = createdAtTimestamp;
    }

    public String getCardType() {
        return cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public String getCardLast4() {
        return cardLast4;
    }

    public void setCardLast4(String cardLast4) {
        this.cardLast4 = cardLast4;
    }


    public Object  getSERVER_TIMESTAMP() {
        return SERVER_TIMESTAMP;
    }

    public void setSERVER_TIMESTAMP(Object  SERVER_TIMESTAMP) {
        this.SERVER_TIMESTAMP = SERVER_TIMESTAMP;
    }
}
