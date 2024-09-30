package com.example.scannerallerpro;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ViewContactFragment extends Fragment {
    private ContactViewModel contactViewModel;
    private RecyclerView recyclerViewContacts;
    private ContactAdapter contactAdapter;
    private FirebaseDatabase database;
    private DatabaseReference databaseReference;
    private FirebaseAuth auth;
    private String userId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_view_contact_, container, false);

        // Initialize RecyclerView
        recyclerViewContacts = view.findViewById(R.id.recyclerViewContacts);
        recyclerViewContacts.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize Firebase
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        // Initialize ViewModel
        contactViewModel = new ViewModelProvider(requireActivity()).get(ContactViewModel.class);

        // Retrieve user's unique ID
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
            loadContactsFromFirebase(); // Load contacts from Firebase
        }

        // Observe the contact list data
        contactViewModel.getContactList().observe(getViewLifecycleOwner(), new Observer<List<Contact>>() { // Change ContactViewModel.Contact to Contact
            @Override
            public void onChanged(List<Contact> contacts) { // Change this line
                contactAdapter = new ContactAdapter(contacts, new ContactAdapter.OnContactClickListener() {
                    @Override
                    public void onCallClick(Contact contact) { // Change this line
                        // Handle call action
                    }

                    @Override
                    public void onDeleteClick(Contact contact) { // Change this line
                        // Handle delete action
                    }
                });
                recyclerViewContacts.setAdapter(contactAdapter);
            }
        });

        return view;
    }

    // Method to load contacts from Firebase
    private void loadContactsFromFirebase() {
        if (userId != null) {
            databaseReference = database.getReference("Users").child(userId).child("Contacts");
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    List<Contact> contactList = new ArrayList<>(); // Change ContactViewModel.Contact to Contact
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Contact contact = snapshot.getValue(Contact.class); // Change to Contact
                        if (contact != null) {
                            contactList.add(contact);
                        }
                    }
                    contactViewModel.setContactList(contactList); // Ensure setContactList accepts List<Contact>
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle possible errors.
                }
            });
        }
    }
}
