package com.example.scannerallerpro;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ContactManager {
    // Define the Contact class
    public static class Contact {
        private String doctorName;
        private String doctorContactPhone;
        private String medicalInstitutionName;
        private String medicalContactPhone;
        private String familyContactName;
        private String familyContactPhone;
        private String relationship;

        // Default constructor required for Firebase
        public Contact() {
        }

        // Constructor with parameters
        public Contact(String doctorName, String doctorContactPhone, String medicalInstitutionName,
                       String medicalContactPhone, String familyContactName, String familyContactPhone,
                       String relationship) {
            this.doctorName = doctorName;
            this.doctorContactPhone = doctorContactPhone;
            this.medicalInstitutionName = medicalInstitutionName;
            this.medicalContactPhone = medicalContactPhone;
            this.familyContactName = familyContactName;
            this.familyContactPhone = familyContactPhone;
            this.relationship = relationship;
        }

        // Getters and setters
        public String getDoctorName() {
            return doctorName;
        }

        public void setDoctorName(String doctorName) {
            this.doctorName = doctorName;
        }

        public String getDoctorContactPhone() {
            return doctorContactPhone;
        }

        public void setDoctorContactPhone(String doctorContactPhone) {
            this.doctorContactPhone = doctorContactPhone;
        }

        public String getMedicalInstitutionName() {
            return medicalInstitutionName;
        }

        public void setMedicalInstitutionName(String medicalInstitutionName) {
            this.medicalInstitutionName = medicalInstitutionName;
        }

        public String getMedicalContactPhone() {
            return medicalContactPhone;
        }

        public void setMedicalContactPhone(String medicalContactPhone) {
            this.medicalContactPhone = medicalContactPhone;
        }

        public String getFamilyContactName() {
            return familyContactName;
        }

        public void setFamilyContactName(String familyContactName) {
            this.familyContactName = familyContactName;
        }

        public String getFamilyContactPhone() {
            return familyContactPhone;
        }

        public void setFamilyContactPhone(String familyContactPhone) {
            this.familyContactPhone = familyContactPhone;
        }

        public String getRelationship() {
            return relationship;
        }

        public void setRelationship(String relationship) {
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
