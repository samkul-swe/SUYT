package edu.northeastern.suyt.controller;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import edu.northeastern.suyt.firebase.AuthConnector;
import edu.northeastern.suyt.firebase.DatabaseConnector;
import edu.northeastern.suyt.firebase.repository.database.UsersRepository;
import edu.northeastern.suyt.model.User;
import edu.northeastern.suyt.model.UserStats;
import edu.northeastern.suyt.ui.activities.SignUpActivity;
import edu.northeastern.suyt.utils.UtilityClass;

public class UserController {
    private static final String TAG = "UserController";
    private static final String USERS_COLLECTION = "Users";
    private static final String SAVED_POSTS_COLLECTION = "saved_posts";

    private final FirebaseAuth mAuth;
    private final DatabaseConnector db;
    private final UtilityClass utility;
    private final Context appContext;

    public UserController(Context context) {
        mAuth = AuthConnector.getFirebaseAuth();
        db = DatabaseConnector.getInstance();
        utility = new UtilityClass();
        appContext = context;
    }

    public void registerUser(String username, String email, String password, RegisterCallback callback) {
        Log.d(TAG, "Starting registration for: " + email);
        try {
            mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Authentication successful");
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            String uid = firebaseUser.getUid();
                            User currentUser = new User();
                            currentUser.setUsername(username);
                            currentUser.setUserId(uid);
                            UserStats userStats = new UserStats(0,0,0);
                            currentUser.setUserStats(userStats);
                            currentUser.setSavedPosts(new ArrayList<>());
                            currentUser.setEmail(email);
                            currentUser.setRank("Plant Soldier");
                            UsersRepository userRepository = new UsersRepository(uid);
                            DatabaseReference userRef = userRepository.getUsersRef();

                            userRef.setValue(currentUser);
                            callback.onSuccess();
                        } else {
                            Log.e(TAG, "User is null after successful authentication");
                            callback.onFailure("Authentication successful but user is null");
                        }
                    } else {
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


    public void logInUser(String email, String password, AuthCallback callback) {
        Log.d("Login Activity", "Attempting log in for: " + email);
        try {
            mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("Login Activity", "Sign in successful");
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            getUserData(firebaseUser.getUid(), callback);
                        } else {
                            callback.onFailure("No User present");
                        }
                    } else {
                        Log.e("Login activity", "Login failed", task.getException());
                        Exception exception = task.getException();

                        if (exception instanceof FirebaseAuthException) {
                            FirebaseAuthException authException = (FirebaseAuthException) exception;
                            String errorCode = authException.getErrorCode();

                            Log.d("Login activity", "Error code: " + errorCode);

                            if (errorCode.equals("ERROR_INVALID_CREDENTIAL")) {
                                callback.onFailure("Invalid Credentials");
                            } else {
                                callback.onFailure("Error: " + authException.getMessage());
                            }
                        } else {
                            callback.onFailure("Unknown error occurred");
                        }
                    }
                });
        }catch (Exception e) {
            callback.onFailure("Unexpected error: " + e.getMessage());
        }
    }

    public void getUserData(String userId, AuthCallback callback) {
        UsersRepository userRepository = new UsersRepository(userId);
        DatabaseReference userRef = userRepository.getUsersRef();

        userRef.get().addOnSuccessListener(dataSnapshot -> {
            if (dataSnapshot.exists()) {
                User user = new User();
                user.setUsername(dataSnapshot.child("username").getValue(String.class));
                user.setEmail(dataSnapshot.child("email").getValue(String.class));
                user.setEmailVerified(Boolean.TRUE.equals(dataSnapshot.child("emailVerified").getValue(Boolean.class)));
                user.setRank(dataSnapshot.child("rank").getValue(String.class));
                if (dataSnapshot.child("savedPosts").exists()) {
                    for (Object postId : Objects.requireNonNull(dataSnapshot.child("savedPosts").getValue(ArrayList.class))) {
                        user.addSavedPost((String) postId);
                    }
                }
                user.setUserStats(dataSnapshot.child("userStats").getValue(UserStats.class));
                user.setUserId(userId);

                Log.d("Login Activity", "User data retrieved: " + user);
                utility.saveUser(appContext, user);
                callback.onSuccess(user);
            } else {
                callback.onFailure("User data not found");
            }
        }).addOnFailureListener(exception -> {
            Log.e("Login Activity", "Error getting user data", exception);
        });
    }

    public void getCurrentUser(UserDataCallback callback) {
        User user = utility.getUser(appContext);
        if (user != null) {
            callback.onSuccess(user.getUsername(), user.getEmail(), user.getSavedPosts(), user.getUserStats(), user.getRank());
        } else {
            callback.onFailure("No user data found");
        }
    }

    public boolean isUserSignedIn() {
        User user = utility.getUser(appContext);
        return user != null;
    }

    public String getCurrentUserId() {
        User user = utility.getUser(appContext);
        return (user != null) ? user.getUserId() : null;
    }

    public Task<Void> updateUserEmail(String newEmail, String password) {
        Log.d(TAG, "Attempting to update email to: " + newEmail);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "No user is currently logged in");
            return Tasks.forException(new Exception("No user is currently logged in"));
        }

        String currentEmail = currentUser.getEmail();
        if (currentEmail == null) {
            Log.e(TAG, "Current user has no email");
            return Tasks.forException(new Exception("Current user has no email"));
        }

        AuthCredential credential = EmailAuthProvider.getCredential(currentEmail, password);

        return currentUser.reauthenticate(credential)
            .continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    Log.e(TAG, "Failed to re-authenticate", task.getException());
                    throw new Exception("Incorrect password. Please try again.");
                }

                Log.d(TAG, "User re-authenticated successfully");
                return currentUser.updateEmail(newEmail);
            })
            .continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    Log.e(TAG, "Failed to update email in Authentication", task.getException());
                    throw Objects.requireNonNull(task.getException());
                }

                Log.d(TAG, "Email updated in Authentication");

                // Update email in Firestore
                UsersRepository userRepository = new UsersRepository(currentUser.getUid());
                DatabaseReference userRef = userRepository.getUsersRef();
                return userRef.setValue("email", newEmail);
            })
            .continueWith(task -> {
                if (!task.isSuccessful()) {
                    Log.e(TAG, "Failed to update email in Firestore", task.getException());
                    throw Objects.requireNonNull(task.getException());
                }

                Log.d(TAG, "Email updated in Firestore");
                return null;
            });
    }

    public void updateUserEmailWithCallback(String newEmail, String password, UpdateCallback callback) {
        updateUserEmail(newEmail, password)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public Task<Void> updateUserPassword(String currentPassword, String newPassword) {
        Log.d(TAG, "Attempting to update password");

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "No user is currently logged in");
            return Tasks.forException(new Exception("No user is currently logged in"));
        }

        String currentEmail = currentUser.getEmail();
        if (currentEmail == null) {
            Log.e(TAG, "Current user has no email");
            return Tasks.forException(new Exception("Current user has no email"));
        }

        AuthCredential credential = EmailAuthProvider.getCredential(currentEmail, currentPassword);

        return currentUser.reauthenticate(credential)
            .continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    Log.e(TAG, "Failed to re-authenticate", task.getException());
                    throw new Exception("Incorrect password. Please try again.");
                }

                Log.d(TAG, "User re-authenticated successfully");
                return currentUser.updatePassword(newPassword);
            })
            .continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    Log.e(TAG, "Failed to update password", task.getException());
                    throw Objects.requireNonNull(task.getException());
                }

                Log.d(TAG, "Password updated successfully");
                return Tasks.forResult(null);
            });
    }

    public void sendPasswordResetEmail(String email, PasswordResetCallback callback) {
        Log.d(TAG, "Sending password reset email to: " + email);

        if (email == null || email.trim().isEmpty()) {
            callback.onFailure("Please enter your email address");
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            callback.onFailure("Please enter a valid email address");
            return;
        }

        mAuth.sendPasswordResetEmail(email.trim())
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.d(TAG, "Password reset email sent successfully");
                    callback.onSuccess();
                } else {
                    Log.e(TAG, "Failed to send password reset email", task.getException());

                    Exception exception = task.getException();
                    String errorMessage = "Failed to send password reset email";

                    if (exception instanceof FirebaseAuthException) {
                        FirebaseAuthException authException = (FirebaseAuthException) exception;
                        String errorCode = authException.getErrorCode();

                        switch (errorCode) {
                            case "ERROR_USER_NOT_FOUND":
                                errorMessage = "No account found with this email address";
                                break;
                            case "ERROR_INVALID_EMAIL":
                                errorMessage = "Invalid email address";
                                break;
                            case "ERROR_TOO_MANY_REQUESTS":
                                errorMessage = "Too many requests. Please try again later";
                                break;
                            default:
                                errorMessage = authException.getMessage();
                                break;
                        }
                    }

                    callback.onFailure(errorMessage);
                }
            });
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

    public interface PasswordResetCallback {
        void onSuccess();
        void onFailure(String errorMessage);
    }

    public interface UserDataCallback {
        void onSuccess(String username, String email, List<String> savedPosts, UserStats userStats, String rank);
        void onFailure(String errorMessage);
    }

    public interface UserStatsCallback {
        void onSuccess(int postsCount, int impactScore, int savedCount);
        void onFailure(String errorMessage);
    }
}