package edu.northeastern.suyt.ui.fragments;

import android.animation.ObjectAnimator;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.GridLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import edu.northeastern.suyt.R;
import edu.northeastern.suyt.controller.UserController;
import edu.northeastern.suyt.model.Achievement;

public class AchievementsFragment extends Fragment {

    private GridLayout gridShelf;
    private List<Achievement> achievementList;
    private TextView tvAchievementStats;
    private TextView tvSelectedAchievementTitle;
    private TextView tvSelectedAchievementDescription;
    private ProgressBar progressAchievement;
    private FloatingActionButton fabRefresh;

    private FirebaseFirestore db;
    private UserController userController;

    // Constants for the grid
    private static final int GRID_COLUMNS = 3;
    private static final int GRID_ROWS = 5;
    private static final int MAX_ACHIEVEMENTS = GRID_COLUMNS * GRID_ROWS;

    private View rootView;

    public AchievementsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_achievement, container, false);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        userController = new UserController();

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize UI components
        initUI();

        // Setup toolbar
        setupToolbar();

        // Initialize achievement list
        achievementList = new ArrayList<>();

        // Load achievements data
        loadAchievements();

        // Set up FAB click listener
        fabRefresh.setOnClickListener(v -> {
            loadAchievements();
            Toast.makeText(getContext(), "Refreshing achievements...", Toast.LENGTH_SHORT).show();
        });
    }

    private void initUI() {
        gridShelf = rootView.findViewById(R.id.gridShelf);
        tvAchievementStats = rootView.findViewById(R.id.tvAchievementStats);
        tvSelectedAchievementTitle = rootView.findViewById(R.id.tvSelectedAchievementTitle);
        tvSelectedAchievementDescription = rootView.findViewById(R.id.tvSelectedAchievementDescription);
        progressAchievement = rootView.findViewById(R.id.progressAchievement);
        fabRefresh = rootView.findViewById(R.id.fabRefresh);
    }

    private void setupToolbar() {
        Toolbar toolbar = rootView.findViewById(R.id.toolbar);
        if (getActivity() instanceof AppCompatActivity) {
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            activity.setSupportActionBar(toolbar);
            if (activity.getSupportActionBar() != null) {
                activity.getSupportActionBar().setTitle("Achievement Garden");
                // Don't show the back button in the fragment
                activity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            }
        }
    }

    private void loadAchievements() {
        // First, create placeholder achievements for the grid
        createPlaceholderAchievements();

        // Get the current user ID
        String userId = userController.getCurrentUserId();
        if (userId == null) {
            Toast.makeText(getContext(), "No user logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        // Load user achievements from Firestore
        db.collection("users").document(userId)
                .collection("achievements")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (getActivity() == null || !isAdded()) {
                        return; // Fragment not attached to activity
                    }

                    List<Achievement> loadedAchievements = new ArrayList<>();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Achievement achievement = document.toObject(Achievement.class);
                        loadedAchievements.add(achievement);
                    }

                    // If there are no achievements in the database, keep using the placeholders
                    if (!loadedAchievements.isEmpty()) {
                        achievementList.clear();
                        achievementList.addAll(loadedAchievements);
                    }

                    // Populate the grid with achievements
                    populateAchievementGrid();

                    // Update the stats text
                    updateAchievementStats();
                })
                .addOnFailureListener(e -> {
                    if (getActivity() == null || !isAdded()) {
                        return; // Fragment not attached to activity
                    }

                    Toast.makeText(getContext(), "Failed to load achievements: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void createPlaceholderAchievements() {
        achievementList.clear();

        // Create sample achievements with different flower types and grid positions
//        achievementList.add(Achievement.createLocked("first_recycle", "First Recycle",
//                "Recycle your first item and watch your garden begin to grow!", R.drawable.flower_tulip, 1, 0));
//
//        achievementList.add(Achievement.createLocked("eco_warrior", "Eco Warrior",
//                "Recycle 10 items and prove your commitment to the planet.", R.drawable.flower_rose, 10, 1));
//
//        achievementList.add(Achievement.createLocked("planet_guardian", "Planet Guardian",
//                "Reach 100 points by recycling consistently.", R.drawable.flower_sunflower, 100, 2));
//
//        achievementList.add(Achievement.createLocked("water_saver", "Water Saver",
//                "Log 5 water-saving activities to earn this beautiful blue flower.", R.drawable.flower_bluebell, 5, 3));
//
//        achievementList.add(Achievement.createLocked("energy_efficient", "Energy Efficient",
//                "Log 5 energy-saving activities to earn this bright yellow flower.", R.drawable.flower_daffodil, 5, 4));
//
//        achievementList.add(Achievement.createLocked("community_hero", "Community Hero",
//                "Participate in a community cleanup event.", R.drawable.flower_orchid, 1, 5));
//
//        achievementList.add(Achievement.createLocked("plastic_reducer", "Plastic Reducer",
//                "Avoid single-use plastic 20 times.", R.drawable.flower_lily, 20, 6));
//
//        achievementList.add(Achievement.createLocked("green_thumb", "Green Thumb",
//                "Plant a tree or start a garden of your own.", R.drawable.flower_daisy, 1, 7));
//
//        achievementList.add(Achievement.createLocked("eco_teacher", "Eco Teacher",
//                "Share eco-friendly tips with 3 friends through the app.", R.drawable.flower_poppy, 3, 8));
//
//        achievementList.add(Achievement.createLocked("master_recycler", "Master Recycler",
//                "Recycle 100 items and become a true master of recycling!", R.drawable.flower_lotus, 100, 9));

        // Add more achievements to fill the grid if needed
        achievementList.add(Achievement.createLocked("zero_waste_day", "Zero Waste Day",
                "Go an entire day without producing any waste.", R.drawable.flower_iris, 1, 10));

        achievementList.add(Achievement.createLocked("carbon_reducer", "Carbon Reducer",
                "Reduce your carbon footprint by using public transportation or biking.", R.drawable.flower_lily_of_the_valley, 5, 11));

        achievementList.add(Achievement.createLocked("waste_auditor", "Waste Auditor",
                "Complete a full audit of your household waste.", R.drawable.flower_hyacinth, 1, 12));

        achievementList.add(Achievement.createLocked("eco_shopper", "Eco Shopper",
                "Shop using only reusable bags 10 times.", R.drawable.flower_buttercup, 10, 13));

        achievementList.add(Achievement.createLocked("composting_champion", "Composting Champion",
                "Start and maintain a compost bin for one month.", R.drawable.flower_violet, 30, 14));

        // For demonstration, make some achievements unlocked
        achievementList.get(0).setUnlocked(true);
        achievementList.get(0).setProgress(1);
        achievementList.get(0).setUnlockDate("May 10, 2025");

        achievementList.get(1).setProgress(7);
        achievementList.get(1).setUnlockDate(null);

        achievementList.get(3).setUnlocked(true);
        achievementList.get(3).setProgress(5);
        achievementList.get(3).setUnlockDate("May 15, 2025");
    }

    private void populateAchievementGrid() {
        // Clear existing views
        gridShelf.removeAllViews();

        // Sort achievements by grid position
        Collections.sort(achievementList, Comparator.comparingInt(Achievement::getGridPosition));

        // Create a view for each achievement position in the grid
        for (int i = 0; i < MAX_ACHIEVEMENTS; i++) {
            // Find the achievement for this position
            Achievement achievement = null;
            for (Achievement a : achievementList) {
                if (a.getGridPosition() == i) {
                    achievement = a;
                    break;
                }
            }

            // Create an empty placeholder if no achievement exists for this position
            if (achievement == null) {
                achievement = Achievement.createLocked("empty_" + i, "Coming Soon",
                        "A future achievement will bloom here", R.drawable.flower_iris, 1, i);
            }

            // Create the view for this achievement
            View achievementView = createAchievementView(achievement);

            // Calculate row and column for the current position
            int row = i / GRID_COLUMNS;
            int col = i % GRID_COLUMNS;

            // Create layout parameters for this view
            GridLayout.Spec rowSpec = GridLayout.spec(row, 1, 1f);
            GridLayout.Spec colSpec = GridLayout.spec(col, 1, 1f);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams(rowSpec, colSpec);
            params.width = 0;
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.setGravity(Gravity.FILL);
            params.setMargins(8, 8, 8, 8);

            // Add the view to the grid
            gridShelf.addView(achievementView, params);
        }
    }

    private View createAchievementView(Achievement achievement) {
        // Inflate the achievement item layout
        View view = LayoutInflater.from(getContext()).inflate(R.layout.item_achievement_flower, null);

        // Get references to the views
        View lockOverlay = view.findViewById(R.id.ivLockOverlay);
        View flower = view.findViewById(R.id.ivFlower);
        TextView nameText = view.findViewById(R.id.tvAchievementName);

        // Set achievement name
        nameText.setText(achievement.getTitle());

        // Set the correct flower image based on unlock status
        if (achievement.isUnlocked()) {
            // Use the unlocked flower image
            ((android.widget.ImageView) flower).setImageResource(achievement.getFlowerResourceId());
            lockOverlay.setVisibility(View.GONE);

            // Make the flower fully visible
            flower.setAlpha(1.0f);
        } else {
            // Use a generic locked flower or a dimmed version
            ((android.widget.ImageView) flower).setImageResource(R.drawable.flower_iris);
            lockOverlay.setVisibility(View.VISIBLE);

            // Optionally make it semi-transparent
            flower.setAlpha(0.5f);
        }

        // Set up click listener
        final Achievement finalAchievement = achievement;
        view.setOnClickListener(v -> showAchievementDetails(finalAchievement));

        return view;
    }

    private void showAchievementDetails(Achievement achievement) {
        // Update info card with selected achievement details
        tvSelectedAchievementTitle.setText(achievement.getTitle());

        String description = achievement.isUnlocked()
                ? achievement.getDescription() + "\nUnlocked on: " + achievement.getUnlockDate()
                : achievement.getDescription() + "\nProgress: " + achievement.getProgress()
                + "/" + achievement.getMaxProgress();

        tvSelectedAchievementDescription.setText(description);

        // Show progress bar for locked achievements
        if (!achievement.isUnlocked() && achievement.getMaxProgress() > 1) {
            progressAchievement.setVisibility(View.VISIBLE);
            progressAchievement.setMax(100);

            // Animate progress bar
            ObjectAnimator animation = ObjectAnimator.ofInt(
                    progressAchievement,
                    "progress",
                    0,
                    (int) achievement.getProgressPercentage()
            );
            animation.setDuration(1000);
            animation.setInterpolator(new DecelerateInterpolator());
            animation.start();
        } else {
            progressAchievement.setVisibility(View.GONE);
        }
    }

    private void updateAchievementStats() {
        int unlockedCount = 0;
        int totalCount = achievementList.size();

        for (Achievement achievement : achievementList) {
            if (achievement.isUnlocked()) {
                unlockedCount++;
            }
        }

        tvAchievementStats.setText("You've unlocked " + unlockedCount + "/" + totalCount + " flowers");
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh achievements when returning to the fragment
        loadAchievements();
    }
}