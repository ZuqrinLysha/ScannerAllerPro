package com.example.scannerallerpro;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class AllergicHistoryFragment extends Fragment {

    private CheckBox chkPeanuts, chkSoybeans, chkCashew, chkAlmond, chkWalnuts;
    private CheckBox chkCowMilk, chkSoyMilk, chkButter, chkCheese, chkYogurt, chkCereal;
    private CheckBox chkGluten, chkWheat, chkBarley;
    private CheckBox chkCrab, chkPrawns, chkLobster, chkScallops, chkSalmon;
    private CheckBox chkOliveOil, chkSunflowerOil, chkCanolaOil, chkVegetableOil, chkCoconutOil;

    private EditText txtOtherAllergic;
    private DatabaseReference databaseReference;
    private FirebaseAuth auth;
    private DatabaseReference usersRef;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_allergic_history, container, false);

        // Initialize CheckBoxes
        initializeCheckBoxes(view);

        // Initialize EditText
        txtOtherAllergic = view.findViewById(R.id.txtOtherAllergic);

        ImageButton btnSave = view.findViewById(R.id.button_save);
        btnSave.setOnClickListener(v -> saveAllergicHistory());

        ImageButton btnBack = view.findViewById(R.id.backArrow);
        btnBack.setOnClickListener(v -> navigateBack());

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        String userEmail = auth.getCurrentUser() != null ? auth.getCurrentUser().getEmail() : null;

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
                                // Set the Firebase reference using the fullName
                                databaseReference = FirebaseDatabase.getInstance()
                                        .getReference("Users")
                                        .child(fullName)
                                        .child("AllergicHistory");

                                // Fetch and apply allergic history to checkboxes
                                loadAllergicHistory();
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

    // Method to navigate back to HomeFragment
    private void navigateBack() {
        Fragment homeFragment = new HomeFragment(); // Create an instance of HomeFragment
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, homeFragment); // Assuming you have a container with this ID
        transaction.addToBackStack(null); // Optional: Add to back stack
        transaction.commit();
    }

    private void initializeCheckBoxes(View view) {
        chkPeanuts = view.findViewById(R.id.chkPeanuts);
        chkSoybeans = view.findViewById(R.id.chkSoybeans);
        chkCashew = view.findViewById(R.id.chkCashew);
        chkAlmond = view.findViewById(R.id.chkAlmond);
        chkWalnuts = view.findViewById(R.id.chkWalnuts);

        chkCowMilk = view.findViewById(R.id.chkCowMilk);
        chkSoyMilk = view.findViewById(R.id.chkSoyMilk);
        chkButter = view.findViewById(R.id.chkButter);
        chkCheese = view.findViewById(R.id.chkCheese);
        chkYogurt = view.findViewById(R.id.chkYogurt);
        chkCereal = view.findViewById(R.id.chkCereal);

        chkGluten = view.findViewById(R.id.chkGluten);
        chkWheat = view.findViewById(R.id.chkWheat);
        chkBarley = view.findViewById(R.id.chkBarley);

        chkCrab = view.findViewById(R.id.chkCrab);
        chkPrawns = view.findViewById(R.id.chkPrawns);
        chkLobster = view.findViewById(R.id.chkLobster);
        chkScallops = view.findViewById(R.id.chkScallops);
        chkSalmon = view.findViewById(R.id.chkSalmon);

        chkOliveOil = view.findViewById(R.id.chkOliveOil);
        chkSunflowerOil = view.findViewById(R.id.chkSunflowerOil);
        chkCanolaOil = view.findViewById(R.id.chkCanolaOil);
        chkVegetableOil = view.findViewById(R.id.chkVegetableOil);
        chkCoconutOil = view.findViewById(R.id.chkCoconutOil);
    }

    private void saveAllergicHistory() {
        if (databaseReference == null) {
            Toast.makeText(getContext(), "Unable to save. Please try again later.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> allergies = new HashMap<>();
        allergies.put("peanuts", chkPeanuts.isChecked());
        allergies.put("soybeans", chkSoybeans.isChecked());
        allergies.put("cashew", chkCashew.isChecked());
        allergies.put("almond", chkAlmond.isChecked());
        allergies.put("walnuts", chkWalnuts.isChecked());
        allergies.put("cowMilk", chkCowMilk.isChecked());
        allergies.put("soyMilk", chkSoyMilk.isChecked());
        allergies.put("butter", chkButter.isChecked());
        allergies.put("cheese", chkCheese.isChecked());
        allergies.put("yogurt", chkYogurt.isChecked());
        allergies.put("cereal", chkCereal.isChecked());
        allergies.put("gluten", chkGluten.isChecked());
        allergies.put("wheat", chkWheat.isChecked());
        allergies.put("barley", chkBarley.isChecked());
        allergies.put("crab", chkCrab.isChecked());
        allergies.put("prawns", chkPrawns.isChecked());
        allergies.put("lobster", chkLobster.isChecked());
        allergies.put("scallops", chkScallops.isChecked());
        allergies.put("salmon", chkSalmon.isChecked());
        allergies.put("oliveOil", chkOliveOil.isChecked());
        allergies.put("sunflowerOil", chkSunflowerOil.isChecked());
        allergies.put("canolaOil", chkCanolaOil.isChecked());
        allergies.put("vegetableOil", chkVegetableOil.isChecked());
        allergies.put("coconutOil", chkCoconutOil.isChecked());

        // Add the user's additional input if it's not empty
        String otherAllergic = txtOtherAllergic.getText().toString().trim();
        if (!otherAllergic.isEmpty()) {
            allergies.put("other", otherAllergic);
        }

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

    private void loadAllergicHistory() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Check and set checkboxes according to saved data, with defaulting to false
                    chkPeanuts.setChecked(getBooleanValue(dataSnapshot, "peanuts"));
                    chkSoybeans.setChecked(getBooleanValue(dataSnapshot, "soybeans"));
                    chkCashew.setChecked(getBooleanValue(dataSnapshot, "cashew"));
                    chkAlmond.setChecked(getBooleanValue(dataSnapshot, "almond"));
                    chkWalnuts.setChecked(getBooleanValue(dataSnapshot, "walnuts"));
                    chkCowMilk.setChecked(getBooleanValue(dataSnapshot, "cowMilk"));
                    chkSoyMilk.setChecked(getBooleanValue(dataSnapshot, "soyMilk"));
                    chkButter.setChecked(getBooleanValue(dataSnapshot, "butter"));
                    chkCheese.setChecked(getBooleanValue(dataSnapshot, "cheese"));
                    chkYogurt.setChecked(getBooleanValue(dataSnapshot, "yogurt"));
                    chkCereal.setChecked(getBooleanValue(dataSnapshot, "cereal"));
                    chkGluten.setChecked(getBooleanValue(dataSnapshot, "gluten"));
                    chkWheat.setChecked(getBooleanValue(dataSnapshot, "wheat"));
                    chkBarley.setChecked(getBooleanValue(dataSnapshot, "barley"));
                    chkCrab.setChecked(getBooleanValue(dataSnapshot, "crab"));
                    chkPrawns.setChecked(getBooleanValue(dataSnapshot, "prawns"));
                    chkLobster.setChecked(getBooleanValue(dataSnapshot, "lobster"));
                    chkScallops.setChecked(getBooleanValue(dataSnapshot, "scallops"));
                    chkSalmon.setChecked(getBooleanValue(dataSnapshot, "salmon"));
                    chkOliveOil.setChecked(getBooleanValue(dataSnapshot, "oliveOil"));
                    chkSunflowerOil.setChecked(getBooleanValue(dataSnapshot, "sunflowerOil"));
                    chkCanolaOil.setChecked(getBooleanValue(dataSnapshot, "canolaOil"));
                    chkVegetableOil.setChecked(getBooleanValue(dataSnapshot, "vegetableOil"));
                    chkCoconutOil.setChecked(getBooleanValue(dataSnapshot, "coconutOil"));

                    // Load other allergies if they exist
                    String otherAllergic = dataSnapshot.child("other").getValue(String.class);
                    if (otherAllergic != null) {
                        txtOtherAllergic.setText(otherAllergic);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to load allergic history.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean getBooleanValue(DataSnapshot dataSnapshot, String key) {
        return dataSnapshot.child(key).getValue(Boolean.class) != null && dataSnapshot.child(key).getValue(Boolean.class);
    }
}
