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
    private List<ContactViewModel.Contact> contactList;

    public ContactAdapter(List<ContactViewModel.Contact> contactList) {
        this.contactList = contactList;
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_contact, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        ContactViewModel.Contact contact = contactList.get(position);
        holder.textViewContactName.setText(contact.fullName);
        holder.textViewPhoneNumber.setText(contact.phoneNumber);
        holder.textRelationship.setText(contact.relationship);

        // Set up button listeners if necessary
        holder.buttonCall.setOnClickListener(v -> {
            // Handle call action
        });

        holder.buttonDelete.setOnClickListener(v -> {
            // Handle delete action
        });
    }

    @Override
    public int getItemCount() {
        return contactList.size();
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
}
