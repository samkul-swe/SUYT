package edu.northeastern.suyt.ui.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import edu.northeastern.suyt.R;
import edu.northeastern.suyt.controller.UserController;
import edu.northeastern.suyt.controller.AnalysisController;
import edu.northeastern.suyt.model.AnalysisResult;
import edu.northeastern.suyt.ui.activities.CreatePostActivity;
import edu.northeastern.suyt.ui.activities.LoginActivity;
import edu.northeastern.suyt.ui.activities.SavedPostsActivity;
import edu.northeastern.suyt.ui.activities.RecentAnalysisActivity;
import edu.northeastern.suyt.ui.dialogs.ChangeEmailDialog;
import edu.northeastern.suyt.ui.dialogs.ChangePasswordDialog;

public class ProfileFragment extends Fragment {
    // UI Components
    private ImageView profileImageView;
    private TextView usernameTextView;
    private TextView emailTextView;
    private TextView rankTextView;
    private Button recentAnalysisButton;
    private LinearLayout changeEmailButton;
    private LinearLayout changePasswordButton;
    private LinearLayout savedPostsButton;
    private Button createPostButton;
    private Button logoutButton;

    // Stats TextViews (for the new UI)
    private TextView postsCountTextView;
    private TextView impactCountTextView;
    private TextView savedCountTextView;

    // Controllers
    private UserController userController;
    private AnalysisController analysisController;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize controllers
        userController = new UserController();
        analysisController = new AnalysisController();

        // Initialize views
        initializeViews(view);

        // Set profile data
        loadProfileData();

        // Load user statistics
        loadUserStats();

        // Set button click listeners
        setupButtonListeners();

        return view;
    }

    private void initializeViews(View view) {
        // Profile header views
        profileImageView = view.findViewById(R.id.profile_image_view);
        usernameTextView = view.findViewById(R.id.username_text_view);
        emailTextView = view.findViewById(R.id.email_text_view);
        rankTextView = view.findViewById(R.id.rank_text_view);

        // Stats views (new UI components)
        postsCountTextView = view.findViewById(R.id.posts_count_text_view);
        impactCountTextView = view.findViewById(R.id.impact_count_text_view);
        savedCountTextView = view.findViewById(R.id.saved_count_text_view);

        // Button views
        recentAnalysisButton = view.findViewById(R.id.recent_analysis_button);
        changeEmailButton = view.findViewById(R.id.change_email);
        changePasswordButton = view.findViewById(R.id.change_password);
        savedPostsButton = view.findViewById(R.id.saved_posts_button);
        createPostButton = view.findViewById(R.id.create_post_button);
        logoutButton = view.findViewById(R.id.logout_button);
    }

    private void loadProfileData() {
        // Get current user data from UserController
        userController.getCurrentUser(new UserController.UserDataCallback() {
            @Override
            public void onSuccess(String username, String email, String rank) {
                if (getActivity() == null || !isAdded()) {
                    return;
                }

                usernameTextView.setText(username != null ? username : "EcoWarrior42");
                emailTextView.setText(email != null ? email : "eco.warrior@example.com");
                rankTextView.setText(rank != null ? rank : "Eco Warrior");

                // Set profile image (in a real app, load from user's stored image)
                profileImageView.setImageResource(R.drawable.placeholder_profile);
            }

            @Override
            public void onFailure(String errorMessage) {
                if (getActivity() == null || !isAdded()) {
                    return;
                }
                // Fallback to default values
                usernameTextView.setText("EcoWarrior42");
                emailTextView.setText("eco.warrior@example.com");
                rankTextView.setText("Eco Warrior");
                profileImageView.setImageResource(R.drawable.placeholder_profile);
            }
        });
    }

    private void loadUserStats() {
        // Load user statistics for the stats cards
        userController.getUserStats(new UserController.UserStatsCallback() {
            @Override
            public void onSuccess(int postsCount, int impactScore, int savedCount) {
                if (getActivity() == null || !isAdded()) {
                    return;
                }

                if (postsCountTextView != null) {
                    postsCountTextView.setText(String.valueOf(postsCount));
                }
                if (impactCountTextView != null) {
                    impactCountTextView.setText(formatCount(impactScore));
                }
                if (savedCountTextView != null) {
                    savedCountTextView.setText(String.valueOf(savedCount));
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                if (getActivity() == null || !isAdded()) {
                    return;
                }
                // Set default values if loading fails
                if (postsCountTextView != null) postsCountTextView.setText("0");
                if (impactCountTextView != null) impactCountTextView.setText("0");
                if (savedCountTextView != null) savedCountTextView.setText("0");
            }
        });
    }

    private String formatCount(int count) {
        // Format large numbers (e.g., 1200 -> 1.2k)
        if (count >= 1000000) {
            return String.format("%.1fM", count / 1000000.0);
        } else if (count >= 1000) {
            return String.format("%.1fk", count / 1000.0);
        } else {
            return String.valueOf(count);
        }
    }

    private void setupButtonListeners() {
        // Recent Analysis Button (NEW)
        recentAnalysisButton.setOnClickListener(v -> {
            openRecentAnalysis();
        });

        // Change Email Button
        changeEmailButton.setOnClickListener(v -> {
            showChangeEmailDialog();
        });

        // Change Password Button
        changePasswordButton.setOnClickListener(v -> {
            showChangePasswordDialog();
        });

        // Saved Posts Button
        savedPostsButton.setOnClickListener(v -> {
            openSavedPosts();
        });

        // Create Post Button
        createPostButton.setOnClickListener(v -> {
            openCreatePost();
        });

        // Logout Button
        logoutButton.setOnClickListener(v -> {
            showLogoutConfirmationDialog();
        });
    }

    private void openRecentAnalysis() {
        // Show loading state
        recentAnalysisButton.setEnabled(false);
        recentAnalysisButton.setText("Loading...");

        // Get user's recent analysis from database
        analysisController.getUserRecentAnalysis(new AnalysisController.RecentAnalysisCallback() {
            @Override
            public void onSuccess(AnalysisResult recentAnalysis) {
                if (getActivity() == null || !isAdded()) {
                    return;
                }

                // Reset button state
                recentAnalysisButton.setEnabled(true);
                recentAnalysisButton.setText("My Recent Analysis");

                if (recentAnalysis != null) {
                    // Open RecentAnalysisActivity with the analysis data
                    Intent intent = new Intent(requireContext(), RecentAnalysisActivity.class);
                    intent.putExtra("analysis_result", recentAnalysis); // Pass the entire AnalysisResult object
                    startActivity(intent);
                } else {
                    // No analysis found
                    Toast.makeText(requireContext(),
                            "No recent analysis found. Start analyzing items to see your results here!",
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                if (getActivity() == null || !isAdded()) {
                    return;
                }

                // Reset button state
                recentAnalysisButton.setEnabled(true);
                recentAnalysisButton.setText("My Recent Analysis");

                Toast.makeText(requireContext(),
                        "Failed to load recent analysis: " + errorMessage,
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showChangeEmailDialog() {
        ChangeEmailDialog dialog = new ChangeEmailDialog(requireContext(),
                emailTextView.getText().toString(),
                newEmail -> {
                    // Update email in database and UI
                    userController.updateUserEmail(newEmail, new UserController.UpdateCallback() {
                        @Override
                        public void onSuccess() {
                            if (getActivity() == null || !isAdded()) {
                                return;
                            }
                            emailTextView.setText(newEmail);
                            Toast.makeText(requireContext(), "Email updated successfully", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(String errorMessage) {
                            if (getActivity() == null || !isAdded()) {
                                return;
                            }
                            Toast.makeText(requireContext(), "Failed to update email: " + errorMessage, Toast.LENGTH_LONG).show();
                        }
                    });
                });
        dialog.show();
    }

    private void showChangePasswordDialog() {
        ChangePasswordDialog dialog = new ChangePasswordDialog(requireContext(),
                success -> {
                    if (getActivity() == null || !isAdded()) {
                        return;
                    }

                    if (success) {
                        Toast.makeText(requireContext(), "Password updated successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), "Failed to update password", Toast.LENGTH_SHORT).show();
                    }
                });
        dialog.show();
    }

    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Sign Out")
                .setMessage("Are you sure you want to sign out of your account?")
                .setPositiveButton("Sign Out", (dialog, which) -> logout())
                .setNegativeButton("Cancel", null)
                .setCancelable(true)
                .show();
    }

    private void openSavedPosts() {
        Intent intent = new Intent(requireContext(), SavedPostsActivity.class);
        startActivity(intent);
    }

    private void openCreatePost() {
        Intent intent = new Intent(requireContext(), CreatePostActivity.class);
        startActivity(intent);
    }

    private void logout() {
        // Show logout progress
        logoutButton.setEnabled(false);
        logoutButton.setText("Signing Out...");

        userController.logoutUser(new UserController.LogoutCallback() {
            @Override
            public void onSuccess() {
                if (getActivity() == null || !isAdded()) {
                    return;
                }

                Intent intent = new Intent(getActivity(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                getActivity().finish();

                Toast.makeText(getActivity(), "Signed out successfully", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(String errorMessage) {
                if (getActivity() == null || !isAdded()) {
                    return;
                }

                // Reset logout button
                logoutButton.setEnabled(true);
                logoutButton.setText("Sign Out");

                Toast.makeText(getContext(), "Logout failed: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh data when returning to this fragment
        loadProfileData();
        loadUserStats();
    }
}