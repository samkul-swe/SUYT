package edu.northeastern.suyt.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;

import edu.northeastern.suyt.R;
import edu.northeastern.suyt.controller.UserController;

public class ForgotPassword extends AppCompatActivity {

    private TextInputEditText emailEditText;
    private Button sendResetLinkButton;
    private Button backToLoginButton;
    private ProgressBar loadingIndicator;
    private UserController userController;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_password);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        userController = new UserController(getApplicationContext());
        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        emailEditText = findViewById(R.id.email_edit_text);
        sendResetLinkButton = findViewById(R.id.send_reset_link_button);
        backToLoginButton = findViewById(R.id.back_to_login_button);
        String signupHtmlText = "<font color='#3344DD'>Back To Login</font>";
        backToLoginButton.setText(Html.fromHtml(signupHtmlText, Html.FROM_HTML_MODE_LEGACY));
        loadingIndicator = findViewById(R.id.loading_indicator);
    }

    private void setupClickListeners() {
        sendResetLinkButton.setOnClickListener(v -> sendPasswordResetEmail());
        backToLoginButton.setOnClickListener(v -> goBackToLogin());
    }

    private void sendPasswordResetEmail() {
        String email = emailEditText.getText().toString().trim();

        if (email.isEmpty()) {
            emailEditText.setError("Email is required");
            emailEditText.requestFocus();
            return;
        }

        setLoadingState(true);

        userController.sendPasswordResetEmail(email, new UserController.PasswordResetCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    setLoadingState(false);
                    showSuccessDialog(email);
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                runOnUiThread(() -> {
                    setLoadingState(false);
                    showErrorDialog(errorMessage);
                });
            }
        });
    }

    private void showSuccessDialog(String email) {
        new AlertDialog.Builder(this)
                .setTitle("Reset Link Sent")
                .setMessage("We've sent a password reset link to " + email + " if it's associated with an account." +
                        "\n\nPlease check your email (including spam folder) and follow the instructions.")
                .setPositiveButton("OK", (dialog, which) -> {
                    goBackToLogin();
                })
                .setCancelable(false)
                .show();
    }

    private void showErrorDialog(String errorMessage) {
        new AlertDialog.Builder(this)
                .setTitle("Reset Failed")
                .setMessage(errorMessage)
                .setPositiveButton("OK", null)
                .show();
    }

    private void setLoadingState(boolean isLoading) {
        loadingIndicator.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        sendResetLinkButton.setEnabled(!isLoading);
        emailEditText.setEnabled(!isLoading);

        if (isLoading) {
            sendResetLinkButton.setText("Sending...");
        } else {
            sendResetLinkButton.setText("Send Reset Link");
        }
    }

    private void goBackToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}