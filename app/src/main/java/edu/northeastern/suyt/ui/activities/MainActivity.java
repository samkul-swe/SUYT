package edu.northeastern.suyt.ui.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.appcheck.FirebaseAppCheck;

import edu.northeastern.suyt.R;
import edu.northeastern.suyt.ui.fragments.AchievementsFragment;
import edu.northeastern.suyt.ui.fragments.HomeFragment;
import edu.northeastern.suyt.ui.fragments.ProfileFragment;
import edu.northeastern.suyt.ui.fragments.RRRFragment;
import edu.northeastern.suyt.utils.SessionManager;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.getLimitedUseAppCheckToken();

        if (new SessionManager(this).isLoggedIn()) {
            try {
                setContentView(R.layout.activity_main);

                BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
                if (bottomNavigationView == null) {
                    finish();
                    return;
                }

                bottomNavigationView.setOnItemSelectedListener(item -> {
                    try {
                        Fragment fragment = null;
                        int id = item.getItemId();

                        if (id == R.id.nav_rrr) {
                            fragment = new RRRFragment();
                        } else if (id == R.id.nav_home) {
                            fragment = new HomeFragment();
                        } else if (id == R.id.nav_profile) {
                            fragment = new ProfileFragment();
                        } else if (id == R.id.nav_achievements) {
                            fragment = new AchievementsFragment();
                        }

                        return loadFragment(fragment);
                    } catch (Exception e) {
                        return false;
                    }
                });

                if (savedInstanceState == null) {
                    try {
                        bottomNavigationView.setSelectedItemId(R.id.nav_home);
                    } catch (Exception e) {
                        loadSimpleFallbackFragment();
                    }
                }

            } catch (Exception e) {
                // Show error and finish
                Toast.makeText(this, "Error loading main screen: " + e.getMessage(), Toast.LENGTH_LONG).show();
                finish();
            }
        }  else {
            // Show error and finish
            Toast.makeText(this, "User not logged in", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            try {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .commit();
                return true;
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

    private void loadSimpleFallbackFragment() {
        try {
            Fragment fallback = new Fragment() {
                @SuppressLint("SetTextI18n")
                @Override
                public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
                    TextView textView = new TextView(requireContext());
                    textView.setText("Welcome! Main screen is loading...");
                    textView.setGravity(Gravity.CENTER);
                    textView.setTextSize(18);
                    return textView;
                }
            };

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fallback)
                    .commit();
        } catch (Exception e) {
            Log.e(TAG, "Even fallback fragment failed", e);
        }
    }
}