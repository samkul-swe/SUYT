package edu.northeastern.suyt.ui.fragments;

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
import edu.northeastern.suyt.ui.adapters.PostAdapter;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private PostAdapter adapter;
    private RecyclingPostController postController;
    private UserStatsController statsController;

    private TextView totalPointsTextView;
    private TextView moneySavedTextView;
    private TextView localUpdateTextView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize controllers
        postController = new RecyclingPostController();
        statsController = new UserStatsController();

        // Initialize user stats views
        totalPointsTextView = view.findViewById(R.id.total_points_text_view);
        moneySavedTextView = view.findViewById(R.id.money_saved_text_view);
        localUpdateTextView = view.findViewById(R.id.local_update_text_view);

        // Load user stats
        loadUserStats();

        // Set up recycler view for posts
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Load and display community posts
        List<RecyclingPost> posts = postController.getAllPosts();
        adapter = new PostAdapter(posts);
        recyclerView.setAdapter(adapter);

        return view;
    }

    private void loadUserStats() {
        // Get user stats from controller
        UserStats stats = statsController.getUserStats();

        // Update UI with stats
        totalPointsTextView.setText(String.valueOf(stats.getTotalPoints()));

        // Calculate money saved (example calculation - $0.10 per point)
        double moneySaved = stats.getTotalPoints() * 0.10;
        moneySavedTextView.setText(String.format("$%.2f", moneySaved));

        // Set local recycling update (in a real app, this would come from a location-based service)
        localUpdateTextView.setText(getLocalRecyclingUpdate());
    }

    private String getLocalRecyclingUpdate() {
        // In a real app, this would fetch data based on user's location
        // For now, we'll return a static message
        return "San Jose now accepts clean pizza boxes in recycling bins! " +
                "Remember to remove any food residue before recycling.";
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh stats when returning to this fragment
        loadUserStats();
    }
}