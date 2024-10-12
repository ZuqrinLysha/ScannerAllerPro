package com.example.scannerallerpro;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class KnowledgeFragment extends Fragment {

    private TextView medFirstDetails;
    private TextView medSecondDetails;
    private TextView medThirdDetails;
    private TextView symptomsLvlDetails;
    private TextView recipeDetails;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_knowledge, container, false);



        // Hide the main toolbar when entering the ScannerFragment
        if (getActivity() != null) {
            Toolbar mainToolbar = getActivity().findViewById(R.id.toolbarHomePage); // Assuming this is the ID of the main toolbar
            if (mainToolbar != null) {
                mainToolbar.setVisibility(View.GONE);
            }
        }

        // Initialize TextView references for details
        medFirstDetails = view.findViewById(R.id.med_first);
        medSecondDetails = view.findViewById(R.id.med_second);
        medThirdDetails = view.findViewById(R.id.med_third);
        symptomsLvlDetails = view.findViewById(R.id.symptoms_lvl_details);
        recipeDetails = view.findViewById(R.id.recipe_details);

        // Set initial visibility to GONE for all detailed sections
        medFirstDetails.setVisibility(View.GONE);
        medSecondDetails.setVisibility(View.GONE);
        medThirdDetails.setVisibility(View.GONE);
        symptomsLvlDetails.setVisibility(View.GONE);
        recipeDetails.setVisibility(View.GONE);

        // Find the CardViews
        CardView cardView = view.findViewById(R.id.card_view);
        CardView cardView2 = view.findViewById(R.id.card_view2);
        CardView cardView3 = view.findViewById(R.id.card_view3);


        // Set OnClickListener for the first CardView to expand/collapse details
        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Toggle visibility of the first medicine details
                medFirstDetails.setVisibility(medFirstDetails.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
                medSecondDetails.setVisibility(medSecondDetails.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
                medThirdDetails.setVisibility(medThirdDetails.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
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

        // Set an OnClickListener for the third card view
        cardView3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Toggle visibility of recipe_details
                if (recipeDetails.getVisibility() == View.GONE) {
                    recipeDetails.setVisibility(View.VISIBLE);
                } else {
                    recipeDetails.setVisibility(View.GONE);
                }
            }
        });


        return view;
    }

}
