package edu.northeastern.suyt.ui.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import edu.northeastern.suyt.R;
import edu.northeastern.suyt.ui.fragments.HomeFragment;
import edu.northeastern.suyt.ui.fragments.ProfileFragment;
import edu.northeastern.suyt.ui.fragments.RRRFragment;
import edu.northeastern.suyt.ui.fragments.TipsFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Use the newer listener API
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            int id = item.getItemId();

            if (id == R.id.nav_rrr) {
                fragment = new RRRFragment();
            } else if (id == R.id.nav_home) {
                fragment = new HomeFragment();
            } else if (id == R.id.nav_tips) {
                fragment = new TipsFragment();
            } else if (id == R.id.nav_profile) {
                fragment = new ProfileFragment();
            } else if (id == R.id.nav_achievements) {
                // Navigate to achievements screen
                startActivity(new Intent(this, AchievementsActivity.class));
                return true;
            }

            return loadFragment(fragment);
        });

        // Check if this is the first creation (not a configuration change)
        if (savedInstanceState == null) {
            // Load Home Fragment by default
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        }
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }
}