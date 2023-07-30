package com.example.it3cpartialappsgrp8;


public class User {
    private String userId;
    private String name;
    private String studentID;
    private String email;
    private String password;
    private String phoneNumber;

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    private String accountId;
    private Double balance;


    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String userId, String name, String studentID, String email, String password, String phoneNumber, Double balance,String accountId) {
        this.userId = userId;
        this.name = name;
        this.studentID = studentID;
        this.email = email;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.balance = balance;
        this.accountId = accountId;

    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setStudentID(String studentID) {
        this.studentID = studentID;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getStudentID() {
        return studentID;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public Double getBalance() {
        return balance;
    }
}
