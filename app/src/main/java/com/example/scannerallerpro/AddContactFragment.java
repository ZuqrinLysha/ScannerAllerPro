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

import java.util.HashMap;
import java.util.Map;

public class AddContactFragment extends Fragment {

    private Spinner spinnerRelationship, spinnerRole, spinnerMedicalCenter;
    private EditText edtFamilyContactName, edtFamilyContactPhone, edtHealthcareContactName, edtHealthcarePhoneNumber, edtMedicalCenterName, edtMedicalCenterPhoneNumber;
    private Button btnSave;
    private ContactViewModel contactViewModel;

    // Initialize Firebase
    private DatabaseReference databaseReference;
    private FirebaseAuth auth;
    private String userEmail; // To store the user's email

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_contact, container, false);

        // Initialize views
        spinnerRelationship = view.findViewById(R.id.spinnerRelationship);
        spinnerRole = view.findViewById(R.id.spinnerRole);
        spinnerMedicalCenter = view.findViewById(R.id.spinnerMedicalCenter);
        edtFamilyContactName = view.findViewById(R.id.edtFamilyContactName);
        edtFamilyContactPhone = view.findViewById(R.id.edtFamilyContactPhone);
        edtHealthcareContactName = view.findViewById(R.id.edtHealthcareContactName);
        edtHealthcarePhoneNumber = view.findViewById(R.id.edtHealthcarePhoneNumber);
        edtMedicalCenterName = view.findViewById(R.id.edtMedicalCenterName);
        edtMedicalCenterPhoneNumber = view.findViewById(R.id.edtMedicalCenterPhoneNumber);
        btnSave = view.findViewById(R.id.btnAddFamilyMember);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        // Initialize ViewModel
        contactViewModel = new ViewModelProvider(requireActivity()).get(ContactViewModel.class);

        // Set up Spinner adapter
        setupSpinners();

        // Retrieve user's email
        retrieveUserEmail();

        // Add contact on button click
        btnSave.setOnClickListener(v -> saveContact());

        return view;
    }

    private void setupSpinners() {
        ArrayAdapter<CharSequence> relationshipAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.relationship_options, android.R.layout.simple_spinner_item);
        relationshipAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRelationship.setAdapter(relationshipAdapter);

        ArrayAdapter<CharSequence> roleAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.healthcare_options, android.R.layout.simple_spinner_item);
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(roleAdapter);

        ArrayAdapter<CharSequence> medicalCenterAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.medical_center_options, android.R.layout.simple_spinner_item);
        medicalCenterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMedicalCenter.setAdapter(medicalCenterAdapter);
    }

    private void saveContact() {
        String familyContactName = edtFamilyContactName.getText().toString();
        String familyPhoneNumber = edtFamilyContactPhone.getText().toString();
        String healthcareContactName = edtHealthcareContactName.getText().toString();
        String healthcarePhoneNumber = edtHealthcarePhoneNumber.getText().toString();
        String medicalCenterName = edtMedicalCenterName.getText().toString();
        String medicalCenterPhoneNumber = edtMedicalCenterPhoneNumber.getText().toString();
        String relationship = spinnerRelationship.getSelectedItem().toString();
        String role = spinnerRole.getSelectedItem().toString();
        String medicalCenter = spinnerMedicalCenter.getSelectedItem().toString();

        // Validation
        if (TextUtils.isEmpty(familyContactName) || TextUtils.isEmpty(familyPhoneNumber)) {
            Toast.makeText(getContext(), "Please fill in all family contact fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (userEmail != null) {
            // Query the database for the user based on their email
            databaseReference.orderByChild("email").equalTo(userEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                            // Retrieve the full name of the user
                            String fullName = userSnapshot.child("fullName").getValue(String.class);
                            if (fullName != null) {
                                // Set the Firebase reference using the fullName
                                DatabaseReference contactRef = databaseReference.child(fullName).child("ContactData").push();

                                // Create a map to store the contact data
                                Map<String, Object> contactData = new HashMap<>();
                                contactData.put("family_name", familyContactName);
                                contactData.put("family_phone", familyPhoneNumber);
                                contactData.put("healthcare_name", healthcareContactName);
                                contactData.put("healthcare_phone", healthcarePhoneNumber);
                                contactData.put("healthcare_role", role);
                                contactData.put("medical_center_name", medicalCenterName);
                                contactData.put("medical_center_phone", medicalCenterPhoneNumber);

                                // Store contact data in Firebase
                                contactRef.setValue(contactData)
                                        .addOnSuccessListener(aVoid -> {
                                            contactViewModel.addContact(new ContactViewModel.Contact(familyContactName, familyPhoneNumber, relationship));
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
        edtFamilyContactName.setText("");
        edtFamilyContactPhone.setText("");
        edtHealthcareContactName.setText("");
        edtHealthcarePhoneNumber.setText("");
        edtMedicalCenterName.setText("");
        edtMedicalCenterPhoneNumber.setText("");
    }

    // Method to retrieve user's email from Firebase
    private void retrieveUserEmail() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            userEmail = currentUser.getEmail(); // Get user email directly
        }
    }
}
