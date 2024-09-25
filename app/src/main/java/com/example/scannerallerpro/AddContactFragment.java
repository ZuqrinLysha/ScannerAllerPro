package com.example.scannerallerpro;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddContactFragment extends Fragment {

    private Spinner spinnerRelationship;
    private EditText edtPhoneNumber;
    private Button btnSave;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_contact, container, false);

        // Initialize the Spinner
        spinnerRelationship = view.findViewById(R.id.spinnerRelationship);
        edtPhoneNumber = view.findViewById(R.id.edtFamilyContactPhone);
        btnSave = view.findViewById(R.id.btnAddFamilyMember);

        // Set up Spinner adapter
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.relationship_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRelationship.setAdapter(adapter);

        // Add onClickListener to validate phone number when saving
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = edtPhoneNumber.getText().toString().trim();

                // Validate phone number
                if (isValidPhoneNumber(phoneNumber)) {
                    // Proceed with saving the contact
                    Toast.makeText(getContext(), "Phone number is valid", Toast.LENGTH_SHORT).show();
                } else {
                    // Show error message
                    edtPhoneNumber.setError("Invalid phone number format! Use 01x-xxx xxxx");
                }
            }
        });

        return view;
    }

    // Function to validate phone number format
    private boolean isValidPhoneNumber(String phoneNumber) {
        // Regular expression to match 01x-xxx xxxx format
        String regex = "^01\\d-\\d{3} \\d{4}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(phoneNumber);
        return matcher.matches();
    }

}
