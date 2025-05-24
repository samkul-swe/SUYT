package edu.northeastern.suyt.firebase;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DatabaseConnector {
    private static DatabaseConnector instance;
    private final FirebaseDatabase database;

    private DatabaseConnector() {
        // Initialize the Firebase Realtime Database
        database = FirebaseDatabase.getInstance();
    }

    public static synchronized DatabaseConnector getInstance() {
        if (instance == null) {
            instance = new DatabaseConnector();
        }
        return instance;
    }

    public DatabaseReference getUsersReference(String userId) {
        return database.getReference("Users").child(userId);
    }

    public DatabaseReference getPostsReference() {
        return database.getReference("Posts");
    }
}