package com.example.scannerallerpro;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

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
        return inflater.inflate(R.layout.fragment_scanner, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Hide the toolbar when entering the ScannerFragment
        if (getActivity() != null) {
            Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
            if (toolbar != null) {
                toolbar.setVisibility(View.GONE);
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


    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Show the toolbar again when leaving the ScannerFragment
        if (getActivity() != null) {
            Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
            if (toolbar != null) {
                toolbar.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

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

        // Build the message from detected allergies
        StringBuilder allergiesMessage = new StringBuilder();
        for (String allergy : detectedAllergies) {
            allergiesMessage.append(allergy).append("\n");
        }

        // Set the static text along with the detected allergies
        String fullMessage = "This ingredient contains the Allergic Reaction:\n" + allergiesMessage.toString();
        warningDesc.setText(fullMessage); // Set the message in the WarningDesc TextView

        // Create an AlertDialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(customView); // Set the custom view for the dialog

        // Create the dialog
        AlertDialog alertDialog = builder.create();

        // Set the button listener to dismiss the dialog
        warningDoneButton.setOnClickListener(v -> alertDialog.dismiss());

        // Show the dialog
        alertDialog.show();
    }
}
