package edu.northeastern.suyt.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import edu.northeastern.suyt.R;
import edu.northeastern.suyt.firebase.DatabaseConnector;
import edu.northeastern.suyt.model.Post;

public class PostDetailActivity extends AppCompatActivity {
    private static final String TAG = "PostDetailActivity";

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
    private FirebaseAuth firebaseAuth;
    private DatabaseConnector databaseConnector;

    private boolean isPostLikedByUser = false;
    private boolean isPostSavedByUser = false;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        firebaseAuth = FirebaseAuth.getInstance();
        databaseConnector = DatabaseConnector.getInstance();

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
        } else {
            // Handle the case where the user is not authenticated
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupToolbar();
        initializeViews();
        loadPostFromIntent();
        setupClickListeners();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Community Post");
        }
    }

    private void initializeViews() {
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
    }

    private void loadPostFromIntent() {
        if (getIntent() != null && getIntent().hasExtra("POST")) {
            post = getIntent().getParcelableExtra("POST");

            if (post != null) {
                loadPostData();
                checkUserPostStatus(post.getPostID());
            } else {
                Toast.makeText(this, "Error loading post data.", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            Toast.makeText(this, "No post data found. Closing activity.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupClickListeners() {
        likeButton.setOnClickListener(v -> {
            if (post != null) {
                toggleLikeStatus();
            }
        });

        findViewById(R.id.share_button).setOnClickListener(v -> {
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

        titleTextView.setText(post.getPostTitle());
        usernameTextView.setText(post.getPostedBy());
        dateTextView.setText(post.getPostedOn());
        descriptionTextView.setText(post.getPostDescription());
        likesTextView.setText(String.valueOf(post.getNumberOfLikes()));
        categoryTextView.setText(post.getPostCategory());

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(post.getPostTitle());
        }

        setCategoryIndicatorColor(post.getPostCategory());
        setPostImage(post.getPostCategory());
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
                imageResource = R.drawable.reuse;
                break;
            case "recycle":
                imageResource = R.drawable.recycle;
                break;
            case "reduce":
                imageResource = R.drawable.reduce;
                break;
            default:
                imageResource = R.drawable.ic_image_placeholder;
                break;
        }
        postImageView.setImageResource(imageResource);
    }

    private void checkUserPostStatus(String postId) {
        if (currentUserId == null || postId == null) {
            Log.w(TAG, "Cannot check user post status: missing user ID or post ID");
            return;
        }

//        checkUserLikeStatus(postId);
//        checkUserSaveStatus(postId);
    }

//    private void checkUserLikeStatus(String postId) {
//        // Use the correct method from DatabaseConnector
//        DatabaseReference userLikesRef = databaseConnector.getUserPostLikeReference(currentUserId, postId);
//
//        userLikesRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                isPostLikedByUser = snapshot.exists() && Boolean.TRUE.equals(snapshot.getValue(Boolean.class));
//                updateLikeButtonUI();
//                Log.d(TAG, "User like status for post " + postId + ": " + isPostLikedByUser);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Log.e(TAG, "Failed to check user like status: " + error.getMessage());
//                isPostLikedByUser = false;
//                updateLikeButtonUI();
//            }
//        });
//    }

//    private void checkUserSaveStatus(String postId) {
//        // Use the correct method from DatabaseConnector
//        DatabaseReference userSavesRef = databaseConnector.getUserPostSaveReference(currentUserId, postId);
//
//        userSavesRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                isPostSavedByUser = snapshot.exists() && Boolean.TRUE.equals(snapshot.getValue(Boolean.class));
//                updateSaveButtonUI();
//                Log.d(TAG, "User save status for post " + postId + ": " + isPostSavedByUser);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Log.e(TAG, "Failed to check user save status: " + error.getMessage());
//                isPostSavedByUser = false;
//                updateSaveButtonUI();
//            }
//        });
//    }

    private void toggleLikeStatus() {
        if (currentUserId == null || post == null) {
            Toast.makeText(this, "Unable to like post. Please try again.", Toast.LENGTH_SHORT).show();
            return;
        }

        likeButton.setEnabled(false);

        if (isPostLikedByUser) {
//            unlikePost();
        } else {
            likePost();
        }
    }

    private void likePost() {
        String postId = post.getPostID();

        int newLikeCount = post.getNumberOfLikes() + 1;
        post.setNumberOfLikes(newLikeCount);

        updateLikesInDatabase(postId, newLikeCount);

        // Use the correct method from DatabaseConnector
//        DatabaseReference userLikeRef = databaseConnector.getUserPostLikeReference(currentUserId, postId);

//        userLikeRef.setValue(true)
//                .addOnSuccessListener(aVoid -> {
//                    isPostLikedByUser = true;
//                    likesTextView.setText(String.valueOf(post.getLikes()));
//                    updateLikeButtonUI();
//                    likeButton.setEnabled(true);
//                    Snackbar.make(findViewById(android.R.id.content), "Post liked! 👍", Snackbar.LENGTH_SHORT).show();
//                    Log.d(TAG, "Successfully liked post " + postId);
//                })
//                .addOnFailureListener(e -> {
//                    post.setLikes(post.getLikes() - 1);
//                    likesTextView.setText(String.valueOf(post.getLikes()));
//                    likeButton.setEnabled(true);
//                    Toast.makeText(this, "Failed to like post: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                    Log.e(TAG, "Failed to like post: " + e.getMessage());
//                });
    }

//    private void unlikePost() {
//        String postId = post.getId();
//
//        int newLikeCount = Math.max(0, post.getLikes() - 1);
//        post.setLikes(newLikeCount);
//
//        updateLikesInDatabase(postId, newLikeCount);
//
//        // Use the correct method from DatabaseConnector
//        DatabaseReference userLikeRef = databaseConnector.getUserPostLikeReference(currentUserId, postId);
//
//        userLikeRef.removeValue()
//                .addOnSuccessListener(aVoid -> {
//                    isPostLikedByUser = false;
//                    likesTextView.setText(String.valueOf(post.getLikes()));
//                    updateLikeButtonUI();
//                    likeButton.setEnabled(true);
//                    Snackbar.make(findViewById(android.R.id.content), "Post unliked", Snackbar.LENGTH_SHORT).show();
//                    Log.d(TAG, "Successfully unliked post " + postId);
//                })
//                .addOnFailureListener(e -> {
//                    post.setLikes(post.getLikes() + 1);
//                    likesTextView.setText(String.valueOf(post.getLikes()));
//                    likeButton.setEnabled(true);
//                    Toast.makeText(this, "Failed to unlike post: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                    Log.e(TAG, "Failed to unlike post: " + e.getMessage());
//                });
//    }

    private void updateLikeButtonUI() {
        if (isPostLikedByUser) {
            likeButton.setIconResource(R.drawable.ic_favorite_filled);
            likeButton.setIconTint(ContextCompat.getColorStateList(this, R.color.colorAccent));
        } else {
            likeButton.setIconResource(R.drawable.ic_favorite_border);
            likeButton.setIconTint(ContextCompat.getColorStateList(this, android.R.color.darker_gray));
        }
    }

    private void toggleSaveStatus() {
        if (currentUserId == null || post == null) {
            Toast.makeText(this, "Unable to save post. Please try again.", Toast.LENGTH_SHORT).show();
            return;
        }

        saveToolbarButton.setEnabled(false);

        if (isPostSavedByUser) {
//            unsavePost();
        } else {
//            savePost();
        }
    }

//    private void savePost() {
//        String postId = post.getId();
//
//        // Use the correct method from DatabaseConnector
//        DatabaseReference userSaveRef = databaseConnector.getUserPostSaveReference(currentUserId, postId);
//
//        userSaveRef.setValue(true)
//                .addOnSuccessListener(aVoid -> {
//                    isPostSavedByUser = true;
//                    updateSaveButtonUI();
//                    saveToolbarButton.setEnabled(true);
//                    Snackbar.make(findViewById(android.R.id.content), "Post saved! 💾", Snackbar.LENGTH_SHORT).show();
//                    Log.d(TAG, "Successfully saved post " + postId);
//                })
//                .addOnFailureListener(e -> {
//                    saveToolbarButton.setEnabled(true);
//                    Toast.makeText(this, "Failed to save post: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                    Log.e(TAG, "Failed to save post: " + e.getMessage());
//                });
//    }

//    private void unsavePost() {
//        String postId = post.getId();
//
//        // Use the correct method from DatabaseConnector
//        DatabaseReference userSaveRef = databaseConnector.getUserPostSaveReference(currentUserId, postId);
//
//        userSaveRef.removeValue()
//                .addOnSuccessListener(aVoid -> {
//                    isPostSavedByUser = false;
//                    updateSaveButtonUI();
//                    saveToolbarButton.setEnabled(true);
//                    Snackbar.make(findViewById(android.R.id.content), "Post unsaved", Snackbar.LENGTH_SHORT).show();
//                    Log.d(TAG, "Successfully unsaved post " + postId);
//                })
//                .addOnFailureListener(e -> {
//                    saveToolbarButton.setEnabled(true);
//                    Toast.makeText(this, "Failed to unsave post: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                    Log.e(TAG, "Failed to unsave post: " + e.getMessage());
//                });
//    }

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

        String shareText = "Check out this " + post.getPostCategory().toLowerCase() +
                " project: \"" + post.getPostTitle() + "\" by " + post.getPostedBy() +
                " on SUYT app!\n\n" + post.getPostDescription();

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Recycling Project: " + post.getPostTitle());

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
        if (postId != null) {
            DatabaseReference postRef = databaseConnector.getPostReference(postId).child("likes");
            postRef.setValue(newLikeCount)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Successfully updated likes count to " + newLikeCount + " for post " + postId);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to update likes in database: " + e.getMessage());
                        Toast.makeText(this, "Failed to update likes count", Toast.LENGTH_SHORT).show();
                    });
        }
    }
}