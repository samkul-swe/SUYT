package edu.northeastern.suyt.model;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.Exclude;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class User {
    @DocumentId
    private String userId;
    private String username;
    private String email;
    private UserStats userStats;
    private List<String> savedPosts;

    public User() {
    }

    public User(String userId, String username, String email) {
        this.userId = userId;
        this.username = username;
        this.email = email;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public UserStats getUserStats() {
        return userStats;
    }

    public void setUserStats(UserStats userStats) {
        this.userStats = userStats;
    }

    public List<String> getSavedPosts() {
        return savedPosts;
    }

    public void setSavedPosts(List<String> savedPosts) {
        this.savedPosts = savedPosts;
    }

    public void addSavedPost(String postId) {
        if (savedPosts == null) {
            savedPosts = new ArrayList<>();
        }
        savedPosts.add(postId);
    }

    public void removeSavedPost(String postId) {
        if (savedPosts != null) {
            savedPosts.remove(postId);
        }
    }

    @Exclude
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("username", username);
        map.put("email", email);
        return map;
    }

    @Exclude
    public static User fromFirebaseUser(com.google.firebase.auth.FirebaseUser firebaseUser, String username) {
        if (firebaseUser == null) return null;
        User user = new User();
        user.setUserId(firebaseUser.getUid());
        user.setEmail(firebaseUser.getEmail());
        user.setUsername(username);
        return user;
    }
}
