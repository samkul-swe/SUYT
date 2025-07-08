package edu.northeastern.suyt.firebase.repository.database;

import com.google.firebase.database.DatabaseReference;

import edu.northeastern.suyt.firebase.DatabaseConnector;

public class AchievementsRepository {
    private final DatabaseReference achievementsRef;

    public AchievementsRepository() {
        achievementsRef = DatabaseConnector.getInstance().getAchievementsReference();
    }

    public DatabaseReference getAchievementsRef() {
        return achievementsRef;
    }
}
