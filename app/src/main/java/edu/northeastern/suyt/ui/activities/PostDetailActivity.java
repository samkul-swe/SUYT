package edu.northeastern.suyt.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;

import edu.northeastern.suyt.R;
import edu.northeastern.suyt.controller.PostController;
import edu.northeastern.suyt.controller.UserController;
import edu.northeastern.suyt.model.Post;
import edu.northeastern.suyt.utils.SessionManager;

public class PostDetailActivity extends AppCompatActivity {
    private static final String TAG = "PostDetailActivity";

    private ImageView postImageView;
    private TextView titleTextView;
    private TextView usernameTextView;
    private TextView dateTextView;
    private TextView descriptionTextView;
    private TextView likesTextView;
    private View categoryIndicator;
    private ImageButton saveToolbarButton;
    private MaterialButton likeButton;
    private MaterialButton shareButton;

    private Post post;
    private UserController userController;
    private PostController postController;
    private SessionManager sessionManager;

    private boolean isPostLikedByUser = false;
    private boolean isPostSavedByUser = false;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        sessionManager = new SessionManager(this);
        currentUserId = sessionManager.getUserId();
        userController = new UserController(currentUserId);

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
        categoryIndicator = findViewById(R.id.category_indicator);

        likeButton = findViewById(R.id.like_button);
        shareButton = findViewById(R.id.share_button);
        saveToolbarButton = findViewById(R.id.save_button_toolbar);
    }

    private void loadPostFromIntent() {
        if (getIntent() != null && getIntent().hasExtra("POST")) {
            post = getIntent().getParcelableExtra("POST");

            if (post != null) {
                postController = new PostController(post.getPostID());
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

        checkUserLikeStatus(postId);
        checkUserSaveStatus(postId);
    }

    private void checkUserLikeStatus(String postId) {
        if (sessionManager.isLikedPost(postId)) {
            isPostLikedByUser = true;
            updateLikeButtonUI();
        } else {
            isPostLikedByUser = false;
            updateLikeButtonUI();
        }
    }

    private void checkUserSaveStatus(String postId) {
        if (sessionManager.isSavedPost(postId)) {
            isPostSavedByUser = true;
            updateSaveButtonUI();
        } else {
            isPostSavedByUser = false;
            updateSaveButtonUI();
        }
    }

    private void toggleLikeStatus() {
        if (currentUserId == null || post == null) {
            Toast.makeText(this, "Unable to like post. Please try again.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isPostLikedByUser) {
            unlikePost();
        } else {
            likePost();
        }

        updateLikeButtonUI();
    }

    private void likePost() {
        String postId = post.getPostID();

        int newLikeCount = post.getNumberOfLikes() + 1;
        post.setNumberOfLikes(newLikeCount);
        likesTextView.setText(String.valueOf(newLikeCount));
        isPostLikedByUser = true;

        sessionManager.addLikedPost(postId);
        updateLikeButtonUI();

        updateLikedInDatabase(postId, newLikeCount);
    }

    private void unlikePost() {
        String postId = post.getPostID();

        int newLikeCount = Math.max(0, post.getNumberOfLikes() - 1);
        post.setNumberOfLikes(newLikeCount);
        likesTextView.setText(String.valueOf(newLikeCount));

        sessionManager.removeLikedPost(postId);
        isPostLikedByUser = false;
        updateLikeButtonUI();

        updateUnlikedInDatabase(postId, newLikeCount);
    }

    private void updateLikeButtonUI() {
        if (isPostLikedByUser) {
            likeButton.setIconResource(R.drawable.ic_favorite_filled);
            likeButton.setIconTint(ContextCompat.getColorStateList(this, android.R.color.darker_gray));
        } else {
            likeButton.setIconResource(R.drawable.ic_favorite_border);
            likeButton.setIconTint(ContextCompat.getColorStateList(this, android.R.color.white));
        }
    }

    private void toggleSaveStatus() {
        if (currentUserId == null || post == null) {
            Toast.makeText(this, "Unable to save post. Please try again.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isPostSavedByUser) {
            unsavePost();
        } else {
            savePost();
        }

        updateSaveButtonUI();
    }

    private void savePost() {
        String postId = post.getPostID();

        sessionManager.addSavedPost(postId);
        isPostSavedByUser = true;
        updateSaveButtonUI();
        updateSavedInDatabase(postId, true);

    }

    private void unsavePost() {
        String postId = post.getPostID();

        sessionManager.removeSavedPost(postId);
        isPostSavedByUser = false;
        updateSaveButtonUI();
        updateSavedInDatabase(postId, false);
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
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
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

    private void updateSavedInDatabase(String postId, boolean isSaved) {
        if (postId != null) {
            if (isSaved) {
                userController.savePost(postId, new UserController.UpdateCallback() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "Successfully updated save for user");
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        Log.e(TAG, "Failed to update save for user: " + errorMessage);
                    }
                });
            } else {
                userController.unsavePost(postId, new UserController.UpdateCallback() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "Successfully updated unsave for user");
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        Log.e(TAG, "Failed to update unsave for user: " + errorMessage);
                    }
                });
            }
        }
    }

    private void updateUnlikedInDatabase(String postId, int newLikeCount) {
        if (postId != null) {
            postController.updateLikes(newLikeCount, new PostController.UpdateLikesCallback() {
                @Override
                public void onSuccess() {
                    userController.unlikePost(postId, new UserController.UpdateCallback() {
                        @Override
                        public void onSuccess() {
                            Log.d(TAG, "Successfully updated unlike for user");
                        }

                        @Override
                        public void onFailure(String errorMessage) {
                            Log.e(TAG, "Failed to update unlike for user: " + errorMessage);
                        }
                    });
                }

                @Override
                public void onFailure() {
                    Log.e(TAG, "Failed to update likes in database");
                }
            });
        }
    }

    private void updateLikedInDatabase(String postId, int newLikeCount) {
        if (postId != null) {
            postController.updateLikes(newLikeCount, new PostController.UpdateLikesCallback() {
                @Override
                public void onSuccess() {
                    userController.likePost(postId, new UserController.UpdateCallback() {
                        @Override
                        public void onSuccess() {
                            Log.d(TAG, "Successfully updated like for user");
                        }

                        @Override
                        public void onFailure(String errorMessage) {
                            Log.e(TAG, "Failed to update like for user: " + errorMessage);
                        }
                    });
                }

                @Override
                public void onFailure() {
                    Log.e(TAG, "Failed to update likes in database");
                }
            });
        }
    }
}