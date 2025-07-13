package edu.northeastern.suyt.ui.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.northeastern.suyt.R;
import edu.northeastern.suyt.controller.UserController;
import edu.northeastern.suyt.firebase.repository.database.PostsRepository;
import edu.northeastern.suyt.model.Post;
import edu.northeastern.suyt.model.User;
import edu.northeastern.suyt.ui.fragments.HomeFragment;
import edu.northeastern.suyt.utils.SessionManager;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private List<Post> posts;
    private OnPostClickListener listener;
    private final ExecutorService executorService;
    private final Handler mainHandler;
    private boolean enableLikeButton;

    public interface OnPostClickListener {
        void onPostClick(Post post);
    }

    public PostAdapter(Context context, boolean enableLikeButton) {
        this.enableLikeButton = enableLikeButton;
        this.executorService = Executors.newFixedThreadPool(4);
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setLikeButtonEnabled(boolean enabled) {
        this.enableLikeButton = enabled;
        notifyDataSetChanged();
    }

    public void setOnPostClickListener(HomeFragment homeFragment) {
        this.listener = homeFragment;
    }

    @SuppressLint({"CheckResult", "NotifyDataSetChanged"})
    public void updateData(List<Post> posts) {
        this.posts = posts;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @SuppressLint("CheckResult")
    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        if (posts == null || posts.isEmpty()) {
            return;
        }

        Post post = posts.get(position);

        executorService.execute(() -> {
            mainHandler.post(() -> {

                if (post.getPostCategory().equalsIgnoreCase("reuse")) {
                    holder.postImageView.setImageResource(R.drawable.reuse);
                } else if (post.getPostCategory().equalsIgnoreCase("recycle")) {
                    holder.postImageView.setImageResource(R.drawable.recycle);
                } else {
                    holder.postImageView.setImageResource(R.drawable.reduce);
                }

                holder.postImageView.setTag(post.getPostImage());
                holder.titleTextView.setText(post.getPostTitle());
                holder.usernameTextView.setText(post.getPostedBy());
                holder.likesTextView.setText(String.valueOf(post.getNumberOfLikes()));
                holder.dateTextView.setText(post.getPostedOn());

                configureLikeButton(holder, post, enableLikeButton);
            });
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPostClick(post);
            }
        });

        setCategoryIndicatorColor(holder, post.getPostCategory());
    }

    private void configureLikeButton(PostViewHolder holder, Post post, boolean enableLikeButton) {
        if (enableLikeButton) {
            holder.likeButton.setEnabled(true);
            holder.likeButton.setAlpha(1.0f);
            holder.likeButton.setOnClickListener(v -> {
                post.setNumberOfLikes(post.getNumberOfLikes() + 1);
                holder.likesTextView.setText(String.valueOf(post.getNumberOfLikes()));

                updateLikeInDatabase(post.getPostID(), post.getNumberOfLikes());
            });
        } else {
            holder.likeButton.setEnabled(false);
            holder.likeButton.setAlpha(0.5f);
            holder.likeButton.setOnClickListener(null);
        }
    }

    private void updateLikeInDatabase(String postId, int newLikeCount) {
        if (postId == null || postId.isEmpty()) {
            return;
        }

        PostsRepository postsRepository = new PostsRepository();

        postsRepository.getPostsRef()
            .child(postId)
            .child("likes")
            .setValue(newLikeCount)
            .addOnSuccessListener(aVoid -> {
                System.out.println("Successfully updated like count for post: " + postId);
            })
            .addOnFailureListener(exception -> {
                System.err.println("Failed to update like count: " + exception.getMessage());
                exception.printStackTrace();
            });
    }

    private void setCategoryIndicatorColor(PostViewHolder holder, String category) {
        if (holder.categoryIndicator != null && category != null) {
            switch (category.toLowerCase()) {
                case "reuse":
                    holder.categoryIndicator.setBackgroundResource(R.color.colorReuse);
                    break;
                case "recycle":
                    holder.categoryIndicator.setBackgroundResource(R.color.colorRecycle);
                    break;
                case "reduce":
                default:
                    holder.categoryIndicator.setBackgroundResource(R.color.colorReduce);
                    break;
            }
        }
    }

    @Override
    public int getItemCount() {
        return posts != null ? posts.size() : 0;
    }

    public void cleanup() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView usernameTextView;
        TextView likesTextView;
        TextView dateTextView;
        ImageView postImageView;
        View categoryIndicator;
        ImageButton likeButton;

        PostViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.post_title_text_view);
            usernameTextView = itemView.findViewById(R.id.username_text_view);
            likesTextView = itemView.findViewById(R.id.likes_text_view);
            dateTextView = itemView.findViewById(R.id.date_text_view);
            postImageView = itemView.findViewById(R.id.post_image_view);
            categoryIndicator = itemView.findViewById(R.id.category_indicator);
            likeButton = itemView.findViewById(R.id.like_button);
        }
    }
}