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
    private String rank;
    private List<String> savedPosts;

    public User() {
        this.savedPosts = new ArrayList<>();
        this.userStats = new UserStats(0,0,0);
        this.rank = "Plant Soldier";
    }

    public User(String userId, String username, String email, boolean emailVerified, UserStats userStats, List<String> savedPosts, String rank) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.emailVerified = emailVerified;
        this.savedPosts = new ArrayList<>();
        this.userStats = new UserStats(0,0,0);
        this.rank = rank;
    }

    protected User(Parcel in) {
        userId = in.readString();
        username = in.readString();
        email = in.readString();
        emailVerified = in.readByte() != 0;
        savedPosts = in.createStringArrayList();
        userStats = in.readParcelable(UserStats.class.getClassLoader());
        rank = in.readString();
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

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
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
        parcel.writeParcelable(userStats, i);
        parcel.writeString(rank);
    }
}
