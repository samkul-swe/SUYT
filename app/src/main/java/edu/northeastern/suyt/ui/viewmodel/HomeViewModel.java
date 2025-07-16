package edu.northeastern.suyt.ui.viewmodel;

import android.os.Parcelable;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.northeastern.suyt.controller.PostsController;
import edu.northeastern.suyt.model.Post;
import edu.northeastern.suyt.utils.GeminiHelper;

public class HomeViewModel extends ViewModel {

    private static final String TAG = "HomeViewModel";
    private static final long CACHE_VALIDITY_PERIOD = 5 * 60 * 1000L; // 5 minutes

    private final MutableLiveData<List<Post>> postsLiveData;
    private final MutableLiveData<Boolean> loadingLiveData;
    private final MutableLiveData<String> errorLiveData;

    private final MutableLiveData<String> quote = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoadingQuote = new MutableLiveData<>(false);

    private long lastFetchTime = 0;
    private Parcelable recyclerViewState;

    private final GeminiHelper geminiHelper;

    private boolean quoteLoaded = false;
    private boolean isLoadingPosts = false;

    public HomeViewModel() {
        postsLiveData = new MutableLiveData<>();
        loadingLiveData = new MutableLiveData<>(false);
        errorLiveData = new MutableLiveData<>();

        geminiHelper = new GeminiHelper();
    }

    public LiveData<List<Post>> getPosts() {
        return postsLiveData;
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
                quote.postValue(generatedQuote);
                isLoadingQuote.postValue(false);
                quoteLoaded = true;
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.e(TAG, "Failed to load quote: " + errorMessage);
                isLoadingQuote.postValue(false);
                quoteLoaded = true;
            }
        });
    }

    public void loadPostsIfNeeded() {
        List<Post> currentPosts = postsLiveData.getValue();
        if (currentPosts == null || currentPosts.isEmpty() || shouldRefresh()) {
            loadPosts();
        } else {
            Log.d(TAG, "Posts already loaded and cache is valid");
        }
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
        errorLiveData.setValue(null);

        PostsController postsController = new PostsController();
        if (postsController.isInitialLoadComplete()) {
            Log.d(TAG, "Posts already loaded, using cached data");
            postsController.getAllPosts(new PostsController.GetAllPostsCallback() {
                @Override
                public void onSuccess(List<Post> posts) {
                    Collections.reverse(posts);
                    Log.d(TAG, "Successfully loaded " + posts.size() + " posts");
                    postsLiveData.setValue(posts);
                }
                @Override
                public void onFailure(Exception e) {
                    Log.e(TAG, "Error getting posts from database", e);
                    errorLiveData.setValue("Error getting posts from database: " + e.getMessage());
                }
            });
        } else {
            Log.d(TAG, "Posts not loaded, loading");
            postsController.loadInitialPosts(new PostsController.PostsLoadedCallback() {
                @Override
                public void onSuccess() {
                    postsController.getAllPosts(new PostsController.GetAllPostsCallback() {
                        @Override
                        public void onSuccess(List<Post> posts) {
                            Collections.reverse(posts);

                            Log.d(TAG, "Successfully loaded " + posts.size() + " posts");
                            postsLiveData.setValue(posts);
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Log.d(TAG, "No posts found in database");
                            postsLiveData.setValue(new ArrayList<>());
                        }
                    });
                }

                @Override
                public void onFailure(Exception e) {
                    Log.e(TAG, "Error processing posts data", e);
                    errorLiveData.setValue("Error processing posts: " + e.getMessage());
                    isLoadingPosts = false;
                    loadingLiveData.setValue(false);
                }
            });
        }
    }

    public void setRecyclerViewState(Parcelable state) {
        this.recyclerViewState = state;
    }

    public Parcelable getRecyclerViewState() {
        return recyclerViewState;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        Log.d(TAG, "HomeViewModel cleared");
    }
}