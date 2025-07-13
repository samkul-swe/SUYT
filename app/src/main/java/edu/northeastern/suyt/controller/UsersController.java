package edu.northeastern.suyt.controller;

import android.content.Context;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.Objects;

import edu.northeastern.suyt.firebase.AuthConnector;
import edu.northeastern.suyt.firebase.repository.database.UsersRepository;
import edu.northeastern.suyt.model.User;
import edu.northeastern.suyt.model.UserStats;
import edu.northeastern.suyt.utils.SessionManager;

public class UsersController {
    private static final String TAG = "UsersController";
    private final FirebaseAuth mAuth;
    private final Context context;

    public UsersController(Context context) {
        mAuth = AuthConnector.getFirebaseAuth();
        this.context = context;
    }

    public void registerUser(String username, String email, String password, RegisterCallback callback) {
        Log.d(TAG, "Starting registration for: " + email);
        try {
            mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Creating user in firebase authentication successful.");
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            Log.d(TAG, "Creating user in firebase database...");
                            String uid = firebaseUser.getUid();
                            User currentUser = new User(uid, username, email, false);
                            saveUserToDatabase(currentUser, callback);
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

    public void saveUserToDatabase(User user, RegisterCallback callback) {
        SessionManager sessionManager = new SessionManager(context);
        DatabaseReference userRef = new UsersRepository(user.getUserId()).getUserRef();
        userRef.setValue(user).addOnSuccessListener(unused -> {
            Log.d(TAG, "User saved to database");
            sessionManager.saveLoginSession();
            sessionManager.saveUserData(user);
            Log.d(TAG, "Login session saved");
            callback.onSuccess();
        }).addOnFailureListener(exception -> {
            Log.e(TAG, "Error saving user to database", exception);
            callback.onFailure("Error saving user to database: " + exception.getMessage());
        });
    }

    public void logInUser(String email, String password, AuthCallback callback) {
        Log.d(TAG, "Attempting log in for: " + email);
        try {
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Sign in successful");
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            if (firebaseUser != null) {
                                getUserData(firebaseUser.getUid(), callback);
                            } else {
                                callback.onFailure("No User present");
                            }
                        } else {
                            Log.e(TAG, "Login failed", task.getException());
                            Exception exception = task.getException();

                            if (exception instanceof FirebaseAuthException) {
                                FirebaseAuthException authException = (FirebaseAuthException) exception;
                                String errorCode = authException.getErrorCode();

                                Log.d(TAG, "Error code: " + errorCode);

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
        DatabaseReference userRef = userRepository.getUserRef();

        userRef.get().addOnSuccessListener(dataSnapshot -> {
            if (dataSnapshot.exists()) {
                User user = new User();
                user.setUsername(dataSnapshot.child("username").getValue(String.class));
                user.setEmail(dataSnapshot.child("email").getValue(String.class));
                user.setEmailVerified(Boolean.TRUE.equals(dataSnapshot.child("emailVerified").getValue(Boolean.class)));
                if (dataSnapshot.child("savedPosts").exists()) {
                    for (Object postId : Objects.requireNonNull(dataSnapshot.child("savedPosts").getValue(ArrayList.class))) {
                        user.addSavedPost((String) postId);
                    }
                }
                if (dataSnapshot.child("userStats").exists()) {

                }
                user.setUserId(userId);
                Log.d(TAG, "User data retrieved: " + user);
                callback.onSuccess(user);
            } else {
                callback.onFailure("User data not found");
            }
        }).addOnFailureListener(exception -> Log.e(TAG, "Error getting user data", exception));
    }

    public void removeUser(String userId, RemoveUserCallback callback) {
        UsersRepository userRepository = new UsersRepository(userId);
        DatabaseReference userRef = userRepository.getUserRef();
        try {
            assert mAuth.getCurrentUser() != null;
            mAuth.getCurrentUser().delete();
            Log.d(TAG, "User removed from firebase authentication");
            userRef.removeValue();
            Log.d(TAG, "User removed from firebase database");
            callback.onSuccess();
        } catch (Exception e) {
            Log.e(TAG, "Error removing user", e);
            callback.onFailure("Error removing user: " + e.getMessage());
        }
    }

    public void updateUsers(UpdateUserCallBack callBack) {
        //TODO: Implement
    }

    //CALLBACK INTERFACES
    public interface RegisterCallback {
        void onSuccess();
        void onFailure(String errorMessage);
    }

    public interface AuthCallback {
        void onSuccess(User user);
        void onFailure(String errorMessage);
    }

    public interface RemoveUserCallback {
        void onSuccess();
        void onFailure(String errorMessage);
    }

    public interface UpdateUserCallBack {
        void onSuccess(boolean success);
        void onFailure(String errorMessage);
    }

    public interface GetUserNameCallback {
        void onSuccess(String username);
        void onFailure(String errorMessage);
    }

}