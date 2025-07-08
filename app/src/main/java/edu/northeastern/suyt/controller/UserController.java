package edu.northeastern.suyt.controller;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

import java.util.Objects;

import edu.northeastern.suyt.firebase.AuthConnector;
import edu.northeastern.suyt.firebase.DatabaseConnector;
import edu.northeastern.suyt.firebase.repository.database.UsersRepository;
import edu.northeastern.suyt.model.User;
import edu.northeastern.suyt.utils.UtilityClass;

public class UserController {
    private static final String TAG = "UserController";

    private final FirebaseAuth mAuth;
    private final UtilityClass utility;
    private final Context appContext;

    public UserController(Context context) {
        mAuth = AuthConnector.getFirebaseAuth();
        utility = new UtilityClass();
        appContext = context;
    }

    public void updateRank(String rank, UpdateCallback callback) {

    }

    public void updateRecyclePoints(int points, UpdateCallback callback) {

    }

    public void updateReducePoints(int points, UpdateCallback callback) {

    }

    public void updateReusePoints(int points, UpdateCallback callback) {

    }

    public void updateUserName(String username, UpdateCallback callback) {

    }

    public void savePost(String postId, UpdateCallback callback) {
        User currentUser = utility.getUser(appContext);
        Log.d(TAG, "Post saved: " + postId);
        currentUser.addSavedPost(postId);
        utility.saveUser(appContext, currentUser);
        try {
            DatabaseConnector.getInstance().getUserReference(currentUser.getUserId()).child("savedPosts").child(postId).setValue(true);
            callback.onSuccess();
        } catch (Exception e) {
            Log.e(TAG, "Error saving post", e);
            callback.onFailure("Error saving post: " + e.getMessage());
        }
    }

    public void unsavePost(String postId, UpdateCallback callback) {
        User currentUser = utility.getUser(appContext);
        Log.d(TAG, "Post unsaved: " + postId);
        currentUser.removeSavedPost(postId);
        utility.saveUser(appContext, currentUser);
        try {
            DatabaseConnector.getInstance().getUserReference(currentUser.getUserId()).child("savedPosts").child(postId).removeValue();
            callback.onSuccess();
        } catch (Exception e) {
            Log.e(TAG, "Error removing post", e);
            callback.onFailure("Error removing post: " + e.getMessage());
        }
    }

    public void authenticateEmail(String email) {

    }

    public void getUserStats(String userId, StatsCallback callback) {

    }

    public Task<Void> updateUserEmail(String newEmail, String password) {
        User user = utility.getUser(appContext);
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
                UsersRepository userRepository = new UsersRepository(user.getUserId());
                DatabaseReference userRef = userRepository.getUserRef();
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
}