package com.example.scannerallerpro;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {
    private List<ContactViewModel.Contact> contactList;
    private OnContactActionListener onContactActionListener;
    private Context context;

    public ContactAdapter(List<ContactViewModel.Contact> contacts, OnContactActionListener listener, Context context) {
        this.contactList = contacts;
        this.onContactActionListener = listener;
        this.context = context;
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_contact, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        Log.d("ContactAdapter", "Binding contact: " + contactList.get(position).getFullName());
        ContactViewModel.Contact contact = contactList.get(position);
        holder.textViewContactName.setText(contact.getFullName());
        holder.textViewPhoneNumber.setText(contact.getPhone());
        holder.textRelationship.setText(contact.getRelationship());

        // Call button action
        holder.buttonCall.setOnClickListener(v -> {
            String phoneNumber = contact.getPhone();
            if (!phoneNumber.isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + phoneNumber));
                context.startActivity(intent); // Start the dialer app
            } else {
                Toast.makeText(context, "Phone number is not available", Toast.LENGTH_SHORT).show();
            }
        });

        // Delete button action
        holder.buttonDelete.setOnClickListener(v -> {
            if (onContactActionListener != null) {
                onContactActionListener.onDeleteContact(contact); // Notify the parent fragment/activity to delete the contact
            }
        });
    }

    @Override
    public int getItemCount() {
        return contactList != null ? contactList.size() : 0;
    }

    public void updateContacts(List<ContactViewModel.Contact> newContactList) {
        if (newContactList != null) {
            this.contactList.clear();
            this.contactList.addAll(newContactList);
            notifyDataSetChanged(); // Notify the RecyclerView that the data has changed
        }
    }

    public static class ContactViewHolder extends RecyclerView.ViewHolder {
        TextView textViewContactName, textViewPhoneNumber, textRelationship;
        Button buttonCall, buttonDelete;

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewContactName = itemView.findViewById(R.id.textViewContactName);
            textViewPhoneNumber = itemView.findViewById(R.id.textViewPhoneNumber);
            textRelationship = itemView.findViewById(R.id.textRelationship);
            buttonCall = itemView.findViewById(R.id.buttonCall);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
        }
    }

    // Callback interface for handling actions like deleting a contact
    public interface OnContactActionListener {
        void onDeleteContact(ContactViewModel.Contact contact);

        void onContactSelected(ContactViewModel.Contact contact);
    }
}
