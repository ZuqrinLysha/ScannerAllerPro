package com.example.scannerallerpro;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

public class KnowledgeFragment extends Fragment {

    public KnowledgeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_knowledge, container, false);

        // Find the CardView
        CardView cardView = view.findViewById(R.id.card_view);
        CardView cardView2 = view.findViewById(R.id.card_view2);

        // Find the CardView and TextViews
        final TextView medInfoDetails = view.findViewById(R.id.med_info_details);
        final TextView symptomsLvlDetails = view.findViewById(R.id.symptoms_lvl_details);

        // Set an OnClickListener to expand/collapse the details
        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Toggle visibility of med_info_details
                if (medInfoDetails.getVisibility() == View.GONE) {
                    medInfoDetails.setVisibility(View.VISIBLE);
                } else {
                    medInfoDetails.setVisibility(View.GONE);
                }
            }
        });

        // Set an OnClickListener for the second card view
        cardView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Toggle visibility of symptomsLvlDetails
                if (symptomsLvlDetails.getVisibility() == View.GONE) {
                    symptomsLvlDetails.setVisibility(View.VISIBLE);
                } else {
                    symptomsLvlDetails.setVisibility(View.GONE);
                }
            }
        });

        return view;
    }
}
