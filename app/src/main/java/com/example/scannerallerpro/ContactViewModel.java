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
}
