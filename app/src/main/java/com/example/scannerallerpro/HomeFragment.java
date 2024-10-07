package com.example.scannerallerpro;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentTransaction;

public class HomeFragment extends Fragment {

    private ViewPager2 viewPager;
    private int[] images = {R.drawable.poster1, R.drawable.poster2, R.drawable.poster3,
            R.drawable.poster4, R.drawable.poster5, R.drawable.poster6, R.drawable.poster7}; // Your image resources
    private Handler handler;
    private Runnable runnable;
    private int currentPage = 0;
    private final int delay = 3000; // Delay in milliseconds for auto sliding (3 seconds)

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        viewPager = view.findViewById(R.id.newsSlider);
        ImageSliderAdapter adapter = new ImageSliderAdapter(images);
        viewPager.setAdapter(adapter);

        // Set up click listeners for each CardView
        setupCardViewListeners(view);

        // Set up auto sliding
        setupAutoSliding();

        return view;
    }

    private void setupCardViewListeners(View view) {
        CardView cardAllergicHistory = view.findViewById(R.id.cardAllergicHistory);
        cardAllergicHistory.setOnClickListener(v -> openFragment(new AllergicHistoryFragment()));

        CardView cardEmergencyContact = view.findViewById(R.id.cardEmergencyContact);
        cardEmergencyContact.setOnClickListener(v -> openFragment(new ViewContactFragment()));

        CardView cardScanner = view.findViewById(R.id.cardScanner);
        cardScanner.setOnClickListener(v -> openFragment(new ScannerFragment()));

        CardView cardKnowledge = view.findViewById(R.id.cardKnowledge);
        cardKnowledge.setOnClickListener(v -> openFragment(new KnowledgeFragment()));
    }

    private void setupAutoSliding() {
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                if (currentPage == images.length) {
                    currentPage = 0; // Reset to the first image
                }
                viewPager.setCurrentItem(currentPage++, true); // Slide to the next image
                handler.postDelayed(this, delay); // Repeat this runnable code block again after 'delay' milliseconds
            }
        };
    }

    private void openFragment(Fragment fragment) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        handler.postDelayed(runnable, delay); // Start auto sliding
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(runnable); // Stop auto sliding
    }
}
