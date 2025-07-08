package edu.northeastern.suyt.controller;

import android.util.Log;

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
    private com.google.firebase.database.ValueEventListener valueEventListener;

    private List<Post> cachedPosts;
    private boolean isInitialLoadComplete = false;

    public PostsController() {
        postsRepository = new PostsRepository();
        postsRef = postsRepository.getPostsRef();
        cachedPosts = new ArrayList<>();
    }

    public void createPost(Post post, PostCreatedCallback callback) {
        postsRef.child(post.getPostID()).setValue(post)
                .addOnSuccessListener(aVoid -> {
                    // Add to cache after successful creation
                    addPostToCache(post);
                    callback.onResult(true);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating post", e);
                    callback.onResult(false);
                });
    }

    // Load initial posts (first 50) - call this once when app starts
    public void loadInitialPosts(GetAllPostsCallback callback) {
        postsRef.limitToLast(50).get()
                .addOnSuccessListener(dataSnapshot -> {
                    cachedPosts.clear();

                    if (dataSnapshot.exists()) {
                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                            try {
                                Post post = createPostFromSnapshot(postSnapshot);
                                if (post != null) {
                                    cachedPosts.add(post);
                                }
                            } catch (Exception e) {
                                Log.w(TAG, "Error parsing post: " + postSnapshot.getKey(), e);
                            }
                        }
                    }

                    isInitialLoadComplete = true;
                    Log.d(TAG, "Loaded " + cachedPosts.size() + " initial posts");
                    callback.onSuccess(new ArrayList<>(cachedPosts)); // Return copy
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading initial posts", e);
                    callback.onFailure(e);
                });
    }

    // Get all cached posts (no database call)
    public void getAllPosts(GetAllPostsCallback callback) {
        if (!isInitialLoadComplete) {
            callback.onFailure(new IllegalStateException("Posts not loaded yet. Call loadInitialPosts() first."));
            return;
        }

        // Return a copy of cached posts immediately
        callback.onSuccess(new ArrayList<>(cachedPosts));
    }

    // Get user's posts from cached data (no database call)
    public void getUserCreatedPosts(String userID, GetAllPostsCallback callback) {
        if (userID == null || userID.trim().isEmpty()) {
            callback.onFailure(new IllegalArgumentException("UserID cannot be null or empty"));
            return;
        }

        if (!isInitialLoadComplete) {
            callback.onFailure(new IllegalStateException("Posts not loaded yet. Call loadInitialPosts() first."));
            return;
        }

        List<Post> userPosts = new ArrayList<>();
        for (Post post : cachedPosts) {
            if (userID.equals(post.getPostedBy())) {
                userPosts.add(post);
            }
        }

        Log.d(TAG, "Found " + userPosts.size() + " cached posts for user: " + userID);
        callback.onSuccess(userPosts);
    }

    // Get post by ID from cached data (no database call)
    public void getPostByID(String postID, GetPostCallback callback) {
        if (postID == null || postID.trim().isEmpty()) {
            callback.onFailure(new IllegalArgumentException("PostID cannot be null or empty"));
            return;
        }

        if (!isInitialLoadComplete) {
            callback.onFailure(new IllegalStateException("Posts not loaded yet. Call loadInitialPosts() first."));
            return;
        }

        for (Post post : cachedPosts) {
            if (postID.equals(post.getPostID())) {
                callback.onSuccess(post);
                return;
            }
        }
    }

    // Get user saved posts from cached data (no database call)
    public void getUserSavedPosts(List<String> savedPostIDs, GetAllPostsCallback callback) {
        if (savedPostIDs == null || savedPostIDs.isEmpty()) {
            callback.onSuccess(new ArrayList<>());
            return;
        }

        if (!isInitialLoadComplete) {
            callback.onFailure(new IllegalStateException("Posts not loaded yet. Call loadInitialPosts() first."));
            return;
        }

        List<Post> savedPosts = new ArrayList<>();
        for (Post post : cachedPosts) {
            if (savedPostIDs.contains(post.getPostID())) {
                savedPosts.add(post);
            }
        }

        Log.d(TAG, "Found " + savedPosts.size() + " saved posts");
        callback.onSuccess(savedPosts);
    }

    // Get posts by category from cached data (no database call)
    public void getPostsByCategory(String category, GetAllPostsCallback callback) {
        if (category == null || category.trim().isEmpty()) {
            callback.onFailure(new IllegalArgumentException("Category cannot be null or empty"));
            return;
        }

        if (!isInitialLoadComplete) {
            callback.onFailure(new IllegalStateException("Posts not loaded yet. Call loadInitialPosts() first."));
            return;
        }

        List<Post> categoryPosts = new ArrayList<>();
        for (Post post : cachedPosts) {
            if (category.equals(post.getPostCategory())) {
                categoryPosts.add(post);
            }
        }

        Log.d(TAG, "Found " + categoryPosts.size() + " posts in category: " + category);
        callback.onSuccess(categoryPosts);
    }

    // Load more posts for pagination (append to existing cache)
    public void loadMorePosts(int limit, GetAllPostsCallback callback) {
        if (cachedPosts.isEmpty()) {
            callback.onFailure(new IllegalStateException("No posts loaded yet. Call loadInitialPosts() first."));
            return;
        }

        // Get the oldest post's key for pagination
        String lastPostKey = cachedPosts.get(0).getPostID(); // Assuming posts are ordered by creation time

        postsRef.orderByKey().endBefore(lastPostKey).limitToLast(limit).get()
                .addOnSuccessListener(dataSnapshot -> {
                    List<Post> newPosts = new ArrayList<>();

                    if (dataSnapshot.exists()) {
                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                            try {
                                Post post = createPostFromSnapshot(postSnapshot);
                                if (post != null) {
                                    newPosts.add(post);
                                }
                            } catch (Exception e) {
                                Log.w(TAG, "Error parsing post: " + postSnapshot.getKey(), e);
                            }
                        }
                    }

                    // Add new posts to the beginning of cached posts
                    cachedPosts.addAll(0, newPosts);
                    Log.d(TAG, "Loaded " + newPosts.size() + " more posts. Total cached: " + cachedPosts.size());

                    callback.onSuccess(newPosts); // Return only the new posts
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading more posts", e);
                    callback.onFailure(e);
                });
    }

    private Post createPostFromSnapshot(DataSnapshot postSnapshot) {
        try {
            Post post = new Post();
            post.setPostID(postSnapshot.getKey());
            post.setPostTitle(postSnapshot.child("title").getValue(String.class));
            post.setPostDescription(postSnapshot.child("description").getValue(String.class));
            post.setPostCategory(postSnapshot.child("category").getValue(String.class));
            post.setPostedBy(postSnapshot.child("postedBy").getValue(String.class));
            post.setPostedOn(postSnapshot.child("postedOn").getValue(String.class));
            post.setPostImage(postSnapshot.child("image").getValue(String.class));

            // Handle potential null values for integers
            Integer likes = postSnapshot.child("likes").getValue(Integer.class);
            post.setNumberOfLikes(likes != null ? likes : 0);

            return post;
        } catch (Exception e) {
            Log.e(TAG, "Error creating post from snapshot", e);
            return null;
        }
    }

    // Add a new post to cache (call after successful creation)
    public void addPostToCache(Post post) {
        if (post != null && isInitialLoadComplete) {
            cachedPosts.add(post); // Add to end (newest)
            Log.d(TAG, "Added new post to cache. Total: " + cachedPosts.size());
        }
    }

    // Remove post from cache (call after successful deletion)
    public void removePostFromCache(String postID) {
        if (postID != null && isInitialLoadComplete) {
            cachedPosts.removeIf(post -> postID.equals(post.getPostID()));
            Log.d(TAG, "Removed post from cache. Total: " + cachedPosts.size());
        }
    }

    // Check if initial posts are loaded
    public boolean isInitialLoadComplete() {
        return isInitialLoadComplete;
    }

    // Get total cached posts count
    public int getCachedPostsCount() {
        return cachedPosts.size();
    }

    // Method to remove the listener when no longer needed
    public void stopListening() {
        if (valueEventListener != null) {
            postsRef.removeEventListener(valueEventListener);
            valueEventListener = null;
        }
    }

    // Callback interfaces
    public interface PostCreatedCallback {
        void onResult(boolean isSaved);
    }

    public interface GetPostCallback {
        void onSuccess(Post post);
        void onFailure(Exception e);
    }

    public interface GetAllPostsCallback {
        void onSuccess(List<Post> posts);
        void onFailure(Exception e);
    }
}