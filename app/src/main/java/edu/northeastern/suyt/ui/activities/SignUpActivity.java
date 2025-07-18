package edu.northeastern.suyt.ui.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import edu.northeastern.suyt.R;
import edu.northeastern.suyt.controller.UsersController;
import edu.northeastern.suyt.utils.SessionManager;

public class SignUpActivity extends AppCompatActivity {

    private EditText usernameEditText;
    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;
    private Button signUpButton;
    private ProgressBar loadingIndicator;
    private UsersController usersController;

    private TextView requirementLength;
    private TextView requirementUppercase;
    private TextView requirementNumber;
    private TextView requirementSpecial;
    private TextView requirementFormat;
    private boolean isLengthValid = false;
    private boolean isUppercaseValid = false;
    private boolean isNumberValid = false;
    private boolean isSpecialValid = false;
    private boolean isValidFormat = false;
    private boolean isUsernameLengthValid = false;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        usersController = new UsersController(this);

        usernameEditText = findViewById(R.id.username_edit_text);
        emailEditText = findViewById(R.id.email_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);
        confirmPasswordEditText = findViewById(R.id.confirm_password_edit_text);
        signUpButton = findViewById(R.id.sign_up_button);
        loadingIndicator = findViewById(R.id.loading_indicator);
        TextView loginTextView = findViewById(R.id.login_text_view);
        requirementLength = findViewById(R.id.requirement_length);
        requirementUppercase = findViewById(R.id.requirement_uppercase);
        requirementNumber = findViewById(R.id.requirement_number);
        requirementSpecial = findViewById(R.id.requirement_special);
        requirementFormat = findViewById(R.id.requirement_format);

        signUpButton.setOnClickListener(v -> attemptSignUp());
        String loginText = "Already have an account? <font color='#3344DD'>Login here!</font>";
        loginTextView.setText(Html.fromHtml(loginText, Html.FROM_HTML_MODE_LEGACY));

        usernameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //does nothing
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //does nothing
            }
            @Override
            public void afterTextChanged(Editable s) {
                validateUserName(s.toString());
            }
        });

        passwordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //does nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //does nothing
            }

            @Override
            public void afterTextChanged(Editable s) {
                validatePassword(s.toString());
            }
        });

        signUpButton.setOnClickListener(v -> attemptSignUp());

        loginTextView.setOnClickListener(v -> navigateToLogin());
    }

    private void validateUserName(String username) {
        int validColor = ContextCompat.getColor(this, R.color.passwordValid);
        int invalidColor = ContextCompat.getColor(this, R.color.darkGray);

        isUsernameLengthValid = !username.isEmpty();
        requirementLength.setTextColor(isLengthValid ? validColor : invalidColor);

        isValidFormat = isValidUsernameFormat(username);
        requirementFormat.setTextColor(isValidFormat ? validColor : invalidColor);
    }

    private boolean isValidUsernameFormat(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }

        String trimmed = username.trim();

        if (trimmed.length() < 3 || trimmed.length() > 30) {
            return false;
        }
        return trimmed.matches("^[a-zA-Z0-9][a-zA-Z0-9._-]*$");
    }

    private void validatePassword(String password) {
        int validColor = ContextCompat.getColor(this, R.color.passwordValid);
        int invalidColor = ContextCompat.getColor(this, R.color.darkGray);

        isLengthValid = password.length() >= 8;
        requirementLength.setTextColor(isLengthValid ? validColor : invalidColor);

        isUppercaseValid = password.matches(".*[A-Z].*");
        requirementUppercase.setTextColor(isUppercaseValid ? validColor : invalidColor);

        isNumberValid = password.matches(".*[0-9].*");
        requirementNumber.setTextColor(isNumberValid ? validColor : invalidColor);

        isSpecialValid = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*");
        requirementSpecial.setTextColor(isSpecialValid ? validColor : invalidColor);

        updateSignUpButton();
    }

    private void updateSignUpButton() {
        boolean isPasswordValid = isLengthValid && isUppercaseValid && isNumberValid && isSpecialValid;
        signUpButton.setEnabled(isPasswordValid);
    }

    private void attemptSignUp() {
        if (usernameEditText == null || emailEditText == null || passwordEditText == null || confirmPasswordEditText == null) {
            Log.e("SignUpActivity", "One or more EditText views are null");
            return;
        }

        String username = usernameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(SignUpActivity.this, "Incomplete form. Please fill in all the details.", Toast.LENGTH_LONG).show();
            return;
        }
        if (!isValidFormat && !isUsernameLengthValid) {
            Toast.makeText(SignUpActivity.this, "Username does not meet requirements.", Toast.LENGTH_LONG).show();
            return;
        }
        if (!(isLengthValid && isUppercaseValid && isNumberValid && isSpecialValid)) {
            Toast.makeText(this, "Password does not meet requirements.", Toast.LENGTH_LONG).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match. Please try again.", Toast.LENGTH_LONG).show();
            return;
        }

        signUpButton.setEnabled(false);
        setLoadingIndicatorVisibility();

        Toast.makeText(SignUpActivity.this, "Contacting authentication server...", Toast.LENGTH_SHORT).show();

        try {
            usersController.registerUser(username, email, password, new UsersController.RegisterCallback() {
                @Override
                public void onSuccess() {
                    new android.os.Handler(getMainLooper()).post(() -> {
                        Log.d("SignUpActivity", "Registration successful");
                        loadingIndicator.setVisibility(View.GONE);
                        Toast.makeText(SignUpActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                        SessionManager sessionManager = new SessionManager(SignUpActivity.this);
                        Log.d("SignUpActivity", "Session manager : " + sessionManager.getUsername());
                        navigateToHome();
                    });
                }
                @Override
                public void onFailure(String errorMessage) {
                    new android.os.Handler(getMainLooper()).post(() -> {
                        loadingIndicator.setVisibility(View.GONE);
                        signUpButton.setEnabled(true);
                        Toast.makeText(SignUpActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    });
                }
            });
        } catch (Exception e) {
            signUpButton.setEnabled(true);
            loadingIndicator.setVisibility(View.GONE);
            Toast.makeText(SignUpActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void navigateToHome() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void navigateToLogin() {
        finish();
    }

    private void setLoadingIndicatorVisibility() {
        if (loadingIndicator != null) {
            loadingIndicator.setVisibility(View.VISIBLE);
        } else {
            Log.e("SignUpActivity", "loadingIndicator is null when trying to set visibility: " + View.VISIBLE);
        }
    }
}