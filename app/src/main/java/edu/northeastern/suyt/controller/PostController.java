package edu.northeastern.suyt.controller;

import android.content.Context;
import android.util.Log;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.northeastern.suyt.firebase.AuthConnector;
import edu.northeastern.suyt.firebase.repository.database.PostsRepository;
import edu.northeastern.suyt.model.Post;
import edu.northeastern.suyt.utils.UtilityClass;

// for particular post
public class PostController {

    private static final String TAG = "PostController";
    private static final String POSTS_COLLECTION = "posts";
    private static final String SAVED_POSTS_COLLECTION = "saved_posts";

    private final FirebaseAuth firebaseAuth;
    private final UtilityClass utility;
    private final Context appContext;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public PostController(Context context) {
        firebaseAuth = AuthConnector.getFirebaseAuth();
        utility = new UtilityClass();
        appContext = context;
    }



    /**
     * Get all posts saved by the current user
     */
    public void getUserSavedPosts(SavedPostsCallback callback) {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            callback.onFailure("User not authenticated");
            return;
        }

        String userId = currentUser.getUid();

        // First, get the list of saved post IDs for this user
        db.collection(SAVED_POSTS_COLLECTION)
                .whereEqualTo("userId", userId)
                .orderBy("savedAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> savedPostIds = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String postId = doc.getString("postId");
                        if (postId != null) {
                            savedPostIds.add(postId);
                        }
                    }

                    if (savedPostIds.isEmpty()) {
                        callback.onSuccess(new ArrayList<>());
                        return;
                    }

                    // Now get the actual posts
                    fetchPostsByIds(savedPostIds, callback);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to get saved post IDs", e);
                    callback.onFailure("Failed to load saved posts: " + e.getMessage());
                });
    }

    /**
     * Fetch posts by their IDs
     */
    private void fetchPostsByIds(List<String> postIds, SavedPostsCallback callback) {
        if (postIds.isEmpty()) {
            callback.onSuccess(new ArrayList<>());
            return;
        }

        // Firestore 'in' queries are limited to 10 items, so we need to batch them
        List<Post> allPosts = new ArrayList<>();
        int batchSize = 10;
        int totalBatches = (int) Math.ceil((double) postIds.size() / batchSize);
        int[] completedBatches = {0};

        for (int i = 0; i < postIds.size(); i += batchSize) {
            int end = Math.min(i + batchSize, postIds.size());
            List<String> batch = postIds.subList(i, end);

            db.collection(POSTS_COLLECTION)
                    .whereIn("id", batch)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            Post post = doc.toObject(Post.class);
                            if (post != null) {
//                                post.setSaved(true); // Mark as saved since we got it from saved posts
                                allPosts.add(post);
                            }
                        }

                        // Check if all batches are complete
                        completedBatches[0]++;
                        if (completedBatches[0] == totalBatches) {
                            // Sort by creation date (most recent first)
//                            allPosts.sort((p1, p2) -> Long.compare(p2.getTimestamp(), p1.getTimestamp()));
                            callback.onSuccess(allPosts);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to fetch posts batch", e);
                        callback.onFailure("Failed to load posts: " + e.getMessage());
                    });
        }
    }

    /**
     * Save a post for the current user
     */
    public void savePost(String postId, SavePostCallback callback) {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            callback.onFailure("User not authenticated");
            return;
        }

        String userId = currentUser.getUid();

        // Check if already saved
        db.collection(SAVED_POSTS_COLLECTION)
                .whereEqualTo("userId", userId)
                .whereEqualTo("postId", postId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        callback.onFailure("Post is already saved");
                        return;
                    }

                    // Create saved post record
                    Map<String, Object> savedPost = new HashMap<>();
                    savedPost.put("userId", userId);
                    savedPost.put("postId", postId);
                    savedPost.put("savedAt", System.currentTimeMillis());

                    db.collection(SAVED_POSTS_COLLECTION)
                            .add(savedPost)
                            .addOnSuccessListener(documentReference -> {
                                Log.d(TAG, "Post saved successfully");
                                callback.onSuccess();
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Failed to save post", e);
                                callback.onFailure("Failed to save post: " + e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to check if post is already saved", e);
                    callback.onFailure("Failed to save post: " + e.getMessage());
                });
    }

    /**
     * Unsave a post for the current user
     */
    public void unsavePost(String postId, SavePostCallback callback) {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            callback.onFailure("User not authenticated");
            return;
        }

        String userId = currentUser.getUid();

        db.collection(SAVED_POSTS_COLLECTION)
                .whereEqualTo("userId", userId)
                .whereEqualTo("postId", postId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        callback.onFailure("Post is not saved");
                        return;
                    }

                    // Delete all matching documents (should be only one)
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        doc.getReference().delete();
                    }

                    Log.d(TAG, "Post unsaved successfully");
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to unsave post", e);
                    callback.onFailure("Failed to unsave post: " + e.getMessage());
                });
    }

    /**
     * Check if a post is saved by the current user
     */
    public void isPostSaved(String postId, PostSavedCallback callback) {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            callback.onResult(false);
            return;
        }

        String userId = currentUser.getUid();

        db.collection(SAVED_POSTS_COLLECTION)
                .whereEqualTo("userId", userId)
                .whereEqualTo("postId", postId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    callback.onResult(!queryDocumentSnapshots.isEmpty());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to check if post is saved", e);
                    callback.onResult(false);
                });
    }

    /**
     * Get all posts (for main feed)
     */
    public void getAllPosts(PostsCallback callback) {
        db.collection(POSTS_COLLECTION)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Post> posts = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Post post = doc.toObject(Post.class);
                        if (post != null) {
                            posts.add(post);
                        }
                    }

                    callback.onSuccess(posts);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to get all posts", e);
                    callback.onFailure("Failed to load posts: " + e.getMessage());
                });
    }

    /**
     * Get saved posts count for a user
     */
    public void getSavedPostsCount(String userId, SavedPostsCountCallback callback) {
        db.collection(SAVED_POSTS_COLLECTION)
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    callback.onSuccess(queryDocumentSnapshots.size());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to get saved posts count", e);
                    callback.onFailure("Failed to get saved posts count: " + e.getMessage());
                });
    }


    // Callback interfaces
    public interface SavedPostsCallback {
        void onSuccess(List<Post> savedPosts);
        void onFailure(String errorMessage);
    }

    public interface SavePostCallback {
        void onSuccess();
        void onFailure(String errorMessage);
    }

    public interface PostsCallback {
        void onSuccess(List<Post> posts);
        void onFailure(String errorMessage);
    }

    public interface SavedPostsCountCallback {
        void onSuccess(int count);
        void onFailure(String errorMessage);
    }
}