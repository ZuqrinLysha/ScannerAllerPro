package com.example.scannerallerpro;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Profile extends AppCompatActivity {

    private FirebaseDatabase database;
    private DatabaseReference userRef;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the content view if this activity has its own layout
        // setContentView(R.layout.activity_profile);

        // Initialize Firebase components
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        // Get current user
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            // Reference to user's medical data
            userRef = database.getReference("MedicalData").child(currentUser.getUid());

            // Load user data
            loadUserData();
        } else {
            Toast.makeText(Profile.this, "No user signed in", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadUserData() {
        if (userRef != null) {
            userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // Handle the data snapshot, e.g., update UI or process data
                        // For example:
                        String fullName = dataSnapshot.child("fullName").getValue(String.class);
                        String email = dataSnapshot.child("email").getValue(String.class);
                        // ... retrieve other fields as needed

                        // Example of updating UI or processing data
                        Toast.makeText(Profile.this, "User data loaded", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(Profile.this, "No data available", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle errors
                    Toast.makeText(Profile.this, "Failed to load data: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}
