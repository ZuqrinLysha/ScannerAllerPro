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

import org.apache.commons.text.similarity.LevenshteinDistance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private List<String> commonMisspellings = Arrays.asList(
            "peanu", "punut", "penut", "peanit",  // Peanut
            "lobstar", "lobsterr", "lobsterrr", "lobtser",  // Lobster
            "paprica", "papricaa", "paprikka", "paprikca",  // Paprika
            "mlik", "mik", "mliks", "milkk",  // Milk
            "eg", "egs", "egss",  // Egg
            "soya", "soyas", "soyy", "soyybeans",  // Soy
            "wheet", "wht", "wheattt",  // Wheat
            "glutten", "gluteen", "glueten", "gluttenous",  // Gluten
            "fishe", "fis", "fsh", "fshh",  // Fish
            "shelfish", "shellfich", "shelpfish",  // Shellfish
            "sesami", "seasame", "sesmse", "sesamae",  // Sesame
            "mastard", "mustrd", "musatd", "mustarrd",  // Mustard
            "citris", "citrouss", "citruos", "citrs",  // Citrus
            "treenuts", "truenuts", "trnut", "tnree nuts",  // Tree nuts
            "garlick", "garlik", "gralic", "grllc",  // Garlic
            "hony", "huny", "honeys", "honney",  // Honey
            "corns", "crn", "corm", "cornn",  // Corn
            "choclolate", "chocoalte", "choclate", "chcolate",  // Chocolate
            "almont", "almand", "almnd", "almoond",  // Almond
            "casheww", "cashew nut", "chashew", "cashewz",  // Cashew
            "strawberyy", "strwberry", "stawberry", "strawberrry",  // Strawberry
            "strawberyy", "tomatoe", "tommato", "tomoto", "tommatoe",  // Tomato
            "avacodo", "avacoda", "avocaddo", "avoacdo",  // Avocado
            "chese", "cheeze", "chz", "cese"  // Cheese
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_scanner, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Toolbar toolbar = view.findViewById(R.id.toolbarScanner);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        toolbar.setTitle("");

        hideMainToolbar();

        btnScanner = view.findViewById(R.id.btnCapture);
        imgCaptured = view.findViewById(R.id.imgCaptured);

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
            cameraIntent.putExtra("android.intent.extras.CAMERA_FACING", 0); // Use rear camera
            cameraIntent.putExtra("android.intent.extra.focusMode", "continuous-picture");
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

                        if (hasAllergyValue instanceof Boolean && (Boolean) hasAllergyValue) {
                            userAllergies.add(allergyName);
                        } else if (hasAllergyValue instanceof String) {
                            String userInputAllergy = (String) hasAllergyValue;
                            if (!userInputAllergy.isEmpty()) {
                                userAllergies.add(userInputAllergy);
                            }
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
        // Scale the bitmap for better clarity in ImageView
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(imageBitmap, 800, 800, true);
        imgCaptured.setImageBitmap(scaledBitmap);
        imgCaptured.setVisibility(View.VISIBLE);

        // Process the scaled bitmap with ML Kit
        InputImage image = InputImage.fromBitmap(scaledBitmap, 0);
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
        String normalizedText = normalizeText(text);

        for (String allergy : userAllergies) {
            String normalizedAllergy = normalizeText(allergy);

            if (matchesAllergy(normalizedText, normalizedAllergy) || isMisspelled(normalizedText, allergy)) {
                detectedAllergies.add(allergy);
            }
        }

        if (!detectedAllergies.isEmpty()) {
            showAllergyAlertDialog(detectedAllergies);
        } else {
            Toast.makeText(getContext(), "No allergies detected in the ingredients.", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isMisspelled(String input, String allergy) {
        LevenshteinDistance levenshtein = new LevenshteinDistance();
        int distance = levenshtein.apply(input, allergy);
        return distance <= 2;  // Allow a small Levenshtein distance
    }

    private boolean isPluralOrPossessive(String input) {
        // Memastikan input adalah dalam huruf kecil dan memeriksa jika input berakhir dengan 's' atau "'s"
        input = input.toLowerCase().trim(); // Normalisasi huruf
        return input.endsWith("s") || input.endsWith("'s");
    }

    private boolean matchesAllergy(String text, String allergy) {
        text = text.toLowerCase().trim();
        allergy = allergy.toLowerCase().trim();

        // Regex pattern to allow plural and possessive forms
        String pattern = "\\b" + allergy + "(s|'s)?\\b";  // Match plural or possessive forms
        pattern = pattern.replace("'s", "'s\\b"); // Handling possessive forms more precisely

        Log.d("ScannerFragment", "Checking allergy: " + allergy + " with pattern: " + pattern);

        Pattern regexPattern = Pattern.compile(pattern);
        Matcher matcher = regexPattern.matcher(text);

        return matcher.find();
    }

    private String normalizeText(String text) {
        text = text.toLowerCase().trim();
        text = text.replaceAll("[^a-zA-Z0-9 ]", "");  // Keep only alphanumeric characters and spaces
        return text;
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
            spannableString.setSpan(new android.text.style.ForegroundColorSpan(Color.RED), 0, spannableString.length(), 0);
            spannableDescription.append(spannableString);
        }

        return spannableDescription;
    }
}