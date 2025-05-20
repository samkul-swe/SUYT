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
import android.widget.TextView;
import android.widget.Toast;

import edu.northeastern.suyt.R;
import edu.northeastern.suyt.controller.UserController;
import edu.northeastern.suyt.ui.activities.LoginActivity;

public class ProfileFragment extends Fragment {
    private UserController userController;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        userController = new UserController();

        // Initialize views
        TextView usernameTextView = view.findViewById(R.id.username_text_view);
        TextView emailTextView = view.findViewById(R.id.email_text_view);
        Button logoutButton = view.findViewById(R.id.logout_button);

        // Set dummy data for now
        usernameTextView.setText("John Doe");
        emailTextView.setText("john.doe@example.com");

        // Set click listener for logout
        logoutButton.setOnClickListener(v -> showLogoutConfirmatioBox());

        return view;
    }

    private void showLogoutConfirmatioBox() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> performLogout())
                .setNegativeButton("Cancel", null)
                .show();

//        boolean success = userController.logoutUser();
//        if (success) {
//            // Navigate to login screen
//            Intent intent = new Intent(getActivity(), LoginActivity.class);
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//            startActivity(intent);
//            getActivity().finish();
//        }
    }

    public void performLogout(){
        userController.logoutUser(new UserController.LogoutCallback() {
            @Override
            public void onSuccess() {
                if(getActivity() == null || !isAdded()){
                    return;
                }

                Intent intent = new Intent(getActivity(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                getActivity().finish();
            }

            @Override
            public void onFailure(String errorMessage) {
                if(getActivity() == null || isAdded()){
                    return;
                }

                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();

            }
        });

    }

}