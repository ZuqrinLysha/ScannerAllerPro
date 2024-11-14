package com.example.scannerallerpro;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPassword extends AppCompatActivity {
    Button btnReset, btnCancel;
    EditText txtForgotPassword;
    ProgressBar progressBarForgotPassword;
    FirebaseAuth Auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        btnReset = findViewById(R.id.btnReset);
        btnCancel = findViewById(R.id.btnCancel);
        txtForgotPassword = findViewById(R.id.txtForgotPassword);
        progressBarForgotPassword = findViewById(R.id.progressBarForgotPassword);
        Auth = FirebaseAuth.getInstance();

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = txtForgotPassword.getText().toString().trim();

                if (TextUtils.isEmpty(email)) {
                    txtForgotPassword.setError("Email cannot be empty");
                    txtForgotPassword.requestFocus();
                    return;
                }

                resetPasswordByEmail(email);
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
                        Toast.makeText(ForgotPassword.this, "Check Your Email. Password reset link has been sent.",
                                Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(ForgotPassword.this, LogIn.class);
                        startActivity(intent);
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ForgotPassword.this, "Error! Reset Password Failed: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        progressBarForgotPassword.setVisibility(View.INVISIBLE);
                        btnReset.setVisibility(View.VISIBLE);
                    }
                });
    }
}
