package edu.northeastern.suyt.ui.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.List;

import edu.northeastern.suyt.R;
import edu.northeastern.suyt.controller.PostsController;
import edu.northeastern.suyt.controller.UserController;
import edu.northeastern.suyt.model.Post;
import edu.northeastern.suyt.model.User;
import edu.northeastern.suyt.ui.adapters.PostAdapter;
import edu.northeastern.suyt.utils.UtilityClass;

public class SavedPostsActivity extends AppCompatActivity implements PostAdapter.OnPostClickListener {

    private static final String TAG = "SavedPostsActivity";

    private RecyclerView recyclerView;
    private PostAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView emptyStateTextView;
    private UtilityClass utility;

    // Controllers
    private PostsController postsController;
    private UserController userController;

    // Data
    private List<Post> savedPostsList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_posts);

        // Initialize controllers
        postsController = new PostsController();
        userController = new UserController(getApplicationContext());
        savedPostsList = new ArrayList<>();

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Saved Posts");
        }

        // Initialize views
        initializeViews();

        // Setup RecyclerView
        setupRecyclerView();

        // Setup SwipeRefresh
        setupSwipeRefresh();

        // Load saved posts
        loadSavedPosts();
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.recycler_view);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        emptyStateTextView = findViewById(R.id.empty_state_text_view);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PostAdapter(this, true); // true indicates this is for saved posts
        recyclerView.setAdapter(adapter);
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setColorSchemeResources(
                R.color.colorPrimary,
                R.color.colorAccent,
                R.color.colorPrimaryDark
        );
        swipeRefreshLayout.setOnRefreshListener(this::loadSavedPosts);
    }

    private void loadSavedPosts() {
        Log.d(TAG, "Loading saved posts...");

        // Show loading if not already refreshing
        if (!swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(true);
        }

        User currentUser = utility.getUser(this);
        if (currentUser.getSavedPosts() == null) {
            updateUI();
            stopRefreshing();
            return;
        }
        // Get user's saved posts
        postsController.getUserSavedPosts(currentUser.getSavedPosts(), new PostsController.GetAllPostsCallback() {
            @Override
            public void onSuccess(List<Post> savedPosts) {
                Log.d(TAG, "Successfully loaded " + savedPosts.size() + " saved posts");
                savedPostsList.clear();
                savedPostsList.addAll(savedPosts);

                runOnUiThread(() -> {
                    updateUI();
                    stopRefreshing();
                });
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Failed to load saved posts: " + e);
                runOnUiThread(() -> {
                    handleLoadingError(String.valueOf(e));
                    stopRefreshing();
                });
            }
        });
    }

    private void updateUI() {
        if (savedPostsList.isEmpty()) {
            showEmptyState();
        } else {
            showSavedPosts();
        }
    }

    private void showEmptyState() {
        recyclerView.setVisibility(View.GONE);
        emptyStateTextView.setVisibility(View.VISIBLE);
        emptyStateTextView.setText("No saved posts yet.\nStart exploring and save posts you like!");
    }

    @SuppressLint("NotifyDataSetChanged")
    private void showSavedPosts() {
        recyclerView.setVisibility(View.VISIBLE);
        emptyStateTextView.setVisibility(View.GONE);

        // Update adapter with new data
        adapter.notifyDataSetChanged();
    }

    private void handleLoadingError(String errorMessage) {
        // Show error state
        recyclerView.setVisibility(View.GONE);
        emptyStateTextView.setVisibility(View.VISIBLE);
        emptyStateTextView.setText("Failed to load saved posts.\nPull down to refresh.");

        // Show toast with specific error
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
    }

    private void stopRefreshing() {
        if (swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void onPostClick(Post post) {
        if (post != null) {
            Intent intent = new Intent(this, PostDetailActivity.class);
            intent.putExtra("POST_ID", post.getPostID());
            intent.putExtra("POST", post); // Pass the entire post object as backup
            startActivity(intent);
        }
    }

    @Override
    public void onPostSave(Post post) {
        // Handle save/unsave action - for now do nothing
    }

    @Override
    public void onPostShare(Post post) {
        // Handle post sharing
        sharePost(post);
    }

    private void savePost(Post post) {
        userController.savePost(post.getPostID(), new UserController.UpdateCallback() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    savedPostsList.add(post);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(SavedPostsActivity.this, "Post saved", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                runOnUiThread(() -> {
                    Toast.makeText(SavedPostsActivity.this, "Failed to save post: " + errorMessage, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void unsavePost(Post post) {
        userController.unsavePost(post.getPostID(), new UserController.UpdateCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    // Remove from list and update UI
                    savedPostsList.remove(post);
                    updateUI();
                    Toast.makeText(SavedPostsActivity.this, "Post removed from saved", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                runOnUiThread(() -> {
                    Toast.makeText(SavedPostsActivity.this, "Failed to remove post: " + errorMessage, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void sharePost(Post post) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, post.getPostTitle());
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                post.getPostTitle() + "\n\n" + "\n\nShared from EcoApp");

        try {
            startActivity(Intent.createChooser(shareIntent, "Share Post"));
        } catch (Exception e) {
            Toast.makeText(this, "Unable to share post", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the list when returning to this activity
        loadSavedPosts();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up
        if (savedPostsList != null) {
            savedPostsList.clear();
        }
    }
}