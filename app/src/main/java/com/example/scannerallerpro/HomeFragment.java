package com.example.scannerallerpro;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentTransaction;

import com.example.scannerallerpro.R;

public class HomeFragment extends Fragment {

    private ViewPager2 viewPager;
    private int[] images = {R.drawable.poster1, R.drawable.poster2, R.drawable.poster3
    ,R.drawable.poster4, R.drawable.poster5, R.drawable.poster6, R.drawable.poster7}; // Your image resources
    private int currentPage = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        viewPager = view.findViewById(R.id.newsSlider);
        ImageSliderAdapter adapter = new ImageSliderAdapter(images);
        viewPager.setAdapter(adapter);

        // Set up click listeners for each CardView
        CardView cardAllergicHistory = view.findViewById(R.id.cardAllergicHistory);
        cardAllergicHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFragment(new AllergicHistoryFragment());
            }
        });

        CardView cardEmergencyContact = view.findViewById(R.id.cardEmergencyContact);
        cardEmergencyContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFragment(new ViewContactFragment());
            }
        });

        CardView cardScanner = view.findViewById(R.id.cardScanner);
        cardScanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFragment(new ScannerFragment());
            }
        });

        CardView cardKnowledge = view.findViewById(R.id.cardKnowledge);
        cardKnowledge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFragment(new KnowledgeFragment());
            }
        });

        return view;
    }

    private void openFragment(Fragment fragment) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
