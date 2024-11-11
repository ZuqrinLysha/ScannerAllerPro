package com.example.scannerallerpro;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class AddContactFragment extends Fragment {

    private Spinner spinnerRelationship;
    private EditText edtContactName, edtPhone;
    private ContactViewModel contactViewModel;
    private Button buttonSave;

    // Initialize Firebase
    private DatabaseReference databaseReference;
    private FirebaseAuth auth;
    private String userEmail; // To store the user's email

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_contact, container, false);

        // Initialize the Toolbar
        Toolbar toolbar = view.findViewById(R.id.toolbarAddContact);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);

        // Initialize views
        spinnerRelationship = view.findViewById(R.id.relationship_options);
        edtContactName = view.findViewById(R.id.edtContactName);
        edtPhone = view.findViewById(R.id.edtPhone);
        buttonSave = view.findViewById(R.id.button_save); // Initialize the button

        // Initialize back arrow button
        ImageButton backArrow = view.findViewById(R.id.backArrow);

        // Set click listeners for the buttons
        backArrow.setOnClickListener(v -> navigateToViewContactFragment());
        backArrow.setOnClickListener(v -> {
            // Check if there are unsaved changes in the input fields
            if (!TextUtils.isEmpty(edtContactName.getText()) || !TextUtils.isEmpty(edtPhone.getText())) {
                // Show an alert dialog to confirm if the user wants to save
                new AlertDialog.Builder(getContext())
                        .setTitle("Unsaved Changes")
                        .setMessage("You have unsaved changes. Do you want to save them?")
                        .setPositiveButton("Yes", (dialog, which) -> saveContactAndNavigate()) // Save and navigate if user says Yes
                        .setNegativeButton("No", (dialog, which) -> navigateToViewContactFragment()) // Just navigate if user says No
                        .show();
            } else {
                // If no changes, navigate back directly
                navigateToViewContactFragment();
            }
        });

        buttonSave.setOnClickListener(v -> saveContact());

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        // Initialize ViewModel
        contactViewModel = new ViewModelProvider(requireActivity()).get(ContactViewModel.class);

        // Set up Spinner adapter
        setupSpinners();

        // Retrieve user's email
        retrieveUserEmail();

        return view;
    }



    private void saveContactAndNavigate() {
        saveContact(); // Save the contact
        navigateToViewContactFragment(); // Navigate after saving
    }

    private void setupSpinners() {
        ArrayAdapter<CharSequence> relationshipAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.relationship_options, android.R.layout.simple_spinner_item);
        relationshipAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRelationship.setAdapter(relationshipAdapter);
    }

    private void saveContact() {
        String contactName = edtContactName.getText().toString();
        String phone = edtPhone.getText().toString();
        String relationship = spinnerRelationship.getSelectedItem().toString();

        // Validation
        if (TextUtils.isEmpty(contactName) || TextUtils.isEmpty(phone)) {
            Toast.makeText(getContext(), "Please fill in all family contact fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check phone number format
        if (!phone.startsWith("+60") || (phone.length() != 12 && phone.length() != 13)) {
            new AlertDialog.Builder(getContext())
                    .setTitle("Invalid Phone Number")
                    .setMessage("Please enter a valid phone number starting with +60 and with 10 or 11 digits after the country code.")
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }

        if (userEmail != null) {
            // Query the database for the user based on their email
            databaseReference.orderByChild("email").equalTo(userEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                            String userId = userSnapshot.getKey(); // Get the unique ID for the user
                            String fullName = userSnapshot.child("fullName").getValue(String.class);

                            if (fullName != null) {
                                // Reference to the user's contact data
                                DatabaseReference contactRef = databaseReference.child(userId).child("ContactData").push();

                                // Prepare the contact data to be saved
                                Map<String, Object> contactData = new HashMap<>();
                                contactData.put("contact_name", contactName);
                                contactData.put("no_phone", phone);
                                contactData.put("relationship", relationship);

                                // Save the contact data
                                contactRef.setValue(contactData)
                                        .addOnSuccessListener(aVoid -> {
                                            // Update the ViewModel with the new contact
                                            ContactViewModel.Contact newContact = new ContactViewModel.Contact(contactRef.getKey(), contactName, phone, relationship);
                                            contactViewModel.addContact(newContact);
                                            Toast.makeText(getContext(), "Contact saved successfully!", Toast.LENGTH_SHORT).show();
                                            clearInputFields();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(getContext(), "Failed to save contact: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            }
                        }
                    } else {
                        Toast.makeText(getContext(), "User not found.", Toast.LENGTH_SHORT).show();
                    }
                }

    @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(getContext(), "Failed to fetch user data.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void clearInputFields() {
        edtContactName.setText("");
        edtPhone.setText("");
    }

    private void retrieveUserEmail() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            userEmail = currentUser.getEmail();
        }
    }

    private void navigateToViewContactFragment() {
        Fragment viewContactFragment = new ViewContactFragment();
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, viewContactFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}


