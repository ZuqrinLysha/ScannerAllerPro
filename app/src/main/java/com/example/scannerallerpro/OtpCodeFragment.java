package com.example.scannerallerpro;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

public class OtpCodeFragment extends Fragment {

    private EditText etOtpCode;
    private Button btnVerifyCode;
    private FirebaseAuth auth;
    private String verificationId; // Store verification ID

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_otp_code, container, false);

        etOtpCode = view.findViewById(R.id.et_Otp_Code);
        btnVerifyCode = view.findViewById(R.id.btn_Enter_code);
        auth = FirebaseAuth.getInstance();

        // Get the verification ID from the arguments passed from ChangeNoPhoneFragment
        if (getArguments() != null) {
            verificationId = getArguments().getString("verificationId");
        }

        btnVerifyCode.setOnClickListener(v -> verifyOtp());

        return view;
    }

    private void verifyOtp() {
        String otpCode = etOtpCode.getText().toString().trim();

        if (otpCode.isEmpty()) {
            Toast.makeText(getContext(), "Please enter the OTP code", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a PhoneAuthCredential with the verification ID and entered OTP
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otpCode);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        auth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "Phone number verified successfully!", Toast.LENGTH_SHORT).show();

                        // Navigate to VerificationFragment to allow the user to change their phone number
                        navigateToVerificationFragment();
                    } else {
                        if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            Toast.makeText(getContext(), "Invalid verification code.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }



    private void navigateToVerificationFragment() {
        VerificationFragment verificationFragment = new VerificationFragment(); // Create an instance of VerificationFragment
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, verificationFragment); // Replace with the VerificationFragment
        transaction.addToBackStack(null); // Add to back stack for navigation
        transaction.commit(); // Commit the transaction
    }
}
