package com.example.scannerallerpro;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class ViewContactFragment extends Fragment {
    private ContactViewModel contactViewModel;
    private RecyclerView recyclerViewContacts;
    private ContactAdapter contactAdapter;
    private FirebaseAuth auth;
    private String userId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_view_contact_, container, false);


        // Hide the Toolbar
        ((AppCompatActivity) requireActivity()).getSupportActionBar().hide(); // Hides the Toolbar

        // Initialize RecyclerView
        recyclerViewContacts = view.findViewById(R.id.recyclerViewContacts);
        recyclerViewContacts.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();

        // Initialize ViewModel
        contactViewModel = new ViewModelProvider(requireActivity()).get(ContactViewModel.class);

        // Retrieve user's unique ID
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        }

        // Observe the contact list data
        contactViewModel.getContactList().observe(getViewLifecycleOwner(), new Observer<List<ContactViewModel.Contact>>() {
            @Override
            public void onChanged(List<ContactViewModel.Contact> contacts) {
                if (contactAdapter == null) { // Handle initial data load
                    contactAdapter = new ContactAdapter(contacts, new ContactAdapter.OnContactActionListener() {
                        @Override
                        public void onDeleteContact(ContactViewModel.Contact contact) {
                            showDeleteConfirmationDialog(contact); // Remove contact using ViewModel
                        }

                        public void onContactSelected(ContactViewModel.Contact contact) {
                            // Handle contact selection here
                        }
                    }, getContext());
                    recyclerViewContacts.setAdapter(contactAdapter);
                } else {
                    // Update existing adapter with new data
                    contactAdapter.updateContacts(contacts);
                }
            }
        });

        // Floating Action Button for Adding Contacts
        com.google.android.material.floatingactionbutton.FloatingActionButton fabAddContact = view.findViewById(R.id.fabAddContact);
        fabAddContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to AddContactFragment when FAB is clicked
                Fragment addContactFragment = new AddContactFragment(); // Ensure you have this fragment

                // Use FragmentTransaction to navigate
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, addContactFragment) // R.id.fragment_container should be the ID of the container where the fragment is displayed
                        .addToBackStack(null) // This adds the transaction to the back stack
                        .commit();
            }
        });

        return view; // Ensure this is at the end of onCreateView
    }

    @Override
    public void onResume() {
        super.onResume();
        loadContactsFromFirebase(); // Refresh contacts when the fragment is resumed
        ((AppCompatActivity) requireActivity()).getSupportActionBar().hide(); // Ensure the Toolbar is hidden
    }

    // Method to load contacts from Firebase
    private void loadContactsFromFirebase() {
        if (userId != null) {
            contactViewModel.loadContacts(); // Use ViewModel to load contacts
        }
    }
    // Method to show confirmation dialog for contact deletion
    private void showDeleteConfirmationDialog(ContactViewModel.Contact contact) {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Contact")
                .setMessage("Are you sure you want to delete this contact?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    contactViewModel.removeContact(contact); // Remove contact from ViewModel and Firebase
                })
                .setNegativeButton("No", null)
                .show();
    }
}

