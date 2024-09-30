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
        private String doctorName;
        private String doctorContactPhone;
        private String medicalInstitutionName;
        private String medicalContactPhone;
        private String familyContactName;
        private String familyContactPhone;
        private String relationship;

        // Default constructor required for calls to DataSnapshot.getValue(Contact.class)
        public Contact() {
        }

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

        // Getters and Setters
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
}
