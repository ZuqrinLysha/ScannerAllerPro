package com.example.scannerallerpro;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class ContactManager {
    // Define the Contact class
    public static class Contact {
        private String contactName; // Updated field name
        private String phone; // Updated field name
        private String relationship; // Updated field name

        // Default constructor required for Firebase
        public Contact() {
        }

        // Constructor with parameters
        public Contact(String contactName, String phone, String relationship) {
            this.contactName = contactName;
            this.phone = phone;
            this.relationship = relationship;
        }

        // Getters and Setters
        public String getcontactName() {
            return contactName;
        }

        public void setcontactName(String contactName) {
            this.contactName = contactName;
        }

        public String getphone() {
            return phone;
        }

        public void setgetphone(String phone) {
            this.phone = phone;
        }

        public String getrelationship() {
            return relationship;
        }

        public void getrelationship(String relationship) {
            this.relationship = relationship;
        }
    }

    // Firebase initialization
    private static FirebaseDatabase database = FirebaseDatabase.getInstance();
    private static DatabaseReference contactsRef = database.getReference("Contacts");
    private static FirebaseAuth auth = FirebaseAuth.getInstance();

    // Method to add a contact to Firebase
    public static void addContact(Contact contact) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            String contactId = contactsRef.child(userId).push().getKey(); // Create a unique key for the contact
            if (contactId != null) {
                contactsRef.child(userId).child(contactId).setValue(contact)
                        .addOnSuccessListener(aVoid -> {
                            Log.d("ContactManager", "Contact added successfully");
                        })
                        .addOnFailureListener(e -> {
                            Log.e("ContactManager", "Failed to add contact", e);
                        });
            }
        } else {
            Log.e("ContactManager", "User not logged in");
        }
    }

    // Method to delete a contact
    public static void deleteContact(String contactId) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            contactsRef.child(userId).child(contactId).removeValue()
                    .addOnSuccessListener(aVoid -> {
                        Log.d("ContactManager", "Contact deleted successfully");
                    })
                    .addOnFailureListener(e -> {
                        Log.e("ContactManager", "Failed to delete contact", e);
                    });
        }
    }

    // Method to update a contact
    public static void updateContact(String contactId, Contact contact) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            Map<String, Object> contactValues = new HashMap<>();
            contactValues.put("contactName", contact.getcontactName());
            contactValues.put("phone", contact.getphone());
            contactValues.put("relationship", contact.getrelationship());

            contactsRef.child(userId).child(contactId).updateChildren(contactValues)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("ContactManager", "Contact updated successfully");
                    })
                    .addOnFailureListener(e -> {
                        Log.e("ContactManager", "Failed to update contact", e);
                    });
        }
    }

    // Method to retrieve contacts (to be implemented)
    // You can add a method to retrieve contacts here if needed.
    /*
    public static void getContacts(ValueEventListener listener) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            contactsRef.child(userId).addListenerForSingleValueEvent(listener);
        }
    }
    */
}
