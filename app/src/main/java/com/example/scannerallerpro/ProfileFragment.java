package com.example.scannerallerpro;

import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileFragment extends Fragment {

    private FirebaseDatabase database;
    private DatabaseReference userRef;
    private FirebaseAuth auth;
    private Switch switchDarkMode;

    // TextViews for displaying user data
    private TextView displayWeight, displayHeight, displayBmi, displayBloodType;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize Firebase
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        // Set up UI elements
        switchDarkMode = rootView.findViewById(R.id.switch_dark_mode);
        displayHeight = rootView.findViewById(R.id.display_height);
        displayWeight = rootView.findViewById(R.id.display_weight);
        displayBmi = rootView.findViewById(R.id.display_bmi);
        displayBloodType = rootView.findViewById(R.id.display_blood_type);

        // Initialize CardViews and set click listeners
        setupCardViewListeners(rootView);

        // Load saved dark mode preference
        loadDarkModePreference();

        // Access activity toolbar and drawer layout
        setupToolbarAndDrawer();

        // Load user data from Firebase if user is logged in
        if (currentUser != null) {
            loadUserData(currentUser.getEmail());
        }

        return rootView;
    }

    private void setupCardViewListeners(View rootView) {
        CardView cvHeight = rootView.findViewById(R.id.cvHeight);
        CardView cvWeight = rootView.findViewById(R.id.cvWeight);
        CardView cvBmi = rootView.findViewById(R.id.cvBmi);
        CardView cvBloodType = rootView.findViewById(R.id.cvBloodType);
        CardView cvChangePhone = rootView.findViewById(R.id.cvChangePhone);
        CardView cvChangePassword = rootView.findViewById(R.id.cvChangePassword);
        CardView cvDeleteAccount = rootView.findViewById(R.id.cvDeleteAccount);

        cvHeight.setOnClickListener(v -> showInputDialog("Enter Your Height", "height"));
        cvWeight.setOnClickListener(v -> showInputDialog("Enter Your Weight", "weight"));
        cvBloodType.setOnClickListener(v -> showInputDialog("Enter Your Blood Type", "bloodType"));
        cvChangePhone.setOnClickListener(v -> showInputDialog("Enter New Phone Number", "phone"));
        cvChangePassword.setOnClickListener(v -> showInputDialog("Enter New Password", "password"));
        cvDeleteAccount.setOnClickListener(v -> confirmDeleteAccount());
    }

    private void loadDarkModePreference() {
        boolean isDarkModeEnabled = getActivity().getSharedPreferences("Preferences", Context.MODE_PRIVATE)
                .getBoolean("dark_mode", false);
        switchDarkMode.setChecked(isDarkModeEnabled);

        // Set listener for the switch
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Save preference
            getActivity().getSharedPreferences("Preferences", Context.MODE_PRIVATE)
                    .edit()
                    .putBoolean("dark_mode", isChecked)
                    .apply();

            // Toggle the theme
            AppCompatDelegate.setDefaultNightMode(isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        });
    }

    private void setupToolbarAndDrawer() {
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            Toolbar toolbar = activity.findViewById(R.id.toolbar);
            activity.setSupportActionBar(toolbar);

            DrawerLayout drawerLayout = activity.findViewById(R.id.drawerLayout);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    activity, drawerLayout, toolbar, R.string.open_drawer, R.string.close_drawer);
            drawerLayout.addDrawerListener(toggle);
            toggle.syncState();
        }
    }

    private void loadUserData(String userEmail) {
        DatabaseReference usersRef = database.getReference("Users");
        usersRef.orderByChild("email").equalTo(userEmail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        String fullName = userSnapshot.child("fullName").getValue(String.class);
                        if (fullName != null) {
                            userRef = database.getReference("Users").child(fullName);
                            loadDataFromFirebase();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to fetch username", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showInputDialog(String title, String field) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(title);

        // Set up the input
        final EditText input = new EditText(getContext());
        input.setInputType(field.equals("phone") ? InputType.TYPE_CLASS_PHONE : InputType.TYPE_CLASS_TEXT);  // Phone input for phone number
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", (dialog, which) -> {
            String value = input.getText().toString().trim();
            if (!value.isEmpty()) {
                saveToFirebase(field, value);
            } else {
                Toast.makeText(getContext(), title + " cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void saveToFirebase(String field, String value) {
        if (userRef != null) {
            // Save medical data under 'MedicalData' for the user
            if (field.equals("height") || field.equals("weight") || field.equals("bmi") || field.equals("bloodType")) {
                userRef.child("MedicalData").child(field).setValue(value).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), field + " saved successfully!", Toast.LENGTH_SHORT).show();
                        updateTextView(field, value);
                        if (field.equals("height") || field.equals("weight")) {
                            recalculateBmi();
                        }
                    } else {
                        Toast.makeText(getContext(), "Failed to save " + field, Toast.LENGTH_SHORT).show();
                    }
                });
            } else if (field.equals("phone")) {
                userRef.child("phone").setValue(value).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "Phone number updated successfully!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Failed to update phone number", Toast.LENGTH_SHORT).show();
                    }
                });
            } else if (field.equals("password")) {
                auth.getCurrentUser().updatePassword(value).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "Password changed successfully!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Failed to change password", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    private void updateTextView(String field, String value) {
        switch (field) {
            case "height":
                displayHeight.setText(value);
                break;
            case "weight":
                displayWeight.setText(value);
                break;
            case "bmi":
                displayBmi.setText(value);
                break;
            case "bloodType":
                displayBloodType.setText(value);
                break;
        }
    }

    private void loadDataFromFirebase() {
        if (userRef != null) {
            userRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult().exists()) {
                    DataSnapshot dataSnapshot = task.getResult();

                    // Load medical data from 'MedicalData' node
                    if (dataSnapshot.hasChild("MedicalData")) {
                        DataSnapshot medicalDataSnapshot = dataSnapshot.child("MedicalData");
                        if (medicalDataSnapshot.hasChild("height")) {
                            String height = medicalDataSnapshot.child("height").getValue(String.class);
                            displayHeight.setText(height);
                        }
                        if (medicalDataSnapshot.hasChild("weight")) {
                            String weight = medicalDataSnapshot.child("weight").getValue(String.class);
                            displayWeight.setText(weight);
                        }
                        if (medicalDataSnapshot.hasChild("bmi")) {
                            String bmi = medicalDataSnapshot.child("bmi").getValue(String.class);
                            displayBmi.setText(bmi);
                        }
                        if (medicalDataSnapshot.hasChild("bloodType")) {
                            String bloodType = medicalDataSnapshot.child("bloodType").getValue(String.class);
                            displayBloodType.setText(bloodType);
                        }
                    }
                    // Load phone number
                    if (dataSnapshot.hasChild("phone")) {
                        String phone = dataSnapshot.child("phone").getValue(String.class);
                        // Display phone number as needed
                    }
                }
            });
        }
    }

    private void recalculateBmi() {
        String heightStr = displayHeight.getText().toString();
        String weightStr = displayWeight.getText().toString();

        if (!heightStr.isEmpty() && !weightStr.isEmpty()) {
            // Convert height to meters and weight to kg
            double height = Double.parseDouble(heightStr) / 100;  // Assuming height is in cm
            double weight = Double.parseDouble(weightStr);  // Assuming weight is in kg
            double bmi = weight / (height * height);

            // Update BMI TextView
            displayBmi.setText(String.format("%.1f", bmi));

            // Save BMI to Firebase
            saveToFirebase("bmi", String.format("%.1f", bmi));
        }
    }

    private void confirmDeleteAccount() {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    FirebaseUser user = auth.getCurrentUser();
                    if (user != null) {
                        user.delete().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(getContext(), "Account deleted successfully.", Toast.LENGTH_SHORT).show();
                                // Optionally navigate to login screen
                            } else {
                                Toast.makeText(getContext(), "Failed to delete account.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
