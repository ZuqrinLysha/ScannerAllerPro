package com.example.scannerallerpro;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LogIn extends AppCompatActivity {
    // Declare views
    EditText txtEmailL, txtPassword;
    TextView txtSignUp, txtForgotPassword;
    Button btnLogin;

    // Declare Firebase Auth instance
    FirebaseAuth auth;

    // Counter for login attempts
    private int loginAttempts = 0;
    private static final int MAX_ATTEMPTS = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        // Initialize views
        txtEmailL = findViewById(R.id.txtEmailL);
        txtPassword = findViewById(R.id.txtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        txtSignUp = findViewById(R.id.txtSignUp);
        txtForgotPassword = findViewById(R.id.txtForgotPassword);

        // Initialize Firebase Auth instance
        auth = FirebaseAuth.getInstance();

        // Set listener for the Log In button
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!validateEmail() || !validatePassword()) {
                    // Validation failed, do nothing
                    return;
                } else {
                    loginUser();
                }
            }
        });

        // Set listener for the Sign Up prompt text
        txtSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LogIn.this, SignUp.class);
                startActivity(intent);
            }
        });

        txtForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LogIn.this, ForgotPassword.class);
                startActivity(intent);

            }
        });


    }

    // Validate the email input field
    public Boolean validateEmail() {
        String val = txtEmailL.getText().toString();
        if (val.isEmpty()) {
            txtEmailL.setError("Email cannot be empty");
            return false;
        } else {
            txtEmailL.setError(null);
            return true;
        }
    }

    // Validate the password input field
    public Boolean validatePassword() {
        String val = txtPassword.getText().toString();
        if (val.isEmpty()) {
            txtPassword.setError("Password cannot be empty");
            return false;
        } else {
            txtPassword.setError(null);
            return true;
        }
    }

    // Log in the user using Firebase Authentication
    public void loginUser() {
        String userEmail = txtEmailL.getText().toString().trim();
        String userPassword = txtPassword.getText().toString().trim();

        auth.signInWithEmailAndPassword(userEmail, userPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Successful login
                    loginAttempts = 0;
                    Intent intent = new Intent(LogIn.this, HomePage.class); // Navigate to HomePage
                    startActivity(intent);
                    finish(); // Optional: finish the LogIn activity so the user cannot navigate back to it
                } else {
                    loginAttempts++;
                    txtPassword.setError("Invalid Credentials");
                    txtPassword.requestFocus();

                    if (loginAttempts > MAX_ATTEMPTS) {
                        // Navigate to ForgotPasswordActivity
                        Intent intent = new Intent(LogIn.this, ForgotPassword.class);
                        startActivity(intent);
                        finish(); // Optional: finish the LogIn activity so the user cannot navigate back to it
                    }
                }
            }
        });
    }
}
