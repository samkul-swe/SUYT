package edu.northeastern.suyt.firebase.repository.database;

import com.google.firebase.database.DatabaseReference;

import edu.northeastern.suyt.firebase.DatabaseConnector;

public class UsersRepository {
    private final DatabaseReference usersRef;

    public UsersRepository() {
        usersRef = DatabaseConnector.getInstance().getUsersReference();
    }

    public DatabaseReference getUsersRef() {
        return usersRef;
    }
}
