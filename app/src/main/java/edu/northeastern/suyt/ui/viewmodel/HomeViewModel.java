package edu.northeastern.suyt.ui.viewmodel;

import android.app.Application;
import android.os.Parcelable;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.northeastern.suyt.controller.UsersController;
import edu.northeastern.suyt.firebase.repository.database.PostsRepository;
import edu.northeastern.suyt.firebase.repository.database.UsersRepository;
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
    private final PostsRepository postsRepository;

    private boolean quoteLoaded = false;
    private boolean isLoadingPosts = false;

    public HomeViewModel() {
        postsLiveData = new MutableLiveData<>();
        loadingLiveData = new MutableLiveData<>(false);
        errorLiveData = new MutableLiveData<>();

        geminiHelper = new GeminiHelper();
        postsRepository = new PostsRepository();
    }

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

    public void refreshQuote() {
        Log.d(TAG, "Refreshing quote");
        quoteLoaded = false;
        loadQuoteIfNeeded();
    }

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
        errorLiveData.setValue(null);

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

                    Collections.reverse(posts);

                    Log.d(TAG, "Successfully loaded " + posts.size() + " posts");
                    lastFetchTime = System.currentTimeMillis();
                    postsLiveData.setValue(posts);
                } else {
                    Log.d(TAG, "No posts found in database");
                    postsLiveData.setValue(new ArrayList<>());
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

    public void setRecyclerViewState(Parcelable state) {
        this.recyclerViewState = state;
    }

    public Parcelable getRecyclerViewState() {
        return recyclerViewState;
    }

    public void clearError() {
        errorLiveData.setValue(null);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        Log.d(TAG, "HomeViewModel cleared");
    }
}