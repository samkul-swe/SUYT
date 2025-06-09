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

    public DatabaseReference getUsersReference(String userId) {
        return database.getReference("Users").child(userId);
    }

    public DatabaseReference getPostsReference() {
        return database.getReference("Posts");
    }

    public DatabaseReference getPostReference(String postId) {
        return database.getReference("Posts").child(postId);
    }

    public DatabaseReference getLikesReference(String userId) {
        return getUsersReference(userId).child("likes");
    }

    public DatabaseReference getUserSavedPostsReference(String userId) {
        return getUsersReference(userId).child("saved_posts");
    }

    public DatabaseReference getUserPostLikeReference(String userId, String postId) {
        return getLikesReference(userId).child(postId);
    }

    public DatabaseReference getUserPostSaveReference(String userId, String postId) {
        return getUserSavedPostsReference(userId).child(postId);
    }

    public DatabaseReference getAnalysisResultsReference() {
        return database.getReference("AnalysisResults");
    }

    public DatabaseReference getAnalysisResultReference(String analysisId) {
        return getAnalysisResultsReference().child(analysisId);
    }

    public DatabaseReference getUserAnalysisHistoryReference(String userId) {
        return getUsersReference(userId).child("analysis_history");
    }

    public DatabaseReference getUserRecentAnalysisReference(String userId) {
        return getUsersReference(userId).child("recent_analysis");
    }

    public DatabaseReference getUserSpecificAnalysisReference(String userId, String analysisId) {
        return getUserAnalysisHistoryReference(userId).child(analysisId);
    }
}