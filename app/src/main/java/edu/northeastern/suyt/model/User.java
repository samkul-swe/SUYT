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
   // private String password;
    private @ServerTimestamp Timestamp createdAt;


    public User() {}

    public User(String userId, String username, String email, String password) {
        this.userId = userId;
        this.username = username;
        this.email = email;
       // this.password = password;
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

    /*public String getPassword() {
        return password;
    }*/

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    /*public void setPassword(String password) {
        this.password = password;
    }*/

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
