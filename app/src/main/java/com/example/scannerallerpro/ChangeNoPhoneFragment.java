package com.example.scannerallerpro;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class ChangeNoPhoneFragment extends Fragment {

    private FirebaseAuth auth;
    private EditText currentPhoneField, verificationCodeField;
    private String verificationId; // Store the verification ID sent by Firebase
    private static final String PREDEFINED_CODE = "123456"; // Hard-coded verification code for testing

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_change_no_phone, container, false);

        // Initialize Firebase references
        auth = FirebaseAuth.getInstance();

        // Initialize fields
        currentPhoneField = view.findViewById(R.id.et_current_phone);
        verificationCodeField = view.findViewById(R.id.et_verification_code);

        // Load user's phone number from Firebase and set it in the input field
        loadUserPhoneNumber();

        // Initialize the back button and set click listener
        ImageButton btnBack = view.findViewById(R.id.back_button);
        btnBack.setOnClickListener(v -> navigateBack());

        // Initialize the "Send Code" button and set click listener
        Button btnVerify = view.findViewById(R.id.btn_verify);
        btnVerify.setOnClickListener(v -> sendVerificationCode());

        // Initialize the "Enter" button and set click listener
        Button btnEnter = view.findViewById(R.id.Enter);
        btnEnter.setOnClickListener(v -> verifyCodeAndProceed());

        return view;
    }

    private void loadUserPhoneNumber() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String phoneNumber = user.getPhoneNumber(); // Retrieve phone number from Firebase
            currentPhoneField.setText(phoneNumber); // Set it to the EditText
        }
    }

    private void sendVerificationCode() {
        String currentPhoneNumber = currentPhoneField.getText().toString().trim();

        if (TextUtils.isEmpty(currentPhoneNumber)) {
            Toast.makeText(getContext(), "Please enter your current mobile number", Toast.LENGTH_SHORT).show();
            return;
        }

        // Set up PhoneAuthProvider to send the verification code (actual code sending)
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(currentPhoneNumber)       // Phone number to verify
                .setTimeout(60L, TimeUnit.SECONDS)        // Timeout duration
                .setActivity(getActivity())               // Activity context
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(PhoneAuthCredential credential) {
                        // Automatically handled verification, proceed directly
                        Toast.makeText(getContext(), "Verification successful!", Toast.LENGTH_SHORT).show();
                        signInWithCredential(credential);
                    }

                    @Override
                    public void onVerificationFailed(FirebaseException e) {
                        if (e instanceof FirebaseAuthInvalidCredentialsException) {
                            Toast.makeText(getContext(), "Invalid phone number.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Verification failed. Please try again.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                        ChangeNoPhoneFragment.this.verificationId = verificationId; // Save verification ID
                        Toast.makeText(getContext(), "Verification code sent to your mobile!", Toast.LENGTH_SHORT).show();
                    }
                })
                .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void verifyCodeAndProceed() {
        String enteredCode = verificationCodeField.getText().toString().trim();

        // Check if the entered code matches the predefined code for testing
        if (TextUtils.isEmpty(enteredCode)) {
            Toast.makeText(getContext(), "Please enter the verification code", Toast.LENGTH_SHORT).show();
            return;
        }

        if (enteredCode.equals(PREDEFINED_CODE)) {
            // If the code matches, simulate successful verification
            Toast.makeText(getContext(), "Verification successful!", Toast.LENGTH_SHORT).show();
            // Navigate to the VerificationFragment
            Fragment verificationFragment = new VerificationFragment();
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, verificationFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        } else {
            Toast.makeText(getContext(), "Invalid verification code. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    // Method to navigate back
    private void navigateBack() {
        Fragment profileFragment = new ProfileFragment();
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, profileFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void signInWithCredential(PhoneAuthCredential credential) {
        auth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getContext(), "Sign in successful!", Toast.LENGTH_SHORT).show();
                            // Navigate to the VerificationFragment
                            Fragment verificationFragment = new VerificationFragment();
                            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                            transaction.replace(R.id.fragment_container, verificationFragment);
                            transaction.addToBackStack(null);
                            transaction.commit();
                        } else {
                            Toast.makeText(getContext(), "Sign in failed. Invalid code.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
