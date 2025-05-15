package edu.northeastern.suyt.controller;

import edu.northeastern.suyt.model.User;

public class UserController {

    public boolean registerUser(String username, String email, String password) {
        // Implement registration logic
        // For now, just return true to simulate success
        return true;
    }

    public User loginUser(String email, String password) {
        // Implement login logic
        // For now, return a dummy user to simulate success
        return new User("1", "dummy_user", email, "");
    }

    public boolean logoutUser() {
        // Implement logout logic
        return true;
    }
}
