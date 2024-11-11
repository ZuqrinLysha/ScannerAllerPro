package com.example.scannerallerpro;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ViewContactFragment extends Fragment {
    private ContactViewModel contactViewModel;
    private RecyclerView recyclerViewContacts;
    private ContactAdapter contactAdapter;
    private FirebaseAuth auth;
    private String userId;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_view_contact_, container, false);

        // Initialize the Toolbar
        Toolbar toolbar = view.findViewById(R.id.toolbarContact);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);

        // Initialize back arrow button
        ImageButton backArrow = view.findViewById(R.id.backArrow);
        backArrow.setOnClickListener(v -> showExitConfirmationDialog());

        // Initialize RecyclerView
        recyclerViewContacts = view.findViewById(R.id.recyclerViewContacts);
        recyclerViewContacts.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize Firebase and ViewModel
        auth = FirebaseAuth.getInstance();
        contactViewModel = new ViewModelProvider(requireActivity()).get(ContactViewModel.class);

        // Retrieve user's unique ID
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        } else {
            // Handle case where the user is not logged in
            // Optionally navigate to login or show an error
        }

        // Load contacts and set up observer
        loadContactsFromFirebase();

        contactViewModel.getContactList().observe(getViewLifecycleOwner(), contacts -> {
            if (contactAdapter == null) {
                // Initialize adapter on first data load
                contactAdapter = new ContactAdapter(contacts, new ContactAdapter.OnContactActionListener() {
                    @Override
                    public void onDeleteContact(ContactViewModel.Contact contact) {
                        showDeleteConfirmationDialog(contact);
                    }

                    @Override
                    public void onContactSelected(ContactViewModel.Contact contact) {
                        // Implement contact selection action if needed
                    }
                }, getContext());
                recyclerViewContacts.setAdapter(contactAdapter);
            } else {
                // Update adapter with new data
                contactAdapter.updateContacts(contacts);
            }
        });

        // Set up FloatingActionButton for Adding Contacts
        FloatingActionButton fabAddContact = view.findViewById(R.id.fabAddContact);
        fabAddContact.setOnClickListener(v -> {

            Fragment addContactFragment = new AddContactFragment();
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, addContactFragment)
                    .addToBackStack(null)
                    .commit();
        });

        // Handle back press to show alert dialog
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                showExitConfirmationDialog();
            }
        });

        return view;
    }

    // Method to show the confirmation dialog when the user tries to exit the fragment
    private void showExitConfirmationDialog() {
        new AlertDialog.Builder(getContext())
                .setMessage("Are you sure you want to leave this page?")
                .setCancelable(false) // Make it non-cancelable so the user has to choose
                .setPositiveButton("Yes", (dialog, id) -> navigateBack())
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        if (activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setDisplayShowTitleEnabled(false); // Hide default title
            activity.getSupportActionBar().show(); // Show the fragment's toolbar


        }
    }

    // Method to load contacts from Firebase via ViewModel
    private void loadContactsFromFirebase() {
        if (userId != null) {
            contactViewModel.loadContacts(); // Load contacts for the current user
        }
    }

    // Navigate back to HomeFragment
    private void navigateBack() {
        Fragment homepageFragment = new HomeFragment();
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, homepageFragment)
                .addToBackStack(null)
                .commit();
    }

    // Show confirmation dialog for contact deletion
    private void showDeleteConfirmationDialog(ContactViewModel.Contact contact) {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Contact")
                .setMessage("Are you sure you want to delete this contact?")
                .setPositiveButton("Yes", (dialog, which) -> contactViewModel.removeContact(contact))
                .setNegativeButton("No", null)
                .show();
    }
}
