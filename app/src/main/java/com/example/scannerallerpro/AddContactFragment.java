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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddContactFragment extends Fragment {

    private Spinner spinnerRelationship;
    private EditText edtDoctorName, edtDoctorContactPhone;
    private EditText edtMedicalInstitutionName, edtMedicalContactPhone;
    private EditText edtFamilyContactName, edtFamilyContactPhone;
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
        edtDoctorName = view.findViewById(R.id.edtDoctorName);
        edtDoctorContactPhone = view.findViewById(R.id.edtDoctorContactPhone);
        edtMedicalInstitutionName = view.findViewById(R.id.edtMedicalInstitutionName);
        edtMedicalContactPhone = view.findViewById(R.id.edtMedicalContactPhone);
        edtFamilyContactName = view.findViewById(R.id.edtFamilyContactName);
        edtFamilyContactPhone = view.findViewById(R.id.edtFamilyContactPhone);
        btnSave = view.findViewById(R.id.btnSaveContact);

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
        btnSave.setOnClickListener(v -> saveContact());

        return view;
    }

    private void saveContact() {
        // Create a contact object
        ContactViewModel.Contact contactData = new ContactViewModel.Contact();

        // Check for filled fields and set values
        if (!TextUtils.isEmpty(edtDoctorName.getText().toString())) {
            contactData.setDoctorName(edtDoctorName.getText().toString());
        }
        if (!TextUtils.isEmpty(edtDoctorContactPhone.getText().toString())) {
            contactData.setDoctorContactPhone(edtDoctorContactPhone.getText().toString());
        }
        if (!TextUtils.isEmpty(edtMedicalInstitutionName.getText().toString())) {
            contactData.setMedicalInstitutionName(edtMedicalInstitutionName.getText().toString());
        }
        if (!TextUtils.isEmpty(edtMedicalContactPhone.getText().toString())) {
            contactData.setMedicalContactPhone(edtMedicalContactPhone.getText().toString());
        }
        if (!TextUtils.isEmpty(edtFamilyContactName.getText().toString())) {
            contactData.setFamilyContactName(edtFamilyContactName.getText().toString());
        }
        if (!TextUtils.isEmpty(edtFamilyContactPhone.getText().toString())) {
            contactData.setFamilyContactPhone(edtFamilyContactPhone.getText().toString());
        }

        // Check if at least one field is filled
        if (TextUtils.isEmpty(contactData.getDoctorName()) && TextUtils.isEmpty(contactData.getFamilyContactName())
                && TextUtils.isEmpty(contactData.getMedicalInstitutionName())) {
            Toast.makeText(getContext(), "Please fill in at least one contact detail.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Set the relationship
        contactData.setRelationship(spinnerRelationship.getSelectedItem().toString());

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
                            clearInputFields();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(getContext(), "Failed to save contact: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        } else {
            Toast.makeText(getContext(), "User ID is not available.", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearInputFields() {
        edtDoctorName.setText("");
        edtDoctorContactPhone.setText("");
        edtMedicalInstitutionName.setText("");
        edtMedicalContactPhone.setText("");
        edtFamilyContactName.setText("");
        edtFamilyContactPhone.setText("");
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
