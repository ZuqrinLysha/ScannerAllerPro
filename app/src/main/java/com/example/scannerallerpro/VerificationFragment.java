package com.example.scannerallerpro;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class VerificationFragment extends Fragment {

    private FirebaseAuth auth;
    private DatabaseReference reference;
    private EditText currentPhoneField, newPhoneField;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_verification, container, false);

        // Initialize Firebase references
        auth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference("Users");

        // Initialize fields
        currentPhoneField = view.findViewById(R.id.et_current_phone2);
        newPhoneField = view.findViewById(R.id.et_new_phone);

        // Initialize the back button and set click listener
        ImageButton btnBack = view.findViewById(R.id.back_button);
        btnBack.setOnClickListener(v -> navigateBack());

        // Initialize the "Save" button and set click listener
        Button btnSavePhone = view.findViewById(R.id.btn_save_phone);
        btnSavePhone.setOnClickListener(v -> verifyCurrentPhoneAndSave());

        return view;
    }

    private void verifyCurrentPhoneAndSave() {
        String currentPhoneNumber = currentPhoneField.getText().toString().trim();
        String newPhoneNumber = newPhoneField.getText().toString().trim();

        // Check if the current phone number field is empty
        if (currentPhoneNumber.isEmpty()) {
            Toast.makeText(getContext(), "Please enter your current mobile number", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if the new phone number field is empty
        if (newPhoneNumber.isEmpty()) {
            Toast.makeText(getContext(), "Please enter a new mobile number", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get the current logged-in user
        FirebaseUser user = auth.getCurrentUser();

        if (user != null) {
            String userId = user.getUid();

            // Check the current phone number in the database
            reference.child(userId).child("phoneNumber").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String storedPhoneNumber = snapshot.getValue(String.class);
                    if (storedPhoneNumber != null && storedPhoneNumber.equals(currentPhoneNumber)) {
                        // Phone numbers match, update to the new number
                        updatePhoneNumber(newPhoneNumber);
                    } else {
                        // Phone numbers do not match
                        showAlert("Current phone number does not match the stored number.");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getContext(), "Error retrieving data.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
        }
    }

    private void updatePhoneNumber(String newPhoneNumber) {
        // Get the current logged-in user
        FirebaseUser user = auth.getCurrentUser();

        if (user != null) {
            String userId = user.getUid();

            // Update the phone number in the existing user row in Firebase
            reference.child(userId).child("phoneNumber").setValue(newPhoneNumber)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(getContext(), "Mobile number updated successfully!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Failed to update mobile number.", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
        }
    }

    // Method to show alert dialog with a message
    private void showAlert(String message) {
        new AlertDialog.Builder(getActivity())
                .setTitle("Verification Failed")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    // Method to navigate back with an alert dialog if there are unsaved changes
    private void navigateBack() {
        String currentPhoneNumber = currentPhoneField.getText().toString().trim();
        String newPhoneNumber = newPhoneField.getText().toString().trim();

        // Check if there are unsaved changes
        if (!currentPhoneNumber.isEmpty() || !newPhoneNumber.isEmpty()) {
            // Show a confirmation dialog if fields are filled but not saved
            new AlertDialog.Builder(getActivity())
                    .setTitle("Unsaved Changes")
                    .setMessage("You have unsaved changes. Do you want to save before leaving?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Save the data to Firebase
                            saveDataToFirebase(newPhoneNumber);
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Navigate back to ChangeNoPhoneFragment without saving
                            Fragment changeNoPhoneFragment = new ChangeNoPhoneFragment();
                            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                            transaction.replace(R.id.fragment_container, changeNoPhoneFragment);
                            transaction.addToBackStack(null);
                            transaction.commit();
                        }
                    })
                    .show(); // Show the dialog
        } else {
            // No unsaved changes, just navigate back to ChangeNoPhoneFragment
            Fragment changeNoPhoneFragment = new ChangeNoPhoneFragment();
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, changeNoPhoneFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }

    // Method to save the data to Firebase
    private void saveDataToFirebase(String newPhoneNumber) {
        // Get the current logged-in user
        FirebaseUser user = auth.getCurrentUser();

        if (user != null) {
            String userId = user.getUid();

            // Update the phone number in the existing user row in Firebase
            reference.child(userId).child("phoneNumber").setValue(newPhoneNumber)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(getContext(), "Mobile number updated successfully!", Toast.LENGTH_SHORT).show();
                            // Navigate back to ProfileFragment after saving
                            Fragment profileFragment = new ProfileFragment();
                            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                            transaction.replace(R.id.fragment_container, profileFragment);
                            transaction.addToBackStack(null);
                            transaction.commit();
                        } else {
                            Toast.makeText(getContext(), "Failed to update mobile number.", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
        }
    }
}
