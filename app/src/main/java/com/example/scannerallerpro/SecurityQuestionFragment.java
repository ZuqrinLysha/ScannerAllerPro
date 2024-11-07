package com.example.scannerallerpro;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
public class SecurityQuestionFragment extends Fragment {

    private static final long DELAY_MILLIS = 2000;
    private static final int MAX_ATTEMPTS = 2;
    private int failedAttempts = 0; // Counter for wrong attempts
    private Spinner securityQuestionSpinner;
    private EditText etSecurityAnswer;
    private Button btnEnterSecurity;
    private ProgressBar progressBar;
    private DatabaseReference userReference;
    private ImageView imgToggleQuestion;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_security_question, container, false);

        // Initialize views
        securityQuestionSpinner = view.findViewById(R.id.security_question);
        etSecurityAnswer = view.findViewById(R.id.et_security_question);
        btnEnterSecurity = view.findViewById(R.id.btn_enter_security);
        progressBar = view.findViewById(R.id.progressBar);
        imgToggleQuestion = view.findViewById(R.id.imgToggleQuestion);

        ImageButton btnBack = view.findViewById(R.id.back_button);
        btnBack.setOnClickListener(v -> navigateBack());

        // Populate spinner with predefined security questions
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.security_questions, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        securityQuestionSpinner.setAdapter(adapter);

        // Check if the user is blocked
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("UserPreferences", getContext().MODE_PRIVATE);
        boolean isBlocked = sharedPreferences.getBoolean("isBlocked", false);
        long blockEndTime = sharedPreferences.getLong("blockEndTime", 0);
        long currentTime = System.currentTimeMillis();

        if (isBlocked && currentTime < blockEndTime) {
            // Button is blocked
            btnEnterSecurity.setEnabled(false);
            long timeLeft = blockEndTime - currentTime;
            new Handler().postDelayed(() -> {
                btnEnterSecurity.setEnabled(true); // Re-enable after the blocking period
                Toast.makeText(getContext(), "You can try again now.", Toast.LENGTH_SHORT).show();
            }, timeLeft);
        } else {
            // Reset block status if block time has passed
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("isBlocked", false);
            editor.apply();
        }

        btnEnterSecurity.setOnClickListener(v -> {
            String selectedQuestion = securityQuestionSpinner.getSelectedItem().toString();
            String userAnswer = etSecurityAnswer.getText().toString().trim();

            if (userAnswer.isEmpty()) {
                Toast.makeText(getContext(), "Please enter your answer.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Disable button and show loading
            btnEnterSecurity.setEnabled(false);
            btnEnterSecurity.setBackgroundColor(getResources().getColor(R.color.softblue));
            progressBar.setVisibility(View.VISIBLE);

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                userReference = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());

                new Handler().postDelayed(() -> {
                    userReference.child("securityAnswer").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String correctAnswer = snapshot.getValue(String.class);

                            if (correctAnswer != null && correctAnswer.equals(userAnswer)) {
                                Toast.makeText(getContext(), "Answer is correct!", Toast.LENGTH_SHORT).show();
                                navigateToChangeNoPhoneFragment();
                            } else {
                                failedAttempts++; // Increment failed attempts
                                if (failedAttempts > MAX_ATTEMPTS) {
                                    showBlockDialog(); // Show block dialog
                                } else {
                                    showAlertDialog("Verification Failed", "The answer provided does not match your records.");
                                }
                            }

                            progressBar.setVisibility(View.GONE);
                            btnEnterSecurity.setEnabled(true);
                            btnEnterSecurity.setBackgroundColor(getResources().getColor(R.color.blue1));
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(getContext(), "Error retrieving data.", Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                            btnEnterSecurity.setEnabled(true);
                            btnEnterSecurity.setBackgroundColor(getResources().getColor(R.color.blue1));
                        }
                    });
                }, DELAY_MILLIS);
            } else {
                Toast.makeText(getContext(), "User not authenticated.", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                btnEnterSecurity.setEnabled(true);
                btnEnterSecurity.setBackgroundColor(getResources().getColor(R.color.blue1));
            }
        });

        imgToggleQuestion.setOnClickListener(v -> togglePasswordVisibility(etSecurityAnswer, imgToggleQuestion));

        return view;
    }

    private void blockUserForPeriod() {
        btnEnterSecurity.setEnabled(false); // Block the button
        // Save block time
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("UserPreferences", getContext().MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        long blockEndTime = System.currentTimeMillis() + 86400000; // 24 hours in milliseconds
        editor.putLong("blockEndTime", blockEndTime);
        editor.putBoolean("isBlocked", true);
        editor.apply();

        // Re-enable after 24 hours
        new Handler().postDelayed(() -> {
            btnEnterSecurity.setEnabled(true); // Re-enable the button after 24 hours
            failedAttempts = 0; // Reset failed attempts
            Toast.makeText(getContext(), "You can try again now.", Toast.LENGTH_SHORT).show();
        }, 86400000); // 24 hours in milliseconds
    }


    private void showAlertDialog(String title, String message) {
        new AlertDialog.Builder(getContext())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }


    // Method to show the block dialog after exceeding the maximum attempts
    private void showBlockDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("Account Blocked")
                .setMessage("Too many failed attempts! You have been blocked for 24 hours from changing mobile phone!.")
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    blockUserForPeriod();
                })
                .show();
    }

    // Method to navigate to the fragment for changing the phone number
    private void navigateToChangeNoPhoneFragment() {
        Fragment changeNoPhoneFragment = new ChangeNoPhoneFragment();
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, changeNoPhoneFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    // Method to navigate back with an alert dialog if there are unsaved changes in the answer field or spinner not selected
    private void navigateBack() {
        String userAnswer = etSecurityAnswer.getText().toString().trim();
        String selectedQuestion = securityQuestionSpinner.getSelectedItem().toString();

        // Check if the answer field is not empty or if the spinner hasn't been selected
        if (!userAnswer.isEmpty() || selectedQuestion.equals("Select a question")) {
            // Show a confirmation dialog if there is input in the answer field or the spinner is not selected
            new AlertDialog.Builder(getContext())
                    .setTitle("Unsaved Answer or Unselected Question")
                    .setMessage("You have entered an answer but haven't submitted it yet, or you haven't selected a security question. Are you sure you want to leave this page?")
                    .setPositiveButton("Yes", (dialog, which) -> getParentFragmentManager().popBackStack()) // Navigate back if "Yes"
                    .setNegativeButton("No", null) // Do nothing if "No"
                    .show();
        } else {
            // No input in the answer field and a security question is selected, just navigate back
            getParentFragmentManager().popBackStack();
        }
    }



    // Method to toggle password visibility
    private void togglePasswordVisibility(EditText editText, ImageView toggleIcon) {
        if (editText.getTransformationMethod().equals(PasswordTransformationMethod.getInstance())) {
            // Show password
            editText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            toggleIcon.setImageResource(R.drawable.eyepassword); // Update the icon to an open eye
        } else {
            // Hide password
            editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
            toggleIcon.setImageResource(R.drawable.eyepassword); // Update the icon to a closed eye
        }
    }
}