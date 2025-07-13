package edu.northeastern.suyt.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.DocumentId;

import java.util.ArrayList;
import java.util.List;

public class User implements Parcelable {
    @DocumentId
    private String userId;
    private String username;
    private String email;
    private boolean emailVerified = false;
    private UserStats userStats;
    private List<String> savedPosts;
    private List<String> likedPosts;

    public User() {
        this.savedPosts = new ArrayList<>();
        this.likedPosts = new ArrayList<>();
        this.userStats = new UserStats(0,0,0);
    }

    public User(String userId, String username, String email, boolean emailVerified) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.emailVerified = emailVerified;
        this.savedPosts = new ArrayList<>();
        this.likedPosts = new ArrayList<>();
        this.userStats = new UserStats(0,0,0);
    }

    protected User(Parcel in) {
        userId = in.readString();
        username = in.readString();
        email = in.readString();
        emailVerified = in.readByte() != 0;
        savedPosts = in.createStringArrayList();
        likedPosts = in.createStringArrayList();
        userStats = in.readParcelable(UserStats.class.getClassLoader());
    }

    public static final Creator<User> CREATOR = new Creator<>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

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

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
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

    public void addLikedPost(String postId) {
        if (likedPosts == null) {
            likedPosts = new ArrayList<>();
        }
        likedPosts.add(postId);
    }

    public void removeLikedPost(String postId) {
        if (likedPosts != null) {
            likedPosts.remove(postId);
        }
    }

    public List<String> getLikedPosts() {
        return likedPosts;
    }

    public void setLikedPosts(List<String> likedPosts) {
        this.likedPosts = likedPosts;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeString(userId);
        parcel.writeString(username);
        parcel.writeString(email);
        parcel.writeByte((byte) (emailVerified ? 1 : 0));
        parcel.writeStringList(savedPosts);
        parcel.writeStringList(likedPosts);
        parcel.writeParcelable(userStats, i);
    }
}
