package com.example.scannerallerpro;

import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.drawerlayout.widget.DrawerLayout;
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

    // TextViews for displaying user data
    TextView display_weight, display_height, display_bmi, display_blood_type;
    TextView btn_Allergic_History;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        // Access activity toolbar and drawer layout
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            Toolbar toolbar = activity.findViewById(R.id.toolbar);
            activity.setSupportActionBar(toolbar);

            DrawerLayout drawerLayout = activity.findViewById(R.id.drawerLayout);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    activity, drawerLayout, toolbar, R.string.open_drawer, R.string.close_drawer);
            drawerLayout.addDrawerListener(toggle);
            toggle.syncState();
        }

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
                                    // Now you have the username and can reference the user by username
                                    userRef = database.getReference("Users").child(fullName);
                                    // Load existing data from Firebase
                                    loadDataFromFirebase();
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(getContext(), "Failed to fetch username", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

        // Initialize the TextViews
        display_height = rootView.findViewById(R.id.display_height);
        display_weight = rootView.findViewById(R.id.display_weight);
        display_bmi = rootView.findViewById(R.id.display_bmi);
        display_blood_type = rootView.findViewById(R.id.display_blood_type);

        // Find the CardView by its ID
        CardView cvAllergicHistory = rootView.findViewById(R.id.cvAllergicHistory);

        // Set the OnClickListener for btn_Allergic_History
        cvAllergicHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to the AllergicHistoryFragment
                Fragment allergicHistoryFragment = new AllergicHistoryFragment();
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();

                // Replace the fragment and add to the back stack (so user can navigate back)
                transaction.replace(R.id.fragment_container, allergicHistoryFragment);
                transaction.addToBackStack(null); // This ensures back navigation works
                transaction.commit();
            }
        });

        // Initialize CardViews and set click listeners
        CardView cvHeight = rootView.findViewById(R.id.cvHeight);
        CardView cvWeight = rootView.findViewById(R.id.cvWeight);
        CardView cvBmi = rootView.findViewById(R.id.cvBmi);
        CardView cvBloodType = rootView.findViewById(R.id.cvBloodType);

        cvHeight.setOnClickListener(v -> showInputDialog("Enter Your Height", "height"));
        cvWeight.setOnClickListener(v -> showInputDialog("Enter Your Weight", "weight"));
        cvBloodType.setOnClickListener(v -> showInputDialog("Enter Your Blood Type", "bloodType"));

        return rootView;
    }

    private void showInputDialog(String title, String field) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(title);

        // Set up the input
        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);  // For text input
        builder.setView(input);

        // Set up the buttons
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

    private void saveToFirebase(String field, String value) {
        if (userRef != null) {
            // Save medical data under 'MedicalData' for the user
            if (field.equals("height") || field.equals("weight") || field.equals("bmi") || field.equals("bloodType")) {
                userRef.child("MedicalData").child(field).setValue(value).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), field + " saved successfully!", Toast.LENGTH_SHORT).show();
                        updateTextView(field, value);
                        if (field.equals("height") || field.equals("weight")) {
                            recalculateBmi();
                        }
                    } else {
                        Toast.makeText(getContext(), "Failed to save " + field, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    private void updateTextView(String field, String value) {
        switch (field) {
            case "height":
                display_height.setText(value);
                break;
            case "weight":
                display_weight.setText(value);
                break;
            case "bmi":
                display_bmi.setText(value);
                break;
            case "bloodType":
                display_blood_type.setText(value);
                break;
        }
    }

    private void loadDataFromFirebase() {
        if (userRef != null) {
            userRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult().exists()) {
                    DataSnapshot dataSnapshot = task.getResult();

                    // Load medical data from 'MedicalData' node
                    if (dataSnapshot.hasChild("MedicalData")) {
                        DataSnapshot medicalDataSnapshot = dataSnapshot.child("MedicalData");
                        if (medicalDataSnapshot.hasChild("height")) {
                            String height = medicalDataSnapshot.child("height").getValue(String.class);
                            display_height.setText(height);
                        }
                        if (medicalDataSnapshot.hasChild("weight")) {
                            String weight = medicalDataSnapshot.child("weight").getValue(String.class);
                            display_weight.setText(weight);
                        }
                        if (medicalDataSnapshot.hasChild("bmi")) {
                            String bmi = medicalDataSnapshot.child("bmi").getValue(String.class);
                            display_bmi.setText(bmi);
                        }
                        if (medicalDataSnapshot.hasChild("bloodType")) {
                            String bloodType = medicalDataSnapshot.child("bloodType").getValue(String.class);
                            display_blood_type.setText(bloodType);
                        }
                    }
                }
            });
        }
    }

    private void recalculateBmi() {
        String heightStr = display_height.getText().toString();
        String weightStr = display_weight.getText().toString();

        if (!heightStr.isEmpty() && !weightStr.isEmpty()) {
            // Convert height to meters and weight to kg
            double height = Double.parseDouble(heightStr) / 100;  // Assuming height is in cm
            double weight = Double.parseDouble(weightStr);  // Assuming weight is in kg
            double bmi = weight / (height * height);

            // Update BMI TextView
            display_bmi.setText(String.format("%.1f", bmi));

            // Save BMI to Firebase
            saveToFirebase("bmi", String.format("%.1f", bmi));
        }
    }
}
