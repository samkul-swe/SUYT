package edu.northeastern.suyt.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.ServerTimestamp;
import java.util.HashMap;
import java.util.Map;


public class User {
    @DocumentId
    private String userId;
    private String username;
    private String email;
    private UserStats userStats;

    public User() {}

    public User(String userId, String username, String email, String password) {
        this.userId = userId;
        this.username = username;
        this.email = email;
    }

    // Getters and Setters
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

    @Exclude
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
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
