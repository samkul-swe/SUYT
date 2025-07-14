package edu.northeastern.suyt.ui.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

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
import edu.northeastern.suyt.ui.adapters.PostAdapter;
import edu.northeastern.suyt.utils.SessionManager;

public class SavedPostsActivity extends AppCompatActivity implements PostAdapter.OnPostClickListener {

    private static final String TAG = "SavedPostsActivity";

    private RecyclerView recyclerView;
    private PostAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView emptyStateTextView;

    private SessionManager sessionManager;
    private UserController userController;
    private PostsController postsController;

    private List<Post> savedPostsList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_posts);

        sessionManager = new SessionManager(this);
        userController = new UserController(sessionManager.getUserId());
        postsController = new PostsController();

        savedPostsList = new ArrayList<Post>();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Saved Posts");
        }

        initializeViews();
        setupRecyclerView();
        setupSwipeRefresh();
        loadSavedPosts();
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.recycler_view);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        emptyStateTextView = findViewById(R.id.empty_state_text_view);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PostAdapter();
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

        if (!swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(true);
        }

        ArrayList<String> savedPosts = sessionManager.getSavedPosts();
        Log.d(TAG, "Saved posts: " + savedPosts);

        if (savedPosts != null) {
            postsController.getUserSavedPosts(savedPosts, new PostsController.GetAllPostsCallback() {
                @Override
                public void onSuccess(List<Post> posts) {
                    savedPostsList = posts;
                    Log.d(TAG, "Saved posts list: " + savedPostsList);
                    updateUI();
                }

                @Override
                public void onFailure(Exception e) {
                    Log.e(TAG, "Error loading saved posts", e);
                }
            });
        }
    }

    private void updateUI() {
        if (savedPostsList.isEmpty()) {
            showEmptyState();
            stopRefreshing();
        } else {
            showSavedPosts();
            stopRefreshing();
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
        adapter.setOnPostClickListener(this);
        recyclerView.setAdapter(adapter);
        adapter.updateData(savedPostsList);
        adapter.notifyDataSetChanged();
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
            intent.putExtra("POST", post);
            startActivity(intent);
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
        loadSavedPosts();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (savedPostsList != null) {
            savedPostsList.clear();
        }
    }
}