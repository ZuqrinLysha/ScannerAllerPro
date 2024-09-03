package com.example.scannerallerpro;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUp extends AppCompatActivity {

    EditText txtFullNameSignUp, txtEmailSignUp, txtUsernameSignUp, txtPasswordSignUp, txtConfirmPssSignUp;
    TextView txtSignUp;
    Button btnSignUp;
    FirebaseDatabase database;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Make sure this matches your layout file

        // Initializing the views
        txtFullNameSignUp = findViewById(R.id.txtFullNameSignUp);
        txtEmailSignUp = findViewById(R.id.txtEmailSignUp);
        txtUsernameSignUp = findViewById(R.id.txtUsernameSignUp);
        txtPasswordSignUp = findViewById(R.id.txtPasswordSignUp);
        txtConfirmPssSignUp = findViewById(R.id.txtConfirmPssSignUp);
        btnSignUp = findViewById(R.id.btnSignUp);
        txtSignUp = findViewById(R.id.txtSignUp); // Initialize txtSignUp

        // Setting the click listener for the Sign-Up button
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Firebase Database instance
                database = FirebaseDatabase.getInstance();
                reference = database.getReference("Users");

                // Retrieving user input
                String fullname = txtFullNameSignUp.getText().toString();
                String email = txtEmailSignUp.getText().toString();
                String username = txtUsernameSignUp.getText().toString();
                String password = txtPasswordSignUp.getText().toString();
                String confirmPassword = txtConfirmPssSignUp.getText().toString();


                // Check if passwords match
                if (!password.equals(confirmPassword)) {
                    Toast.makeText(SignUp.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Creating a helper class object to store the data
                HelperClass helperClass = new HelperClass(
                        fullname,
                        email,
                        username,
                        password,
                        confirmPassword,
                        "", // Initial value for height
                        "", // Initial value for weight
                        "", // Initial value for BMI
                        "", // Initial value for blood type
                        ""  // Initial value for allergic history
                );

                reference.child(username).setValue(helperClass);

                // Displaying a success message
                Toast.makeText(SignUp.this, "Sign-up successful!", Toast.LENGTH_SHORT).show();

                // Redirecting to the LogIn activity
                Intent intent = new Intent(SignUp.this, LogIn.class);
                startActivity(intent);
            }
        });

        // Setting the click listener for the Sign-Up prompt text
        txtSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Redirecting to the LogIn activity
                Intent intent = new Intent(SignUp.this, LogIn.class);
                startActivity(intent);
            }
        });
    }
}
