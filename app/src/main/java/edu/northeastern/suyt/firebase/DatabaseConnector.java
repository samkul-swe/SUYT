package edu.northeastern.suyt.firebase;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DatabaseConnector {
    private static DatabaseConnector instance;
    private final FirebaseDatabase database;

    private DatabaseConnector() {
        database = FirebaseDatabase.getInstance();
    }

    public static synchronized DatabaseConnector getInstance() {
        if (instance == null) {
            instance = new DatabaseConnector();
        }
        return instance;
    }

    public DatabaseReference getUserReference(String userId) {
        return database.getReference("Users").child(userId);
    }

    public DatabaseReference getUsersReference() {
        return database.getReference("Users");
    }

    public DatabaseReference getPostsReference() {
        return database.getReference("Posts");
    }

    public DatabaseReference getPostReference(String postId) {
        return database.getReference("Posts").child(postId);
    }
}