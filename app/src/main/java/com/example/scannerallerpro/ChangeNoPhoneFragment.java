package com.example.scannerallerpro;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ChangeNoPhoneFragment extends Fragment {

    private FirebaseAuth auth;
    private DatabaseReference reference;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_change_no_phone, container, false);

        // Initialize Firebase references
        auth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference("Users");

        // Initialize the back button and set click listener
        ImageButton btnBack = view.findViewById(R.id.back_button);
        btnBack.setOnClickListener(v -> navigateBack());

        // Initialize the "Save" button and set click listener
        Button btnSavePhone = view.findViewById(R.id.btn_save_phone);
        btnSavePhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newPhoneNumber = ((EditText) view.findViewById(R.id.et_new_phone)).getText().toString().trim();

                if (newPhoneNumber.isEmpty()) {
                    Toast.makeText(getContext(), "Please enter a new mobile number", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Get the current logged-in user
                FirebaseUser user = auth.getCurrentUser();

                if (user != null) {
                    String userId = user.getUid();

                    // Update the phone number in the existing user row in Firebase
                    reference.child(userId).child("phoneNumber").setValue(newPhoneNumber)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(getContext(), "Mobile number updated successfully!", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(getContext(), "Failed to update mobile number.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                } else {
                    Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }

    // Method to navigate back to ProfileFragment
    private void navigateBack() {
        Fragment profileFragment = new ProfileFragment(); // Create an instance of ProfileFragment
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, profileFragment); // Use the correct fragment instance
        transaction.addToBackStack(null); // Optional: Add to back stack
        transaction.commit();
    }
}
