package com.example.it3cpartialappsgrp8;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;


public class RegisterUser extends AppCompatActivity {

    private EditText nameEditText, studentIDEditText, emailEditText, passwordEditText, phoneNumberEditText;
    private Button registerButton;
    private TextView termsTextView;
    private CheckBox checkBox;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    boolean isThreadFinished = false;
    int uniqueNumber = 0;
String uniqueNumberString;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);

        nameEditText = findViewById(R.id.register_name);
        studentIDEditText = findViewById(R.id.register_studentID);
        emailEditText = findViewById(R.id.register_email);
        passwordEditText = findViewById(R.id.register_password);
        phoneNumberEditText = findViewById(R.id.register_phone);
        registerButton = findViewById(R.id.register_user);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        termsTextView = findViewById(R.id.termsTextView);

        // Set the text and make it clickable
        SpannableString spannableString = new SpannableString(termsTextView.getText());

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View view) {
                // Handle the click action, for example, open a link to the Terms and Conditions page
            showTermsAndAgreementDialog();
            }
        };
        spannableString.setSpan(clickableSpan, 9, 28, 0);
        termsTextView.setText(spannableString);
        termsTextView.setMovementMethod(LinkMovementMethod.getInstance());
        termsTextView.setLinkTextColor(Color.BLUE);


        checkBox = findViewById(R.id.termsAndAgreementCheckBox);
        boolean isChecked = checkBox.isChecked();
        registerButton.setEnabled(isChecked);

        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Handle the CheckBox click action here
                boolean isChecked = checkBox.isChecked();
                registerButton.setEnabled(isChecked);
            }
        });
    }

    private void registerUser() {
        ProgressDialog dialog;
        dialog = new ProgressDialog(RegisterUser.this, android.R.style.Theme_DeviceDefault_Dialog);
        dialog.setCancelable(false);
        dialog.setMessage("Please wait...");
        dialog.show();
        String name = nameEditText.getText().toString().trim();
        String studentID = studentIDEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String phoneNumber = phoneNumberEditText.getText().toString().trim();

        // Validate input fields
        if (name.isEmpty() || studentID.isEmpty() || email.isEmpty() || password.isEmpty() || phoneNumber.isEmpty()) {
            Toast.makeText(RegisterUser.this, "All fields are required", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            return;
        }

        // Register the user with Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            if (firebaseUser != null) {
                                String userId = firebaseUser.getUid();
                                // Create an instance of the Random class
                                Random random = new Random();

                                // Generate a 3-digit random number
                                     int randomNumber = random.nextInt(900) + 100; // 100 to 999
                                    // Save user details to the database
                                    User user = new User(userId, name, studentID, email, password, phoneNumber, 0.0, studentID+randomNumber);
                                    mDatabase.child("users").child(userId).setValue(user);


                                    Toast.makeText(RegisterUser.this, "User registered successfully", Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                    finish(); // Optional: Finish the activity after successful registration


                            }
                        } else {
                            dialog.dismiss();
                            // Handle registration failure
                            if (task.getException() instanceof FirebaseAuthException) {
                                String errorCode = ((FirebaseAuthException) task.getException()).getErrorCode();
                                // You can handle specific error codes here if needed
                                Toast.makeText(RegisterUser.this, "Registration failed: " + errorCode, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(RegisterUser.this, "Registration failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    private void showTermsAndAgreementDialog() {
        // Read the HTML content from the file in assets folder
        String htmlContent = readHtmlFileFromAssets("terms-and-agreement.html");

        // Create a WebView to display the HTML content
        WebView webView = new WebView(this);
        webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null);
        webView.setWebViewClient(new WebViewClient());

        // Create an AlertDialog and set the WebView as its view
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Terms and Agreement");
        builder.setView(webView);
        builder.setPositiveButton("Close", (dialog, which) -> {
            // Handle the "Agree" button click if needed
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private String readHtmlFileFromAssets(String fileName) {
        try {
            InputStream inputStream = getAssets().open(fileName);
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            return new String(buffer);
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }


}
