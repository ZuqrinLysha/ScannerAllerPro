package com.example.scannerallerpro;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
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
    private float scaleFactor = 1.0f;
    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector gestureDetector;
    private Handler zoomHandler = new Handler();
    private boolean isZooming = false;
    private boolean isHolding = false;
    private boolean isPausedByUser = false;

    private int[] images = {
            R.drawable.poster1, R.drawable.poster2, R.drawable.poster3,
            R.drawable.poster4, R.drawable.poster5, R.drawable.poster6, R.drawable.poster7
    }; // Image resources

    @SuppressLint("ClickableViewAccessibility")
    private void setupViewPagerTouchListener(ViewPager2 viewPager) {
        viewPager.setOnTouchListener((v, event) -> {
            scaleGestureDetector.onTouchEvent(event);
            gestureDetector.onTouchEvent(event);

            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                isPausedByUser = true;
                handler.removeCallbacks(runnable);
            } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                isPausedByUser = false;
                handler.postDelayed(runnable, delay);
            }

            return true; // Capture touch events for zooming and sliding
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize Toolbar
        toolbar = view.findViewById(R.id.toolbarHomePage);
        if (toolbar != null) {
            ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
            toolbar.setTitle("");
        } else {
            Log.e("HomeFragment", "Toolbar not found");
        }

        // Initialize DrawerLayout
        drawerLayout = requireActivity().findViewById(R.id.drawerLayout);
        if (drawerLayout != null) {
            toolbar.setNavigationIcon(R.drawable.baseline_menu); // Replace with your drawable
            toolbar.setNavigationOnClickListener(v -> {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    drawerLayout.openDrawer(GravityCompat.START);
                }
            });
        } else {
            Log.e("HomeFragment", "DrawerLayout not found");
        }

        // Initialize ViewPager2
        viewPager = view.findViewById(R.id.newsSlider);
        if (viewPager != null) {
            ImageSliderAdapter adapter = new ImageSliderAdapter(images, requireContext());
            viewPager.setAdapter(adapter);

            setupAutoSliding();
            // Set up hold-to-pause functionality
            viewPager.setOnTouchListener((v, event) -> {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // User starts holding down - pause the slider
                        isPausedByUser = true;
                        handler.removeCallbacks(runnable);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        // User releases the hold - resume the slider
                        isPausedByUser = false;
                        handler.postDelayed(runnable, delay);
                        break;
                }
                return false; // Return false to allow other listeners to process the touch event as well
            });

        } else {
            Log.e("HomeFragment", "ViewPager2 not found");
        }

        // Set up touch listeners for pinch-to-zoom and gestures
        scaleGestureDetector = new ScaleGestureDetector(getContext(), new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                scaleFactor *= detector.getScaleFactor();
                scaleFactor = Math.max(0.5f, Math.min(scaleFactor, 3.0f));
                viewPager.setScaleX(scaleFactor);
                viewPager.setScaleY(scaleFactor);
                return true;
            }
        });

        gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                scaleFactor = 1.0f;
                viewPager.setScaleX(scaleFactor);
                viewPager.setScaleY(scaleFactor);
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                isZooming = true;
                zoomInContinuously();
            }
        });

        viewPager.setOnTouchListener((v, event) -> {
            scaleGestureDetector.onTouchEvent(event);
            gestureDetector.onTouchEvent(event);

            if (event.getAction() == MotionEvent.ACTION_UP) {
                isZooming = false;
                zoomHandler.removeCallbacksAndMessages(null);
            }
            return true;
        });

        setupCardViewListeners(view);

        return view;
    }

    private void zoomInContinuously() {
        if (isZooming && scaleFactor < 3.0f) {
            scaleFactor += 0.1f;
            viewPager.setScaleX(scaleFactor);
            viewPager.setScaleY(scaleFactor);
            zoomHandler.postDelayed(this::zoomInContinuously, 50);
        }
    }

    private void setupAutoSliding() {
        handler = new Handler();
        runnable = () -> {
            if (!isPausedByUser && viewPager != null) {
                currentPage = (currentPage + 1) % images.length;
                viewPager.setCurrentItem(currentPage, true);
                handler.postDelayed(runnable, delay);
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
        isPausedByUser = false;
        handler.postDelayed(runnable, delay);
    }

    @Override
    public void onPause() {
        super.onPause();
        isPausedByUser = true;
        handler.removeCallbacks(runnable);
    }
}