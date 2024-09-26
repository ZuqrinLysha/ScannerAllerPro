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
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;

public class AddContactFragment extends Fragment {

    private Spinner spinnerRelationship;
    private EditText edtFirstName, edtPhoneNumber;
    private Button btnSave;
    private ContactViewModel contactViewModel;

    // Initialize Firebase
    private FirebaseDatabase database;
    private DatabaseReference databaseReference;
    private FirebaseAuth auth;

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
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            String userEmail = currentUser.getEmail();
            if (userEmail != null) {
                // Reference to the users node
                DatabaseReference usersRef = database.getReference("Users");
                usersRef.orderByChild("email").equalTo(userEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                                String fullName = userSnapshot.child("fullName").getValue(String.class);
                                if (fullName != null) {
                                    // Set the Firebase reference using the fullName
                                    databaseReference = FirebaseDatabase.getInstance()
                                            .getReference("Users")
                                            .child(fullName)
                                            .child("ContactEmergency");
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle possible errors
                        Toast.makeText(getContext(), "Error loading user data", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

        // Initialize ViewModel
        contactViewModel = new ViewModelProvider(requireActivity()).get(ContactViewModel.class);

        // Set up Spinner adapter
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.relationship_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRelationship.setAdapter(adapter);

        // Add contact on button click
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String firstName = edtFirstName.getText().toString().trim();
                String phoneNumber = edtPhoneNumber.getText().toString().trim();
                String relationship = spinnerRelationship.getSelectedItem().toString();

                // Validate input
                if (TextUtils.isEmpty(firstName)) {
                    Toast.makeText(getContext(), "Please enter the first name", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Validate phone number
                if (isValidPhoneNumber(phoneNumber)) {
                    // Create contact object without last name
                    ContactManager.Contact contact = new ContactManager.Contact(firstName, phoneNumber, relationship);

                    // Store contact data in Firebase directly under ContactEmergency
                    databaseReference.push().setValue(contact)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getContext(), "Contact saved successfully!", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Failed to save contact: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });

                    // Pass data to ViewModel without last name
                    ContactViewModel.Contact contactData = new ContactViewModel.Contact(firstName, phoneNumber, relationship);
                    contactViewModel.setContact(contactData);

                    // Clear input fields for the next contact
                    edtFirstName.setText("");
                    edtPhoneNumber.setText("");
                } else {
                    edtPhoneNumber.setError("Invalid phone number format! Only numbers allowed.");
                }
            }
        });

        return view;
    }

    // Function to validate phone number format (allowing only numbers)
    private boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber.matches("\\d{10,11}"); // Example: allowing 10 or 11 digits
    }
}
