package edu.northeastern.suyt.ui.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import edu.northeastern.suyt.R;
import edu.northeastern.suyt.controller.RecyclingPostController;
import edu.northeastern.suyt.model.RecyclingPost;

public class PostDetailActivity extends AppCompatActivity {

    private ImageView postImageView;
    private TextView titleTextView;
    private TextView usernameTextView;
    private TextView dateTextView;
    private TextView descriptionTextView;
    private TextView likesTextView;
    private TextView categoryTextView;
    private View categoryIndicator;

    private RecyclingPostController postController;
    private RecyclingPost post;
    private Toolbar toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        // Set up toolbar with back button
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Community Post");
        }

        // Initialize controller
        postController = new RecyclingPostController();

        // Initialize views
        postImageView = findViewById(R.id.post_image_view);
        titleTextView = findViewById(R.id.title_text_view);
        usernameTextView = findViewById(R.id.username_text_view);
        dateTextView = findViewById(R.id.date_text_view);
        descriptionTextView = findViewById(R.id.description_text_view);
        likesTextView = findViewById(R.id.likes_count_text_view);
        categoryTextView = findViewById(R.id.category_text_view);
        Button likeButton = findViewById(R.id.like_button);
        Button shareButton = findViewById(R.id.share_button);
        categoryIndicator = findViewById(R.id.category_indicator);

        // Get post ID from intent
        int postId = getIntent().getIntExtra("POST_ID", -1);
        if (postId != -1) {
            loadPost(postId);
        } else {
            // Handle error - no post ID
            Toast.makeText(this, "Error loading post", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Set up button listeners
        likeButton.setOnClickListener(v -> {
            if (post != null) {
                // Increment likes and update UI
                post.setLikes(post.getLikes() + 1);
                likesTextView.setText(String.valueOf(post.getLikes()));
                Toast.makeText(this, "You liked this post!", Toast.LENGTH_SHORT).show();
            }
        });

        shareButton.setOnClickListener(v -> {
            if (post != null) {
                // Create share intent
                String shareText = "Check out this recycling project: " + post.getTitle() +
                        " by " + post.getUsername() + " on SUYT app!";

                android.content.Intent shareIntent = new android.content.Intent();
                shareIntent.setAction(android.content.Intent.ACTION_SEND);
                shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareText);
                shareIntent.setType("text/plain");

                startActivity(android.content.Intent.createChooser(shareIntent, "Share via"));
            }
        });
    }

    private void loadPost(int postId) {
        // Get post by ID
        post = postController.getPostById(postId);

        if (post != null) {
            // Set post data to views
            titleTextView.setText(post.getTitle());
            usernameTextView.setText(post.getUsername());
            dateTextView.setText(post.getDate());
            descriptionTextView.setText(post.getDescription());
            likesTextView.setText(String.valueOf(post.getLikes()));
            categoryTextView.setText(post.getCategory());

            // Set category indicator color
            if (post.getCategory().equals("Reuse")) {
                categoryIndicator.setBackgroundResource(R.color.colorReuse);
            } else if (post.getCategory().equals("Recycle")) {
                categoryIndicator.setBackgroundResource(R.color.colorRecycle);
            } else {
                categoryIndicator.setBackgroundResource(R.color.colorReduce);
            }

            // Set image based on category (placeholder)
            if (post.getCategory().equals("Reuse")) {
                postImageView.setImageResource(R.drawable.placeholder_reuse);
            } else if (post.getCategory().equals("Recycle")) {
                postImageView.setImageResource(R.drawable.placeholder_recycle);
            } else {
                postImageView.setImageResource(R.drawable.placeholder_reduce);
            }
        } else {
            // Handle error - post not found
            Toast.makeText(this, "Post not found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Handle back button click
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}