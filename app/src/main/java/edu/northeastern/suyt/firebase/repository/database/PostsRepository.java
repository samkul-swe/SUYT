package edu.northeastern.suyt.firebase.repository.database;

import com.google.firebase.database.DatabaseReference;

import edu.northeastern.suyt.firebase.DatabaseConnector;

public class PostsRepository {
    private DatabaseReference postsRef;
    private DatabaseReference postRef;

    public PostsRepository() {
        postsRef = DatabaseConnector.getInstance().getPostsReference();
    }

    public PostsRepository(String postId) {
        postRef = DatabaseConnector.getInstance().getPostReference(postId);
    }

    public DatabaseReference getPostsRef() {
        return postsRef;
    }

    public DatabaseReference getPostRef() {
        return postRef;
    }
}
