package edu.northeastern.suyt.controller;

import java.util.ArrayList;
import java.util.List;

import edu.northeastern.suyt.model.RecyclingPost;

public class SavedPostController {

    private RecyclingPostController postController;

    public SavedPostController() {
        postController = new RecyclingPostController();
    }

    /**
     * Get all saved posts for the current user
     * In a real app, this would fetch from a database based on user ID
     * For now, we'll return a subset of the sample posts
     */
    public List<RecyclingPost> getSavedPosts() {
        // In a real app, query the database for posts saved by the current user
        // For demo purposes, we'll just return a few of the sample posts
        List<RecyclingPost> allPosts = postController.getAllPosts();
        List<RecyclingPost> savedPosts = new ArrayList<>();

        // Simulate that the user has saved some posts (e.g., posts with ID 1, 3, and 5)
        for (RecyclingPost post : allPosts) {
            if (post.getId() == 1 || post.getId() == 3 || post.getId() == 5) {
                savedPosts.add(post);
            }
        }

        return savedPosts;
    }

    /**
     * Save a post for the current user
     */
    public boolean savePost(int postId) {
        // In a real app, save the post to the user's saved posts in the database
        // For now, just return success
        return true;
    }

    /**
     * Remove a post from the current user's saved posts
     */
    public boolean unsavePost(int postId) {
        // In a real app, remove the post from the user's saved posts in the database
        // For now, just return success
        return true;
    }

    /**
     * Check if a post is saved by the current user
     */
    public boolean isPostSaved(int postId) {
        // In a real app, check if the post is in the user's saved posts
        // For demo purposes, we'll just check if it's one of our "saved" posts
        return postId == 1 || postId == 3 || postId == 5;
    }
}
