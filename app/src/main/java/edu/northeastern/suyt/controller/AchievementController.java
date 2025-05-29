package edu.northeastern.suyt.controller;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import edu.northeastern.suyt.R;
import edu.northeastern.suyt.model.Achievement;

public class AchievementController {
    private static final String TAG = "AchievementController";
    private static final String USERS_COLLECTION = "users";
    private static final String ACHIEVEMENTS_COLLECTION = "achievements";

    private final FirebaseFirestore db;
    private final UserController userController;

    public AchievementController() {
        db = FirebaseFirestore.getInstance();
        userController = new UserController();
    }

    /**
     * Initialize achievements for a new user
     */
    public void initializeUserAchievements(String userId, AchievementCallback callback) {
        if (userId == null || userId.isEmpty()) {
            callback.onFailure("Invalid user ID");
            return;
        }

        // Create default achievements
        List<Achievement> defaultAchievements = createDefaultAchievements();

        // Create a batch write operation
        db.runBatch(batch -> {
            for (Achievement achievement : defaultAchievements) {
                batch.set(
                        db.collection(USERS_COLLECTION)
                                .document(userId)
                                .collection(ACHIEVEMENTS_COLLECTION)
                                .document(achievement.getId()),
                        achievement
                );
            }
        }).addOnSuccessListener(aVoid -> {
            Log.d(TAG, "Default achievements initialized successfully");
            callback.onSuccess(defaultAchievements);
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error initializing achievements", e);
            callback.onFailure("Failed to initialize achievements: " + e.getMessage());
        });
    }

    /**
     * Get all achievements for the current user
     */
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

    /**
     * Update achievement progress
     */
    public void updateAchievementProgress(String achievementId, int progressToAdd, AchievementUpdateCallback callback) {
        String userId = userController.getCurrentUserId();
        if (userId == null) {
            callback.onFailure("No user is signed in");
            return;
        }

        // Reference to the achievement document
        db.collection(USERS_COLLECTION)
                .document(userId)
                .collection(ACHIEVEMENTS_COLLECTION)
                .document(achievementId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Achievement achievement = documentSnapshot.toObject(Achievement.class);
                        if (achievement != null) {
                            boolean wasUnlockedBefore = achievement.isUnlocked();

                            // Update progress
                            int newProgress = achievement.getProgress() + progressToAdd;
                            achievement.setProgress(newProgress);

                            // Check if newly unlocked
                            boolean isNewlyUnlocked = !wasUnlockedBefore && achievement.isUnlocked();

                            // If newly unlocked, set unlock date
                            if (isNewlyUnlocked) {
                                String currentDate = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
                                        .format(new Date());
                                achievement.setUnlockDate(currentDate);
                            }

                            // Update in Firestore
                            documentSnapshot.getReference().set(achievement)
                                    .addOnSuccessListener(aVoid -> {
                                        callback.onSuccess(achievement, isNewlyUnlocked);
                                    })
                                    .addOnFailureListener(e -> {
                                        callback.onFailure("Failed to update achievement: " + e.getMessage());
                                    });
                        } else {
                            callback.onFailure("Failed to parse achievement data");
                        }
                    } else {
                        callback.onFailure("Achievement not found");
                    }
                })
                .addOnFailureListener(e -> {
                    callback.onFailure("Failed to get achievement: " + e.getMessage());
                });
    }

    /**
     * Check and update achievements based on a recycling event
     */
    public void processRecyclingEvent(int points, AchievementCallback callback) {
        getUserAchievements(new AchievementCallback() {
            @Override
            public void onSuccess(List<Achievement> achievements) {
                List<Achievement> updatedAchievements = new ArrayList<>();
                List<Achievement> newlyUnlocked = new ArrayList<>();

                for (Achievement achievement : achievements) {
                    boolean wasUnlocked = achievement.isUnlocked();

                    // Update relevant achievements based on their ID
                    if (achievement.getId().equals("first_recycle") && !achievement.isUnlocked()) {
                        achievement.setProgress(1);
                        achievement.setUnlocked(true);

                        String currentDate = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
                                .format(new Date());
                        achievement.setUnlockDate(currentDate);

                        updatedAchievements.add(achievement);
                        if (!wasUnlocked && achievement.isUnlocked()) {
                            newlyUnlocked.add(achievement);
                        }
                    }
                    else if (achievement.getId().equals("eco_warrior")) {
                        int newProgress = achievement.getProgress() + 1;
                        achievement.setProgress(newProgress);

                        if (!wasUnlocked && achievement.isUnlocked()) {
                            String currentDate = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
                                    .format(new Date());
                            achievement.setUnlockDate(currentDate);
                            newlyUnlocked.add(achievement);
                        }

                        updatedAchievements.add(achievement);
                    }
                    else if (achievement.getId().equals("planet_guardian")) {
                        // This achievement is points-based
                        int currentPoints = 10;
//                                userController.getUserPoints();
                        achievement.setProgress(currentPoints);

                        if (!wasUnlocked && achievement.isUnlocked()) {
                            String currentDate = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
                                    .format(new Date());
                            achievement.setUnlockDate(currentDate);
                            newlyUnlocked.add(achievement);
                        }

                        updatedAchievements.add(achievement);
                    }
                    // Add other achievement types here
                }

                // Batch update the achievements in Firestore
                if (!updatedAchievements.isEmpty()) {
                    updateAchievementsInFirestore(updatedAchievements, newlyUnlocked, callback);
                } else {
                    callback.onSuccess(achievements);
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                callback.onFailure(errorMessage);
            }
        });
    }

    /**
     * Update a batch of achievements in Firestore
     */
    private void updateAchievementsInFirestore(List<Achievement> achievements,
                                               List<Achievement> newlyUnlocked,
                                               AchievementCallback callback) {
        String userId = userController.getCurrentUserId();
        if (userId == null) {
            callback.onFailure("No user is signed in");
            return;
        }

        db.runBatch(batch -> {
            for (Achievement achievement : achievements) {
                batch.set(
                        db.collection(USERS_COLLECTION)
                                .document(userId)
                                .collection(ACHIEVEMENTS_COLLECTION)
                                .document(achievement.getId()),
                        achievement
                );
            }
        }).addOnSuccessListener(aVoid -> {
            Log.d(TAG, "Achievements updated successfully");
            callback.onSuccess(achievements);

            // Log newly unlocked achievements
            for (Achievement achievement : newlyUnlocked) {
                Log.d(TAG, "Newly unlocked: " + achievement.getTitle());
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error updating achievements", e);
            callback.onFailure("Failed to update achievements: " + e.getMessage());
        });
    }

    /**
     * Get user's total points for achievements
     */
    private int getUserPoints() {
        // This would normally query the user's points from Firestore
        // For now, return a placeholder value
        return 75;
    }

    /**
     * Create default achievements list
     */
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
