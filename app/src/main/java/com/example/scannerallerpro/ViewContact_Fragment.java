package com.example.scannerallerpro;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ViewContact_Fragment extends Fragment {
    private ContactViewModel contactViewModel;
    private TextView textViewContactName, textViewPhoneNumber, textRelationship;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_view_contact_, container, false);


        // Initialize views
        textViewContactName = view.findViewById(R.id.textViewContactName);
        textViewPhoneNumber = view.findViewById(R.id.textViewPhoneNumber);
        textRelationship = view.findViewById(R.id.textRelationship);
        // Initialize ViewModel
        contactViewModel = new ViewModelProvider(requireActivity()).get(ContactViewModel.class);

        // Observe the contact data
        contactViewModel.getContact().observe(getViewLifecycleOwner(), new Observer<ContactViewModel.Contact>() {
            @Override
            public void onChanged(ContactViewModel.Contact contact) {
                if (contact != null) {
                    textViewContactName.setText(contact.fullName);
                    textViewPhoneNumber.setText(contact.phoneNumber);
                    textRelationship.setText(contact.relationship);
                }
            }
        });
        return view;
    }
}