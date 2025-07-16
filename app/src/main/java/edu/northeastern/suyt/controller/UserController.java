package edu.northeastern.suyt.controller;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

import edu.northeastern.suyt.firebase.repository.database.UsersRepository;

public class UserController {
    private static final String TAG = "UserController";

    private final DatabaseReference userRef;
    private final FirebaseAuth mAuth;
    FirebaseUser currentUser;

    public UserController(String userId) {
        UsersRepository userRepository = new UsersRepository(userId);
        userRef = userRepository.getUserRef();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
    }

    public void likePost(String postId, UpdateCallback callback) {
        Log.d(TAG, "Post liked: " + postId);
        try {
            userRef.child("likedPosts").child(postId).setValue(true);
            callback.onSuccess();
        } catch (Exception e) {
            Log.e(TAG, "Error liking post", e);
            callback.onFailure("Error liking post: " + e.getMessage());
        }
    }

    public void unlikePost(String postId, UpdateCallback callback) {
        Log.d(TAG, "Post unliked: " + postId);
        try {
            userRef.child("likedPosts").child(postId).removeValue();
            callback.onSuccess();
        } catch (Exception e) {
            Log.e(TAG, "Error unliking post", e);
            callback.onFailure("Error unliking post: " + e.getMessage());
        }
    }

    public void updateRank(String rank, UpdateCallback callback) {
        Log.d(TAG, "Updating rank to: " + rank);
        if (rank == null || rank.isEmpty()) {
            callback.onFailure("Invalid rank");
            return;
        }
        try {
            userRef.child("rank").setValue(rank);
            callback.onSuccess();
        } catch (Exception e) {
            Log.e(TAG, "Error updating rank", e);
            callback.onFailure("Error updating rank: " + e.getMessage());
        }
    }

    public void updateRecyclePoints(int points, UpdateCallback callback) {
        Log.d(TAG, "Updating recycle points to: " + points);
        if (points == 0) {
            callback.onFailure("Invalid points");
            return;
        }
        try {
            userRef.child("userStats").child("recyclePoints").setValue(points);
            callback.onSuccess();
        } catch (Exception e) {
            Log.e(TAG, "Error updating recycle points", e);
            callback.onFailure("Error updating recycle points: " + e.getMessage());
        }
    }

    public void updateReducePoints(int points, UpdateCallback callback) {
        Log.d(TAG, "Updating reduce points to: " + points);
        if (points == 0) {
            callback.onFailure("Invalid points");
            return;
        }
        try {
            userRef.child("userStats").child("reducePoints").setValue(points);
            callback.onSuccess();
        } catch (Exception e) {
            Log.e(TAG, "Error updating reduce points", e);
            callback.onFailure("Error updating reduce points: " + e.getMessage());
        }
    }

    public void updateReusePoints(int points, UpdateCallback callback) {
        Log.d(TAG, "Updating recuse points to: " + points);
        if (points == 0) {
            callback.onFailure("Invalid points");
            return;
        }
        try {
            userRef.child("userStats").child("reusePoints").setValue(points);
            callback.onSuccess();
        } catch (Exception e) {
            Log.e(TAG, "Error updating reuse points", e);
            callback.onFailure("Error updating reuse points: " + e.getMessage());
        }
    }

    public void updateUserName(String username, UpdateCallback callback) {
        Log.d(TAG, "Updating username to: " + username);
        if (username == null || username.isEmpty()) {
            callback.onFailure("Invalid username");
            return;
        }
        try {
            userRef.child("username").setValue(username);
            callback.onSuccess();
        } catch (Exception e) {
            Log.e(TAG, "Error updating username", e);
            callback.onFailure("Error updating username: " + e.getMessage());
        }
    }

    public void savePost(String postId, UpdateCallback callback) {
        Log.d(TAG, "Post saved: " + postId);
        try {
            userRef.child("savedPosts").child(postId).setValue(true);
            callback.onSuccess();
        } catch (Exception e) {
            Log.e(TAG, "Error saving post", e);
            callback.onFailure("Error saving post: " + e.getMessage());
        }
    }

    public void unsavePost(String postId, UpdateCallback callback) {
        Log.d(TAG, "Post unsaved: " + postId);
        try {
            userRef.child("savedPosts").child(postId).removeValue();
            callback.onSuccess();
        } catch (Exception e) {
            Log.e(TAG, "Error removing post", e);
            callback.onFailure("Error removing post: " + e.getMessage());
        }
    }

    public void logoutUser(LogoutCallback callback){
        try{
            Log.d("Logout Activity", "Logging out from profile");
            mAuth.signOut();
            callback.onSuccess();
        }catch(Exception e){
            Log.e(TAG, "Error during logout", e);
            callback.onFailure("Logout failed: " + e.getMessage());
        }
    }

    // CALLBACK INTERFACES
    public interface UpdateCallback {
        void onSuccess();
        void onFailure(String errorMessage);
    }

    public interface LogoutCallback {
        void onSuccess();
        void onFailure(String errorMessage);
    }

    public interface PasswordResetCallback {
        void onSuccess();
        void onFailure(String errorMessage);
    }
}