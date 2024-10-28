package com.example.scannerallerpro;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class ContactViewModel extends ViewModel {
    private final MutableLiveData<List<Contact>> contacts;
    private final DatabaseReference databaseReference;

    public ContactViewModel() {
        contacts = new MutableLiveData<>(new ArrayList<>());
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        loadContacts(); // Load contacts initially
    }

    public LiveData<List<Contact>> getContacts() {
        return contacts;
    }

    // Load contacts from Firebase based on current user
    public void loadContacts() {
        String userId = getCurrentUserId(); // Get the current user's unique ID
        if (userId.isEmpty()) {
            Log.e("ContactViewModel", "User not logged in.");
            return; // Early return if user is not logged in
        }

        // Query for contacts based on the user's unique ID
        databaseReference.child(userId).child("ContactData").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Contact> contactList = new ArrayList<>();
                for (DataSnapshot contactDataSnapshot : dataSnapshot.getChildren()) {
                    Contact contact = contactDataSnapshot.getValue(Contact.class);
                    if (contact != null) {
                        contactList.add(contact);
                    }
                }
                contacts.setValue(contactList); // Update LiveData
                Log.d("ContactViewModel", "Contacts loaded: " + contactList.size());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("ContactViewModel", "Firebase operation cancelled: " + databaseError.getMessage());
            }
        });
    }

    // Method to refresh contacts
    public void refreshContacts() {
        loadContacts();
    }

    public LiveData<List<Contact>> getContactList() {
        return contacts;
    }

    public void addContact(Contact contact) {
        List<Contact> currentList = new ArrayList<>(contacts.getValue()); // Create a new list
        if (currentList != null && !currentList.contains(contact)) {
            currentList.add(contact);
            contacts.setValue(currentList);

            // Implement Firebase addition logic
            String userId = getCurrentUserId(); // Get the current user's ID
            if (!userId.isEmpty()) {
                databaseReference.child(userId).child("ContactData").child(contact.getId()).setValue(contact)
                        .addOnSuccessListener(aVoid -> Log.d("ContactViewModel", "Contact added successfully"))
                        .addOnFailureListener(e -> Log.e("ContactViewModel", "Error adding contact: " + e.getMessage()));
            }
        }
    }

    public void removeContact(Contact contact) {
        List<Contact> currentList = new ArrayList<>(contacts.getValue()); // Create a new list
        if (currentList != null && currentList.contains(contact)) {
            currentList.remove(contact);
            contacts.setValue(currentList);

            // Implement Firebase removal logic
            String userId = getCurrentUserId(); // Get the current user's ID
            if (!userId.isEmpty()) {
                databaseReference.child(userId).child("ContactData").child(contact.getId()).removeValue()
                        .addOnSuccessListener(aVoid -> Log.d("ContactViewModel", "Contact removed successfully"))
                        .addOnFailureListener(e -> Log.e("ContactViewModel", "Error removing contact: " + e.getMessage()));
            }
        }
    }

    private String getCurrentUserId() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        return auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "";
    }

    // Contact class definition
    public static class Contact {
        private String id; // Ensure you have an ID field for contacts
        private String fullName;
        private String phone;
        private String relationship;

        // Default constructor required for calls to DataSnapshot.getValue(Contact.class)
        public Contact() {
        }

        // Full constructor
        public Contact(String id, String fullName, String phone, String relationship) {
            this.id = id;
            this.fullName = fullName;
            this.phone = phone;
            this.relationship = relationship;
        }

        // Getters and Setters
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getRelationship() {
            return relationship;
        }

        public void setRelationship(String relationship) {
            this.relationship = relationship;
        }
    }
}