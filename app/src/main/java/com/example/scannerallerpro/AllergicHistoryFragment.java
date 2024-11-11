package com.example.scannerallerpro;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import org.apache.commons.text.similarity.LevenshteinDistance;


import java.util.HashMap;
import java.util.Map;

public class AllergicHistoryFragment extends Fragment {

    private CheckBox[] allergyCheckBoxes;
    private EditText txtOtherAllergic;
    private DatabaseReference databaseReference;
    private FirebaseAuth auth;
    private boolean hasUnsavedChanges = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_allergic_history, container, false);

        initializeUI(view);

        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            databaseReference = FirebaseDatabase.getInstance()
                    .getReference("Users")
                    .child(userId)
                    .child("AllergicHistory");

            loadAllergicHistory();
        }

        return view;
    }

    private void initializeUI(View view) {
        // Initialize CheckBoxes
        initializeCheckBoxes(view);

        txtOtherAllergic = view.findViewById(R.id.txtOtherAllergic);
        txtOtherAllergic.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                hasUnsavedChanges = true;
            }
            @Override
            public void afterTextChanged(android.text.Editable editable) {}
        });

        Button btnSave = view.findViewById(R.id.button_save);
        ImageButton btnBack = view.findViewById(R.id.backArrow);

        btnSave.setOnClickListener(v -> saveAllergicHistory());
        btnBack.setOnClickListener(v -> handleBackNavigation());
    }

    private void initializeCheckBoxes(View view) {
        allergyCheckBoxes = new CheckBox[]{
                view.findViewById(R.id.chkPeanuts),
                view.findViewById(R.id.chkSoybeans),
                view.findViewById(R.id.chkCashew),
                view.findViewById(R.id.chkAlmond),
                view.findViewById(R.id.chkWalnuts),
                view.findViewById(R.id.chkCowMilk),
                view.findViewById(R.id.chkSoyMilk),
                view.findViewById(R.id.chkButter),
                view.findViewById(R.id.chkCheese),
                view.findViewById(R.id.chkYogurt),
                view.findViewById(R.id.chkCereal),
                view.findViewById(R.id.chkGluten),
                view.findViewById(R.id.chkWheat),
                view.findViewById(R.id.chkBarley),
                view.findViewById(R.id.chkCrab),
                view.findViewById(R.id.chkPrawns),
                view.findViewById(R.id.chkLobster),
                view.findViewById(R.id.chkScallops),
                view.findViewById(R.id.chkSalmon),
                view.findViewById(R.id.chkOliveOil),
                view.findViewById(R.id.chkSunflowerOil),
                view.findViewById(R.id.chkCanolaOil),
                view.findViewById(R.id.chkVegetableOil),
                view.findViewById(R.id.chkCoconutOil)
        };

        for (CheckBox checkBox : allergyCheckBoxes) {
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> hasUnsavedChanges = true);
        }
    }

    private void handleBackNavigation() {
        if (hasUnsavedChanges) {
            new AlertDialog.Builder(getContext())
                    .setTitle("Unsaved Changes")
                    .setMessage("You have unsaved changes. Do you want to save them before leaving?")
                    .setPositiveButton("Yes", (dialog, which) -> saveAllergicHistoryAndNavigate())
                    .setNegativeButton("No", (dialog, which) -> navigateBack())
                    .show();
        } else {
            navigateBack();
        }
    }

    private void navigateBack() {
        Fragment homeFragment = new HomeFragment();
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, homeFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void saveAllergicHistoryAndNavigate() {
        saveAllergicHistory();
        navigateBack();
    }

    private void saveAllergicHistory() {
        if (databaseReference == null) {
            Toast.makeText(getContext(), "Unable to save. Please try again later.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> allergies = getAllergiesMap();
        String otherAllergic = txtOtherAllergic.getText().toString().trim();
        Log.d("AllergicHistory", "Other Allergies Text: " + otherAllergic);

        if (!otherAllergic.isEmpty()) {
            // Check for cow milk and soy keywords
            if (containsMilkOrWhey(otherAllergic) && allergyCheckBoxes[5].isChecked()) {
                Log.d("AllergicHistory", "Detected cow milk keyword and checkbox is checked.");
                allergies.put("Milk", true);
            } else {
                Log.d("AllergicHistory", "Cow milk keyword not detected or checkbox not checked.");
            }

            if (containsSoy(otherAllergic) && allergyCheckBoxes[1].isChecked()) {
                Log.d("AllergicHistory", "Detected soy keyword and checkbox is checked.");
                allergies.put("soy", true);
                allergies.put("soybeans", true);
            } else {
                Log.d("AllergicHistory", "Soy keyword not detected or checkbox not checked.");
            }

            // Store the exact text entered by the user
            allergies.put("other", otherAllergic);
        }

        // Debug the complete allergies map before saving
        Log.d("AllergicHistory", "Allergies Map to Save: " + allergies);

        databaseReference.setValue(allergies).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(getContext(), "Allergic history saved successfully!", Toast.LENGTH_SHORT).show();
                hasUnsavedChanges = false;
            } else {
                Toast.makeText(getContext(), "Failed to save allergic history.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private boolean containsMilkOrWhey(String input) {
        // Normalize input: removing spaces and apostrophes
        input = input.toLowerCase().replace("'", "").replace(" ", "");

        Log.d("AllergicHistory", "Normalized Input (Milk/Whey): " + input);

        // List of milk-related keywords, normalized
        String[] milkKeywords = {"milk", "Cow's ", "cheese", "butter", "yogurt", "cream", "dairy", "whey"};

        for (String keyword : milkKeywords) {
            // Check if input contains any milk-related keyword
            if (input.contains(keyword)) {
                Log.d("AllergicHistory", "Detected milk-related keyword: " + keyword);
                return true;
            }
        }

        return false;
    }


    private boolean containsSoy(String input) {
        // Normalize the input by removing apostrophes, converting to lowercase, and removing spaces between words
        input = input.trim().toLowerCase().replace("'", "").replace(" ", "");

        Log.d("AllergicHistory", "Normalized Input (Soy): " + input);  // Log the normalized input

        // Define keywords for soy-related products (normalize the keywords as well)
        String[] soyKeywords = {"soy", "soybean", "soybeans", "soymilk", "soy's", "soy milk"};

        for (String keyword : soyKeywords) {
            // Normalize the keyword (remove apostrophes and spaces)
            String normalizedKeyword = keyword.toLowerCase().replace("'", "").replace(" ", "");

            if (input.contains(normalizedKeyword)) {
                Log.d("AllergicHistory", "Detected soy-related keyword: " + keyword);  // Log detected keyword
                return true;
            }
        }

        return false;
    }

    // Method to detect misspellings using Levenshtein Distance
    private boolean isMisspelled(String input, String keyword) {
        LevenshteinDistance levenshtein = new LevenshteinDistance();
        int distance = levenshtein.apply(input, keyword);
        return distance <= 2;  // You can adjust the threshold for detecting misspellings
    }

    private Map<String, Object> getAllergiesMap() {
        Map<String, Object> allergies = new HashMap<>();
        String[] allergyKeys = {"peanuts", "soybeans", "cashew", "almond", "walnuts",
                "milk", "soy", "butter", "cheese", "yogurt",
                "cereal", "gluten", "wheat", "barley", "crab",
                "prawns", "lobster", "scallops", "salmon", "oliveOil",
                "sunflowerOil", "canolaOil", "vegetableOil", "coconutOil"};

        for (int i = 0; i < allergyCheckBoxes.length; i++) {
            allergies.put(allergyKeys[i], allergyCheckBoxes[i].isChecked());
        }

        String otherAllergic = txtOtherAllergic.getText().toString().trim();
        if (!otherAllergic.isEmpty()) {
            allergies.put("other", otherAllergic);
        }
        return allergies;
    }

    private void loadAllergicHistory() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String[] allergyKeys = {"peanuts", "soybeans", "cashew", "almond", "walnuts",
                            "milk", "soy", "butter", "cheese", "yogurt",
                            "cereal", "gluten", "wheat", "barley", "crab",
                            "prawns", "lobster", "scallops", "salmon", "oliveOil",
                            "sunflowerOil", "canolaOil", "vegetableOil", "coconutOil"};

                    for (int i = 0; i < allergyCheckBoxes.length; i++) {
                        allergyCheckBoxes[i].setChecked(dataSnapshot.child(allergyKeys[i]).getValue(Boolean.class) != null
                                && dataSnapshot.child(allergyKeys[i]).getValue(Boolean.class));
                    }

                    String otherAllergy = dataSnapshot.child("other").getValue(String.class);
                    if (otherAllergy != null) {
                        txtOtherAllergic.setText(otherAllergy);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to load allergic history.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
