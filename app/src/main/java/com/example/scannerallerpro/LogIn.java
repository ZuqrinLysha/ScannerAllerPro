package com.example.scannerallerpro;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LogIn extends AppCompatActivity {
    // Declare views and Firebase references
    EditText txtEmailL, txtPassword;
    TextView txtSignUp, txtForgotPassword;
    Button btnLogin;
    ImageView imgTogglePasswordLogin;
    boolean isPasswordVisible = false;

    // Flag to track if the password has been updated
    private boolean passwordUpdated = false;

    FirebaseAuth auth;
    FirebaseDatabase database;
    DatabaseReference reference;

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
        imgTogglePasswordLogin = findViewById(R.id.imgTogglePasswordLogin);

        // Initialize Firebase Auth and Database references
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("Users");

        // Set listener for the Log In button
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validateEmail() && validatePassword()) {
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

        // Set listener for the Forgot Password prompt text
        txtForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LogIn.this, ForgotPassword.class);
                startActivity(intent);
            }
        });

        // Setting up the eye toggle for password visibility
        imgTogglePasswordLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPasswordVisible) {
                    txtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    txtPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    imgTogglePasswordLogin.setImageResource(R.drawable.eyepassword); // Eye closed drawable
                } else {
                    txtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    txtPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    imgTogglePasswordLogin.setImageResource(R.drawable.baseline_remove_red_eye_24); // Eye open drawable
                }
                isPasswordVisible = !isPasswordVisible;
                txtPassword.setSelection(txtPassword.getText().length()); // Keep cursor at the end
            }
        });
    }

    // Validate the email input field
    public Boolean validateEmail() {
        String val = txtEmailL.getText().toString().trim();
        if (val.isEmpty()) {
            txtEmailL.setError("Email cannot be empty");
            return false;
        } else if (!val.contains("@")) {
            txtEmailL.setError("Invalid email format");
            return false;
        } else {
            txtEmailL.setError(null);
            return true;
        }
    }

    // Validate the password input field
    public Boolean validatePassword() {
        String val = txtPassword.getText().toString().trim();
        if (val.isEmpty()) {
            txtPassword.setError("Password cannot be empty");
            return false;
        } else if (val.length() < 6) {
            txtPassword.setError("Password must be at least 6 characters");
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
                    FirebaseUser user = auth.getCurrentUser();

                    if (user != null) {
                        // Successful login, reset login attempts
                        loginAttempts = 0;

                        // Check if the password is already up-to-date
                        if (!passwordUpdated) {
                            // Update password in Firebase Authentication
                            updatePassword(userPassword);

                            // Update password in Firebase Realtime Database
                            updateUserPasswordInDatabase(userEmail, userPassword);

                            // Set the flag to true to indicate password has been updated
                            passwordUpdated = true;

                        }
                            // Navigate to HomePage after login
                            Intent intent = new Intent(LogIn.this, HomePage.class);
                            startActivity(intent);
                            finish(); // Optional: finish the LogIn activity so the user cannot navigate back to it


                    }
                } else {
                    loginAttempts++;
                    String errorMessage = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                    Toast.makeText(LogIn.this, "Login failed: " + errorMessage, Toast.LENGTH_LONG).show();
                    txtPassword.setError("Invalid Credentials");
                    txtPassword.requestFocus();

                    if (loginAttempts >= MAX_ATTEMPTS) {
                        // Navigate to ForgotPasswordActivity
                        Intent intent = new Intent(LogIn.this, ForgotPassword.class);
                        startActivity(intent);
                        finish(); // Optional: finish the LogIn activity so the user cannot navigate back to it
                    }
                }
            }
        });
    }

    // Update the password in Firebase Authentication
    private void updatePassword(String newPassword) {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            user.updatePassword(newPassword).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                }
            });
        }
    }

    private void updateUserPasswordInDatabase(String email, String newPassword) {
        reference.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String userId = snapshot.getKey(); // Get the user ID
                    if (userId != null) {
                        reference.child(userId).child("Password Updated").setValue(newPassword)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            // Show the Toast message only if the password was updated
                                            Toast.makeText(LogIn.this, "Password updated in database", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(LogIn.this, "Failed to query database", Toast.LENGTH_SHORT).show();
            }
        });
    }
    }