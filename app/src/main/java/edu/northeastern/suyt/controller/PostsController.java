package edu.northeastern.suyt.controller;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.List;

import edu.northeastern.suyt.firebase.repository.database.PostsRepository;
import edu.northeastern.suyt.firebase.repository.database.UsersRepository;
import edu.northeastern.suyt.model.Post;

public class PostsController {
    private final String TAG = "PostsController";
    PostsRepository postsRepository = new PostsRepository();
    private final DatabaseReference postsRef = postsRepository.getPostsRef();
    private com.google.firebase.database.ValueEventListener valueEventListener;

    private static final List<Post> cachedPosts = new ArrayList<>();
    private static boolean isInitialLoadComplete;

    public PostsController() {

    }

    public void createPost(Post post, PostCreatedCallback callback) {
        postsRef.child(post.getPostID()).setValue(post)
            .addOnSuccessListener(aVoid -> {
                addPostToCache(post);
                callback.onResult(true);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error creating post", e);
                callback.onResult(false);
            });
    }

    public void loadInitialPosts(PostsLoadedCallback callback) {
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
                callback.onSuccess();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error loading initial posts", e);
                callback.onFailure(e);
            });
    }

    public void getAllPosts(GetAllPostsCallback callback) {
        if (!isInitialLoadComplete) {
            callback.onFailure(new IllegalStateException("Posts not loaded yet. Call loadInitialPosts() first."));
            return;
        }

        callback.onSuccess(new ArrayList<>(cachedPosts));
    }

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

    public void getUserSavedPosts(List<String> savedPostIDs, GetAllPostsCallback callback) {
        if (savedPostIDs == null || savedPostIDs.isEmpty()) {
            callback.onSuccess(new ArrayList<>());
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

    public void loadMorePosts(int limit, GetAllPostsCallback callback) {
        if (cachedPosts.isEmpty()) {
            callback.onFailure(new IllegalStateException("No posts loaded yet. Call loadInitialPosts() first."));
            return;
        }

        String lastPostKey = cachedPosts.get(0).getPostID();

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

                    cachedPosts.addAll(0, newPosts);
                    Log.d(TAG, "Loaded " + newPosts.size() + " more posts. Total cached: " + cachedPosts.size());

                    callback.onSuccess(newPosts);
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

            String userID = postSnapshot.child("postedBy").getValue(String.class);
            getUserName(userID, new UsersController.GetUserNameCallback() {
                @Override
                public void onSuccess(String username) {
                    post.setPostedBy(username);
                }
                @Override
                public void onFailure(String errorMessage) {
                    Log.e(TAG, "Error getting username: " + errorMessage);
                }
            });
            post.setPostTitle(postSnapshot.child("postTitle").getValue(String.class));
            post.setPostDescription(postSnapshot.child("postDescription").getValue(String.class));
            post.setPostImage(postSnapshot.child("postImage").getValue(String.class));
            post.setPostCategory(postSnapshot.child("postCategory").getValue(String.class));

            Integer likes = postSnapshot.child("numberOfLikes").getValue(Integer.class);
            post.setNumberOfLikes(likes != null ? likes : 0);

            post.setPostedOn(postSnapshot.child("postedOn").getValue(String.class));

            return post;
        } catch (Exception e) {
            Log.e(TAG, "Error creating post from snapshot: " + postSnapshot.getKey(), e);
            return null;
        }
    }

    public void getUserName(String userId, UsersController.GetUserNameCallback callback) {
        UsersRepository userRepository = new UsersRepository(userId);
        DatabaseReference userRef = userRepository.getUserRef();
        try{
            userRef.get().addOnSuccessListener(dataSnapshot -> {
                if (dataSnapshot.exists()) {
                    String username = dataSnapshot.child("username").getValue(String.class);
                    Log.d(TAG, "Username retrieved: " + username);
                    callback.onSuccess(username);
                } else {
                    callback.onFailure("User data not found");
                }
            }).addOnFailureListener(exception -> Log.e(TAG, "Error getting user data", exception));
        } catch (Exception e) {
            Log.e(TAG, "Error getting username", e);
            callback.onFailure("Error getting username: " + e.getMessage());
        }
    }

    public void addPostToCache(Post post) {
        if (post != null && isInitialLoadComplete) {
            cachedPosts.add(post); // Add to end (newest)
            Log.d(TAG, "Added new post to cache. Total: " + cachedPosts.size());
        }
    }

    public void removePostFromCache(String postID) {
        if (postID != null && isInitialLoadComplete) {
            cachedPosts.removeIf(post -> postID.equals(post.getPostID()));
            Log.d(TAG, "Removed post from cache. Total: " + cachedPosts.size());
        }
    }

    public boolean isInitialLoadComplete() {
        return isInitialLoadComplete;
    }

    public int getCachedPostsCount() {
        return cachedPosts.size();
    }

    public void stopListening() {
        if (valueEventListener != null) {
            postsRef.removeEventListener(valueEventListener);
            valueEventListener = null;
        }
    }

    // Callback interfaces
    public interface PostsLoadedCallback {
        void onSuccess();
        void onFailure(Exception e);
    }


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