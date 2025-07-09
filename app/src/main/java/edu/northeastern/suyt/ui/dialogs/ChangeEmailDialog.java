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
import edu.northeastern.suyt.model.User;
import edu.northeastern.suyt.utils.UtilityClass;

public class ChangeEmailDialog extends Dialog implements View.OnClickListener {

    private EditText currentEmailEditText;
    private EditText newEmailEditText;
    private EditText passwordEditText;
    private Button saveButton;
    private Button cancelButton;

    private String currentEmail;
    private OnEmailChangedListener listener;
    private UserController userController;
    private UtilityClass utility;

    public interface OnEmailChangedListener {
        void onEmailChanged(String newEmail);
    }

    public ChangeEmailDialog(@NonNull Context context, String currentEmail, OnEmailChangedListener listener) {
        super(context);
        this.currentEmail = currentEmail;
        this.listener = listener;
        User currentUser = utility.getUser(context);
        this.userController = new UserController(currentUser.getUserId());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_change_email);

        // Initialize views
        currentEmailEditText = findViewById(R.id.current_email_edit_text);
        newEmailEditText = findViewById(R.id.new_email_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);
        saveButton = findViewById(R.id.save_button);
        cancelButton = findViewById(R.id.cancel_button);

        // Set current email
        currentEmailEditText.setText(currentEmail);

        // Set click listeners
        saveButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.save_button) {
            attemptEmailChange();
        } else if (v.getId() == R.id.cancel_button) {
            dismiss();
        }
    }

    private void attemptEmailChange() {
        // Get inputs
        String currentEmail = currentEmailEditText.getText().toString().trim();
        String newEmail = newEmailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Validate inputs
        if (newEmail.isEmpty()) {
            Toast.makeText(getContext(), "Please enter a new email", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.isEmpty()) {
            Toast.makeText(getContext(), "Please enter your password", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newEmail.equals(currentEmail)) {
            Toast.makeText(getContext(), "New email must be different from current email", Toast.LENGTH_SHORT).show();
            return;
        }

        // In a real app, validate the password and update the email in the database
        // For now, just simulate a successful update
        boolean success = true;
//                userController.updateUserEmail(newEmail, password);

        if (success) {
            // Notify listener of successful change
            if (listener != null) {
                listener.onEmailChanged(newEmail);
            }

            // Dismiss dialog
            dismiss();
        } else {
            Toast.makeText(getContext(), "Failed to update email. Please check your password and try again.", Toast.LENGTH_SHORT).show();
        }
    }
}
