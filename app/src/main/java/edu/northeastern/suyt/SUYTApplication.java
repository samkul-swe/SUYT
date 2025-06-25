package edu.northeastern.suyt;

import android.util.Log;

import com.google.firebase.FirebaseApp;

import edu.northeastern.suyt.utils.Sharable;

public class SUYTApplication extends Sharable {
    private static final String TAG = "SUYTApplication";

    @Override
    public void onCreate() {
        try {
            super.onCreate();

            FirebaseApp firebaseApp = FirebaseApp.initializeApp(this);

            if (firebaseApp != null) {
                Log.d(TAG, "Firebase initialized successfully: " + firebaseApp.getName());
            } else {
                Log.w(TAG, "Firebase initialization returned null");
            }

        } catch (Exception e) {
            // Print full stack trace
            e.printStackTrace();
            // Don't swallow the exception - let it crash to see full error
            throw new RuntimeException("SUYTApplication initialization failed", e);
        }
    }
}