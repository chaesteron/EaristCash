package com.example.it3cpartialappsgrp8;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPassword extends AppCompatActivity {

    private EditText enter_email;
    private Button reset_button;
    private ProgressBar progressBar;

    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        enter_email = findViewById(R.id.forgot_email);
        reset_button = findViewById(R.id.reset_password);
        progressBar = findViewById(R.id.progressBar);

        auth = FirebaseAuth.getInstance();

        reset_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPassword();
            }
        });
    }

    private void resetPassword() {
        String email = enter_email.getText().toString().trim();

        if (email.isEmpty()) {
            enter_email.setError("Email is required!");
            enter_email.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            enter_email.setError("Provide a valid email!");
            enter_email.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        auth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                progressBar.setVisibility(View.GONE);
                if (task.isSuccessful()) {
                    Toast.makeText(ForgotPassword.this, "Check your email to reset your password.", Toast.LENGTH_LONG).show();
                    navigateToLogin(); // Navigate to login after successful password reset
                } else {
                    Toast.makeText(ForgotPassword.this, "Try again! Something went wrong.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void navigateToLogin() {
        // Add the code here to navigate back to the login activity
        startActivity(new Intent(ForgotPassword.this, MainActivity.class));
        finish(); // Optional: Close the current activity to prevent navigating back to it using the back button
    }
}
