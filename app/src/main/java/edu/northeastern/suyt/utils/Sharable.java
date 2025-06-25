package edu.northeastern.suyt.utils;

import android.app.Application;

import edu.northeastern.suyt.model.User;

public class Sharable extends Application {
    private User currentUser;

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }
}

