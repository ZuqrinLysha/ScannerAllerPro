package com.example.scannerallerpro;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

public class ContactViewModel extends ViewModel {
    private MutableLiveData<List<Contact>> contacts;

    public ContactViewModel() {
        contacts = new MutableLiveData<>(new ArrayList<>()); // Initialize with an empty list
    }

    // Method to get the list of contacts
    public LiveData<List<Contact>> getContactList() {
        return contacts;
    }

    // Method to set the list of contacts
    public void setContactList(List<Contact> newContacts) {
        contacts.setValue(newContacts); // Update the LiveData with the new list
    }

    // Method to add a contact
    public void addContact(Contact contact) {
        List<Contact> currentList = contacts.getValue();
        if (currentList != null) {
            currentList.add(contact);
            contacts.setValue(currentList); // Update the LiveData with the new list
        }
    }

    // Method to remove a contact
    public void removeContact(Contact contact) {
        List<Contact> currentList = contacts.getValue();
        if (currentList != null) {
            currentList.remove(contact); // Remove the contact from the list
            contacts.setValue(currentList); // Update the LiveData with the new list
        }
    }

    // Updated Contact class definition
    public static class Contact {
        private String FamilyContactName;
        private String FamilyContactPhone;
        private String HealthCareContactName;
        private String HealthCareContactPhone;
        private String MedicalCenterContactName;
        private String MedicalCenterContactPhone;
        private String relationship; // This can be renamed to a more specific term if needed

        // Default constructor required for calls to DataSnapshot.getValue(Contact.class)
        public Contact() {
        }

        public Contact(String FamilyContactName, String FamilyContactPhone, String HealthCareContactName,
                       String HealthCareContactPhone, String MedicalCenterContactName,
                       String MedicalCenterContactPhone, String relationship) {
            this.FamilyContactName = FamilyContactName;
            this.FamilyContactPhone = FamilyContactPhone;
            this.HealthCareContactName = HealthCareContactName;
            this.HealthCareContactPhone = HealthCareContactPhone;
            this.MedicalCenterContactName = MedicalCenterContactName;
            this.MedicalCenterContactPhone = MedicalCenterContactPhone;
            this.relationship = relationship;
        }

        // Getters and Setters
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

        public String getRelationship() {
            return relationship;
        }

        public void setRelationship(String relationship) {
            this.relationship = relationship;
        }
    }
}
