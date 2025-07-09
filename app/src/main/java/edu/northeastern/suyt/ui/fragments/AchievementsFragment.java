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
import edu.northeastern.suyt.model.User;
import edu.northeastern.suyt.utils.UtilityClass;

public class AchievementsFragment extends Fragment {

    private GridLayout gridShelf;
    private List<Achievement> achievementList;
    private TextView tvAchievementStats;
    private TextView tvSelectedAchievementTitle;
    private TextView tvSelectedAchievementDescription;
    private ProgressBar progressAchievement;
    private FloatingActionButton fabRefresh;
    private UtilityClass utility;

    private FirebaseFirestore db;
    private UserController userController;

    private static final int GRID_COLUMNS = 3;
    private static final int GRID_ROWS = 5;
    private static final int MAX_ACHIEVEMENTS = GRID_COLUMNS * GRID_ROWS;

    private View rootView;

    public AchievementsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_achievement, container, false);

        db = FirebaseFirestore.getInstance();
        User currentUser = utility.getUser(getContext());
        userController = new UserController(currentUser.getUserId());

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initUI();

        setupToolbar();

        achievementList = new ArrayList<>();

        loadAchievements();

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
                activity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            }
        }
    }

    private void loadAchievements() {
        createPlaceholderAchievements();

        User currentUser = utility.getUser(getContext());
        // Get the current user ID
        String userId = currentUser.getUserId();
        if (userId == null) {
            Toast.makeText(getContext(), "No user logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users").document(userId)
                .collection("achievements")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (getActivity() == null || !isAdded()) {
                        return;
                    }

                    List<Achievement> loadedAchievements = new ArrayList<>();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Achievement achievement = document.toObject(Achievement.class);
                        loadedAchievements.add(achievement);
                    }

                    if (!loadedAchievements.isEmpty()) {
                        achievementList.clear();
                        achievementList.addAll(loadedAchievements);
                    }

                    populateAchievementGrid();

                    updateAchievementStats();
                })
                .addOnFailureListener(e -> {
                    if (getActivity() == null || !isAdded()) {
                        return;
                    }

                    Toast.makeText(getContext(), "Failed to load achievements: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void createPlaceholderAchievements() {
        achievementList.clear();

        achievementList.add(Achievement.createLocked("first_recycle", "First Recycle",
                "Recycle your first item and watch your garden begin to grow!", R.drawable.flower_tulip, 1));

        achievementList.add(Achievement.createLocked("eco_warrior", "Eco Warrior",
                "Recycle 10 items and prove your commitment to the planet.", R.drawable.flower_rose, 10));

        achievementList.add(Achievement.createLocked("planet_guardian", "Planet Guardian",
                "Reach 100 points by recycling consistently.", R.drawable.flower_sunflower, 100));

        achievementList.add(Achievement.createLocked("water_saver", "Water Saver",
                "Log 5 water-saving activities to earn this beautiful blue flower.", R.drawable.flower_bluebell, 5));

        achievementList.add(Achievement.createLocked("energy_efficient", "Energy Efficient",
                "Log 5 energy-saving activities to earn this bright yellow flower.", R.drawable.flower_daffodil, 5));

        achievementList.add(Achievement.createLocked("community_hero", "Community Hero",
                "Participate in a community cleanup event.", R.drawable.flower_orchid, 1));

        achievementList.add(Achievement.createLocked("plastic_reducer", "Plastic Reducer",
                "Avoid single-use plastic 20 times.", R.drawable.flower_lily, 20));

        achievementList.add(Achievement.createLocked("green_thumb", "Green Thumb",
                "Plant a tree or start a garden of your own.", R.drawable.flower_daisy, 1));

        achievementList.add(Achievement.createLocked("eco_teacher", "Eco Teacher",
                "Share eco-friendly tips with 3 friends through the app.", R.drawable.flower_poppy, 3));

        achievementList.add(Achievement.createLocked("master_recycler", "Master Recycler",
                "Recycle 100 items and become a true master of recycling!", R.drawable.flower_lotus, 100));

        // Add more achievements to fill the grid if needed
        achievementList.add(Achievement.createLocked("zero_waste_day", "Zero Waste Day",
                "Go an entire day without producing any waste.", R.drawable.flower_iris, 1));

        achievementList.add(Achievement.createLocked("carbon_reducer", "Carbon Reducer",
                "Reduce your carbon footprint by using public transportation or biking.", R.drawable.flower_lily_of_the_valley, 5));

        achievementList.add(Achievement.createLocked("waste_auditor", "Waste Auditor",
                "Complete a full audit of your household waste.", R.drawable.flower_hyacinth, 1));

        achievementList.add(Achievement.createLocked("eco_shopper", "Eco Shopper",
                "Shop using only reusable bags 10 times.", R.drawable.flower_buttercup, 10));

        achievementList.add(Achievement.createLocked("composting_champion", "Composting Champion",
                "Start and maintain a compost bin for one month.", R.drawable.flower_violet, 30));

        achievementList.get(0).setUnlocked(true);
        achievementList.get(0).setUnlockDate("May 10, 2025");

        achievementList.get(1).setUnlockDate(null);

        achievementList.get(3).setUnlocked(true);
        achievementList.get(3).setUnlockDate("May 15, 2025");
    }

    private void populateAchievementGrid() {

        // TODO : Fix this

//        gridShelf.removeAllViews();
//
//        Collections.sort(achievementList, Comparator.comparingInt(Achievement::getGridPosition));
//
//        for (int i = 0; i < MAX_ACHIEVEMENTS; i++) {
//            Achievement achievement = null;
//            for (Achievement a : achievementList) {
//                if (a.getGridPosition() == i) {
//                    achievement = a;
//                    break;
//                }
//            }
//
//            if (achievement == null) {
//                achievement = Achievement.createLocked("empty_" + i, "Coming Soon",
//                        "A future achievement will bloom here", R.drawable.flower_iris, 1);
//            }
//
//            View achievementView = createAchievementView(achievement);
//
//            int row = i / GRID_COLUMNS;
//            int col = i % GRID_COLUMNS;
//
//            GridLayout.Spec rowSpec = GridLayout.spec(row, 1, 1f);
//            GridLayout.Spec colSpec = GridLayout.spec(col, 1, 1f);
//            GridLayout.LayoutParams params = new GridLayout.LayoutParams(rowSpec, colSpec);
//            params.width = 0;
//            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
//            params.setGravity(Gravity.FILL);
//            params.setMargins(8, 8, 8, 8);
//
//            gridShelf.addView(achievementView, params);
//        }
    }

    private View createAchievementView(Achievement achievement) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.item_achievement_flower, null);
        View lockOverlay = view.findViewById(R.id.ivLockOverlay);
        View flower = view.findViewById(R.id.ivFlower);
        TextView nameText = view.findViewById(R.id.tvAchievementName);

        nameText.setText(achievement.getTitle());

        if (achievement.isUnlocked()) {
            ((android.widget.ImageView) flower).setImageResource(achievement.getFlowerResourceId());
            lockOverlay.setVisibility(View.GONE);
            flower.setAlpha(1.0f);
        } else {
            ((android.widget.ImageView) flower).setImageResource(R.drawable.flower_iris);
            lockOverlay.setVisibility(View.VISIBLE);
            flower.setAlpha(0.5f);
        }

        final Achievement finalAchievement = achievement;
        view.setOnClickListener(v -> showAchievementDetails(finalAchievement));

        return view;
    }

    private void showAchievementDetails(Achievement achievement) {

        //TODO : Fix this

//        tvSelectedAchievementTitle.setText(achievement.getTitle());
//
//        String description = achievement.isUnlocked()
//                ? achievement.getDescription() + "\nUnlocked on: " + achievement.getUnlockDate()
//                : achievement.getDescription() + "\nProgress: " + achievement.getProgress()
//                + "/" + achievement.getMaxProgress();
//
//        tvSelectedAchievementDescription.setText(description);
//
//        if (!achievement.isUnlocked() && achievement.getMaxProgress() > 1) {
//            progressAchievement.setVisibility(View.VISIBLE);
//            progressAchievement.setMax(100);
//
//            ObjectAnimator animation = ObjectAnimator.ofInt(
//                    progressAchievement,
//                    "progress",
//                    0,
//                    (int) achievement.getProgressPercentage()
//            );
//            animation.setDuration(1000);
//            animation.setInterpolator(new DecelerateInterpolator());
//            animation.start();
//        } else {
//            progressAchievement.setVisibility(View.GONE);
//        }
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
        loadAchievements();
    }
}