package edu.northeastern.suyt.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;

import edu.northeastern.suyt.R;
import edu.northeastern.suyt.firebase.repository.database.PostsRepository;
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
    private ImageButton saveToolbarButton;
    private MaterialButton likeButton;

    private Post post;
    private PostsRepository postsRepository;

    private boolean isPostLikedByUser = false;
    private boolean isPostSavedByUser = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        postsRepository = new PostsRepository();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Community Post");
        }

        postImageView = findViewById(R.id.post_image_view);
        titleTextView = findViewById(R.id.title_text_view);
        usernameTextView = findViewById(R.id.username_text_view);
        dateTextView = findViewById(R.id.date_text_view);
        descriptionTextView = findViewById(R.id.description_text_view);
        likesTextView = findViewById(R.id.likes_count_text_view);
        categoryTextView = findViewById(R.id.category_text_view);
        categoryIndicator = findViewById(R.id.category_indicator);

        likeButton = findViewById(R.id.like_button);
        Button shareButton = findViewById(R.id.share_button);
        saveToolbarButton = findViewById(R.id.save_button_toolbar);

        if (getIntent() != null && getIntent().hasExtra("POST")) {
            post = getIntent().getParcelableExtra("POST");

            if (post != null) {
                loadPostData();
                checkUserPostStatus(post.getId());
            } else {
                Toast.makeText(this, "Error loading post data.", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            Toast.makeText(this, "No post data found. Closing activity.", Toast.LENGTH_SHORT).show();
            finish();
        }

        likeButton.setOnClickListener(v -> {
            if (post != null) {
                toggleLikeStatus();
            }
        });

        shareButton.setOnClickListener(v -> {
            if (post != null) {
                sharePost();
            }
        });

        saveToolbarButton.setOnClickListener(v -> {
            if (post != null) {
                toggleSaveStatus();
            }
        });
    }

    private void loadPostData() {
        if (post == null) return;

        titleTextView.setText(post.getTitle());
        usernameTextView.setText(post.getUsername());
        dateTextView.setText(post.getDate());
        descriptionTextView.setText(post.getDescription());
        likesTextView.setText(String.valueOf(post.getLikes()));
        categoryTextView.setText(post.getCategory());

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(post.getTitle());
        }

        setCategoryIndicatorColor(post.getCategory());
        setPostImage(post.getCategory());

        if (isPostLikedByUser) {
            updateLikeButtonUI();
        }
    }

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
        categoryIndicator.setBackgroundColor(ContextCompat.getColor(this, colorResource));
    }

    private void setPostImage(String category) {
        if (postImageView == null || category == null) return;

        int imageResource;
        switch (category.toLowerCase()) {
            case "reuse":
                imageResource = R.drawable.reuse; // Make sure these drawables exist
                break;
            case "recycle":
                imageResource = R.drawable.recycle; // Make sure these drawables exist
                break;
            case "reduce":
                imageResource = R.drawable.reduce; // Make sure these drawables exist
                break;
            default:
                imageResource = R.drawable.ic_image_placeholder; // Fallback image
                break;
        }
        postImageView.setImageResource(imageResource);
    }

    private void checkUserPostStatus(String postId) {
        isPostLikedByUser = false;
        isPostSavedByUser = false;

        updateLikeButtonUI();
        updateSaveButtonUI();
    }


    private void toggleLikeStatus() {
        String currentUserId = "test_user_id"; // Placeholder

        if (isPostLikedByUser) {
            post.setLikes(post.getLikes() - 1);
            updateLikesInDatabase(post.getId(), post.getLikes());
            Snackbar.make(findViewById(android.R.id.content), "Post unliked.", Snackbar.LENGTH_SHORT).show();
        } else {
            post.setLikes(post.getLikes() + 1);
            updateLikesInDatabase(post.getId(), post.getLikes()); // Increment in DB
            Snackbar.make(findViewById(android.R.id.content), "Post liked!", Snackbar.LENGTH_SHORT).show();
        }
        isPostLikedByUser = !isPostLikedByUser; // Toggle local state
        likesTextView.setText(String.valueOf(post.getLikes()));
        updateLikeButtonUI(); // Update UI after toggling
    }

    private void updateLikeButtonUI() {
        if (isPostLikedByUser) {;
            likeButton.setIconResource(R.drawable.ic_favorite_filled);
            likeButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.colorAccent));
        } else {
            likeButton.setIconResource(R.drawable.ic_favorite_border);
            likeButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.colorAccent));
        }
    }

    private void toggleSaveStatus() {
        String currentUserId = "test_user_id";

        if (isPostSavedByUser) {
            Snackbar.make(findViewById(android.R.id.content), "Post unsaved.", Snackbar.LENGTH_SHORT).show();
        } else {
            Snackbar.make(findViewById(android.R.id.content), "Post saved!", Snackbar.LENGTH_SHORT).show();
        }
        isPostSavedByUser = !isPostSavedByUser;
        updateSaveButtonUI();
    }

    private void updateSaveButtonUI() {
        if (isPostSavedByUser) {
            saveToolbarButton.setImageResource(R.drawable.ic_save_filled);
            saveToolbarButton.setColorFilter(ContextCompat.getColor(this, R.color.colorAccent));
        } else {
            saveToolbarButton.setImageResource(R.drawable.ic_save_border);
            saveToolbarButton.setColorFilter(ContextCompat.getColor(this, android.R.color.white));
        }
    }


    private void sharePost() {
        if (post == null) return;

        String shareText = "Check out this " + post.getCategory().toLowerCase() +
                " project: \"" + post.getTitle() + "\" by " + post.getUsername() +
                " on SUYT app!\n\n" + post.getDescription();

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        shareIntent.setType("text/plain");

        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Recycling Project: " + post.getTitle());

        startActivity(Intent.createChooser(shareIntent, "Share via"));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (post != null) {
            outState.putParcelable("saved_post", post);
        }
        outState.putBoolean("is_post_liked", isPostLikedByUser);
        outState.putBoolean("is_post_saved", isPostSavedByUser);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.containsKey("saved_post")) {
            post = savedInstanceState.getParcelable("saved_post");
            if (post != null) {
                loadPostData();
                isPostLikedByUser = savedInstanceState.getBoolean("is_post_liked", false);
                isPostSavedByUser = savedInstanceState.getBoolean("is_post_saved", false);
                updateLikeButtonUI();
                updateSaveButtonUI();
            }
        }
    }

    private void updateLikesInDatabase(String postId, int newLikeCount) {
        if (postsRepository != null && postId != null) {
            DatabaseReference postRef = postsRepository.getPostsRef().child(postId).child("likes");
            postRef.setValue(newLikeCount)
                .addOnSuccessListener(aVoid -> {
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update likes in DB: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
        }
    }
}