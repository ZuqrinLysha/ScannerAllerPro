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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class SignUp extends AppCompatActivity {

    EditText txtFullNameSignUp, txtEmailSignUp, txtPasswordSignUp, txtConfirmPssSignUp, txtPhoneNumberSignUp;
    TextView txtSignUp;
    Button btnSignUp;
    ImageView imgTogglePassword, imgToggleConfirmPassword;
    FirebaseAuth auth;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initializing the views
        txtFullNameSignUp = findViewById(R.id.txtFullNameSignUp);
        txtEmailSignUp = findViewById(R.id.txtEmailSignUp);
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
                String password = txtPasswordSignUp.getText().toString().trim();
                String confirmPassword = txtConfirmPssSignUp.getText().toString().trim();
                String phoneNumber = txtPhoneNumberSignUp.getText().toString().trim();

                // Validate input fields
                if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || phoneNumber.isEmpty()) {
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
                                    String userEmail = user.getEmail();
                                    DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");

                                    // Query the database for the user based on their email
                                    Query query = usersRef.orderByChild("email").equalTo(userEmail);
                                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.exists()) {
                                                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                                                    // Retrieve the full name of the user
                                                    String fullName = txtFullNameSignUp.getText().toString().trim(); // Use the input fullName directly
                                                    if (fullName != null) {
                                                        // Save user info in the Realtime Database using user UID as the key
                                                        reference = usersRef.child(user.getUid());

                                                        HashMap<String, Object> userData = new HashMap<>();
                                                        userData.put("fullName", fullName);
                                                        userData.put("email", userEmail);
                                                        userData.put("phoneNumber", phoneNumber);

                                                        reference.setValue(userData)
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
                                                    }
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                            Toast.makeText(SignUp.this, "Failed to fetch user data.", Toast.LENGTH_SHORT).show();
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
        if (editText.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
            // Show the password
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            imageView.setImageResource(R.drawable.baseline_remove_red_eye_24); // Eye open drawable
        } else {
            // Hide the password
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            imageView.setImageResource(R.drawable.eyepassword); // Eye closed drawable
        }
        editText.setSelection(editText.getText().length()); // Keep cursor at the end
    }
}
