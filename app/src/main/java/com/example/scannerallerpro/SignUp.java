package com.example.scannerallerpro;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class SignUp extends AppCompatActivity {

    EditText txtFullNameSignUp, txtEmailSignUp, txtUsernameSignUp, txtPasswordSignUp, txtConfirmPssSignUp, txtPhoneNumberSignUp;
    TextView txtSignUp;
    Button btnSignUp;
    ImageView imgTogglePassword, imgToggleConfirmPassword;
    FirebaseAuth auth;
    FirebaseDatabase database;
    DatabaseReference reference;
    boolean isPasswordVisible = false;
    boolean isConfirmPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initializing the views
        txtFullNameSignUp = findViewById(R.id.txtFullNameSignUp);
        txtEmailSignUp = findViewById(R.id.txtEmailSignUp);
        txtUsernameSignUp = findViewById(R.id.txtUsernameSignUp);
        txtPasswordSignUp = findViewById(R.id.txtPasswordSignUp);
        txtConfirmPssSignUp = findViewById(R.id.txtConfirmPssSignUp);
        txtPhoneNumberSignUp = findViewById(R.id.txtPhoneNumberSignUp);
        btnSignUp = findViewById(R.id.btnSignUp);
        txtSignUp = findViewById(R.id.txtSignUp);
        imgTogglePassword = findViewById(R.id.imgTogglePassword);
        imgToggleConfirmPassword = findViewById(R.id.imgToggleConfirmPassword);

        // Initialize Firebase Auth instance
        auth = FirebaseAuth.getInstance();

        // Setting the click listener for the Sign-Up button
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fullName = txtFullNameSignUp.getText().toString().trim();
                String email = txtEmailSignUp.getText().toString().trim();
                String username = txtUsernameSignUp.getText().toString().trim();
                String password = txtPasswordSignUp.getText().toString().trim();
                String confirmPassword = txtConfirmPssSignUp.getText().toString().trim();
                String phoneNumber = txtPhoneNumberSignUp.getText().toString().trim();

                // Validate input fields
                if (fullName.isEmpty() || email.isEmpty() || username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || phoneNumber.isEmpty()) {
                    Toast.makeText(SignUp.this, "All fields are required", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!password.equals(confirmPassword)) {
                    Toast.makeText(SignUp.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Create a new user with Firebase Authentication
                auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign-up successful
                                    FirebaseUser user = auth.getCurrentUser();
                                    database = FirebaseDatabase.getInstance();
                                    reference = database.getReference("Users");

                                    // Use a HashMap to store user data with unique keys for password and confirmPassword
                                    HashMap<String, Object> userData = new HashMap<>();
                                    userData.put("fullName", fullName);
                                    userData.put("email", email);
                                    userData.put("username", username);
                                    userData.put("phoneNumber", phoneNumber);

                                    // Use unique keys for storing password and confirm password
                                    HashMap<String, String> passwords = new HashMap<>();
                                    passwords.put("password", password);
                                    passwords.put("confirmPassword", confirmPassword);

                                    // Store the passwords under a separate node for security (optional)
                                    userData.put("passwords", passwords);

                                    // Save user info in the Realtime Database using user UID as the key
                                    reference.child(user.getUid()).setValue(userData)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Toast.makeText(SignUp.this, "User information saved!", Toast.LENGTH_SHORT).show();
                                                    } else {
                                                        Toast.makeText(SignUp.this, "Failed to save user information.", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });


                                    Toast.makeText(SignUp.this, "Sign-up successful!", Toast.LENGTH_SHORT).show();

                                    Intent intent = new Intent(SignUp.this, LogIn.class);
                                    startActivity(intent);
                                    finish(); // Optional: finish the SignUp activity so the user cannot navigate back to it
                                } else {
                                    // Sign-up failed
                                    String errorMessage = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                                    Toast.makeText(SignUp.this, "Sign-up failed: " + errorMessage, Toast.LENGTH_LONG).show();
                                }
                            }
                        });
            }
        });

        // Redirect to LogIn activity when "Sign In" text is clicked
        txtSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignUp.this, LogIn.class);
                startActivity(intent);
            }
        });

        // Setting up the eye toggle for password visibility
        imgTogglePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePasswordVisibility(txtPasswordSignUp, imgTogglePassword);
            }
        });

        // Setting up the eye toggle for confirm password visibility
        imgToggleConfirmPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePasswordVisibility(txtConfirmPssSignUp, imgToggleConfirmPassword);
            }
        });
    }

    private void togglePasswordVisibility(EditText editText, ImageView imageView) {
        if (isPasswordVisible) {
            // Hide the password
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            imageView.setImageResource(R.drawable.eyepassword); // Eye closed drawable
        } else {
            // Show the password
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            imageView.setImageResource(R.drawable.baseline_remove_red_eye_24); // Eye open drawable
        }
        isPasswordVisible = !isPasswordVisible;
        editText.setSelection(editText.getText().length()); // Keep cursor at the end
    }
}