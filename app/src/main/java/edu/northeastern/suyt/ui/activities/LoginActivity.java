package edu.northeastern.suyt.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import edu.northeastern.suyt.R;
import edu.northeastern.suyt.controller.UserController;
import edu.northeastern.suyt.model.User;

public class LoginActivity extends AppCompatActivity {
    private EditText emailEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private TextView signUpTextView, forgotPasswordTextView;
    private UserController userController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userController = new UserController();

        if (userController.isUserSignedIn()) {
            navigateToMain();
            return;
        }

        // Initialize views
        emailEditText = findViewById(R.id.email_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);
        loginButton = findViewById(R.id.login_button);
        signUpTextView = findViewById(R.id.sign_up_text_view);
        String signupHtmlText = "Don't have an account? <font color='#3344DD'>Sign up here!</font>";
        signUpTextView.setText(Html.fromHtml(signupHtmlText, Html.FROM_HTML_MODE_LEGACY));

        forgotPasswordTextView = findViewById(R.id.forgot_password);
        String forgotPassword = "<font color='#3344DD'>Forgot Password?</font>";
        forgotPasswordTextView.setText(Html.fromHtml(forgotPassword, Html.FROM_HTML_MODE_LEGACY));

        // Set click listeners
        loginButton.setOnClickListener(v -> attemptLogin());

        signUpTextView.setOnClickListener(v -> navigateToSignUp());
    }

    private void attemptLogin() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Validate inputs
        if (email.isEmpty()) {
            emailEditText.setError("Email is required");
            emailEditText.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            passwordEditText.setError("Password is required");
            passwordEditText.requestFocus();
            return;
        }

        loginButton.setEnabled(false);

        // Attempt login
        userController.signInUser(email, password, new UserController.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                runOnUiThread(() -> {
                    // Hide progress
//                    if (progressBar != null) {
//                        progressBar.setVisibility(View.GONE);
//                    }

                    // Login successful
                    Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                    navigateToMain();
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                runOnUiThread(() -> {
                    // Hide progress
//                    if (progressBar != null) {
//                        progressBar.setVisibility(View.GONE);
//                    }

                    // Re-enable login button
                    loginButton.setEnabled(true);

                    // Show error message
                    Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    Log.e("TAG", "Login failed: " + errorMessage);
                });
            }
        });
    }

    private void navigateToSignUp() {
        Intent intent = new Intent(this, SignUpActivity.class);
        startActivity(intent);
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish(); // This will close the login activity so user can't go back
    }
}