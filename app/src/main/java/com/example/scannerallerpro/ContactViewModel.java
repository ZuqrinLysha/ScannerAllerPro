package com.example.scannerallerpro;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ContactViewModel extends ViewModel {
    private final MutableLiveData<Contact> contact = new MutableLiveData<>();

    public void setContact(Contact contact) {
        this.contact.setValue(contact);
    }

    public LiveData<Contact> getContact() {
        return contact;
    }

    public static class Contact {
        String fullName;
        String phoneNumber;
        String relationship;

        public Contact(String fullName, String phoneNumber, String relationship) {
            this.fullName = fullName;
            this.phoneNumber = phoneNumber;
            this.relationship = relationship;
        }
    }
}
