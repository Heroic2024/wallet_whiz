package com.example.mainproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;

public class others extends AppCompatActivity {

    private LinearLayout profileSection, settingsSection, helpSection, contactUsSection, exitSection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.others);

        // Find the sections
        profileSection = findViewById(R.id.profile_section);
        settingsSection = findViewById(R.id.settings_section);
        helpSection = findViewById(R.id.help_section);
        contactUsSection = findViewById(R.id.contact_us_section);
        exitSection = findViewById(R.id.exit_section);

        // Navigate to Profile activity
        profileSection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(others.this, profile.class);
                startActivity(intent);
            }
        });

        // Navigate to Settings activity
        settingsSection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(others.this, activity_setting.class);
                startActivity(intent);
            }
        });

        // Navigate to Help activity
        helpSection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(others.this, activity_help.class);
                startActivity(intent);
            }
        });

        // Navigate to Contact Us activity
        contactUsSection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(others.this, contact_us.class);
                startActivity(intent);
            }
        });

        // Exit the app
        exitSection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishAffinity(); // Closes all activities and exits the app
            }
        });
    }
}
