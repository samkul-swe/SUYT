package edu.northeastern.suyt.ui.viewmodel;

import android.os.Parcelable;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.List;

import edu.northeastern.suyt.firebase.repository.database.PostsRepository;
import edu.northeastern.suyt.model.Post;

public class HomeViewModel extends ViewModel {

    private MutableLiveData<List<Post>> postsLiveData;
    private MutableLiveData<Boolean> loadingLiveData;
    private MutableLiveData<String> errorLiveData;

    private long lastFetchTime = 0;
    private static final long CACHE_VALIDITY_PERIOD = 5 * 60 * 1000L; // 5 minutes

    private Parcelable recyclerViewState;

    public HomeViewModel() {
        postsLiveData = new MutableLiveData<>();
        loadingLiveData = new MutableLiveData<>();
        errorLiveData = new MutableLiveData<>();
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

    public void loadPostsIfNeeded() {
        List<Post> currentPosts = postsLiveData.getValue();
        if (currentPosts == null || currentPosts.isEmpty() || shouldRefresh()) {
            loadPosts();
        }
    }

    public void refreshPosts() {
        loadPosts();
    }

    private boolean shouldRefresh() {
        return (System.currentTimeMillis() - lastFetchTime) > CACHE_VALIDITY_PERIOD;
    }

    private void loadPosts() {
        loadingLiveData.setValue(true);

        PostsRepository postsRepository = new PostsRepository();
        Task<DataSnapshot> task = postsRepository.getPostsRef().get();

        task.addOnSuccessListener(dataSnapshot -> {
            if (dataSnapshot.exists()) {
                List<Post> posts = new ArrayList<>();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Post post = new Post();
                    post.setId(postSnapshot.getKey());
                    post.setUsername(postSnapshot.child("username").getValue(String.class));
                    post.setTitle(postSnapshot.child("title").getValue(String.class));
                    post.setDescription(postSnapshot.child("description").getValue(String.class));
                    post.setImageUrl(postSnapshot.child("imageUrl").getValue(String.class));
                    post.setCategory(postSnapshot.child("category").getValue(String.class));

                    Integer likes = postSnapshot.child("likes").getValue(Integer.class);
                    post.setLikes(likes != null ? likes : 0);

                    post.setDate(postSnapshot.child("date").getValue(String.class));
                    posts.add(post);
                }

                lastFetchTime = System.currentTimeMillis();
                postsLiveData.setValue(posts);
                loadingLiveData.setValue(false);
            }
        }).addOnFailureListener(exception -> {
            errorLiveData.setValue(exception.getMessage());
            loadingLiveData.setValue(false);
        });
    }

    public void setRecyclerViewState(Parcelable state) {
        this.recyclerViewState = state;
    }

    public Parcelable getRecyclerViewState() {
        return recyclerViewState;
    }
}