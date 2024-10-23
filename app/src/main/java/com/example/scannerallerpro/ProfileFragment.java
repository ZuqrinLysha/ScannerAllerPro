    package com.example.scannerallerpro;

    import android.content.Context;
    import android.content.DialogInterface;
    import android.os.Bundle;
    import android.text.InputType;
    import android.util.Log;
    import android.view.LayoutInflater;
    import android.view.View;
    import android.view.ViewGroup;
    import android.widget.EditText;
    import android.widget.Switch;
    import android.widget.TextView;
    import android.widget.Toast;
    import android.content.Intent;
    import android.view.Menu;
    import android.view.MenuInflater;
    import android.view.MenuItem;


    import androidx.annotation.NonNull;
    import androidx.annotation.Nullable;
    import androidx.appcompat.app.ActionBarDrawerToggle;
    import androidx.appcompat.app.AlertDialog;
    import androidx.appcompat.app.AppCompatActivity;
    import androidx.appcompat.app.AppCompatDelegate;
    import androidx.appcompat.widget.Toolbar;
    import androidx.cardview.widget.CardView;
    import androidx.drawerlayout.widget.DrawerLayout;
    import androidx.fragment.app.Fragment;
    import androidx.fragment.app.FragmentTransaction;

    import com.google.firebase.auth.FirebaseAuth;
    import com.google.firebase.auth.FirebaseUser;
    import com.google.firebase.database.DataSnapshot;
    import com.google.firebase.database.DatabaseError;
    import com.google.firebase.database.DatabaseReference;
    import com.google.firebase.database.FirebaseDatabase;
    import com.google.firebase.database.ValueEventListener;

    public class ProfileFragment extends Fragment {

        private FirebaseDatabase database;
        private DatabaseReference userRef;
        private FirebaseAuth auth;
        private Switch switchTheme;

        // TextViews for displaying user data
        private TextView displayWeight, displayHeight, displayBmi, displayBloodType;


        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

            // Initialize Firebase
            database = FirebaseDatabase.getInstance();
            auth = FirebaseAuth.getInstance();
            FirebaseUser currentUser = auth.getCurrentUser();

            // Initialize the Toolbar
            Toolbar toolbar = rootView.findViewById(R.id.ToolbarLogout); // Use rootView instead of view
            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
            setHasOptionsMenu(true); // Important to enable options menu in this fragment

            if (((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
                ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);
            }

            // Set up UI elements
            switchTheme = rootView.findViewById(R.id.switchTheme);
            displayHeight = rootView.findViewById(R.id.display_height);
            displayWeight = rootView.findViewById(R.id.display_weight);
            displayBmi = rootView.findViewById(R.id.display_bmi);
            displayBloodType = rootView.findViewById(R.id.display_blood_type);

            // Initialize CardViews and set click listeners
            setupCardViewListeners(rootView);

            // Load saved dark mode preference
            loadDarkModePreference();

            // Load user data from Firebase if user is logged in
            if (currentUser != null) {
                loadUserData(currentUser.getEmail());
            }

            return rootView;
        }


        @Override
        public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
            inflater.inflate(R.menu.logout_menu, menu);
            Log.d("ProfileFragment", "Menu created");
            super.onCreateOptionsMenu(menu, inflater);
        }


        @Override
        public boolean onOptionsItemSelected(@NonNull MenuItem item) {
            if (item.getItemId() == R.id.action_logout) {
                // Show confirmation dialog before logging out
                new AlertDialog.Builder(getActivity())
                        .setTitle("Confirm Logout")
                        .setMessage("Are you sure you want to exit the app?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Perform log out action here
                                FirebaseAuth.getInstance().signOut(); // Log out the user from Firebase
                                // Navigate back to the login activity or close the app
                                Intent intent = new Intent(getActivity(), LogIn.class);
                                startActivity(intent);
                                getActivity().finish(); // Finish current activity
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Dismiss the dialog, do nothing
                                dialog.dismiss();
                            }
                        })
                        .show(); // Show the dialog
                return true;
            }
            return super.onOptionsItemSelected(item);
        }


        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setHasOptionsMenu(true); // Enable options menu in the fragment
        }


        private void setupCardViewListeners(View rootView) {
            CardView cvHeight = rootView.findViewById(R.id.cvHeight);
            CardView cvWeight = rootView.findViewById(R.id.cvWeight);
            CardView cvBmi = rootView.findViewById(R.id.cvBmi);
            CardView cvBloodType = rootView.findViewById(R.id.cvBloodType);
            CardView cvChangePhone = rootView.findViewById(R.id.cvChangePhone);
            CardView cvResetSecurityQuestion = rootView.findViewById(R.id.cvResetSecurityQuestion);
            CardView cvChangePassword = rootView.findViewById(R.id.cvChangePassword);
            CardView cvDeleteAccount = rootView.findViewById(R.id.cvDeleteAccount);

            cvHeight.setOnClickListener(v -> showInputDialog("Enter Your Height", "height"));
            cvWeight.setOnClickListener(v -> showInputDialog("Enter Your Weight", "weight"));
            cvBloodType.setOnClickListener(v -> showBloodTypeDialog());
            cvResetSecurityQuestion.setOnClickListener(v -> navigateToResetSecurityQuestionFragment());
            cvChangePhone.setOnClickListener(v -> navigateToSecurityQuestionFragment());  // Only need to set this once
            cvChangePassword.setOnClickListener(v -> navigateToChangePasswordFragment());
            cvDeleteAccount.setOnClickListener(v -> confirmDeleteAccount());
        }

        private void navigateToChangePasswordFragment() {
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, new ChangePasswordFragment());
            transaction.addToBackStack(null); // Optional: Add to back stack for navigation
            transaction.commit();
        }

        private void navigateToResetSecurityQuestionFragment() {
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, new ResetSecurityQuestionFragment());
            transaction.addToBackStack(null); // Optional: Add to back stack for navigation
            transaction.commit();
        }

        private void navigateToSecurityQuestionFragment() {
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, new SecurityQuestionFragment());
            transaction.addToBackStack(null); // Optional: Add to back stack for navigation
            transaction.commit();
        }

        private void loadDarkModePreference() {
            boolean isDarkModeEnabled = getActivity().getSharedPreferences("Preferences", Context.MODE_PRIVATE)
                    .getBoolean("dark_mode", false);
            switchTheme.setChecked(isDarkModeEnabled);

            // Set listener for the switch
            switchTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
                // Save preference
                getActivity().getSharedPreferences("Preferences", Context.MODE_PRIVATE)
                        .edit()
                        .putBoolean("dark_mode", isChecked)
                        .apply();

                // Toggle the theme
                AppCompatDelegate.setDefaultNightMode(isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
            });
        }


        private void loadUserData(String userEmail) {
            DatabaseReference usersRef = database.getReference("Users");
            usersRef.orderByChild("email").equalTo(userEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                            String fullName = userSnapshot.child("fullName").getValue(String.class);
                            if (fullName != null) {
                                userRef = database.getReference("Users").child(fullName);
                                loadDataFromFirebase();
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(getContext(), "Failed to fetch username", Toast.LENGTH_SHORT).show();
                }
            });
        }

        private void showInputDialog(String title, String field) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle(title);

            // Set up the input
            final EditText input = new EditText(getContext());
            input.setInputType(field.equals("phone") ? InputType.TYPE_CLASS_PHONE : InputType.TYPE_CLASS_TEXT);  // Phone input for phone number
            builder.setView(input);

            // Set up the buttons
            builder.setPositiveButton("OK", (dialog, which) -> {
                String value = input.getText().toString().trim();
                if (!value.isEmpty()) {
                    saveToFirebase(field, value);
                } else {
                    Toast.makeText(getContext(), title + " cannot be empty", Toast.LENGTH_SHORT).show();
                }
            });
            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

            builder.show();
        }

        private void showBloodTypeDialog() {
            String[] bloodTypes = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Select Your Blood Type");
            builder.setItems(bloodTypes, (dialog, which) -> {
                String selectedBloodType = bloodTypes[which];
                displayBloodType.setText(selectedBloodType);
                saveToFirebase("bloodType", selectedBloodType); // Save to Firebase
            });
            builder.show();
        }

        private void saveToFirebase(String field, String value) {
            if (userRef != null) {
                // Save medical data under 'MedicalData' for the user
                if (field.equals("height") || field.equals("weight") || field.equals("bmi") || field.equals("bloodType")) {
                    userRef.child("MedicalData").child(field).setValue(value).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(getContext(), field + " saved successfully!", Toast.LENGTH_SHORT).show();
                            updateTextView(field, value);
                            if (field.equals("height") || field.equals("weight")) {
                                recalculateBmi();
                            }
                        } else {
                            Toast.makeText(getContext(), "Failed to save " + field, Toast.LENGTH_SHORT).show();
                        }
                    });
                } else if (field.equals("phone")) {
                    userRef.child("phone").setValue(value).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(getContext(), "Phone number updated successfully!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Failed to update phone number", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else if (field.equals("password")) {
                    auth.getCurrentUser().updatePassword(value).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(getContext(), "Password changed successfully!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Failed to change password", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }

        private void updateTextView(String field, String value) {
            if (value == null || value.isEmpty()) {
                // Optionally handle cases where value is null or empty
                Toast.makeText(getContext(), "Invalid value for " + field, Toast.LENGTH_SHORT).show();
                return;
            }

            switch (field) {
                case "height":
                    displayHeight.setText(value + " cm"); // Append " cm" for height
                    break;
                case "weight":
                    displayWeight.setText(value + " kg"); // Append " kg" for weight
                    break;
                case "bmi":
                    displayBmi.setText(value); // BMI display
                    break;
                case "bloodType":
                    displayBloodType.setText(value); // Blood Type display
                    break;
            }
        }

        private void loadDataFromFirebase() {
            userRef.child("MedicalData").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String height = dataSnapshot.child("height").getValue(String.class);
                    String weight = dataSnapshot.child("weight").getValue(String.class);
                    String bloodType = dataSnapshot.child("bloodType").getValue(String.class);
                    String bmi = dataSnapshot.child("bmi").getValue(String.class);

                    if (height != null) {
                        displayHeight.setText(height + " cm");
                    }
                    if (weight != null) {
                        displayWeight.setText(weight + " kg");
                    }
                    if (bloodType != null) {
                        displayBloodType.setText(bloodType);
                    }
                    if (bmi != null) {
                        displayBmi.setText(bmi);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(getContext(), "Failed to load data", Toast.LENGTH_SHORT).show();
                }
            });
        }

        private void recalculateBmi() {
            userRef.child("MedicalData").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String heightStr = dataSnapshot.child("height").getValue(String.class);
                    String weightStr = dataSnapshot.child("weight").getValue(String.class);

                    if (heightStr != null && weightStr != null) {
                        double height = Double.parseDouble(heightStr) / 100; // Convert cm to meters
                        double weight = Double.parseDouble(weightStr);
                        double bmi = weight / (height * height);
                        userRef.child("MedicalData").child("bmi").setValue(String.format("%.2f", bmi)).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                updateTextView("bmi", String.format("%.2f", bmi));
                            } else {
                                Toast.makeText(getContext(), "Failed to update BMI", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(getContext(), "Failed to load data", Toast.LENGTH_SHORT).show();
                }
            });
        }


        private void confirmDeleteAccount() {
            new AlertDialog.Builder(getContext())
                    .setTitle("Confirm Delete Account")
                    .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (user != null) {
                            String userId = user.getUid(); // Get the unique ID of the current user

                            // Delete user from Firebase Authentication
                            user.delete().addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    // Delete user data from Firebase Realtime Database
                                    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);

                                    // Delete sign-up data, medical data, and emergency contacts
                                    userRef.removeValue().addOnCompleteListener(deleteTask -> {
                                        if (deleteTask.isSuccessful()) {
                                            Toast.makeText(getContext(), "Account deleted successfully.", Toast.LENGTH_SHORT).show();
                                            // Navigate back to login or exit app
                                            Intent intent = new Intent(getActivity(), LogIn.class);
                                            startActivity(intent);
                                            getActivity().finish(); // End the current activity
                                        } else {
                                            Toast.makeText(getContext(), "Failed to delete user data.", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                } else {
                                    Toast.makeText(getContext(), "Failed to delete account.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    })
                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                    .show();
        }
    }