package edu.northeastern.suyt.ui.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import edu.northeastern.suyt.R;
import edu.northeastern.suyt.model.AnalysisResult;
import edu.northeastern.suyt.ui.activities.CreatePostActivity;
import edu.northeastern.suyt.ui.activities.LoginActivity;
import edu.northeastern.suyt.ui.activities.SavedPostsActivity;
import edu.northeastern.suyt.ui.activities.RecentAnalysisActivity;
import edu.northeastern.suyt.ui.viewmodel.ProfileViewModel;
import edu.northeastern.suyt.utils.SessionManager;

public class ProfileFragment extends Fragment {
    private ImageView profileImageView;
    private TextView usernameTextView;
    private TextView emailTextView;
    private TextView rankTextView;
    private Button recentAnalysisButton;
    private LinearLayout savedPostsButton;
    private Button createPostButton;
    private Button logoutButton;
    private Button deleteAccountButton;

    private ProfileViewModel viewModel;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        sessionManager = new SessionManager(requireContext());
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        viewModel.initialize(sessionManager);

        initializeViews(view);
        setupViewModelObservers();
        setupButtonListeners();

        return view;
    }

    private void initializeViews(View view) {
        profileImageView = view.findViewById(R.id.profile_image_view);
        usernameTextView = view.findViewById(R.id.username_text_view);
        emailTextView = view.findViewById(R.id.email_text_view);
        rankTextView = view.findViewById(R.id.rank_text_view);

        recentAnalysisButton = view.findViewById(R.id.recent_analysis_button);
        savedPostsButton = view.findViewById(R.id.saved_posts_button);
        createPostButton = view.findViewById(R.id.create_post_button);
        logoutButton = view.findViewById(R.id.logout_button);
        deleteAccountButton = view.findViewById(R.id.delete_account_button);

        // Set placeholder profile image
        profileImageView.setImageResource(R.drawable.placeholder_profile);
    }

    private void setupViewModelObservers() {
        viewModel.getUsername().observe(getViewLifecycleOwner(), username -> {
            if (username != null) {
                usernameTextView.setText(username);
            }
        });

        viewModel.getEmail().observe(getViewLifecycleOwner(), email -> {
            if (email != null) {
                emailTextView.setText(email);
            }
        });

        viewModel.getRank().observe(getViewLifecycleOwner(), rank -> {
            if (rank != null) {
                rankTextView.setText(rank);
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show();
                viewModel.clearErrorMessage();
            }
        });

        viewModel.getRecentAnalysis().observe(getViewLifecycleOwner(), analysis -> {
            if (analysis != null) {
                openRecentAnalysisActivity(analysis);
            }
        });

        viewModel.getIsAnalysisLoading().observe(getViewLifecycleOwner(), isLoading -> {
            recentAnalysisButton.setEnabled(!isLoading);
            recentAnalysisButton.setText(isLoading ? "Loading..." : "My Recent Analysis");
        });

        viewModel.getIsLogoutLoading().observe(getViewLifecycleOwner(), isLoading -> {
            logoutButton.setEnabled(!isLoading);
            logoutButton.setText(isLoading ? "Signing Out..." : "Sign Out");
        });

        viewModel.getEmailUpdateSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success != null) {
                String message = success ? "Email updated successfully" : "Failed to update email";
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                viewModel.clearSuccessFlags();
            }
        });

        viewModel.getPasswordUpdateSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success != null) {
                String message = success ? "Password updated successfully" : "Failed to update password";
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                viewModel.clearSuccessFlags();
            }
        });

        viewModel.getLogoutSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                if (getActivity() != null) {
                    getActivity().finish();
                }
                Toast.makeText(getActivity(), "Signed out successfully", Toast.LENGTH_SHORT).show();
                viewModel.clearSuccessFlags();
            }
        });
    }

    private void setupButtonListeners() {
        recentAnalysisButton.setOnClickListener(v -> viewModel.loadRecentAnalysis());

        savedPostsButton.setOnClickListener(v -> openSavedPosts());

        createPostButton.setOnClickListener(v -> openCreatePost());

        logoutButton.setOnClickListener(v -> showLogoutConfirmationDialog());

        deleteAccountButton.setOnClickListener(v -> showDeleteAccountConfirmationDialog());
    }

    private void openRecentAnalysisActivity(AnalysisResult analysis) {
        Intent intent = new Intent(requireContext(), RecentAnalysisActivity.class);
        intent.putExtra("analysis_result", analysis);
        startActivity(intent);
    }

    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(requireContext())
            .setTitle("Sign Out")
            .setMessage("Are you sure you want to sign out of your account?")
            .setPositiveButton("Sign Out", (dialog, which) -> viewModel.logout())
            .setNegativeButton("Cancel", null)
            .setCancelable(true)
            .show();
    }

    private void showDeleteAccountConfirmationDialog() {
        new AlertDialog.Builder(requireContext())
            .setTitle("Delete Account")
            .setMessage("Are you sure you want to delete your account?")
            .setPositiveButton("Delete", (dialog, which) -> viewModel.deleteAccount(requireContext()))
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

    @Override
    public void onResume() {
        super.onResume();
        viewModel.loadProfileData();
    }
}