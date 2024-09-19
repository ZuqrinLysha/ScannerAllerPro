package com.example.scannerallerpro;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class AllergicHistoryFragment extends Fragment {

    private CheckBox chkPeanuts, chkDairy, chkGluten, chkSeafood, chkEggs, chkSoy, chkSesame, chkWheat;
    private Button btnSaveAllergies;

    // Firebase Database reference
    private DatabaseReference databaseReference;
    private FirebaseAuth auth;
    private DatabaseReference usersRef;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_allergic_history, container, false);

        // Initialize checkboxes
        chkPeanuts = view.findViewById(R.id.chkPeanuts);
        chkDairy = view.findViewById(R.id.chkDairy);
        chkGluten = view.findViewById(R.id.chkGluten);
        chkSeafood = view.findViewById(R.id.chkSeafood);
        chkEggs = view.findViewById(R.id.chkEggs);
        chkSoy = view.findViewById(R.id.chkSoy);
        chkSesame = view.findViewById(R.id.chkSesame);
        chkWheat = view.findViewById(R.id.chkWheat);

        // Save Button
        btnSaveAllergies = view.findViewById(R.id.btnSaveAllergies);
        btnSaveAllergies.setOnClickListener(v -> saveAllergicHistory());

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        String userEmail = auth.getCurrentUser().getEmail();

        if (userEmail != null) {
            usersRef = FirebaseDatabase.getInstance().getReference("Users");

            // Query the database for the user based on their email
            usersRef.orderByChild("email").equalTo(userEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                            // Retrieve the full name of the user
                            String fullName = userSnapshot.child("fullName").getValue(String.class);
                            if (fullName != null) {
                                // Set the Firebase reference using the fullName instead of userId
                                databaseReference = FirebaseDatabase.getInstance()
                                        .getReference("Users")
                                        .child(fullName)
                                        .child("AllergicHistory");
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(getContext(), "Failed to fetch user data.", Toast.LENGTH_SHORT).show();
                }
            });
        }

        return view;
    }

    private void saveAllergicHistory() {
        if (databaseReference == null) {
            Toast.makeText(getContext(), "Unable to save. Please try again later.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Boolean> allergies = new HashMap<>();
        allergies.put("peanuts", chkPeanuts.isChecked());
        allergies.put("dairy", chkDairy.isChecked());
        allergies.put("gluten", chkGluten.isChecked());
        allergies.put("seafood", chkSeafood.isChecked());
        allergies.put("eggs", chkEggs.isChecked());
        allergies.put("soybeans", chkSoy.isChecked());
        allergies.put("sesame", chkSesame.isChecked());
        allergies.put("wheat", chkWheat.isChecked());

        if (!allergies.isEmpty()) {
            // Store data in Firebase
            databaseReference.setValue(allergies).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(getContext(), "Allergic history saved successfully!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Failed to save allergic history.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(getContext(), "No allergies selected!", Toast.LENGTH_SHORT).show();
        }
    }
}
