package edu.northeastern.suyt.ui.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import edu.northeastern.suyt.R;
import edu.northeastern.suyt.controller.UserStatsController;
import edu.northeastern.suyt.model.UserStats;


public class RRRFragment extends Fragment implements View.OnClickListener {
    private TextView totalPointsTextView;
    private TextView totalActivitiesTextView;
    private TextView recyclePointsTextView;
    private TextView reusePointsTextView;
    private TextView reducePointsTextView;

    private Button recycleButton;
    private Button reuseButton;
    private Button reduceButton;

    private TextView infoContentTextView;
    private CardView infoCardView;

    private ProgressBar recycleProgressBar;
    private ProgressBar reuseProgressBar;
    private ProgressBar reduceProgressBar;

    private UserStatsController statsController;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rrr, container, false);

        statsController = new UserStatsController();

        // Initialize views
        totalPointsTextView = view.findViewById(R.id.total_points_text_view);
        totalActivitiesTextView = view.findViewById(R.id.total_activities_text_view);
        recyclePointsTextView = view.findViewById(R.id.recycle_points_text_view);
        reusePointsTextView = view.findViewById(R.id.reuse_points_text_view);
        reducePointsTextView = view.findViewById(R.id.reduce_points_text_view);

        recycleButton = view.findViewById(R.id.recycle_button);
        reuseButton = view.findViewById(R.id.reuse_button);
        reduceButton = view.findViewById(R.id.reduce_button);

        infoContentTextView = view.findViewById(R.id.info_content_text_view);
        infoCardView = view.findViewById(R.id.info_card_view);

        recycleProgressBar = view.findViewById(R.id.recycle_progress_bar);
        reuseProgressBar = view.findViewById(R.id.reuse_progress_bar);
        reduceProgressBar = view.findViewById(R.id.reduce_progress_bar);

        // Set click listeners
        recycleButton.setOnClickListener(this);
        reuseButton.setOnClickListener(this);
        reduceButton.setOnClickListener(this);

        // Load user stats
        loadUserStats();

        // Set default content (Recycle)
        setSelectedButton(recycleButton);
        showRecycleInfo();

        return view;
    }

    private void loadUserStats() {
        UserStats stats = statsController.getUserStats();

        // Set text values
        totalPointsTextView.setText(String.valueOf(stats.getTotalPoints()));
        totalActivitiesTextView.setText(String.valueOf(stats.getTotalActivities()));
        recyclePointsTextView.setText(String.valueOf(stats.getRecyclePoints()));
        reusePointsTextView.setText(String.valueOf(stats.getReusePoints()));
        reducePointsTextView.setText(String.valueOf(stats.getReducePoints()));

        // Calculate max for progress bars
        int maxPoints = Math.max(stats.getRecyclePoints(),
                Math.max(stats.getReusePoints(), stats.getReducePoints()));
        // Add a little extra for visual appeal
        maxPoints = (int)(maxPoints * 1.2);

        // Set progress bars
        recycleProgressBar.setMax(maxPoints);
        reuseProgressBar.setMax(maxPoints);
        reduceProgressBar.setMax(maxPoints);

        recycleProgressBar.setProgress(stats.getRecyclePoints());
        reuseProgressBar.setProgress(stats.getReusePoints());
        reduceProgressBar.setProgress(stats.getReducePoints());
    }

    @Override
    public void onClick(View v) {
        // Reset all buttons
        resetButtons();

        // Set selected button and show appropriate info
        if (v.getId() == R.id.recycle_button) {
            setSelectedButton(recycleButton);
            showRecycleInfo();
        } else if (v.getId() == R.id.reuse_button) {
            setSelectedButton(reuseButton);
            showReuseInfo();
        } else if (v.getId() == R.id.reduce_button) {
            setSelectedButton(reduceButton);
            showReduceInfo();
        }
    }

    private void resetButtons() {
        recycleButton.setBackgroundResource(R.drawable.button_normal);
        reuseButton.setBackgroundResource(R.drawable.button_normal);
        reduceButton.setBackgroundResource(R.drawable.button_normal);

        recycleButton.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black));
        reuseButton.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black));
        reduceButton.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black));
    }

    private void setSelectedButton(Button button) {
        if (button == recycleButton) {
            button.setBackgroundResource(R.drawable.button_selected_recycle);
            infoCardView.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorRecycle));
        } else if (button == reuseButton) {
            button.setBackgroundResource(R.drawable.button_selected_reuse);
            infoCardView.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorReuse));
        } else if (button == reduceButton) {
            button.setBackgroundResource(R.drawable.button_selected_reduce);
            infoCardView.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorReduce));
        }

        button.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white));
    }

    private void showRecycleInfo() {
        infoContentTextView.setText(statsController.getRecycleInfo());
    }

    private void showReuseInfo() {
        infoContentTextView.setText(statsController.getReuseInfo());
    }

    private void showReduceInfo() {
        infoContentTextView.setText(statsController.getReduceInfo());
    }

    // Method to refresh stats (can be called from parent activity if needed)
    public void refreshStats() {
        if (isAdded()) {
            loadUserStats();
        }
    }
}