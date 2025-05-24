package edu.northeastern.suyt.firebase.repository.database;

import com.google.firebase.database.DatabaseReference;

import edu.northeastern.suyt.firebase.DatabaseConnector;

public class PostsRepository {
    private final DatabaseReference postsRef;

    public PostsRepository() {
        postsRef = DatabaseConnector.getInstance().getPostsReference();
    }

    public DatabaseReference getPostsRef() {
        return postsRef;
    }
}
