package com.example.scannerallerpro;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class AboutUsFragment extends Fragment {

    private TextView contactDetails;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about_us, container, false);

        contactDetails = view.findViewById(R.id.contact_details);

        // Set click listener to the contact details TextView
        contactDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendEmail();
            }
        });

        return view;
    }

    private void sendEmail() {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:")); // only email apps should handle this
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"allerproscanner@gmail.com"});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Feedback for AllerPro");
        emailIntent.putExtra(Intent.EXTRA_TEXT, ""); // Optional message body

        // Try to start the email intent
        try {
            startActivity(Intent.createChooser(emailIntent, "Send email..."));
        } catch (android.content.ActivityNotFoundException ex) {
            // Handle the case where no email app is available
            Toast.makeText(getActivity(), "No email client found", Toast.LENGTH_SHORT).show();
        }
    }
}
