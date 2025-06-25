package edu.northeastern.suyt.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import edu.northeastern.suyt.R;
import edu.northeastern.suyt.controller.UserController;

public class ChangePasswordDialog extends Dialog implements View.OnClickListener {
    private EditText currentPasswordEditText;
    private EditText newPasswordEditText;
    private EditText confirmPasswordEditText;
    private Button saveButton;
    private Button cancelButton;

    private OnPasswordChangedListener listener;
    private UserController userController;

    public interface OnPasswordChangedListener {
        void onPasswordChanged(boolean success);
    }

    public ChangePasswordDialog(@NonNull Context context, OnPasswordChangedListener listener) {
        super(context);
        this.listener = listener;
        this.userController = new UserController(getContext());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_change_password);

        // Initialize views
        currentPasswordEditText = findViewById(R.id.current_password_edit_text);
        newPasswordEditText = findViewById(R.id.new_password_edit_text);
        confirmPasswordEditText = findViewById(R.id.confirm_password_edit_text);
        saveButton = findViewById(R.id.save_button);
        cancelButton = findViewById(R.id.cancel_button);

        // Set click listeners
        saveButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.save_button) {
            attemptPasswordChange();
        } else if (v.getId() == R.id.cancel_button) {
            dismiss();
        }
    }

    private void attemptPasswordChange() {
        // Get inputs
        String currentPassword = currentPasswordEditText.getText().toString().trim();
        String newPassword = newPasswordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        // Validate inputs
        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newPassword.equals(currentPassword)) {
            Toast.makeText(getContext(), "New password must be different from current password", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(getContext(), "New passwords don't match", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newPassword.length() < 6) {
            Toast.makeText(getContext(), "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        // In a real app, validate the current password and update the new password in the database
        // For now, just simulate a successful update
        boolean success = true;
//                userController.updateUserPassword(currentPassword, newPassword);

        if (success) {
            // Notify listener of successful change
            if (listener != null) {
                listener.onPasswordChanged(true);
            }

            // Dismiss dialog
            dismiss();
        } else {
            Toast.makeText(getContext(), "Failed to update password. Please check your current password and try again.", Toast.LENGTH_SHORT).show();

            // Notify listener of failed change
            if (listener != null) {
                listener.onPasswordChanged(false);
            }
        }
    }
}
