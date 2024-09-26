package com.example.scannerallerpro;

public class ContactManager {
    public static class Contact {
        private String firstName;
        private String phoneNumber;
        private String relationship;

        // Default constructor required for Firebase
        public Contact() {
        }

        public Contact(String firstName, String phoneNumber, String relationship) {
            this.firstName = firstName;
            this.phoneNumber = phoneNumber;
            this.relationship = relationship;
        }

        // Getters and setters
        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
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
    }

    // Other methods for managing contacts
    public static void addContact(Contact contact) {
        // Logic to add contact if needed
    }
}
