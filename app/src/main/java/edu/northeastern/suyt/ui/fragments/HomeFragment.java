package edu.northeastern.suyt.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import edu.northeastern.suyt.R;
import edu.northeastern.suyt.model.Post;
import edu.northeastern.suyt.ui.activities.PostDetailActivity;
import edu.northeastern.suyt.ui.adapters.PostAdapter;
import edu.northeastern.suyt.ui.viewmodel.HomeViewModel;
import edu.northeastern.suyt.utils.SessionManager;

public class HomeFragment extends Fragment implements PostAdapter.OnPostClickListener {

    private TextView userRankTextView;
    private TextView reducePointsTextView;
    private TextView reusePointsTextView;
    private TextView recyclePointsTextView;
    private TextView quoteForUserTextView;
    private RecyclerView recyclerView;

    // Loading indicator
    private ProgressBar quoteLoadingProgress;

    private HomeViewModel viewModel;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private PostAdapter postAdapter;
    private LinearLayoutManager layoutManager;

    private String currentQuote;
    private String userRank;
    private int reducePoints;
    private int reusePoints;
    private int recyclePoints;

    private static final long QUOTE_VALIDITY_PERIOD = 24 * 60 * 60 * 1000L; // 24 hours
    private static final String DEFAULT_QUOTE = "Every small action counts towards a greener tomorrow!";


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        if (savedInstanceState != null) {
            restoreInstanceState(savedInstanceState);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        findViews(view);

        setupRecyclerView();

        setupViewModelObservers();
        loadUserStats();
        viewModel.loadPostsIfNeeded();

        quoteForUserTextView.setText(DEFAULT_QUOTE);

        viewModel.getQuote().observe(getViewLifecycleOwner(), quote -> {
            if (quote != null) {
                quoteForUserTextView.setText(quote);
            }
        });

        viewModel.getIsLoadingQuote().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading) {
                quoteLoadingProgress.setVisibility(View.VISIBLE);
            } else {
                quoteLoadingProgress.setVisibility(View.GONE);
            }
        });

        viewModel.loadQuoteIfNeeded();
    }

    private void findViews(View view) {
        userRankTextView = view.findViewById(R.id.user_rank_text_view);
        quoteForUserTextView = view.findViewById(R.id.quote_for_user_text_view);
        quoteLoadingProgress = view.findViewById(R.id.quote_loading_progress);
        reducePointsTextView = view.findViewById(R.id.reduce_points_text_view);
        reusePointsTextView = view.findViewById(R.id.reuse_points_text_view);
        recyclePointsTextView = view.findViewById(R.id.recycle_points_text_view);
        recyclerView = view.findViewById(R.id.recycler_view);
    }

    private void setupRecyclerView() {
        layoutManager = new LinearLayoutManager(requireContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemViewCacheSize(20);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserStats();
    }

    @Override
    public void onPause() {
        super.onPause();

        if (layoutManager != null) {
            viewModel.setRecyclerViewState(layoutManager.onSaveInstanceState());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (layoutManager != null) {
            viewModel.setRecyclerViewState(layoutManager.onSaveInstanceState());
        }

        recyclerView = null;
        postAdapter = null;
        layoutManager = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (postAdapter != null) {
            postAdapter.cleanup();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("currentQuote", currentQuote);
        outState.putString("userRank", userRank);
        outState.putInt("reducePoints", reducePoints);
        outState.putInt("reusePoints", reusePoints);
        outState.putInt("recyclePoints", recyclePoints);
    }

    private void setupViewModelObservers() {
        viewModel.getPosts().observe(getViewLifecycleOwner(), posts -> {
            if (posts != null) {
                updatePostsUI(posts);
                restoreScrollPosition();
            }
        });

        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(requireContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void restoreInstanceState(@NonNull Bundle savedInstanceState) {
        currentQuote = savedInstanceState.getString("currentQuote");
        userRank = savedInstanceState.getString("userRank");
        reducePoints = savedInstanceState.getInt("reducePoints", 0);
        reusePoints = savedInstanceState.getInt("reusePoints", 0);
        recyclePoints = savedInstanceState.getInt("recyclePoints", 0);
    }

    private void loadUserStats() {
        SessionManager sessionManager = new SessionManager(requireContext());

        userRank = sessionManager.getUserRank();
        reducePoints = sessionManager.getReducePoints();
        reusePoints = sessionManager.getReusePoints();
        recyclePoints = sessionManager.getRecyclePoints();

        if (!isAdded()) return;

        userRankTextView.setText(userRank);
        reducePointsTextView.setText(String.valueOf(reducePoints));
        reusePointsTextView.setText(String.valueOf(reusePoints));
        recyclePointsTextView.setText(String.valueOf(recyclePoints));
    }

    private void updatePostsUI(List<Post> posts) {
        if (!isAdded() || recyclerView == null) return;

        mainHandler.post(() -> {
            if (isAdded() && recyclerView != null) {
                recyclerView.setVisibility(View.VISIBLE);

                if (postAdapter == null) {
                    // Create adapter with like button disabled for HomeFragment
                    postAdapter = new PostAdapter(requireContext(), false); // false = disable like button
                    postAdapter.setOnPostClickListener(this);
                    recyclerView.setAdapter(postAdapter);
                } else {
                    // Ensure like button stays disabled
                    postAdapter.setLikeButtonEnabled(false);
                }

                postAdapter.updateData(posts);
            }
        });
    }

    private void restoreScrollPosition() {
        if (layoutManager != null) {
            android.os.Parcelable savedState = viewModel.getRecyclerViewState();
            if (savedState != null) {
                layoutManager.onRestoreInstanceState(savedState);
            }
        }
    }

    @Override
    public void onPostClick(Post post) {
        if (isAdded() && getActivity() != null) {
            Intent intent = new Intent(getActivity(), PostDetailActivity.class);
            intent.putExtra("POST", post);
            startActivity(intent);
        }
    }
}