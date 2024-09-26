package com.example.scannerallerpro;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AddContactFragment extends Fragment {

    private Spinner spinnerRelationship;
    private EditText edtFirstName, edtPhoneNumber;
    private Button btnSave;
    private ContactViewModel contactViewModel;

    // Initialize Firebase
    private FirebaseDatabase database;
    private DatabaseReference databaseReference;
    private FirebaseAuth auth;
    private String userId; // To store the user's unique ID

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_contact, container, false);

        // Initialize views
        spinnerRelationship = view.findViewById(R.id.spinnerRelationship);
        edtFirstName = view.findViewById(R.id.edtFirstName);
        edtPhoneNumber = view.findViewById(R.id.edtFamilyContactPhone);
        btnSave = view.findViewById(R.id.btnAddFamilyMember);

        // Initialize Firebase
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        // Initialize ViewModel
        contactViewModel = new ViewModelProvider(requireActivity()).get(ContactViewModel.class);

        // Set up Spinner adapter
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.relationship_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRelationship.setAdapter(adapter);

        // Retrieve user's unique ID
        retrieveUserId();

        // Add contact on button click
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String firstName = edtFirstName.getText().toString();
                String phoneNumber = edtPhoneNumber.getText().toString();
                String relationship = spinnerRelationship.getSelectedItem().toString();

                if (TextUtils.isEmpty(firstName) || TextUtils.isEmpty(phoneNumber)) {
                    Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Create contact object
                ContactViewModel.Contact contactData = new ContactViewModel.Contact(firstName, phoneNumber, relationship);

                // Store contact data in Firebase under user's node
                if (userId != null) {
                    String contactId = database.getReference("Users").child(userId).child("Contacts").push().getKey();
                    if (contactId != null) {
                        database.getReference("Users").child(userId).child("Contacts").child(contactId).setValue(contactData)
                                .addOnSuccessListener(aVoid -> {
                                    // Add to ViewModel after successful Firebase write
                                    contactViewModel.addContact(contactData);
                                    Toast.makeText(getContext(), "Contact saved successfully!", Toast.LENGTH_SHORT).show();

                                    // Clear input fields for the next contact
                                    edtFirstName.setText("");
                                    edtPhoneNumber.setText("");
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(getContext(), "Failed to save contact: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }
                } else {
                    Toast.makeText(getContext(), "User ID is not available.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }

    // Method to retrieve user's unique ID from Firebase
    private void retrieveUserId() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid(); // Get user ID directly
            // Optionally, you can also fetch other user details if needed
        } else {
            Toast.makeText(getContext(), "No user is logged in.", Toast.LENGTH_SHORT).show();
        }
    }
}
