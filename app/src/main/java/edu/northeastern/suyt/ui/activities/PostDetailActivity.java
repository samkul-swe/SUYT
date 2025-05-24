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

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import edu.northeastern.suyt.R;
import edu.northeastern.suyt.model.Post;

public class PostDetailActivity extends AppCompatActivity {

    private ImageView postImageView;
    private TextView titleTextView;
    private TextView usernameTextView;
    private TextView dateTextView;
    private TextView descriptionTextView;
    private TextView likesTextView;
    private TextView categoryTextView;
    private View categoryIndicator;

    private Post post;

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

        // Get post object from intent (using Parcelable)
        if (getIntent() != null && getIntent().hasExtra("POST")) {
            post = getIntent().getParcelableExtra("POST");

            if (post != null) {
                // Load the post data into the UI
                loadPostData();
            } else {
                // Handle error - post is null
                Toast.makeText(this, "Error loading post data", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            // Handle error - no post data in intent
            Toast.makeText(this, "No post data found", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Set up button listeners
        likeButton.setOnClickListener(v -> {
            if (post != null) {
                // Increment likes and update UI
                post.setLikes(post.getLikes() + 1);
                likesTextView.setText(String.valueOf(post.getLikes()));
                Toast.makeText(this, "You liked this post!", Toast.LENGTH_SHORT).show();

                // TODO: Update likes in Firebase/database here if needed
                // updateLikesInDatabase(post.getId(), post.getLikes());
            }
        });

        shareButton.setOnClickListener(v -> {
            if (post != null) {
                sharePost();
            }
        });
    }

    /**
     * Load post data into the UI components
     */
    private void loadPostData() {
        if (post == null) return;

        // Set basic post information
        titleTextView.setText(post.getTitle());
        usernameTextView.setText(post.getUsername());
        dateTextView.setText(post.getDate());
        descriptionTextView.setText(post.getDescription());
        likesTextView.setText(String.valueOf(post.getLikes()));
        categoryTextView.setText(post.getCategory());

        // Set toolbar title to post title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(post.getTitle());
        }

        // Set category indicator color based on category
        setCategoryIndicatorColor(post.getCategory());

        // Load image - replace with your image loading logic
        loadPostImage(post.getImageUrl());
    }

    /**
     * Set the category indicator color based on the post category
     */
    private void setCategoryIndicatorColor(String category) {
        if (categoryIndicator == null || category == null) return;

        int colorResource;
        switch (category.toLowerCase()) {
            case "reuse":
                colorResource = R.color.colorReuse;
                break;
            case "recycle":
                colorResource = R.color.colorRecycle;
                break;
            case "reduce":
                colorResource = R.color.colorReduce;
                break;
            default:
                colorResource = R.color.colorPrimary;
                break;
        }

        categoryIndicator.setBackgroundResource(colorResource);
    }

    /**
     * Load the post image - implement your image loading logic here
     */
    private void loadPostImage(String imageUrl) {
        if (postImageView == null) return;

        if (imageUrl != null && !imageUrl.isEmpty()) {
            // If you're using Glide or Picasso for image loading:
             Glide.with(this)
             .load(imageUrl)
             .placeholder(R.drawable.placeholder_image)
             .error(R.drawable.placeholder_image)
             .into(postImageView);
        } else {
            // No image URL, show placeholder
            postImageView.setImageResource(R.drawable.placeholder_image);
        }
    }

    /**
     * Share the post content
     */
    private void sharePost() {
        String shareText = "Check out this " + post.getCategory().toLowerCase() +
                " project: \"" + post.getTitle() + "\" by " + post.getUsername() +
                " on SUYT app!\n\n" + post.getDescription();

        android.content.Intent shareIntent = new android.content.Intent();
        shareIntent.setAction(android.content.Intent.ACTION_SEND);
        shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareText);
        shareIntent.setType("text/plain");

        // Add subject for email sharing
        shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Recycling Project: " + post.getTitle());

        startActivity(android.content.Intent.createChooser(shareIntent, "Share via"));
    }

    /**
     * Handle saving instance state to preserve post data across configuration changes
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (post != null) {
            outState.putParcelable("saved_post", post);
        }
    }

    /**
     * Handle restoring instance state
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.containsKey("saved_post")) {
            post = savedInstanceState.getParcelable("saved_post");
            if (post != null) {
                loadPostData();
            }
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

    private void updateLikesInDatabase(String postId, int newLikeCount) {
         DatabaseReference postRef = FirebaseDatabase.getInstance()
             .getReference("posts").child(postId).child("likes");
         postRef.setValue(newLikeCount);
    }
}