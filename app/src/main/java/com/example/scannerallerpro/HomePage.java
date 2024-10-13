package com.example.scannerallerpro;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HomePage extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private BottomNavigationView bottomNavigationView;
    private FragmentManager fragmentManager;
    private TextView tvFullName; // Declare the TextView for user's full name
    private FirebaseAuth auth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        // Initialize Firebase Auth and Database
        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        // Initialize DrawerLayout
        drawerLayout = findViewById(R.id.drawerLayout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, R.string.open_drawer, R.string.close_drawer);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Initialize NavigationView
        NavigationView navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Set the TextView for full name
        View headerView = navigationView.getHeaderView(0); // Get the header view of the NavigationView
        tvFullName = headerView.findViewById(R.id.tvFullName); // Initialize TextView

        // Fetch user full name
        getUserFullName();

        // Initialize BottomNavigationView
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setBackground(null);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.home) {
                openFragment(new HomeFragment()); // HomeFragment now contains the toolbar
                return true;
            } else if (item.getItemId() == R.id.scanner) {
                openFragment(new ScannerFragment());
                return true;
            } else if (item.getItemId() == R.id.knowledge) {
                openFragment(new KnowledgeFragment());
                return true;
            } else if (item.getItemId() == R.id.profile) {
                openFragment(new ProfileFragment());
                return true;
            }
            return false;
        });

        // Initialize FragmentManager and open the default fragment
        fragmentManager = getSupportFragmentManager();
        openFragment(new HomeFragment());
    }

    private void getUserFullName() {
        String userId = auth.getCurrentUser().getUid(); // Get the current user's ID
        databaseReference.child(userId).child("fullName").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String fullName = snapshot.getValue(String.class); // Get the full name from the database
                    if (fullName != null) {
                        tvFullName.setText(fullName.toUpperCase()); // Set the full name in capital letters
                    } else {
                        tvFullName.setText("FULL NAME"); // Fallback in case the name doesn't exist
                    }
                } else {
                    tvFullName.setText("FULL NAME"); // Fallback in case the name doesn't exist
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HomePage.this, "Failed to retrieve name", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.about_us) {
            openFragment(new AboutUsFragment());
        } else if (itemId == R.id.nav_profile) {
            openFragment(new ProfileFragment());
        } else if (itemId == R.id.nav_emergency_contact) {
            openFragment(new ViewContactFragment());
        } else if (itemId == R.id.nav_allergic_history) {
            openFragment(new AllergicHistoryFragment());
        } else if (itemId == R.id.LogOut) {
            // Show confirmation dialog before logging out
            new AlertDialog.Builder(this)
                    .setTitle("Confirm Exit")
                    .setMessage("Are you sure you want to exit the application?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Log out the user
                            FirebaseAuth.getInstance().signOut();

                            // Redirect to LogIn page
                            Intent intent = new Intent(HomePage.this, LogIn.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // This will clear the back stack
                            startActivity(intent);
                            finish(); // Close the current activity

                            Toast.makeText(HomePage.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss(); // Dismiss the dialog if "No" is clicked
                        }
                    })
                    .create()
                    .show();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void openFragment(Fragment fragment) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
