package edu.northeastern.suyt.ui.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import edu.northeastern.suyt.R;
import edu.northeastern.suyt.controller.RecyclingPostController;
import edu.northeastern.suyt.controller.UserStatsController;
import edu.northeastern.suyt.model.RecyclingPost;
import edu.northeastern.suyt.model.UserStats;
import edu.northeastern.suyt.ui.activities.PostDetailActivity;
import edu.northeastern.suyt.ui.adapters.PostAdapter;

public class HomeFragment extends Fragment implements PostAdapter.OnPostClickListener {


    private TextView userRankTextView;
    private TextView totalPointsTextView;
    private TextView aheadOfUsersTextView;
    private TextView co2SavedTextView;
    private RecyclerView recyclerView;
    private PostAdapter adapter;

    private RecyclingPostController postController;
    private UserStatsController statsController;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize controllers
        postController = new RecyclingPostController();
        statsController = new UserStatsController();

        // Initialize views
        userRankTextView = view.findViewById(R.id.user_rank_text_view);
        totalPointsTextView = view.findViewById(R.id.total_points_text_view);
        aheadOfUsersTextView = view.findViewById(R.id.ahead_of_users_text_view);
        co2SavedTextView = view.findViewById(R.id.co2_saved_text_view);
        recyclerView = view.findViewById(R.id.recycler_view);

        // Set up recycler view
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Load user stats
        loadUserStats();

        // Load community posts
        loadCommunityPosts();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh data when returning to this fragment
        loadUserStats();
        loadCommunityPosts();
    }

    private void loadUserStats() {
        // Get user stats from controller
        UserStats stats = statsController.getUserStats();

        // Update UI with stats
        userRankTextView.setText(statsController.getUserRank());
        totalPointsTextView.setText(String.valueOf(stats.getTotalPoints()));

        // Calculate and display stats for comparison
        int aheadPercentage = statsController.getAheadOfUsersPercentage();
        aheadOfUsersTextView.setText(String.format("You're ahead of %d%% of users", aheadPercentage));

        // Display CO2 savings
        float co2Saved = statsController.calculateCO2Saved();
        co2SavedTextView.setText(String.format("%.1f kg CO2 saved", co2Saved));
    }

    private void loadCommunityPosts() {
        // Get all posts from controller
        List<RecyclingPost> posts = postController.getAllPosts();

        // Initialize adapter with posts and click listener
        adapter = new PostAdapter(posts, this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onPostClick(RecyclingPost post) {
        // Open post detail activity when a post is clicked
        Intent intent = new Intent(getActivity(), PostDetailActivity.class);
        intent.putExtra("POST_ID", post.getId());
        startActivity(intent);
    }
}