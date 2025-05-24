package edu.northeastern.suyt.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.tasks.Task;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.ai.type.GenerateContentResponse;
import com.google.firebase.ai.type.Schema;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import edu.northeastern.suyt.R;
import edu.northeastern.suyt.firebase.repository.database.UsersRepository;
import edu.northeastern.suyt.gemini.GeminiClient;
import edu.northeastern.suyt.model.Post;
import edu.northeastern.suyt.model.UserStats;
import edu.northeastern.suyt.ui.activities.PostDetailActivity;
import edu.northeastern.suyt.ui.adapters.PostAdapter;
import edu.northeastern.suyt.viewmodel.HomeViewModel;

public class HomeFragment extends Fragment implements PostAdapter.OnPostClickListener {

    private TextView userRankTextView;
    private TextView reducePointsTextView;
    private TextView reusePointsTextView;
    private TextView recyclePointsTextView;
    private TextView quoteForUserTextView;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;

    private HomeViewModel viewModel;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private static final String PREF_NAME = "quote_preferences";
    private static final String KEY_QUOTE = "saved_quote";
    private static final String KEY_TIMESTAMP = "quote_timestamp";
    private static final long QUOTE_VALIDITY_PERIOD = 24 * 60 * 60 * 1000L; // 24 hours

    private static final String KEY_CURRENT_QUOTE = "current_quote";
    private static final String KEY_USER_RANK = "user_rank";
    private static final String KEY_REDUCE_POINTS = "reduce_points";
    private static final String KEY_REUSE_POINTS = "reuse_points";
    private static final String KEY_RECYCLE_POINTS = "recycle_points";

    private SharedPreferences preferences;
    private PostAdapter postAdapter;
    private ThreadPoolExecutor geminiExecutor;
    private LinearLayoutManager layoutManager;

    private String currentQuote;
    private String userRank;
    private int reducePoints;
    private int reusePoints;
    private int recyclePoints;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        preferences = requireContext().getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        int numThreads = Runtime.getRuntime().availableProcessors();
        geminiExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(numThreads);

        if (savedInstanceState != null) {
            restoreInstanceState(savedInstanceState);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        userRankTextView = view.findViewById(R.id.user_rank_text_view);
        quoteForUserTextView = view.findViewById(R.id.quote_for_user_text_view);
        reducePointsTextView = view.findViewById(R.id.reduce_points_text_view);
        reusePointsTextView = view.findViewById(R.id.reuse_points_text_view);
        recyclePointsTextView = view.findViewById(R.id.recycle_points_text_view);
        recyclerView = view.findViewById(R.id.recycler_view);

        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);

        layoutManager = new LinearLayoutManager(requireContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemViewCacheSize(20);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(() -> {
                viewModel.refreshPosts();
            });
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupViewModelObservers();
        loadUserStats();
        viewModel.loadPostsIfNeeded();
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
        swipeRefreshLayout = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (geminiExecutor != null && !geminiExecutor.isShutdown()) {
            geminiExecutor.shutdown();
        }

        if (postAdapter != null) {
            postAdapter.cleanup();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(KEY_CURRENT_QUOTE, currentQuote);
        outState.putString(KEY_USER_RANK, userRank);
        outState.putInt(KEY_REDUCE_POINTS, reducePoints);
        outState.putInt(KEY_REUSE_POINTS, reusePoints);
        outState.putInt(KEY_RECYCLE_POINTS, recyclePoints);
    }

    private void setupViewModelObservers() {
        viewModel.getPosts().observe(getViewLifecycleOwner(), posts -> {
            if (posts != null) {
                updatePostsUI(posts);
                restoreScrollPosition();
            }
        });

        viewModel.getLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setRefreshing(isLoading);
            }
        });

        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(requireContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void restoreInstanceState(@NonNull Bundle savedInstanceState) {
        currentQuote = savedInstanceState.getString(KEY_CURRENT_QUOTE);
        userRank = savedInstanceState.getString(KEY_USER_RANK);
        reducePoints = savedInstanceState.getInt(KEY_REDUCE_POINTS, 0);
        reusePoints = savedInstanceState.getInt(KEY_REUSE_POINTS, 0);
        recyclePoints = savedInstanceState.getInt(KEY_RECYCLE_POINTS, 0);
    }

    private void loadUserStats() {
        String currentUserId = getCurrentUserId();

        if (currentUserId == null || currentUserId.isEmpty()) {
            setDefaultUserStats();
            return;
        }

        UsersRepository usersRepository = new UsersRepository(currentUserId);
        Task<DataSnapshot> task = usersRepository.getUsersRef().get();

        task.addOnSuccessListener(dataSnapshot -> {
            if (dataSnapshot.child("stats").exists()) {
                UserStats userStats = new UserStats();

                userStats.setRecyclePoints(dataSnapshot.child("stats").child("recycle").getValue(Integer.class));
                userStats.setReducePoints(dataSnapshot.child("stats").child("reduce").getValue(Integer.class));
                userStats.setReusePoints(dataSnapshot.child("stats").child("reuse").getValue(Integer.class));

                reducePoints = userStats.getReducePoints();
                reusePoints = userStats.getReusePoints();
                recyclePoints = userStats.getRecyclePoints();
                userRank = calculateUserRank(reducePoints + reusePoints + recyclePoints);

                mainHandler.post(() -> {
                    updateUserStatsUI();
                });

            }
        }).addOnFailureListener(exception -> {
            Log.e("HomeFragment", "Error loading user stats", exception);
            setDefaultUserStats();
            updateUserStatsUI();
        });
    }

    private void updateUserStatsUI() {
        if (!isAdded()) return;

        userRankTextView.setText(userRank);
        reducePointsTextView.setText(String.valueOf(reducePoints));
        reusePointsTextView.setText(String.valueOf(reusePoints));
        recyclePointsTextView.setText(String.valueOf(recyclePoints));

        if (currentQuote == null) {
            loadQuote();
        } else {
            quoteForUserTextView.setText(currentQuote);
        }
    }

    private void setDefaultUserStats() {
        reducePoints = 0;
        reusePoints = 0;
        recyclePoints = 0;
    }

    private String calculateUserRank(int totalPoints) {
        if (totalPoints >= 1000) {
            return "Eco Master";
        } else if (totalPoints >= 500) {
            return "Green Champion";
        } else if (totalPoints >= 200) {
            return "Eco Warrior";
        } else if (totalPoints >= 50) {
            return "Green Helper";
        } else {
            return "Beginner";
        }
    }

    private String getCurrentUserId() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        return currentUser != null ? currentUser.getUid() : null;
    }

    private void loadQuote() {
        String savedQuote = preferences.getString(KEY_QUOTE, null);
        long timestamp = preferences.getLong(KEY_TIMESTAMP, 0);
        long currentTime = System.currentTimeMillis();

        if (savedQuote != null && currentTime - timestamp < QUOTE_VALIDITY_PERIOD) {
            currentQuote = savedQuote;
            if (isAdded() && quoteForUserTextView != null) {
                quoteForUserTextView.setText(currentQuote);
            }
        } else {
            generateNewQuote();
        }
    }

    private void generateNewQuote() {
        if (!isAdded()) return;

        String prompt = "Give me a motivational witty quote about recycling, reusing or reducing";
        Schema schema = Schema.str();

        ListenableFuture<GenerateContentResponse> response = new GeminiClient(schema).generateResult(prompt);
        Futures.addCallback(
                response,
                new FutureCallback<>() {
                    @Override
                    public void onSuccess(GenerateContentResponse result) {
                        if (isAdded()) {
                            mainHandler.post(() -> {
                                if (isAdded() && quoteForUserTextView != null) {
                                    currentQuote = result.getText();
                                    quoteForUserTextView.setText(currentQuote);

                                    preferences.edit()
                                            .putString(KEY_QUOTE, currentQuote)
                                            .putLong(KEY_TIMESTAMP, System.currentTimeMillis())
                                            .apply();
                                }
                            });
                        }
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        t.printStackTrace();
                        if (isAdded()) {
                            mainHandler.post(() -> {
                                if (isAdded() && quoteForUserTextView != null) {
                                    currentQuote = "Every small action counts towards a greener tomorrow!";
                                    quoteForUserTextView.setText(currentQuote);
                                }
                            });
                        }
                    }
                },
                geminiExecutor
        );
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