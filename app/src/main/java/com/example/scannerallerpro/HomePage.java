package com.example.scannerallerpro;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.scannerallerpro.Profile;
import com.example.scannerallerpro.R;

public class HomePage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        // Find CardViews by ID
        CardView cardScanner = findViewById(R.id.scanner);
        CardView cardProfile = findViewById(R.id.profile);
        CardView cardAllergicInfo = findViewById(R.id.allergicinfo);


        cardProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePage.this, Profile.class);
                startActivity(intent);
            }
        });


    }
}
