package edu.northeastern.suyt;

import android.app.Application;
import android.util.Log;

import com.google.firebase.FirebaseApp;

public class SUYTApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            FirebaseApp.initializeApp(this);
        } catch (Exception e) {
            Log.e("Firebase", "Failed to initialize Firebase", e);
        }
    }

}