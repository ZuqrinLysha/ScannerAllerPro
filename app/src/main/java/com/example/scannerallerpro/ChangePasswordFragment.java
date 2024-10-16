package com.example.scannerallerpro;

import android.content.DialogInterface;
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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordFragment extends Fragment {

    private EditText etCurrentPassword, etNewPassword, etConfirmNewPassword;
    private Button btnChangePassword;
    private FirebaseAuth auth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_change_password, container, false);

        etCurrentPassword = view.findViewById(R.id.et_old_password);
        etNewPassword = view.findViewById(R.id.et_new_password);
        etConfirmNewPassword = view.findViewById(R.id.et_confirm_password);
        btnChangePassword = view.findViewById(R.id.btn_save_password);
        ImageButton btnBack = view.findViewById(R.id.back_button);

        // Set up the back button's click listener
        btnBack.setOnClickListener(v -> navigateBack());

        auth = FirebaseAuth.getInstance();

        btnChangePassword.setOnClickListener(v -> changePassword());

        return view;
    }

    // Method to navigate back with an alert dialog if there are unsaved changes
    private void navigateBack() {
        String currentPassword = etCurrentPassword.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmNewPassword = etConfirmNewPassword.getText().toString().trim();

        // Check if there are unsaved changes
        if (!currentPassword.isEmpty() || !newPassword.isEmpty() || !confirmNewPassword.isEmpty()) {
            // Show a confirmation dialog if fields are filled but not saved
            new AlertDialog.Builder(getActivity())
                    .setTitle("Unsaved Changes")
                    .setMessage("You have unsaved changes. Do you want to save before leaving?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            changePassword(); // Attempt to change password before navigating back
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Navigate back to ProfileFragment without saving
                            Fragment profileFragment = new ProfileFragment();
                            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                            transaction.replace(R.id.fragment_container, profileFragment);
                            transaction.addToBackStack(null);
                            transaction.commit();
                        }
                    })
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

    private void changePassword() {
        String currentPassword = etCurrentPassword.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmNewPassword = etConfirmNewPassword.getText().toString().trim();

        // Validate input
        if (TextUtils.isEmpty(currentPassword) || TextUtils.isEmpty(newPassword) || TextUtils.isEmpty(confirmNewPassword)) {
            Toast.makeText(getContext(), "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword.equals(confirmNewPassword)) {
            Toast.makeText(getContext(), "New password and confirmation do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);

            // Re-authenticate the user
            user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        // Update the password for the same user (no new row created)
                        user.updatePassword(newPassword).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(getContext(), "Password changed successfully!", Toast.LENGTH_SHORT).show();
                                    // Navigate back to ProfileFragment or close the current fragment
                                    navigateBack(); // Optional: Go back to profile
                                } else {
                                    Toast.makeText(getContext(), "Failed to change password: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else {
                        Toast.makeText(getContext(), "Current password is incorrect: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}
