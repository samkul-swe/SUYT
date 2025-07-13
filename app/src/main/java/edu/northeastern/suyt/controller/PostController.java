package edu.northeastern.suyt.controller;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import edu.northeastern.suyt.firebase.DatabaseConnector;
import edu.northeastern.suyt.model.Post;

public class PostController {
    private final String TAG = "PostController";
    private DatabaseReference postRef;
    private String currentPostID;
    private Post cachedPost;

    public PostController(String postID) {
        this.postRef = DatabaseConnector.getInstance().getPostReference(postID);
        this.currentPostID = postID;
    }

    public void updatePostTitle(String newTitle, UpdatePostTitleCallback callback) {

    }

    public void updatePostDescription(String newDescription, UpdatePostDescriptionCallback callback) {

    }

    public void updatePostCategory(String newCategory, UpdatePostCategoryCallback callback) {

    }

    public void updatePostImage(String newImageUrl, UpdatePostImageCallback callback) {

    }

    public void getPostDetails(PostDetailsCallback callback) {
        postRef.get()
                .addOnSuccessListener(dataSnapshot -> {
                    if (dataSnapshot.exists()) {
                        try {
                            Post post = createPostFromSnapshot(dataSnapshot);
                            if (post != null) {
                                cachedPost = post;
                                Log.d(TAG, "Retrieved post details for: " + currentPostID);
                                callback.onSuccess(post);
                            } else {
                                callback.onFailure(new Exception("Failed to parse post data"));
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing post data", e);
                            callback.onFailure(e);
                        }
                    } else {
                        Log.d(TAG, "Post not found: " + currentPostID);
                        callback.onFailure(new Exception("Post not found"));
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting post details for: " + currentPostID, e);
                    callback.onFailure(e);
                });
    }

    public void getPostCreator(PostCreatorCallback callback) {
        if (cachedPost != null) {
            String creator = cachedPost.getPostedBy();
            if (creator != null && !creator.trim().isEmpty()) {
                Log.d(TAG, "Retrieved creator from cache: " + creator);
                callback.onSuccess(creator);
                return;
            }
        }

        postRef.child("postedBy").get()
                .addOnSuccessListener(dataSnapshot -> {
                    if (dataSnapshot.exists()) {
                        String creator = dataSnapshot.getValue(String.class);
                        if (creator != null && !creator.trim().isEmpty()) {
                            Log.d(TAG, "Retrieved post creator: " + creator);
                            callback.onSuccess(creator);
                        } else {
                            callback.onFailure(new Exception("Creator information not available"));
                        }
                    } else {
                        Log.d(TAG, "Creator not found for post: " + currentPostID);
                        callback.onFailure(new Exception("Creator not found"));
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting post creator for: " + currentPostID, e);
                    callback.onFailure(e);
                });
    }

    public void updateLikes(int newLikeCount, UpdateLikesCallback callback) {
        if (newLikeCount < 0) {
            callback.onFailure();
            return;
        }

        postRef.child("numberOfLikes").setValue(newLikeCount)
            .addOnSuccessListener(aVoid -> {
                if (cachedPost != null) {
                    cachedPost.setNumberOfLikes(newLikeCount);
                }
                Log.d(TAG, "Updated likes for post " + currentPostID + " to: " + newLikeCount);
                callback.onSuccess();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error updating likes for post: " + currentPostID, e);
                callback.onFailure();
            });
    }

    public void getCurrentLikeCount(LikeCountCallback callback) {
        if (cachedPost != null) {
            callback.onSuccess(cachedPost.getNumberOfLikes());
            return;
        }

        postRef.child("numberOfLikes").get()
                .addOnSuccessListener(dataSnapshot -> {
                    if (dataSnapshot.exists()) {
                        Integer likes = dataSnapshot.getValue(Integer.class);
                        int likeCount = (likes != null) ? likes : 0;
                        callback.onSuccess(likeCount);
                    } else {
                        callback.onSuccess(0);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting like count for: " + currentPostID, e);
                    callback.onFailure(e);
                });
    }

    public String getCurrentPostID() {
        return currentPostID;
    }

    public DatabaseReference getPostRef() {
        return postRef;
    }

    public void setPostRef(DatabaseReference newPostRef) {
        this.postRef = newPostRef;
        this.currentPostID = newPostRef.getKey();
        this.cachedPost = null;
    }

    public Post getCachedPost() {
        return cachedPost;
    }

    public void clearCache() {
        this.cachedPost = null;
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
            Integer likes = postSnapshot.child("likes").getValue(Integer.class);
            post.setNumberOfLikes(likes != null ? likes : 0);

            return post;
        } catch (Exception e) {
            Log.e(TAG, "Error creating post from snapshot", e);
            return null;
        }
    }

    // Callback interfaces
    public interface UpdatePostTitleCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

    public interface UpdatePostDescriptionCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

    public interface UpdatePostCategoryCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

    public interface UpdatePostImageCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

    public interface PostDetailsCallback {
        void onSuccess(Post post);
        void onFailure(Exception e);
    }

    public interface PostCreatorCallback {
        void onSuccess(String creatorID);
        void onFailure(Exception e);
    }

    public interface UpdateLikesCallback {
        void onSuccess();
        void onFailure();
    }

    public interface LikeCountCallback {
        void onSuccess(int likeCount);
        void onFailure(Exception e);
    }

    public interface SavePostCallback {
        void onSuccess();
        void onFailure(Exception e);
    }
}