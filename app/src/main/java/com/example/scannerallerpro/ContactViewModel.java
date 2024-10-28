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


    // Load contacts from Firebase based on current user
    public void loadContacts() {
        String userEmail = getCurrentUserEmail();
        if (userEmail.isEmpty()) {
            Log.e("ContactViewModel", "User not logged in.");
            return; // Early return if user is not logged in
        }

        databaseReference.orderByChild("email").equalTo(userEmail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Contact> contactList = new ArrayList<>();
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot contactSnapshot : userSnapshot.child("ContactData").getChildren()) {
                        Contact contact = contactSnapshot.getValue(Contact.class);
                        if (contact != null) {
                            contactList.add(contact);
                        }
                    }
                }
                contacts.setValue(contactList); // Update LiveData
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
        }
    }

    public void removeContact(Contact contact) {
        List<Contact> currentList = new ArrayList<>(contacts.getValue()); // Create a new list
        if (currentList != null && currentList.contains(contact)) {
            currentList.remove(contact);
            contacts.setValue(currentList);

            // Implement Firebase removal logic
            String userId = getCurrentUserEmail(); // Get the current user's email or ID
            if (!userId.isEmpty()) {
                databaseReference.child("Users").orderByChild("email").equalTo(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                        for (DataSnapshot snapshot : userSnapshot.getChildren()) {
                            // Assuming 'contact.getId()' returns a unique identifier for the contact
                            databaseReference.child("Users").child(snapshot.getKey()).child("ContactData").child(contact.getId()).removeValue()
                                    .addOnSuccessListener(aVoid -> Log.d("ContactViewModel", "Contact removed successfully"))
                                    .addOnFailureListener(e -> Log.e("ContactViewModel", "Error removing contact: " + e.getMessage()));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("ContactViewModel", "Firebase operation cancelled: " + databaseError.getMessage());
                    }
                });
            }
        }
    }

    private String getCurrentUserEmail() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        return auth.getCurrentUser() != null ? auth.getCurrentUser().getEmail() : "";
    }

    // Contact class definition
    public static class Contact {
        private String id; // Ensure you have an ID field for contacts
        private String fullName;
        private String phoneNumber;
        private String relationship;

        public Contact() { /* Default constructor */ }

        public Contact(String id, String fullName, String phoneNumber) {
            this.id = id; // Assign ID
            this.fullName = fullName;
            this.phoneNumber = phoneNumber;
            this.relationship = relationship;
        }

        public String getId() { return id; }
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
        public String getRelationship() { return relationship; }
        public void setRelationship(String relationship) { this.relationship = relationship; }
    }
}
