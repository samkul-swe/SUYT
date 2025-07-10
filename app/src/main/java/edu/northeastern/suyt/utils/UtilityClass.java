package edu.northeastern.suyt.utils;

import android.content.Context;

import edu.northeastern.suyt.model.User;

public class UtilityClass {

    public void saveUser(Context context, User user) {
        Sharable app = (Sharable) context.getApplicationContext();
        app.setCurrentUser(user);
    }

    public User getUser(Context context) {
        Sharable app = (Sharable) context.getApplicationContext();
        return app.getCurrentUser();
    }

    public String getUserID(Context context){
        Sharable app = (Sharable) context.getApplicationContext();
        return app.getCurrentUser().getUserId();
    }
}
