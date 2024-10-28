package com.example.scannerallerpro;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
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
    private Switch switchTheme;
    private String currentHeight = null;
    private String currentWeight = null;

    // TextViews for displaying user data
    private TextView displayWeight, displayHeight, displayBmi, displayBloodType;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize Firebase
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        // Initialize the Toolbar
        Toolbar toolbar = rootView.findViewById(R.id.ToolbarLogout);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        setHasOptionsMenu(true); // Enable options menu in this fragment

        if (((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // Set up UI elements
        switchTheme = rootView.findViewById(R.id.switchTheme);
        displayHeight = rootView.findViewById(R.id.display_height);
        displayWeight = rootView.findViewById(R.id.display_weight);
        displayBmi = rootView.findViewById(R.id.display_bmi);
        displayBloodType = rootView.findViewById(R.id.display_blood_type);

        // Initialize CardViews and set click listeners
        setupCardViewListeners(rootView);

        // Load saved dark mode preference
        loadDarkModePreference();

        // Load user data from Firebase if user is logged in
        if (currentUser != null) {
            loadUserData(currentUser.getEmail());
        }

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.logout_menu, menu);
        Log.d("ProfileFragment", "Menu created");
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            // Show confirmation dialog before logging out
            new AlertDialog.Builder(getActivity())
                    .setTitle("Confirm Logout")
                    .setMessage("Are you sure you want to exit the app?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        FirebaseAuth.getInstance().signOut(); // Log out the user from Firebase
                        Intent intent = new Intent(getActivity(), LogIn.class);
                        startActivity(intent);
                        getActivity().finish();
                    })
                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                    .show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true); // Enable options menu in the fragment
    }

    private void setupCardViewListeners(View rootView) {
        CardView cvHeight = rootView.findViewById(R.id.cvHeight);
        CardView cvWeight = rootView.findViewById(R.id.cvWeight);
        CardView cvBmi = rootView.findViewById(R.id.cvBmi);
        CardView cvBloodType = rootView.findViewById(R.id.cvBloodType);
        CardView cvChangePhone = rootView.findViewById(R.id.cvChangePhone);
        CardView cvResetSecurityQuestion = rootView.findViewById(R.id.cvResetSecurityQuestion);
        CardView cvChangePassword = rootView.findViewById(R.id.cvChangePassword);
        CardView cvDeleteAccount = rootView.findViewById(R.id.cvDeleteAccount);

        cvHeight.setOnClickListener(v -> showInputDialog("Enter Your Height", "height"));
        cvWeight.setOnClickListener(v -> showInputDialog("Enter Your Weight", "weight"));
        cvBloodType.setOnClickListener(v -> showBloodTypeDialog());
        cvResetSecurityQuestion.setOnClickListener(v -> navigateToResetSecurityQuestionFragment());
        cvChangePhone.setOnClickListener(v -> navigateToSecurityQuestionFragment());
        cvChangePassword.setOnClickListener(v -> navigateToChangePasswordFragment());
        cvDeleteAccount.setOnClickListener(v -> confirmDeleteAccount());
    }

    private void navigateToChangePasswordFragment() {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, new ChangePasswordFragment());
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void navigateToResetSecurityQuestionFragment() {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, new ResetSecurityQuestionFragment());
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void navigateToSecurityQuestionFragment() {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, new SecurityQuestionFragment());
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void loadDarkModePreference() {
        boolean isDarkModeEnabled = getActivity().getSharedPreferences("Preferences", Context.MODE_PRIVATE)
                .getBoolean("dark_mode", false);
        switchTheme.setChecked(isDarkModeEnabled);

        switchTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            getActivity().getSharedPreferences("Preferences", Context.MODE_PRIVATE)
                    .edit()
                    .putBoolean("dark_mode", isChecked)
                    .apply();
            AppCompatDelegate.setDefaultNightMode(isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        });
    }

    private void loadUserData(String userEmail) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            userRef = database.getReference("Users").child(currentUser.getUid());
            loadDataFromFirebase();
        }
    }

    private void loadDataFromFirebase() {
        userRef.child("MedicalData").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String height = dataSnapshot.child("height").getValue(String.class);
                String weight = dataSnapshot.child("weight").getValue(String.class);
                String bloodType = dataSnapshot.child("bloodType").getValue(String.class);
                String bmi = dataSnapshot.child("bmi").getValue(String.class);

                if (height != null) {
                    displayHeight.setText(height + " cm");
                }
                if (weight != null) {
                    displayWeight.setText(weight + " kg");
                }
                if (bloodType != null) {
                    displayBloodType.setText(bloodType);
                }
                if (bmi != null) {
                    displayBmi.setText(bmi);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to fetch medical data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showInputDialog(String title, String field) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(title);

        final EditText input = new EditText(getContext());
        input.setInputType(field.equals("phone") ? InputType.TYPE_CLASS_PHONE : InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

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

    private void showBloodTypeDialog() {
        String[] bloodTypes = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Select Your Blood Type");
        builder.setItems(bloodTypes, (dialog, which) -> {
            String selectedBloodType = bloodTypes[which];
            saveToFirebase("bloodType", selectedBloodType);
        });
        builder.show();
    }

    private void saveToFirebase(String field, String value) {

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            DatabaseReference userMedicalDataRef = database.getReference("Users")
                    .child(currentUser.getUid())
                    .child("MedicalData");

            userMedicalDataRef.child(field).setValue(value)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Saved successfully", Toast.LENGTH_SHORT).show();
                        updateTextView(field, value);
                        // Update currentHeight or currentWeight when height or weight changes
                        if (field.equals("height")) {
                            currentHeight = value;
                        } else if (field.equals("weight")) {
                            currentWeight = value;
                        }
                        calculateAndSaveBMI(); // Calculate BMI if both values are available
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Failed to save", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void calculateAndSaveBMI() {
        if (currentHeight != null && currentWeight != null) {
            try {
                // Parse height and weight
                float heightInMeters = Float.parseFloat(currentHeight) / 100;
                float weightInKg = Float.parseFloat(currentWeight);

                // Calculate BMI
                float bmi = weightInKg / (heightInMeters * heightInMeters);

                // Format BMI to one decimal place
                String formattedBMI = String.format("%.1f", bmi);

                // Save BMI to Firebase and update the UI
                saveToFirebase("bmi", formattedBMI);
            } catch (NumberFormatException e) {
                Log.e("ProfileFragment", "Invalid height or weight format for BMI calculation.");
            }
        }
    }

    private void updateTextView(String field, String value) {
        switch (field) {
            case "height":
                displayHeight.setText(value + " cm");
                break;
            case "weight":
                displayWeight.setText(value + " kg");
                break;
            case "bmi":
                displayBmi.setText(value);
                break;
            case "bloodType":
                displayBloodType.setText(value);
                break;
        }
    }

    private void confirmDeleteAccount() {
        new AlertDialog.Builder(getContext())
                .setTitle("Confirm Delete Account")
                .setMessage("Are you sure you want to delete your account permanently?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null) {
                        deleteAccount(user);
                    }
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void deleteAccount(FirebaseUser user) {
        DatabaseReference userRef = database.getReference("Users").child(user.getUid());

        userRef.removeValue().addOnSuccessListener(aVoid -> {
            user.delete().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(getContext(), "Account deleted successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getActivity(), LogIn.class);
                    startActivity(intent);
                    getActivity().finish();
                } else {
                    Toast.makeText(getContext(), "Account deletion failed", Toast.LENGTH_SHORT).show();
                }
            });
        }).addOnFailureListener(e -> Toast.makeText(getContext(), "Error deleting data", Toast.LENGTH_SHORT).show());
    }
}
