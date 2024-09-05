package com.example.scannerallerpro;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class ForgotPassword extends AppCompatActivity {
    Button btnReset, btnCancel;
    EditText txtForgotPassword;
    ProgressBar progressBarForgotPassword;
    FirebaseAuth Auth;
    RadioGroup radioGroupMethod;
    RadioButton radioEmail, radioPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        btnReset = findViewById(R.id.btnReset);
        btnCancel = findViewById(R.id.btnCancel);
        txtForgotPassword = findViewById(R.id.txtForgotPassword);
        progressBarForgotPassword = findViewById(R.id.progressBarForgotPassword);
        Auth = FirebaseAuth.getInstance();
        radioGroupMethod = findViewById(R.id.radioGroupMethod);
        radioEmail = findViewById(R.id.radioEmail);
        radioPhone = findViewById(R.id.radioPhone);

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input = txtForgotPassword.getText().toString().trim();
                int selectedId = radioGroupMethod.getCheckedRadioButtonId();

                if (TextUtils.isEmpty(input)) {
                    txtForgotPassword.setError("Input cannot be empty");
                    txtForgotPassword.requestFocus();
                    return;
                }

                if (selectedId == R.id.radioEmail) {
                    // Reset via email
                    resetPasswordByEmail(input);
                } else if (selectedId == R.id.radioPhone) {
                    // Reset via phone
                    resetPasswordByPhone(input);
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void resetPasswordByEmail(String email) {
        progressBarForgotPassword.setVisibility(View.VISIBLE);
        btnReset.setVisibility(View.INVISIBLE);

        Auth.sendPasswordResetEmail(email)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(ForgotPassword.this, "Check Your Email. Password reset link has been sent.", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(ForgotPassword.this, LogIn.class);
                        startActivity(intent);
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ForgotPassword.this, "Error! Reset Password Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        progressBarForgotPassword.setVisibility(View.INVISIBLE);
                        btnReset.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void resetPasswordByPhone(String phone) {
        progressBarForgotPassword.setVisibility(View.VISIBLE);
        btnReset.setVisibility(View.INVISIBLE);

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phone,                      // Phone number to verify
                60,                        // Timeout duration
                TimeUnit.SECONDS,          // Unit of timeout
                this,                      // Activity (for callback binding)
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                        progressBarForgotPassword.setVisibility(View.INVISIBLE);
                        btnReset.setVisibility(View.VISIBLE);
                        Toast.makeText(ForgotPassword.this, "Verification successful. Please enter the verification code.", Toast.LENGTH_SHORT).show();
                        // Handle auto verification or navigate to code input screen
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        progressBarForgotPassword.setVisibility(View.INVISIBLE);
                        btnReset.setVisibility(View.VISIBLE);
                        Toast.makeText(ForgotPassword.this, "Verification failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        super.onCodeSent(verificationId, forceResendingToken);
                        progressBarForgotPassword.setVisibility(View.INVISIBLE);
                        btnReset.setVisibility(View.VISIBLE);
                        Toast.makeText(ForgotPassword.this, "Verification code sent. Please check your messages.", Toast.LENGTH_SHORT).show();
                        // Save verificationId to use it later for verification
                        Intent intent = new Intent(ForgotPassword.this, LogIn.class);
                        intent.putExtra("verificationId", verificationId);
                        startActivity(intent);
                        finish();
                    }
                }
        );
    }
}
