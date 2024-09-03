package com.example.scannerallerpro;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Profile extends AppCompatActivity {

    private FirebaseDatabase database;
    private DatabaseReference userRef;
    private FirebaseAuth auth;
    DatabaseReference reference;

    // TextViews for displaying user data
    TextView txtHeight, txtWeight, txtBmi, txtBlood;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize Firebase
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("MedicalData");

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            userRef = database.getReference("MedicalData").child(currentUser.getUid());
        }

        // Initialize the TextViews
        txtHeight = findViewById(R.id.txtHeight);
        txtWeight = findViewById(R.id.txtWeight);
        txtBmi = findViewById(R.id.txtBMI);
        txtBlood = findViewById(R.id.txtBlood);

        // Initialize CardViews and set click listeners
        CardView cvHeight = findViewById(R.id.cvHeight);
        CardView cvWeight = findViewById(R.id.cvWeight);
        CardView cvBmi = findViewById(R.id.cvBMI);
        CardView cvBloodType = findViewById(R.id.cvBloodType);

        cvHeight.setOnClickListener(v -> showInputDialog("Enter Your Height", "height"));
        cvWeight.setOnClickListener(v -> showInputDialog("Enter Your Weight", "weight"));
        cvBmi.setOnClickListener(v -> showInputDialog("Enter Your BMI", "BMI"));
        cvBloodType.setOnClickListener(v -> showInputDialog("Enter Your Blood Type", "Blood Type"));

        // Load existing data from Firebase
        loadDataFromFirebase();
    }

    private void showInputDialog(String title, String field) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);

        // Set up the input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);  // For text input
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", (dialog, which) -> {
            String value = input.getText().toString().trim();
            if (!value.isEmpty()) {
                saveToFirebase(field, value);
            } else {
                Toast.makeText(Profile.this, title + " cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void saveToFirebase(String field, String value) {
        if (userRef != null) {
            userRef.child(field).setValue(value).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(Profile.this, field + " saved successfully!", Toast.LENGTH_SHORT).show();
                    updateTextView(field, value);  // Update TextView
                } else {
                    Toast.makeText(Profile.this, "Failed to save " + field, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void updateTextView(String field, String value) {
        switch (field) {
            case "height":
                txtHeight.setText("" + value);
                break;
            case "weight":
                txtWeight.setText("" + value);
                break;
            case "bmi":
                txtBmi.setText("" + value);
                break;
            case "bloodType":
                txtBlood.setText("" + value);
                break;
        }
    }

    private void loadDataFromFirebase() {
        if (userRef != null) {
            userRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult().exists()) {
                    DataSnapshot dataSnapshot = task.getResult();
                    if (dataSnapshot.hasChild("height")) {
                        String height = dataSnapshot.child("height").getValue(String.class);
                        txtHeight.setText("Height: " + height);
                    }
                    if (dataSnapshot.hasChild("weight")) {
                        String weight = dataSnapshot.child("weight").getValue(String.class);
                        txtWeight.setText("Weight: " + weight);
                    }
                    if (dataSnapshot.hasChild("bmi")) {
                        String bmi = dataSnapshot.child("bmi").getValue(String.class);
                        txtBmi.setText("BMI: " + bmi);
                    }
                    if (dataSnapshot.hasChild("bloodType")) {
                        String bloodType = dataSnapshot.child("bloodType").getValue(String.class);
                        txtBlood.setText("Blood Type: " + bloodType);
                    }
                }
            });
        }
    }
}
