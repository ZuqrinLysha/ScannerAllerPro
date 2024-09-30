package com.example.scannerallerpro;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.scannerallerpro.R;
import com.example.scannerallerpro.ViewPagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class EmergencyContactFragment extends Fragment {
    TabLayout tabLayout;
    ViewPager2 viewpager;
    ViewPagerAdapter vpAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_emergency_contact, container, false);

        // Initialize TabLayout and ViewPager2
        tabLayout = view.findViewById(R.id.tabLayout);
        viewpager = view.findViewById(R.id.viewpager);

        // Set up the ViewPager adapter
        vpAdapter = new ViewPagerAdapter(getActivity());
        viewpager.setAdapter(vpAdapter);

        // Attach TabLayout to ViewPager2 using TabLayoutMediator
        new TabLayoutMediator(tabLayout, viewpager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Add Contact");
                    break;
                case 1:
                    tab.setText("View Contact");
                    break;
            }
        }).attach();

        return view;
    }
}
