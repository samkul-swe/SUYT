package edu.northeastern.suyt.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import edu.northeastern.suyt.R;
import edu.northeastern.suyt.controller.RecyclingPostController;
import edu.northeastern.suyt.model.RecyclingPost;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private List<RecyclingPost> posts;
    private RecyclingPostController postController;

    public PostAdapter(List<RecyclingPost> posts) {
        this.posts = posts;
        this.postController = new RecyclingPostController();
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        RecyclingPost post = posts.get(position);

        holder.titleTextView.setText(post.getTitle());
        holder.usernameTextView.setText(post.getUsername());
        holder.descriptionTextView.setText(post.getDescription());
        holder.likesTextView.setText(String.valueOf(post.getLikes()));
        holder.dateTextView.setText(post.getDate());

        // In a real app, we would load the image using a library like Glide or Picasso
        // For now, we'll use a placeholder based on the category
        if (post.getCategory().equals("Reuse")) {
            holder.postImageView.setImageResource(R.drawable.placeholder_reuse);
        } else if (post.getCategory().equals("Recycle")) {
            holder.postImageView.setImageResource(R.drawable.placeholder_recycle);
        } else {
            holder.postImageView.setImageResource(R.drawable.placeholder_reduce);
        }

        // Set up the category indicator color
        if (post.getCategory().equals("Reuse")) {
            holder.categoryIndicator.setBackgroundResource(R.color.colorReuse);
        } else if (post.getCategory().equals("Recycle")) {
            holder.categoryIndicator.setBackgroundResource(R.color.colorRecycle);
        } else {
            holder.categoryIndicator.setBackgroundResource(R.color.colorReduce);
        }

        // Set up like button click listener
        holder.likeButton.setOnClickListener(v -> {
            // In a real app, this would update the like count in the database
            post.setLikes(post.getLikes() + 1);
            holder.likesTextView.setText(String.valueOf(post.getLikes()));
            postController.likePost(post.getId());
        });
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView usernameTextView;
        TextView descriptionTextView;
        TextView likesTextView;
        TextView dateTextView;
        ImageView postImageView;
        View categoryIndicator;
        ImageButton likeButton;

        PostViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.post_title_text_view);
            usernameTextView = itemView.findViewById(R.id.username_text_view);
            descriptionTextView = itemView.findViewById(R.id.description_text_view);
            likesTextView = itemView.findViewById(R.id.likes_text_view);
            dateTextView = itemView.findViewById(R.id.date_text_view);
            postImageView = itemView.findViewById(R.id.post_image_view);
            categoryIndicator = itemView.findViewById(R.id.category_indicator);
            likeButton = itemView.findViewById(R.id.like_button);
        }
    }
}
