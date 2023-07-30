package com.example.it3cpartialappsgrp8;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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

public class ChangePassword extends AppCompatActivity {

    private EditText enter_email;
    private Button verify, back;
    private ProgressBar progressBar;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        enter_email = findViewById(R.id.enter_email);
        verify = findViewById(R.id.verifybtn);
        progressBar = findViewById(R.id.progressBar2);
        auth = FirebaseAuth.getInstance();
        back = findViewById(R.id.back_button);

        verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verify();
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void verify() {
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
                    Toast.makeText(ChangePassword.this, "Check your email to reset your password.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(ChangePassword.this, "Try again! Something went wrong.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
