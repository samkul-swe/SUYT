package edu.northeastern.suyt.controller;

import android.util.Log;

import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import edu.northeastern.suyt.R;
import edu.northeastern.suyt.model.Achievement;

public class AchievementsController {
    private static final String TAG = "AchievementsController";

    public AchievementsController() {

    }

    public void getUserAchievements(AchievementCallback callback) {
        String userId = userController.getCurrentUserId();
        if (userId == null) {
            callback.onFailure("No user is signed in");
            return;
        }

        db.collection(USERS_COLLECTION)
                .document(userId)
                .collection(ACHIEVEMENTS_COLLECTION)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Achievement> achievements = new ArrayList<>();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Achievement achievement = document.toObject(Achievement.class);
                        achievements.add(achievement);
                    }

                    callback.onSuccess(achievements);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting achievements", e);
                    callback.onFailure("Failed to get achievements: " + e.getMessage());
                });
    }

    public void achievementDisplayHelper(AchievementCallback callback) {
        // helps with the grid position of the achievements
    }

    private List<Achievement> createDefaultAchievements() {
        List<Achievement> achievements = new ArrayList<>();

        // Create achievements with grid positions (0-14 for a 5x3 grid)
        achievements.add(Achievement.createLocked("first_recycle", "First Recycle",
                "Recycle your first item and watch your garden begin to grow!", R.drawable.flower_tulip, 1, 0));

        achievements.add(Achievement.createLocked("eco_warrior", "Eco Warrior",
                "Recycle 10 items and prove your commitment to the planet.", R.drawable.flower_rose, 10, 1));

        achievements.add(Achievement.createLocked("planet_guardian", "Planet Guardian",
                "Reach 100 points by recycling consistently.", R.drawable.flower_sunflower, 100, 2));

        achievements.add(Achievement.createLocked("water_saver", "Water Saver",
                "Log 5 water-saving activities to earn this beautiful blue flower.", R.drawable.flower_bluebell, 5, 3));

        achievements.add(Achievement.createLocked("energy_efficient", "Energy Efficient",
                "Log 5 energy-saving activities to earn this bright yellow flower.", R.drawable.flower_daffodil, 5, 4));

        achievements.add(Achievement.createLocked("community_hero", "Community Hero",
                "Participate in a community cleanup event.", R.drawable.flower_orchid, 1, 5));

        achievements.add(Achievement.createLocked("plastic_reducer", "Plastic Reducer",
                "Avoid single-use plastic 20 times.", R.drawable.flower_lily, 20, 6));

        achievements.add(Achievement.createLocked("green_thumb", "Green Thumb",
                "Plant a tree or start a garden of your own.", R.drawable.flower_daisy, 1, 7));

        achievements.add(Achievement.createLocked("eco_teacher", "Eco Teacher",
                "Share eco-friendly tips with 3 friends through the app.", R.drawable.flower_poppy, 3, 8));

        achievements.add(Achievement.createLocked("master_recycler", "Master Recycler",
                "Recycle 100 items and become a true master of recycling!", R.drawable.flower_lotus, 100, 9));

        // Add more achievements to fill the grid
        achievements.add(Achievement.createLocked("zero_waste_day", "Zero Waste Day",
                "Go an entire day without producing any waste.", R.drawable.flower_iris, 1, 0));

        achievements.add(Achievement.createLocked("carbon_reducer", "Carbon Reducer",
                "Reduce your carbon footprint by using public transportation or biking.", R.drawable.flower_lily_of_the_valley, 5, 1));

        achievements.add(Achievement.createLocked("waste_auditor", "Waste Auditor",
                "Complete a full audit of your household waste.", R.drawable.flower_hyacinth, 1, 2));

        achievements.add(Achievement.createLocked("eco_shopper", "Eco Shopper",
                "Shop using only reusable bags 10 times.", R.drawable.flower_buttercup, 10, 3));

        achievements.add(Achievement.createLocked("composting_champion", "Composting Champion",
                "Start and maintain a compost bin for one month.", R.drawable.flower_violet, 30, 4));

        return achievements;
    }

    public interface AchievementCallback {
        void onSuccess(List<Achievement> achievements);
        void onFailure(String errorMessage);
    }

    public interface AchievementUpdateCallback {
        void onSuccess(Achievement achievement, boolean newlyUnlocked);
        void onFailure(String errorMessage);
    }
}
