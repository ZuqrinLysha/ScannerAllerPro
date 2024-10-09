package com.example.scannerallerpro;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.util.ArrayList;
import java.util.List;

public class ScannerFragment extends Fragment {

    private Button btnScanner;
    private TextView txtScanner;
    private ImageView imgCaptured;
    private List<String> userAllergies = new ArrayList<>();
    private DatabaseReference databaseReference;
    private FirebaseAuth auth;

    private static final int REQUEST_CAMERA_CODE = 100;
    private static final int CAMERA_INTENT_CODE = 101;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_scanner, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize the Toolbar
        Toolbar toolbar = view.findViewById(R.id.toolbarScanner); // Use the correct ID from the layout
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        toolbar.setTitle(""); // Remove the title from the toolbar

        ImageButton btnBack = view.findViewById(R.id.backArrow);
        btnBack.setOnClickListener(v -> navigateBack());

        // Handle back button press
        toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());

        // Hide the main toolbar when entering the ScannerFragment
        if (getActivity() != null) {
            Toolbar mainToolbar = getActivity().findViewById(R.id.toolbarHomePage); // Assuming this is the ID of the main toolbar
            if (mainToolbar != null) {
                mainToolbar.setVisibility(View.GONE);
            }
        }

        btnScanner = view.findViewById(R.id.btnCapture);
        txtScanner = view.findViewById(R.id.txtScanner);
        imgCaptured = view.findViewById(R.id.imgCaptured);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        String userEmail = auth.getCurrentUser() != null ? auth.getCurrentUser().getEmail() : null;

        if (userEmail != null) {
            DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");
            usersRef.orderByChild("email").equalTo(userEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                            String fullName = userSnapshot.child("fullName").getValue(String.class);
                            if (fullName != null) {
                                databaseReference = FirebaseDatabase.getInstance()
                                        .getReference("Users")
                                        .child(fullName)
                                        .child("AllergicHistory");
                                fetchUserAllergies();
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

        btnScanner.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_INTENT_CODE);
            } else {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_CODE);
            }
        });
    }
    // Method to navigate back to HomeFragment
    private void navigateBack() {
        Fragment homeFragment = new HomeFragment(); // Create an instance of HomeFragment
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, homeFragment); // Assuming you have a container with this ID
        transaction.addToBackStack(null); // Optional: Add to back stack
        transaction.commit();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Show the main toolbar again when leaving the ScannerFragment
        if (getActivity() != null) {
            Toolbar mainToolbar = getActivity().findViewById(R.id.toolbarScanner);
            if (mainToolbar != null) {
                mainToolbar.setVisibility(View.VISIBLE);
            }
        }
    }

    private void fetchUserAllergies() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot allergySnapshot : snapshot.getChildren()) {
                        String allergyName = allergySnapshot.getKey();
                        Boolean hasAllergy = allergySnapshot.getValue(Boolean.class);
                        if (Boolean.TRUE.equals(hasAllergy)) {
                            userAllergies.add(allergyName);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to retrieve allergies.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_INTENT_CODE && resultCode == getActivity().RESULT_OK) {
            Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
            if (imageBitmap != null) {
                imgCaptured.setImageBitmap(imageBitmap);
                imgCaptured.setVisibility(View.VISIBLE);
                processImage(imageBitmap);
            }
        }
    }

    private void processImage(Bitmap imageBitmap) {
        InputImage image = InputImage.fromBitmap(imageBitmap, 0);
        TextRecognizerOptions options = new TextRecognizerOptions.Builder().build();
        TextRecognizer recognizer = TextRecognition.getClient(options);

        recognizer.process(image)
                .addOnSuccessListener(text -> {
                    Log.d("ScannerFragment", "Extracted Text: " + text.getText()); // Log the extracted text
                    checkAllergiesInText(text.getText()); // Proceed to check for allergies
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Text recognition failed.", Toast.LENGTH_SHORT).show();
                    Log.e("ScannerFragment", "Error: " + e.getMessage());
                });
    }

    private void checkAllergiesInText(String text) {
        List<String> detectedAllergies = new ArrayList<>();

        // Normalize the text to lower case for comparison
        String normalizedText = text.toLowerCase();

        // Check for direct allergens from the user's allergy list
        for (String allergy : userAllergies) {
            String normalizedAllergy = allergy.toLowerCase();

            // Check if the normalized text contains the normalized allergy
            if (normalizedText.contains(normalizedAllergy)) {
                detectedAllergies.add(allergy);
            }

            // Additional check for singular/plural variations
            if (normalizedAllergy.endsWith("s")) { // Check if allergy ends with 's'
                String singularForm = normalizedAllergy.substring(0, normalizedAllergy.length() - 1); // Remove the 's'
                if (normalizedText.contains(singularForm)) {
                    detectedAllergies.add(allergy);
                }
            } else {
                String pluralForm = normalizedAllergy + "s"; // Add 's' to form plural
                if (normalizedText.contains(pluralForm)) {
                    detectedAllergies.add(allergy);
                }
            }
        }

        // If allergens are detected, show the allergy alert dialog
        if (!detectedAllergies.isEmpty()) {
            showAllergyAlertDialog(detectedAllergies);
        } else {
            Toast.makeText(getContext(), "No allergies detected in the ingredients.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showAllergyAlertDialog(List<String> detectedAllergies) {
        // Inflate your custom dialog layout
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View customView = inflater.inflate(R.layout.warning_dialog, null); // Reference your dialog layout here

        // Find views in the custom layout
        TextView warningTitle = customView.findViewById(R.id.WarningTitle);
        TextView warningDesc = customView.findViewById(R.id.WarningDesc);
        Button warningDoneButton = customView.findViewById(R.id.WarningDone);

        // Set the title and description
        warningTitle.setText("Allergy Alert!");
        StringBuilder descriptionBuilder = new StringBuilder("The following allergens were detected:\n");
        for (String allergy : detectedAllergies) {
            descriptionBuilder.append("- ").append(allergy).append("\n");
        }
        warningDesc.setText(descriptionBuilder.toString());

        // Create the AlertDialog
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        dialogBuilder.setView(customView);
        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

        // Handle the done button click
        warningDoneButton.setOnClickListener(v -> alertDialog.dismiss());

// Show the dialog
        alertDialog.show();
    }
}