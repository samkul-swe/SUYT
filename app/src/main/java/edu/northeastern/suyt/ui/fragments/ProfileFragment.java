package edu.northeastern.suyt.ui.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import edu.northeastern.suyt.R;
import edu.northeastern.suyt.controller.UserController;
import edu.northeastern.suyt.ui.activities.CreatePostActivity;
import edu.northeastern.suyt.ui.activities.LoginActivity;
import edu.northeastern.suyt.ui.activities.SavedPostsActivity;
import edu.northeastern.suyt.ui.dialogs.ChangeEmailDialog;
import edu.northeastern.suyt.ui.dialogs.ChangePasswordDialog;

public class ProfileFragment extends Fragment {
    private ImageView profileImageView;
    private TextView usernameTextView;
    private TextView emailTextView;
    private TextView rankTextView;
    private Button changeEmailButton;
    private Button changePasswordButton;
    private Button savedPostsButton;
    private Button createPostButton;
    private Button logoutButton;

    private UserController userController;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        userController = new UserController();

        // Initialize views
        profileImageView = view.findViewById(R.id.profile_image_view);
        usernameTextView = view.findViewById(R.id.username_text_view);
        emailTextView = view.findViewById(R.id.email_text_view);
        rankTextView = view.findViewById(R.id.rank_text_view);
        changeEmailButton = view.findViewById(R.id.change_email_button);
        changePasswordButton = view.findViewById(R.id.change_password_button);
        savedPostsButton = view.findViewById(R.id.saved_posts_button);
        createPostButton = view.findViewById(R.id.create_post_button);
        logoutButton = view.findViewById(R.id.logout_button);

        // Set profile data
        loadProfileData();

        // Set button click listeners
        setupButtonListeners();

        return view;
    }

    private void loadProfileData() {
        // In a real app, this would fetch the user's profile from a database
        // For now, use sample data
        usernameTextView.setText("EcoWarrior42");
        emailTextView.setText("eco.warrior@example.com");
        rankTextView.setText("Rank: Eco Warrior");

        // Set profile image (in a real app, load from user's stored image)
        profileImageView.setImageResource(R.drawable.placeholder_profile);
    }

    private void setupButtonListeners() {
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
            logout();
        });
    }

    private void showChangeEmailDialog() {
        // In a real app, show a dialog to change email
        ChangeEmailDialog dialog = new ChangeEmailDialog(requireContext(),
                emailTextView.getText().toString(),
                newEmail -> {
                    // Update email in database and UI
                    emailTextView.setText(newEmail);
                    Toast.makeText(requireContext(), "Email updated", Toast.LENGTH_SHORT).show();
                });
        dialog.show();
    }

    private void showChangePasswordDialog() {
        // In a real app, show a dialog to change password
        ChangePasswordDialog dialog = new ChangePasswordDialog(requireContext(),
                success -> {
                    if (success) {
                        Toast.makeText(requireContext(), "Password updated", Toast.LENGTH_SHORT).show();
                    }
                });
        dialog.show();
    }

    private void openSavedPosts() {
        // In a real app, open the saved posts screen
        Intent intent = new Intent(requireContext(), SavedPostsActivity.class);
        startActivity(intent);
    }

    private void openCreatePost() {
        // In a real app, open the create post screen
        Intent intent = new Intent(requireContext(), CreatePostActivity.class);
        startActivity(intent);
    }

    private void logout() {
        // In a real app, log the user out and navigate to login screen
        boolean success = true;
//                userController.logoutUser();
        if (success) {
            // Navigate to login screen
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            getActivity().finish();
        }
    }

}