package edu.northeastern.suyt.ui.viewmodel;

import android.os.Parcelable;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.northeastern.suyt.firebase.repository.database.PostsRepository;
import edu.northeastern.suyt.model.Post;
import edu.northeastern.suyt.utils.GeminiHelper;

public class HomeViewModel extends ViewModel {

    private static final String TAG = "HomeViewModel";
    private static final long CACHE_VALIDITY_PERIOD = 5 * 60 * 1000L; // 5 minutes

    // Posts related LiveData
    private MutableLiveData<List<Post>> postsLiveData;
    private MutableLiveData<Boolean> loadingLiveData;
    private MutableLiveData<String> errorLiveData;

    // Quote related LiveData
    private MutableLiveData<String> quote = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoadingQuote = new MutableLiveData<>(false);

    // Cache management
    private long lastFetchTime = 0;
    private Parcelable recyclerViewState;

    // Dependencies
    private final GeminiHelper geminiHelper;
    private final PostsRepository postsRepository;

    // State management
    private boolean quoteLoaded = false;
    private boolean isLoadingPosts = false;

    public HomeViewModel() {
        // Initialize LiveData
        postsLiveData = new MutableLiveData<>();
        loadingLiveData = new MutableLiveData<>(false);
        errorLiveData = new MutableLiveData<>();

        // Initialize dependencies
        geminiHelper = new GeminiHelper();
        postsRepository = new PostsRepository();
    }

    // Getters for LiveData
    public LiveData<List<Post>> getPosts() {
        return postsLiveData;
    }

    public LiveData<Boolean> getLoading() {
        return loadingLiveData;
    }

    public LiveData<String> getError() {
        return errorLiveData;
    }

    public LiveData<String> getQuote() {
        return quote;
    }

    public LiveData<Boolean> getIsLoadingQuote() {
        return isLoadingQuote;
    }

    // Quote management methods
    public void loadQuoteIfNeeded() {
        if (quoteLoaded || Boolean.TRUE.equals(isLoadingQuote.getValue())) {
            Log.d(TAG, "Quote already loaded or loading, skipping");
            return;
        }

        Log.d(TAG, "Loading quote from Gemini API");
        isLoadingQuote.postValue(true);

        geminiHelper.generateNewQuote(new GeminiHelper.QuoteCallback() {
            @Override
            public void onSuccess(String generatedQuote) {
                Log.d(TAG, "Quote loaded successfully: " + generatedQuote);
                quote.postValue(generatedQuote); // Use setValue - callback is on main thread now
                isLoadingQuote.postValue(false);
                quoteLoaded = true;
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.e(TAG, "Failed to load quote: " + errorMessage);
                // Keep the default quote on failure
                isLoadingQuote.postValue(false);
                quoteLoaded = true; // Don't retry automatically
            }
        });
    }

    public void refreshQuote() {
        Log.d(TAG, "Refreshing quote");
        quoteLoaded = false;
        loadQuoteIfNeeded();
    }

    // Posts management methods
    public void loadPostsIfNeeded() {
        List<Post> currentPosts = postsLiveData.getValue();
        if (currentPosts == null || currentPosts.isEmpty() || shouldRefresh()) {
            loadPosts();
        } else {
            Log.d(TAG, "Posts already loaded and cache is valid");
        }
    }

    public void refreshPosts() {
        Log.d(TAG, "Refreshing posts");
        loadPosts();
    }

    private boolean shouldRefresh() {
        return (System.currentTimeMillis() - lastFetchTime) > CACHE_VALIDITY_PERIOD;
    }

    private void loadPosts() {
        if (isLoadingPosts) {
            Log.d(TAG, "Posts already loading, skipping");
            return;
        }

        Log.d(TAG, "Loading posts from Firebase");
        isLoadingPosts = true;
        loadingLiveData.setValue(true);
        errorLiveData.setValue(null); // Clear previous errors

        Task<DataSnapshot> task = postsRepository.getPostsRef().get();

        task.addOnSuccessListener(dataSnapshot -> {
            try {
                if (dataSnapshot.exists()) {
                    List<Post> posts = new ArrayList<>();

                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        Post post = createPostFromSnapshot(postSnapshot);
                        if (post != null) {
                            posts.add(post);
                        }
                    }

                    // Sort posts by date (newest first) - optional
                    Collections.reverse(posts);

                    Log.d(TAG, "Successfully loaded " + posts.size() + " posts");
                    lastFetchTime = System.currentTimeMillis();
                    postsLiveData.setValue(posts);
                } else {
                    Log.d(TAG, "No posts found in database");
                    postsLiveData.setValue(new ArrayList<>()); // Empty list
                }
            } catch (Exception e) {
                Log.e(TAG, "Error processing posts data", e);
                errorLiveData.setValue("Error processing posts: " + e.getMessage());
            } finally {
                isLoadingPosts = false;
                loadingLiveData.setValue(false);
            }
        }).addOnFailureListener(exception -> {
            Log.e(TAG, "Failed to load posts", exception);
            errorLiveData.setValue("Failed to load posts: " + exception.getMessage());
            isLoadingPosts = false;
            loadingLiveData.setValue(false);
        });
    }

    private Post createPostFromSnapshot(DataSnapshot postSnapshot) {
        try {
            Post post = new Post();
            post.setPostID(postSnapshot.getKey());
            post.setPostedBy(postSnapshot.child("userId").getValue(String.class));
            post.setPostTitle(postSnapshot.child("title").getValue(String.class));
            post.setPostDescription(postSnapshot.child("description").getValue(String.class));
            post.setPostImage(postSnapshot.child("imageUrl").getValue(String.class));
            post.setPostCategory(postSnapshot.child("category").getValue(String.class));

            Integer likes = postSnapshot.child("likes").getValue(Integer.class);
            post.setNumberOfLikes(likes != null ? likes : 0);

            post.setPostedOn(postSnapshot.child("date").getValue(String.class));

            return post;
        } catch (Exception e) {
            Log.e(TAG, "Error creating post from snapshot: " + postSnapshot.getKey(), e);
            return null;
        }
    }

    // RecyclerView state management
    public void setRecyclerViewState(Parcelable state) {
        this.recyclerViewState = state;
    }

    public Parcelable getRecyclerViewState() {
        return recyclerViewState;
    }

    // Clear error
    public void clearError() {
        errorLiveData.setValue(null);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        Log.d(TAG, "HomeViewModel cleared");
        // Clean up any resources if needed
    }
}