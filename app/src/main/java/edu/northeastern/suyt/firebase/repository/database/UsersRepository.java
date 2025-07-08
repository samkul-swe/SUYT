package edu.northeastern.suyt.firebase.repository.database;

import com.google.firebase.database.DatabaseReference;

import edu.northeastern.suyt.firebase.DatabaseConnector;

public class UsersRepository {
    private DatabaseReference usersRef;
    private DatabaseReference userRef;

    public UsersRepository() {
        usersRef = DatabaseConnector.getInstance().getUsersReference();
    }

    public UsersRepository(String userId) {
        userRef = DatabaseConnector.getInstance().getUserReference(userId);
    }

    public DatabaseReference getUsersRef() {
        return usersRef;
    }

    public DatabaseReference getUserRef() {
        return userRef;
    }
}
