package edu.northeastern.suyt.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import edu.northeastern.suyt.R;
import edu.northeastern.suyt.controller.UsersController;
import edu.northeastern.suyt.model.User;
import edu.northeastern.suyt.utils.SessionManager;

public class LoginActivity extends AppCompatActivity {
    private final String TAG = "LoginActivity";
    private EditText emailEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private ProgressBar loadingIndicator;
    private UsersController usersController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Check if already logged in
        if (new SessionManager(this).isLoggedIn()) {
            navigateToMain();
            return;
        }

        usersController = new UsersController(this);

        emailEditText = findViewById(R.id.email_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);
        loginButton = findViewById(R.id.login_button);
        Button signupButton = findViewById(R.id.sign_up_text_view);
        String signupHtmlText = "Don't have an account? <font color='#3344DD'>Sign up here!</font>";
        signupButton.setText(Html.fromHtml(signupHtmlText, Html.FROM_HTML_MODE_LEGACY));
        loadingIndicator = findViewById(R.id.loading_indicator);

        Button forgotPasswordButton = findViewById(R.id.forgot_password);
        String forgotPassword = "<font color='#3344DD'>Forgot Password?</font>";
        forgotPasswordButton.setText(Html.fromHtml(forgotPassword, Html.FROM_HTML_MODE_LEGACY));

        loginButton.setOnClickListener(v -> attemptLogin());
        signupButton.setOnClickListener(v -> navigateToSignUp());
    }

    private void attemptLogin() {
        SessionManager sessionManager = new SessionManager(this);
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

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
        setLoadingIndicatorVisibility(View.VISIBLE);

        try{
            usersController.logInUser(email, password, new UsersController.AuthCallback() {
                @Override
                public void onSuccess(User user) {
                    runOnUiThread(() -> {
                        setLoadingIndicatorVisibility(View.GONE);
                        Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                        // After successful login
                        sessionManager.saveLoginSession();
                        sessionManager.saveUserData(user);
                        navigateToMain();
                    });
                }

                @Override
                public void onFailure(String errorMessage) {
                    runOnUiThread(() -> {
                        setLoadingIndicatorVisibility(View.GONE);
                        loginButton.setEnabled(true);

                        Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        String title;
                        String message;

                        if(errorMessage.contains("Verify Email")){
                            title = "Email Verification Required";
                            message = "Please verify your email before signing in. A new verification email has been sent to the registered email address.";
                        }else if(errorMessage.equals("Invalid Credentials")){
                            title = "Check your email or password";
                            message = "Please verify your email and password. Use 'Sign Up' to register or 'Forgot Password' to reset your password.";
                        }else{
                            Log.e(TAG, "Login failed: " + errorMessage);
                            title = "Login Failed";
                            message = "Error in processing request. Please try again later.";
                        }

                        showErrorDialog(message, title);
                        Log.e(TAG, "Login failed: " + errorMessage);
                    });
                }
            });

        }catch(Exception e){
            loginButton.setEnabled(true);
            setLoadingIndicatorVisibility(View.GONE);
            Toast.makeText(LoginActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showErrorDialog(String errorMessage, String title){
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage(errorMessage)
                .setPositiveButton("OK", (dialog, which) -> {

                })
                .setCancelable(false)
                .show();
    }

    private void navigateToSignUp() {
        Intent intent = new Intent(this, SignUpActivity.class);
        startActivity(intent);
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void setLoadingIndicatorVisibility(int visible) {
        if (loadingIndicator != null) {
            loadingIndicator.setVisibility(visible);
        } else {
            Log.e(TAG, "loadingIndicator is null when trying to set visibility: " + visible);
        }
    }
}