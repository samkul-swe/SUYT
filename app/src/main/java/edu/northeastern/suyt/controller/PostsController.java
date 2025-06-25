package edu.northeastern.suyt.controller;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.List;

import edu.northeastern.suyt.firebase.repository.database.PostsRepository;
import edu.northeastern.suyt.model.Post;

public class PostsController {
    private String TAG = "PostsController";
    private PostsRepository postsRepository;
    private DatabaseReference postsRef;
    private List<Post> allPosts;

    public PostsController() {
        postsRepository = new PostsRepository();
        postsRef = postsRepository.getPostsRef();
    }

    public void createPost(Post post, PostCreatedCallback callback) {
        try {
            postsRef.child(post.getPostID()).setValue(post);
            callback.onResult(true);
        } catch (Exception e) {
            callback.onResult(false);
        }
    }

    public void getAllPosts(GetAllPostsCallback callback) {
        try {
            new Thread(() -> {
                allPosts = new ArrayList<>();
                PostsRepository postsRepository = new PostsRepository();

                Task<DataSnapshot> task = postsRepository.getPostsRef().get();
                task.addOnSuccessListener(dataSnapshot -> {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                            Post post = new Post();
                            // Populate the post object
                            post.setPostID(postSnapshot.getKey());
                            post.setPostTitle(postSnapshot.child("title").getValue(String.class));
                            post.setPostDescription(postSnapshot.child("description").getValue(String.class));
                            post.setPostCategory(postSnapshot.child("category").getValue(String.class));
                            post.setPostedBy(postSnapshot.child("postedBy").getValue(String.class));
                            post.setPostedOn(postSnapshot.child("postedOn").getValue(String.class));
                            post.setPostImage(postSnapshot.child("image").getValue(String.class));
                            post.setNumberOfLikes(postSnapshot.child("likes").getValue(Integer.class)););
                            allPosts.add(post);
                        }
                        callback.onSuccess(allPosts);
                    } else {
                        callback.onFailure(new Exception("No posts found"));
                    }
                }).addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting posts - firebase", e);
                    callback.onFailure(e);
                });
            }).start();
        } catch (Exception e) {
            Log.e(TAG, "Error getting posts - thread error", e);
            callback.onFailure(e);
        }
    }

    //Callback interfaces
    public interface PostCreatedCallback {
        void onResult(boolean isSaved);
    }

    public interface GetAllPostsCallback {
        void onSuccess(List<Post> posts);
        void onFailure(Exception e);
    }
}
