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
        loadContacts(); // Load contacts when ViewModel is created
    }

    // Load contacts from Firebase based on current user
    public void loadContacts() {  // Change access modifier to public
        String userEmail = getCurrentUserEmail(); // Get current user's email
        databaseReference.orderByChild("email").equalTo(userEmail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Contact> contactList = new ArrayList<>(); // Clear the list to avoid duplicates
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String userId = userSnapshot.getKey(); // Get user ID
                    for (DataSnapshot contactSnapshot : userSnapshot.child("ContactData").getChildren()) {
                        Contact contact = contactSnapshot.getValue(Contact.class);
                        if (contact != null) {
                            contactList.add(contact); // Add loaded contact to the list
                        }
                    }
                }
                contacts.setValue(contactList); // Update LiveData with the new contact list
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle possible errors
            }
        });
    }

    public LiveData<List<Contact>> getContactList() {
        return contacts;
    }

    public void addContact(Contact contact) {
        List<Contact> currentList = contacts.getValue();
        if (currentList != null && !currentList.contains(contact)) {
            currentList.add(contact);
            contacts.setValue(currentList);

            // Add contact to Firebase
            String userEmail = getCurrentUserEmail();
            databaseReference.orderByChild("email").equalTo(userEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        String userId = userSnapshot.getKey();
                        String contactId = databaseReference.child(userId).child("ContactData").push().getKey();
                        databaseReference.child(userId).child("ContactData").child(contactId).setValue(contact)
                                .addOnSuccessListener(aVoid -> {
                                    // Contact added successfully
                                })
                                .addOnFailureListener(e -> {
                                    // Handle failure
                                });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle errors gracefully
                }
            });
        }
    }

    public void removeContact(Contact contact) {
        List<Contact> currentList = contacts.getValue();
        if (currentList != null && currentList.contains(contact)) {
            currentList.remove(contact);
            contacts.setValue(currentList);

            // Remove from Firebase
            String userEmail = getCurrentUserEmail();
            databaseReference.orderByChild("email").equalTo(userEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        String userId = userSnapshot.getKey();
                        String contactId = ""; // Initialize contactId

                        // Find contactId
                        for (DataSnapshot contactSnapshot : userSnapshot.child("ContactData").getChildren()) {
                            Contact existingContact = contactSnapshot.getValue(Contact.class);
                            if (existingContact != null && existingContact.equals(contact)) {
                                contactId = contactSnapshot.getKey();
                                break;
                            }
                        }

                        // Log for debugging
                        if (!contactId.isEmpty()) {
                            Log.d("ContactViewModel", "Removing contact: " + contact.getFullName() + " with ID: " + contactId);
                            databaseReference.child(userId).child("ContactData").child(contactId).removeValue()
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d("ContactViewModel", "Contact successfully deleted from Firebase");
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("ContactViewModel", "Failed to delete contact: " + e.getMessage());
                                    });
                        } else {
                            Log.d("ContactViewModel", "Contact ID not found for: " + contact.getFullName());
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("ContactViewModel", "Firebase operation cancelled: " + databaseError.getMessage());
                }
            });
        }
    }


    private String getCurrentUserEmail() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        return auth.getCurrentUser() != null ? auth.getCurrentUser().getEmail() : ""; // Get current user's email
    }

    public void setContactList(List<Contact> contactList) {
        if (contactList != null) {
            contacts.setValue(contactList); // Update LiveData with the new contact list
        }
    }

    // Contact class definition
    public static class Contact {
        private String fullName;
        private String phoneNumber;
        private String relationship;

        public Contact() {
            // Default constructor required for calls to DataSnapshot.getValue(Contact.class)
        }

        public Contact(String fullName, String phoneNumber, String relationship) {
            this.fullName = fullName;
            this.phoneNumber = phoneNumber;
            this.relationship = relationship;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }

        public String getRelationship() {
            return relationship;
        }

        public void setRelationship(String relationship) {
            this.relationship = relationship;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Contact contact = (Contact) o;
            return phoneNumber.equals(contact.phoneNumber) &&
                    fullName.equals(contact.fullName) &&
                    relationship.equals(contact.relationship);
        }

        @Override
        public int hashCode() {
            return phoneNumber.hashCode() + fullName.hashCode() + relationship.hashCode();
        }
    }
}
