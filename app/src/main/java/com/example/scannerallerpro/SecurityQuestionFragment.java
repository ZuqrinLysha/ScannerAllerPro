package com.example.scannerallerpro;

import android.app.AlertDialog;
import android.content.DialogInterface;
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

    // Method to show the block dialog after exceeding the maximum attempts
    private void showBlockDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("Account Blocked")
                .setMessage("Too many failed attempts. Please wait 10 seconds before trying again.")
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    blockUserForPeriod();
                })
                .show();
    }

    // Method to block the user from retrying for 10 seconds
    private void blockUserForPeriod() {
        btnEnterSecurity.setEnabled(false); // Block the button
        new Handler().postDelayed(() -> {
            btnEnterSecurity.setEnabled(true); // Re-enable after 24 jam
            failedAttempts = 0; // Reset failed attempts
            Toast.makeText(getContext(), "You can try again now.", Toast.LENGTH_SHORT).show();
        }, 86400000); // 24 jam seconds delay
    }

    // Method to navigate to the fragment for changing the phone number
    private void navigateToChangeNoPhoneFragment() {
        Fragment changeNoPhoneFragment = new ChangeNoPhoneFragment();
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, changeNoPhoneFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    // Method to navigate back to the previous fragment
    private void navigateBack() {
        getParentFragmentManager().popBackStack();
    }

    // Method to show an alert dialog for wrong answers
    private void showAlertDialog(String title, String message) {
        new AlertDialog.Builder(getContext())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .show();
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
