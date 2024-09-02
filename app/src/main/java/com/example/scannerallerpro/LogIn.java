package com.example.scannerallerpro;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class LogIn extends AppCompatActivity {
    //Declare views
    EditText txtEmailL, txtPassword;
    TextView txtSignUp, txtForgotPassword;
    Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        // Initialize views
        txtEmailL = findViewById(R.id.txtEmailL);
        txtPassword = findViewById(R.id.txtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        txtSignUp = findViewById(R.id.txtSignUp);

        // Set listener for the Log In button
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!validateEmail() | !validatePassword()) {
                    // Validation failed, do nothing
                    return;
                } else {
                    checkUser();
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
    }

    //Validate the email input field
    public Boolean validateEmail() {
        String val = txtEmailL.getText().toString();
        if (val.isEmpty()) {
            //Show an error if the email field is empty
            txtEmailL.setError("Email cannot be empty");
            return false;
        } else {
            // Clear any error if the email is invalid
            txtEmailL.setError(null);
            return true;
        }
    }

    //Validate the password input field
    public Boolean validatePassword() {
        String val = txtPassword.getText().toString();
        if (val.isEmpty()) {
            //Show an error if the password field is empty
            txtPassword.setError("Password cannot be empty");
            return false;
        } else {
            // Clear any error if the password is valid
            txtPassword.setError(null);
            return true;
        }
    }

    //Check if the user exists in the database
    public void checkUser() {
        // Get user input such as email and password
        String userEmail = txtEmailL.getText().toString().trim();
        String userPassword = txtPassword.getText().toString().trim();

        // Firebase Database instance
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        Query checkUserDatabase = reference.orderByChild("email").equalTo(userEmail);

        // Check if the user exists in the database
        checkUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            // If the user exists, check the password
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        // Get the password from the database
                        String passwordFromDB = userSnapshot.child("password").getValue(String.class);

                        // Check if the password matches the user's input
                        if (Objects.equals(passwordFromDB, userPassword)) {
                            Intent intent = new Intent(LogIn.this, HomePage.class); // Navigate to HomePage
                            startActivity(intent);
                            finish();
                            // Exit the method after successful login
                        } else {
                            // Show an error if the password is invalid
                            txtPassword.setError("Invalid Credentials");
                            txtPassword.requestFocus();
                        }
                        // Exit the loop and method after checking credentials
                        return;
                    }
                } else {
                    // Show an error if the user does not exist
                    txtEmailL.setError("User does not exist");
                    txtEmailL.requestFocus();
                }
            }

            @Override
            // Handle error
            public void onCancelled(DatabaseError error) {
            }
        });
    }

}

