package edu.northeastern.suyt;

import android.app.Application;
import android.util.Log;

import com.google.firebase.FirebaseApp;

public class SUYTApplication extends Application {
    private static final String TAG = "SUYTApplication";

    @Override
    public void onCreate() {
        Log.d(TAG, "=== SUYTApplication onCreate() START ===");

        try {
            Log.d(TAG, "Calling super.onCreate()...");
            super.onCreate();
            Log.d(TAG, "super.onCreate() completed successfully");

            Log.d(TAG, "Initializing Firebase...");
            FirebaseApp firebaseApp = FirebaseApp.initializeApp(this);

            if (firebaseApp != null) {
                Log.d(TAG, "Firebase initialized successfully: " + firebaseApp.getName());
            } else {
                Log.w(TAG, "Firebase initialization returned null");
            }

            Log.d(TAG, "=== SUYTApplication onCreate() COMPLETED SUCCESSFULLY ===");

        } catch (Exception e) {
            Log.e(TAG, "=== SUYTApplication onCreate() FAILED ===", e);
            // Print full stack trace
            e.printStackTrace();
            // Don't swallow the exception - let it crash to see full error
            throw new RuntimeException("SUYTApplication initialization failed", e);
        }
    }
}