package com.example.scannerallerpro;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class ResetSecurityQuestionFragment extends Fragment {

    private Spinner resetSecurityQuestionSpinner;
    private EditText etResetSecurityQuestion;
    private Button btnSubmitSecurityQuestion;
    private ImageView imgToggleQuestion;
    private FirebaseAuth auth;
    private ProgressBar progressBar; // Progress bar
    private DatabaseReference reference;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reset_security_question, container, false);

        // Initialize views AFTER inflating the layout
        resetSecurityQuestionSpinner = view.findViewById(R.id.reset_security_question);
        etResetSecurityQuestion = view.findViewById(R.id.et_reset_security_question);
        btnSubmitSecurityQuestion = view.findViewById(R.id.btn_submit_security_question);
        imgToggleQuestion = view.findViewById(R.id.imgToggleQuestion);
        progressBar = view.findViewById(R.id.progressBar);

        // Populate spinner with security questions (example)
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.security_questions, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        resetSecurityQuestionSpinner.setAdapter(adapter); // Now this should work

        // Initialize Firebase Auth and references
        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            reference = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());
        }

        // Initialize views
        resetSecurityQuestionSpinner = view.findViewById(R.id.reset_security_question);
        etResetSecurityQuestion = view.findViewById(R.id.et_reset_security_question);
        btnSubmitSecurityQuestion = view.findViewById(R.id.btn_submit_security_question);
        imgToggleQuestion = view.findViewById(R.id.imgToggleQuestion); // Initialize ImageView
        progressBar = view.findViewById(R.id.progressBar); // Progress bar

        // Set up the back button's click listener
        ImageButton btnBack = view.findViewById(R.id.back_button);
        btnBack.setOnClickListener(v -> navigateBack());

        // Set up the button click listener
        btnSubmitSecurityQuestion.setOnClickListener(v -> updateSecurityQuestion());

        // Setting up the eye toggle for question visibility
        imgToggleQuestion.setOnClickListener(v -> togglePasswordVisibility(etResetSecurityQuestion, imgToggleQuestion));

        return view; // Return the inflated view
    }

    private void navigateBack() {
        String userAnswer = etResetSecurityQuestion.getText().toString().trim();

        // Check if there are unsaved changes
        if (!userAnswer.isEmpty() || resetSecurityQuestionSpinner.getSelectedItemPosition() != 0) {
            // Show a confirmation dialog if fields are filled but not saved
            new AlertDialog.Builder(getActivity())
                    .setTitle("Unsaved Changes")
                    .setMessage("You have unsaved changes. Do you want to discard them?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        // Navigate back to ProfileFragment without saving
                        Fragment profileFragment = new ProfileFragment();
                        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                        transaction.replace(R.id.fragment_container, profileFragment);
                        transaction.addToBackStack(null);
                        transaction.commit();
                    })
                    .setNegativeButton("No", null)
                    .show(); // Show the dialog
        } else {
            // No unsaved changes, just navigate back to ProfileFragment
            Fragment profileFragment = new ProfileFragment();
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, profileFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }

    private void updateSecurityQuestion() {
        String selectedQuestion = resetSecurityQuestionSpinner.getSelectedItem().toString();
        String answer = etResetSecurityQuestion.getText().toString().trim();

        // Validate input fields
        if (selectedQuestion.isEmpty() || answer.isEmpty()) {
            Toast.makeText(getContext(), "Please select a question and provide an answer.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Change the button to grey to indicate loading
        btnSubmitSecurityQuestion.setEnabled(false); // Disable the button
        btnSubmitSecurityQuestion.setBackgroundColor(getResources().getColor(R.color.softblue)); // Change color to grey

        // Show the progress bar
        progressBar.setVisibility(View.VISIBLE);

        // Create a HashMap to hold the updated data
        HashMap<String, Object> updates = new HashMap<>();
        updates.put("securityQuestion", selectedQuestion);
        updates.put("securityAnswer", answer);

        // Update the security question and answer in Firebase
        reference.updateChildren(updates).addOnCompleteListener(task -> {
            // Set a delay of 10 seconds before hiding the progress bar
            new Handler().postDelayed(() -> {
                progressBar.setVisibility(View.GONE); // Hide progress bar
                btnSubmitSecurityQuestion.setEnabled(true); // Re-enable the button
                btnSubmitSecurityQuestion.setBackgroundColor(getResources().getColor(R.color.blue1)); // Restore original color

                if (task.isSuccessful()) {
                    Toast.makeText(getContext(), "Security question updated successfully!", Toast.LENGTH_SHORT).show();
                    // Navigate to ProfileFragment after successful update
                    Fragment profileFragment = new ProfileFragment();
                    FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                    transaction.replace(R.id.fragment_container, profileFragment);
                    transaction.addToBackStack(null);
                    transaction.commit();
                } else {
                    Toast.makeText(getContext(), "Failed to update security question.", Toast.LENGTH_SHORT).show();
                }
            }, 5000); // 5 seconds delay (10000 milliseconds)
        });
    }


    // Function to toggle password visibility
    private void togglePasswordVisibility(EditText editText, ImageView imageView) {
        if (editText.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
            // Show the password
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            imageView.setImageResource(R.drawable.baseline_remove_red_eye_24); // Eye open drawable
        } else {
            // Hide the password
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            imageView.setImageResource(R.drawable.eyepassword); // Eye closed drawable
        }
        editText.setSelection(editText.getText().length()); // Keep cursor at the end
    }
}
