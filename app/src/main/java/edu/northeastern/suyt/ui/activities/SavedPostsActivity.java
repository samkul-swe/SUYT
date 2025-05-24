//package edu.northeastern.suyt.ui.activities;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.view.MenuItem;
//import android.view.View;
//import android.widget.TextView;
//
//import androidx.annotation.Nullable;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.appcompat.widget.Toolbar;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
//
//import java.util.List;
//
//import edu.northeastern.suyt.R;
//import edu.northeastern.suyt.model.Post;
//import edu.northeastern.suyt.ui.adapters.PostAdapter;
//
//public class SavedPostsActivity extends AppCompatActivity implements PostAdapter.OnPostClickListener {
//
//    private RecyclerView recyclerView;
//    private PostAdapter adapter;
//    private SwipeRefreshLayout swipeRefreshLayout;
//    private TextView emptyStateTextView;
//
//    @Override
//    protected void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_saved_posts);
//
//        // Set up toolbar with back button
//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//        if (getSupportActionBar() != null) {
//            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//            getSupportActionBar().setTitle("Saved Posts");
//        }
//
//        // Initialize views
//        recyclerView = findViewById(R.id.recycler_view);
//        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
//        emptyStateTextView = findViewById(R.id.empty_state_text_view);
//
//        // Set up RecyclerView
//        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//
//        // Set up SwipeRefreshLayout
//        swipeRefreshLayout.setColorSchemeResources(
//                R.color.colorPrimary,
//                R.color.colorAccent,
//                R.color.colorPrimaryDark
//        );
//        swipeRefreshLayout.setOnRefreshListener(this::loadSavedPosts);
//
//        // Load saved posts
//        loadSavedPosts();
//    }
//
//    private void loadSavedPosts() {
//        // Get saved posts from database
//        List<Post> savedPosts = null;
//
//        // Check if there are saved posts
//        if (savedPosts.isEmpty()) {
//            // Show empty state
//            recyclerView.setVisibility(View.GONE);
//            emptyStateTextView.setVisibility(View.VISIBLE);
//        } else {
//            // Show posts
//            recyclerView.setVisibility(View.VISIBLE);
//            emptyStateTextView.setVisibility(View.GONE);
//
//            // Set up adapter
//            adapter = new PostAdapter(savedPosts, this);
//            recyclerView.setAdapter(adapter);
//        }
//
//        // Stop refresh animation if it's running
//        if (swipeRefreshLayout.isRefreshing()) {
//            swipeRefreshLayout.setRefreshing(false);
//        }
//    }
//
//    @Override
//    public void onPostClick(Post post) {
//        // Open post detail activity when a post is clicked
//        Intent intent = new Intent(this, PostDetailActivity.class);
//        intent.putExtra("POST_ID", post.getId());
//        startActivity(intent);
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        if (item.getItemId() == android.R.id.home) {
//            // Handle back button click
//            onBackPressed();
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        // Refresh saved posts when returning to this activity
//        loadSavedPosts();
//    }
//}