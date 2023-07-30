package com.example.it3cpartialappsgrp8;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView register, forgotPass;
    private EditText emailEditText, passwordEditText, editAmount;
    private Button signIn, btnPayment;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;



    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        register = findViewById(R.id.login_register);
        register.setOnClickListener(this);
        signIn = findViewById(R.id.login_btn);
        signIn.setOnClickListener(this);
        forgotPass = findViewById(R.id.login_forgot);
        forgotPass.setOnClickListener(this);
        emailEditText = findViewById(R.id.login_email);
        passwordEditText = findViewById(R.id.login_password);
        mAuth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.progressBar);



    }



    public void onClick(View v) {
        if (v.getId() == R.id.login_register) {
            startActivity(new Intent(MainActivity.this, RegisterUser.class));
        } else if (v.getId() == R.id.login_btn) {
            userLogin();
        } else if (v.getId() == R.id.login_forgot) {
            startActivity(new Intent(MainActivity.this, ForgotPassword.class));
        }
    }

    private void userLogin() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty()) {
            emailEditText.setError("Email is required!");
            emailEditText.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Please enter a valid email!");
            emailEditText.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            passwordEditText.setError("Password is required");
            passwordEditText.requestFocus();
            return;
        }

        if (password.length() < 6) {
            passwordEditText.setError("Min password length is 6 characters!");
            passwordEditText.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                progressBar.setVisibility(View.GONE);

                if (task.isSuccessful()) {

                    if(FirebaseAuth.getInstance().getCurrentUser()!=null){
                        startActivity(new Intent(MainActivity.this, Dashboard.class));
                        finish(); // Optional, if you want to close the current activity after login

                    }else{
                        Toast.makeText(MainActivity.this, "Failed to login. Check Internet Connection", Toast.LENGTH_LONG).show();
                    }

                } else {
                    Toast.makeText(MainActivity.this, "Failed to login. Please check your credentials", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
