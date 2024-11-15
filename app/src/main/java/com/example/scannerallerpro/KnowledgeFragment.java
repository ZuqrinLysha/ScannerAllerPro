package com.example.scannerallerpro;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
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

public class KnowledgeFragment extends Fragment {

    private TextView medFirstDetails;
    private TextView medSecondDetails;
    private TextView medThirdDetails;
    private TextView symptomsLvlDetails;
    private TextView quinoa_details;
    private TextView coconut_details;
    private TextView rice_details;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_knowledge, container, false);

        // Initialize TextView references for details
        medFirstDetails = view.findViewById(R.id.med_first);
        medSecondDetails = view.findViewById(R.id.med_second);
        medThirdDetails = view.findViewById(R.id.med_third);
        symptomsLvlDetails = view.findViewById(R.id.symptoms_lvl_details);
        quinoa_details = view.findViewById(R.id.quinoa_details);
        coconut_details = view.findViewById(R.id.coconut_details); // Missing initialization
        rice_details = view.findViewById(R.id.rice_details); // Missing initialization

        // Initialize the Toolbar
        Toolbar toolbar = view.findViewById(R.id.toolbar_knowledge);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);

        // Initialize back arrow button
        ImageButton backArrow = view.findViewById(R.id.back_button);
        backArrow.setOnClickListener(v -> navigateToHomeFragment());

        // Set initial visibility to GONE for all detailed sections
        medFirstDetails.setVisibility(View.GONE);
        medSecondDetails.setVisibility(View.GONE);
        medThirdDetails.setVisibility(View.GONE);
        symptomsLvlDetails.setVisibility(View.GONE);
        quinoa_details.setVisibility(View.GONE);
        coconut_details.setVisibility(View.GONE);
        rice_details.setVisibility(View.GONE);

        // Find the CardViews
        CardView cardView = view.findViewById(R.id.card_view);
        CardView cardView2 = view.findViewById(R.id.card_view2);
        CardView cardView3 = view.findViewById(R.id.card_view3);
        CardView cardView4 = view.findViewById(R.id.card_view4);
        CardView cardView5 = view.findViewById(R.id.card_view5);

        // Set OnClickListener for the first CardView to expand/collapse details
        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bounceAnimation(v); // Add bounce animation on click
                toggleVisibilityWithAnimation(medFirstDetails);
                toggleVisibilityWithAnimation(medSecondDetails);
                toggleVisibilityWithAnimation(medThirdDetails);
            }
        });

        // Set an OnClickListener for the second card view
        cardView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bounceAnimation(v); // Add bounce animation on click
                toggleVisibilityWithAnimation(symptomsLvlDetails);
            }
        });

        // Set an OnClickListener for the third card view
        cardView3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bounceAnimation(v); // Add bounce animation on click
                toggleVisibilityWithAnimation(quinoa_details);
            }
        });

        // Set an OnClickListener for the third card view
        cardView4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bounceAnimation(v); // Add bounce animation on click
                toggleVisibilityWithAnimation(coconut_details);
            }
        });

        // Set an OnClickListener for the third card view
        cardView5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bounceAnimation(v); // Add bounce animation on click
                toggleVisibilityWithAnimation(rice_details);
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        if (activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setDisplayShowTitleEnabled(false); // Hide default title
            activity.getSupportActionBar().show(); // Show the fragment's toolbar


        }
    }

    private void toggleVisibilityWithAnimation(TextView textView) {
        if (textView.getVisibility() == View.GONE) {
            textView.setVisibility(View.VISIBLE);
            ObjectAnimator animator = ObjectAnimator.ofFloat(textView, "alpha", 0f, 1f);
            animator.setDuration(300); // Animation duration in milliseconds
            animator.start();
        } else {
            ObjectAnimator animator = ObjectAnimator.ofFloat(textView, "alpha", 1f, 0f);
            animator.setDuration(300); // Animation duration in milliseconds
            animator.addListener(new android.animation.AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(android.animation.Animator animation) {
                    textView.setVisibility(View.GONE);
                }
            });
            animator.start();
        }
    }

    private void bounceAnimation(View view) {
        // Create a bounce animation
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 0.9f, 1.1f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 0.9f, 1.1f, 1f);

        scaleX.setDuration(300); // Animation duration in milliseconds
        scaleY.setDuration(300); // Animation duration in milliseconds

        // Create an AnimatorSet to play both scale animations together
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(scaleX).with(scaleY);
        animatorSet.start();
    }

    private void navigateToHomeFragment() {
        // Show a confirmation dialog
        new AlertDialog.Builder(getContext())
                .setTitle("Confirm Navigation")
                .setMessage("Are you sure you want to leave this page?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Navigate to HomeFragment if the user confirms
                    Fragment homepageFragment = new HomeFragment();
                    getParentFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, homepageFragment)
                            .addToBackStack(null)
                            .commit();
                })
                .setNegativeButton("No", (dialog, which) -> {
                    // Do nothing, just dismiss the dialog
                    dialog.dismiss();
                })
                .show();
    }
}
