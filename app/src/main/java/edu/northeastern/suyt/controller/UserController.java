package edu.northeastern.suyt.controller;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

import edu.northeastern.suyt.model.User;

public class UserController {
    private static final String TAG = "UserController";
    private static final String USERS_COLLECTION = "users";

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    public UserController() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Register a new user with Firebase Authentication and Firestore
     */
    public void registerUser(String username, String email, String password, RegisterCallback callback) {
        Log.d(TAG, "Starting registration for: " + email);

        try {
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Authentication successful");

                            // Get Firebase user
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            if (firebaseUser != null) {
                                // Create User object
                                firebaseUser.sendEmailVerification()
                                        .addOnCompleteListener(emailTask -> {
                                            if(emailTask.isSuccessful()) {
                                                Log.d("Signup Activity", "Verification email sent to: " + email);
                                            }else{
                                                Log.e("Signup Activity", "Failed to send verification email",
                                                        emailTask.getException());
                                            }
                                        });
                                User user = User.fromFirebaseUser(firebaseUser, username);

                                Map<String, Object> userData = user.toMap();
                                userData.put("emailVerified", false);

                                // Save to Firestore
                                db.collection(USERS_COLLECTION)
                                        .document(user.getUserId())
                                        .set(userData)
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d(TAG, "User data stored successfully - calling onSuccess");
                                            callback.onSuccess();
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e(TAG, "Failed to store user data", e);
                                            callback.onFailure("Failed to store user data: " + e.getMessage());
                                        });
                            } else {
                                Log.e(TAG, "User is null after successful authentication");
                                callback.onFailure("Authentication successful but user is null");
                            }
                        } else {
                            // Authentication failed
                            Log.e(TAG, "Authentication failed", task.getException());
                            String errorMessage = task.getException() != null ?
                                    task.getException().getMessage() : "Authentication failed";
                            callback.onFailure(errorMessage);
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Unexpected exception in registerUser", e);
            callback.onFailure("Unexpected error: " + e.getMessage());
        }
    }


    public void signInUser(String email, String password, AuthCallback callback) {
        Log.d("Signup Activity", "Attempting sign in for: " + email);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Sign in successful");

                        // Get user data from Firestore
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            if (firebaseUser.isEmailVerified()) {
                                db.collection(USERS_COLLECTION)
                                        .document(firebaseUser.getUid())
                                        .update("emailVerified", true)
                                        .addOnCompleteListener(updateTask -> {
                                            getUserData(firebaseUser.getUid(), callback);
                                        });
                            } else {
                                firebaseUser.sendEmailVerification()
                                        .addOnCompleteListener(emailTask -> {
                                            if (emailTask.isSuccessful()) {
                                                Log.d("Signup Activity", "Verification sent from login");
                                            } else {
                                                Log.e("SignUp Activity", "Failed to send verification email", emailTask.getException());
                                            }
                                        });
                                mAuth.signOut();
                                callback.onFailure("Please verify your email before signing in. " +
                                        "A new verification email has been sent to " + email);
                            }
                        } else {
                            callback.onFailure("Sign in successful but user is null");
                        }


                    } else {
                        // Sign in failed
                        Log.e(TAG, "Sign in failed", task.getException());
                        String errorMessage = task.getException() != null ?
                                task.getException().getMessage() : "Sign in failed";
                        callback.onFailure(errorMessage);
                    }
                });
    }

    /**
     * Get user data from Firestore
     */
    public void getUserData(String userId, AuthCallback callback) {
        db.collection(USERS_COLLECTION)
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Convert document to User object
                        User user = documentSnapshot.toObject(User.class);
                        callback.onSuccess(user);
                    } else {
                        callback.onFailure("User data not found");
                    }
                })
                .addOnFailureListener(e -> {
                    callback.onFailure("Failed to get user data: " + e.getMessage());
                });
    }

    /**
     * Get current user data from Firestore
     */
    public void getCurrentUserData(AuthCallback callback) {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null) {
            getUserData(firebaseUser.getUid(), callback);
        } else {
            callback.onFailure("No user is signed in");
        }
    }

    /**
     * Check if a user is signed in
     */
    public boolean isUserSignedIn() {
        return mAuth.getCurrentUser() != null;
    }

    /**
     * Get the current Firebase user ID
     */
    public String getCurrentUserId() {
        FirebaseUser user = mAuth.getCurrentUser();
        return (user != null) ? user.getUid() : null;
    }

    /**
     * Sign out the current user
     */
    public void signOut() {
        mAuth.signOut();
    }

    /**
     * Update user profile
     */
    public void updateUserProfile(User user, UpdateCallback callback) {
        if (user == null || user.getUserId() == null) {
            callback.onFailure("Invalid user data");
            return;
        }

        db.collection(USERS_COLLECTION)
                .document(user.getUserId())
                .update(user.toMap())
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /**
     * Increment user recycling count and points
     */
    public void incrementRecycling(int pointsToAdd, UpdateCallback callback) {
        String userId = getCurrentUserId();
        if (userId == null) {
            callback.onFailure("No user signed in");
            return;
        }

        // Use a transaction to safely update counters
        db.runTransaction(transaction -> {
                    DocumentSnapshot snapshot = transaction.get(
                            db.collection(USERS_COLLECTION).document(userId));

                    if (snapshot.exists()) {
                        // Get current values
                        Long currentCount = snapshot.getLong("recycleCount");
                        Long currentPoints = snapshot.getLong("points");

                        int newCount = (currentCount != null ? currentCount.intValue() : 0) + 1;
                        int newPoints = (currentPoints != null ? currentPoints.intValue() : 0) + pointsToAdd;

                        // Update with new values
                        transaction.update(db.collection(USERS_COLLECTION).document(userId),
                                "recycleCount", newCount,
                                "points", newPoints);
                    }

                    return null;
                }).addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void logoutUser(LogoutCallback callback){
        try{
            Log.d("Signup Activity", "Logging out from profile");
            String userId = getCurrentUserId();
            mAuth.signOut();
            callback.onSuccess();
        }catch(Exception e){
            Log.e(TAG, "Error during logout", e);
            callback.onFailure("Logout failed: " + e.getMessage());
        }
    }




    public interface RegisterCallback {
        void onSuccess();
        void onFailure(String errorMessage);
    }

    public interface AuthCallback {
        void onSuccess(User user);
        void onFailure(String errorMessage);
    }

    public interface UpdateCallback {
        void onSuccess();
        void onFailure(String errorMessage);
    }

    public interface LogoutCallback {
        void onSuccess();
        void onFailure(String errorMessage);
    }
}