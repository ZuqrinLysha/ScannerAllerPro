package com.example.scannerallerpro;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ContactManager {
    // Define the Contact class
    public static class Contact {
        private String HealthCareContactName; // Updated field name
        private String HealthCareContactPhone; // Updated field name
        private String MedicalCenterContactName; // Updated field name
        private String MedicalCenterContactPhone; // Updated field name
        private String FamilyContactName; // Updated field name
        private String FamilyContactPhone; // Updated field name
        private String spinnerRelationship; // Updated field name

        // Default constructor required for Firebase
        public Contact() {
        }

        // Constructor with parameters
        public Contact(String HealthCareContactName, String HealthCareContactPhone,
                       String MedicalCenterContactName, String MedicalCenterContactPhone,
                       String FamilyContactName, String FamilyContactPhone,
                       String spinnerRelationship) {
            this.HealthCareContactName = HealthCareContactName;
            this.HealthCareContactPhone = HealthCareContactPhone;
            this.MedicalCenterContactName = MedicalCenterContactName;
            this.MedicalCenterContactPhone = MedicalCenterContactPhone;
            this.FamilyContactName = FamilyContactName;
            this.FamilyContactPhone = FamilyContactPhone;
            this.spinnerRelationship = spinnerRelationship;
        }

        // Getters and setters
        public String getHealthCareContactName() {
            return HealthCareContactName;
        }

        public void setHealthCareContactName(String HealthCareContactName) {
            this.HealthCareContactName = HealthCareContactName;
        }

        public String getHealthCareContactPhone() {
            return HealthCareContactPhone;
        }

        public void setHealthCareContactPhone(String HealthCareContactPhone) {
            this.HealthCareContactPhone = HealthCareContactPhone;
        }

        public String getMedicalCenterContactName() {
            return MedicalCenterContactName;
        }

        public void setMedicalCenterContactName(String MedicalCenterContactName) {
            this.MedicalCenterContactName = MedicalCenterContactName;
        }

        public String getMedicalCenterContactPhone() {
            return MedicalCenterContactPhone;
        }

        public void setMedicalCenterContactPhone(String MedicalCenterContactPhone) {
            this.MedicalCenterContactPhone = MedicalCenterContactPhone;
        }

        public String getFamilyContactName() {
            return FamilyContactName;
        }

        public void setFamilyContactName(String FamilyContactName) {
            this.FamilyContactName = FamilyContactName;
        }

        public String getFamilyContactPhone() {
            return FamilyContactPhone;
        }

        public void setFamilyContactPhone(String FamilyContactPhone) {
            this.FamilyContactPhone = FamilyContactPhone;
        }

        public String getSpinnerRelationship() {
            return spinnerRelationship;
        }

        public void setSpinnerRelationship(String spinnerRelationship) {
            this.spinnerRelationship = spinnerRelationship;
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
                            // Optionally, add success logic here
                        })
                        .addOnFailureListener(e -> {
                            // Optionally, add failure logic here
                        });
            }
        } else {
            // Handle case when user is not logged in
        }
    }
}
