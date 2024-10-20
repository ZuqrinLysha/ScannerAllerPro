package com.example.scannerallerpro;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class ChangeNoPhoneFragment extends Fragment {

    private PhoneAuthProvider.ForceResendingToken forceResendingToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    private String mVerificationId;
    private static final String TAG = "MAIN_TAG";
    private ProgressDialog pd;
    private FirebaseAuth firebaseAuth; // Declare FirebaseAuth

    // Declare views
    private EditText etCurrentPhone;
    private Button btnSendCode;
    private ImageView backButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_change_no_phone, container, false);

        // Initialize views
        etCurrentPhone = view.findViewById(R.id.et_current_phone);
        btnSendCode = view.findViewById(R.id.btn_sendCode);
        ImageButton btnBack = view.findViewById(R.id.back_button);

        // Set up the back button's click listener
        btnBack.setOnClickListener(v -> navigateBack());

        firebaseAuth = FirebaseAuth.getInstance();
        pd = new ProgressDialog(getActivity());
        pd.setTitle("Please wait");
        pd.setCanceledOnTouchOutside(false);

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                // Automatically verifies the code
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                pd.dismiss();
                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    Toast.makeText(getActivity(), "Invalid phone number.", Toast.LENGTH_SHORT).show();
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    Toast.makeText(getActivity(), "Quota exceeded. Try again later.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "Verification failed. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                mVerificationId = verificationId;
                forceResendingToken = token;
                pd.dismiss();
                Toast.makeText(getActivity(), "Code sent! Please check your SMS.", Toast.LENGTH_SHORT).show();

                // Navigate to OTP input fragment
                navigateToOtpFragment();
            }
        };

        btnSendCode.setOnClickListener(v -> {
            String phoneNumber = etCurrentPhone.getText().toString().trim();
            if (TextUtils.isEmpty(phoneNumber)) {
                Toast.makeText(getActivity(), "Please enter your phone number", Toast.LENGTH_SHORT).show();
                return;
            } else {
                startPhoneNumberVerification(phoneNumber);
            }
        });

        return view; // Return the inflated view
    }

    private void navigateBack() {
        String et_current_phone = etCurrentPhone.getText().toString().trim();
        if (!et_current_phone.isEmpty()) {
            new AlertDialog.Builder(getActivity())
                    .setTitle("Unsaved Changes")
                    .setMessage("You have unsent the code. Do you want to send it now?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        // Send the code if the user confirms
                        startPhoneNumberVerification(et_current_phone); // Pass the current phone number
                    })
                    .setNegativeButton("No", (dialog, which) -> {
                        // Navigate back to ProfileFragment without sending the code
                        Fragment profileFragment = new ProfileFragment();
                        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                        transaction.replace(R.id.fragment_container, profileFragment);
                        transaction.addToBackStack(null);
                        transaction.commit();
                    })
                    .show(); // Show the dialog
        } else {
            // No unsent code, just navigate back to ProfileFragment
            Fragment profileFragment = new ProfileFragment();
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, profileFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }



    private void startPhoneNumberVerification(String phone) {
        pd.setMessage("Verifying Phone Number");
        pd.show();

        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber("+601111161720")       // Phone number to verify
                .setTimeout(60L, TimeUnit.SECONDS) // Timeout duration
                .setActivity(getActivity())  // Activity for callback binding
                .setCallbacks(mCallbacks)    // OnVerificationStateChangedCallbacks
                .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void navigateToOtpFragment() {
        OtpCodeFragment otpCodeFragment = new OtpCodeFragment();
        Bundle args = new Bundle();
        args.putString("verificationId", mVerificationId); // Pass verification ID to the next fragment
        otpCodeFragment.setArguments(args); // Set arguments for the OTP fragment

        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, otpCodeFragment); // Replace with your fragment container ID
        transaction.addToBackStack(null); // Optional: add this transaction to the back stack
        transaction.commit();
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), task -> {
                    if (task.isSuccessful()) {
                        // Phone number verified successfully
                        Toast.makeText(getActivity(), "Phone number verified!", Toast.LENGTH_SHORT).show();
                        // Proceed with your app's logic after verification
                    } else {
                        // Verification failed
                        if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            Toast.makeText(getActivity(), "Invalid verification code.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}