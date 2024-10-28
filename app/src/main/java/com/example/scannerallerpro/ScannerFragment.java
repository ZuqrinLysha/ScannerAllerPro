package com.example.scannerallerpro;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
    private ImageView imgCaptured;
    private List<String> userAllergies = new ArrayList<>();
    private DatabaseReference databaseReference;
    private FirebaseAuth auth;

    private static final int REQUEST_CAMERA_CODE = 100;
    private static final int CAMERA_INTENT_CODE = 101;
    private static final String USERS_NODE = "Users";
    private static final String ALLERGIC_HISTORY_NODE = "AllergicHistory";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_scanner, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize the Toolbar
        Toolbar toolbar = view.findViewById(R.id.toolbarScanner);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        toolbar.setTitle("");

        // Hide the main toolbar when entering the ScannerFragment
        hideMainToolbar();

        btnScanner = view.findViewById(R.id.btnCapture);
        imgCaptured = view.findViewById(R.id.imgCaptured);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();
            databaseReference = FirebaseDatabase.getInstance()
                    .getReference(USERS_NODE)
                    .child(userId)
                    .child(ALLERGIC_HISTORY_NODE);
            fetchUserAllergies();
        }

        btnScanner.setOnClickListener(v -> checkCameraPermission());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        showMainToolbar();
    }

    private void hideMainToolbar() {
        if (getActivity() != null) {
            Toolbar mainToolbar = getActivity().findViewById(R.id.toolbarHomePage);
            if (mainToolbar != null) {
                mainToolbar.setVisibility(View.GONE);
            }
        }
    }

    private void showMainToolbar() {
        if (getActivity() != null) {
            Toolbar mainToolbar = getActivity().findViewById(R.id.toolbarHomePage);
            if (mainToolbar != null) {
                mainToolbar.setVisibility(View.VISIBLE);
            }
        }
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(cameraIntent, CAMERA_INTENT_CODE);
        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_CODE);
        }
    }

    private void fetchUserAllergies() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot allergySnapshot : snapshot.getChildren()) {
                        String allergyName = allergySnapshot.getKey();
                        Object hasAllergyValue = allergySnapshot.getValue();

                        // Check if the value is a Boolean or a String
                        if (hasAllergyValue instanceof Boolean && (Boolean) hasAllergyValue) {
                            userAllergies.add(allergyName);
                        } else if (hasAllergyValue instanceof String) {
                            String userInputAllergy = (String) hasAllergyValue;
                            if (!userInputAllergy.isEmpty()) {
                                userAllergies.add(userInputAllergy);
                            }
                        } else {
                            Log.e("ScannerFragment", "Invalid data type for allergy: " + allergyName);
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
        if (requestCode == CAMERA_INTENT_CODE && resultCode == getActivity().RESULT_OK && data != null) {
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
        TextRecognizer recognizer = TextRecognition.getClient(new TextRecognizerOptions.Builder().build());

        recognizer.process(image)
                .addOnSuccessListener(text -> {
                    Log.d("ScannerFragment", "Extracted Text: " + text.getText());
                    checkAllergiesInText(text.getText());
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Text recognition failed.", Toast.LENGTH_SHORT).show();
                    Log.e("ScannerFragment", "Error: " + e.getMessage());
                });
    }

    private void checkAllergiesInText(String text) {
        List<String> detectedAllergies = new ArrayList<>();
        String normalizedText = text.toLowerCase();

        for (String allergy : userAllergies) {
            String normalizedAllergy = allergy.toLowerCase();
            if (normalizedText.contains(normalizedAllergy) || checkPlurality(normalizedText, normalizedAllergy)) {
                detectedAllergies.add(allergy);
            }
        }

        if (!detectedAllergies.isEmpty()) {
            showAllergyAlertDialog(detectedAllergies);
        } else {
            Toast.makeText(getContext(), "No allergies detected in the ingredients.", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkPlurality(String text, String allergy) {
        String singularForm = allergy.endsWith("s") ? allergy.substring(0, allergy.length() - 1) : allergy + "s";
        return text.contains(singularForm) || text.contains(singularForm.endsWith("s") ? singularForm.substring(0, singularForm.length() - 1) : singularForm + "s");
    }

    private void showAllergyAlertDialog(List<String> detectedAllergies) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View customView = inflater.inflate(R.layout.warning_dialog, null);

        TextView warningTitle = customView.findViewById(R.id.WarningTitle);
        TextView warningDesc = customView.findViewById(R.id.WarningDesc);
        Button warningDoneButton = customView.findViewById(R.id.WarningDone);

        warningTitle.setText("Allergy Alert!");
        SpannableStringBuilder spannableDescription = createSpannableDescription(detectedAllergies);
        warningDesc.setText(spannableDescription);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(customView);
        AlertDialog alertDialog = builder.create();

        warningDoneButton.setOnClickListener(v -> alertDialog.dismiss());
        alertDialog.show();
    }

    private SpannableStringBuilder createSpannableDescription(List<String> detectedAllergies) {
        SpannableStringBuilder spannableDescription = new SpannableStringBuilder();
        String introText = "The following allergens were detected:\n";
        spannableDescription.append(introText);

        for (String allergy : detectedAllergies) {
            String allergyText = "• " + allergy + "\n";
            SpannableString spannableString = new SpannableString(allergyText);
            spannableString.setSpan(new android.text.style.ForegroundColorSpan(Color.RED), 2, 2 + allergy.length(), 0);
            spannableDescription.append(spannableString);
        }

        return spannableDescription;
    }
}
