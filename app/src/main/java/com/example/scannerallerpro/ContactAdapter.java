package com.example.scannerallerpro;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {
    private List<Contact> contactList; // Use the standalone Contact class
    private OnContactClickListener onContactClickListener; // Listener for handling button clicks

    // Constructor to initialize the contact list and listener
    public ContactAdapter(List<Contact> contactList, OnContactClickListener listener) {
        this.contactList = contactList;
        this.onContactClickListener = listener; // Set the listener
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_contact, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        Contact contact = contactList.get(position); // Use the standalone Contact class
        holder.textViewContactName.setText(contact.getFamilyContactName());
        holder.textViewPhoneNumber.setText(contact.getFamilyContactPhone());
        holder.textRelationship.setText(contact.getRelationship());

        // Set up button listeners
        holder.buttonCall.setOnClickListener(v -> {
            if (onContactClickListener != null) {
                onContactClickListener.onCallClick(contact); // Pass the contact to the listener
            }
        });

        holder.buttonDelete.setOnClickListener(v -> {
            if (onContactClickListener != null) {
                onContactClickListener.onDeleteClick(contact); // Pass the contact to the listener
            }
        });
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    // ViewHolder class
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

    // Interface for handling contact click events
    public interface OnContactClickListener {
        void onCallClick(Contact contact);
        void onDeleteClick(Contact contact);
    }
}
