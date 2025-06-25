package edu.northeastern.suyt.firebase.repository.database;

import com.google.firebase.database.DatabaseReference;

import edu.northeastern.suyt.firebase.DatabaseConnector;

public class UserRepository {
    private final DatabaseReference userRef;

    public UserRepository(String userId) {
        userRef = DatabaseConnector.getInstance().getUserReference(userId);
    }

    public DatabaseReference getUserRef() {
        return userRef;
    }
}
