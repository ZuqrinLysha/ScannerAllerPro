package com.example.scannerallerpro;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.widget.ViewPager2;

public class HomeFragment extends Fragment {

    private ViewPager2 viewPager;
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private Handler handler;
    private Runnable runnable;
    private int currentPage = 0;
    private final int delay = 3000; // Delay in milliseconds for auto sliding
    private ScaleGestureDetector scaleGestureDetector;

    private int[] images = {
            R.drawable.poster1, R.drawable.poster2, R.drawable.poster3,
            R.drawable.poster4, R.drawable.poster5, R.drawable.poster6, R.drawable.poster7
    }; // Your image resources

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize Toolbar
        toolbar = view.findViewById(R.id.toolbarHomePage);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        toolbar.setTitle(""); // Remove the title from the toolbar

        // Set navigation icon for the toolbar
        toolbar.setNavigationIcon(R.drawable.baseline_menu); // Replace with your drawable resource
        toolbar.setNavigationOnClickListener(v -> {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        // Initialize DrawerLayout
        drawerLayout = requireActivity().findViewById(R.id.drawerLayout);

        // Initialize ViewPager2
        viewPager = view.findViewById(R.id.newsSlider);
        if (viewPager == null) {
            throw new NullPointerException("ViewPager not found in the layout");
        }
        ImageSliderAdapter adapter = new ImageSliderAdapter(images, requireContext());
        viewPager.setAdapter(adapter);

        // Set up auto sliding
        setupAutoSliding();

        // Set up click listeners for CardViews
        setupCardViewListeners(view);

        return view;
    }

    private void setupAutoSliding() {
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                if (currentPage >= images.length) {
                    currentPage = 0; // Reset to the first image
                }
                viewPager.setCurrentItem(currentPage++, true); // Slide to the next image
                handler.postDelayed(this, delay); // Repeat this runnable code block again after 'delay' milliseconds
            }
        };
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

    private void openFragment(Fragment fragment) {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
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

    public boolean onTouchEvent(MotionEvent event) {
        scaleGestureDetector.onTouchEvent(event);
        return true; // Ensure that touch events are handled properly
    }
}
