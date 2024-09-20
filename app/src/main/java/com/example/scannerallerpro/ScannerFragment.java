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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScannerFragment extends Fragment {

    private Button btnScanner;
    private TextView txtScanner;
    private List<String> userAllergies = new ArrayList<>();
    private DatabaseReference databaseReference;
    private FirebaseAuth auth;

    private static final int REQUEST_CAMERA_CODE = 100;
    private static final int CAMERA_INTENT_CODE = 101;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scanner, container, false);

        btnScanner = view.findViewById(R.id.btnCapture);
        txtScanner = view.findViewById(R.id.txtScanner);

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

        return view;
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
            extractTextFromImage(imageBitmap);
        }
    }

    private void extractTextFromImage(Bitmap bitmap) {
        InputImage image = InputImage.fromBitmap(bitmap, 0);
        TextRecognizer recognizer = TextRecognition.getClient(new TextRecognizerOptions.Builder().build());

        recognizer.process(image)
                .addOnSuccessListener(result -> {
                    StringBuilder extractedText = new StringBuilder();
                    for (Text.TextBlock block : result.getTextBlocks()) {
                        extractedText.append(block.getText()).append("\n");
                    }

                    String capturedText = extractedText.toString().trim();
                    txtScanner.setText(capturedText); // Display the captured text
                    highlightAllergens(capturedText); // Highlight allergens
                })
                .addOnFailureListener(e -> {
                    Log.e("OCR", "Text recognition failed", e);
                    Toast.makeText(getContext(), "Failed to recognize text.", Toast.LENGTH_SHORT).show();
                });
    }

    private void highlightAllergens(String text) {
        if (userAllergies.isEmpty()) {
            Toast.makeText(getContext(), "No allergies loaded!", Toast.LENGTH_SHORT).show();
            return;
        }

        SpannableString spannableText = new SpannableString(text);
        boolean allergenDetected = false;

        for (String allergy : userAllergies) {
            String regex = "\\b" + Pattern.quote(allergy) + "\\b";
            Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(text);

            while (matcher.find()) {
                spannableText.setSpan(new ForegroundColorSpan(Color.RED), matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                allergenDetected = true;
            }
        }

        txtScanner.setText(spannableText);

        }
    }
