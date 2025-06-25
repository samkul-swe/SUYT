package edu.northeastern.suyt.ui.activities;

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
import edu.northeastern.suyt.controller.UserController;

public class SignUpActivity extends AppCompatActivity {

    private EditText usernameEditText;
    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;
    private Button signUpButton;
    private TextView loginTextView;
    private ProgressBar loadingIndicator;
    private UserController userController;

    private TextView requirementLength;
    private TextView requirementUppercase;
    private TextView requirementNumber;
    private TextView requirementSpecial;
    private boolean isLengthValid = false;
    private boolean isUppercaseValid = false;
    private boolean isNumberValid = false;
    private boolean isSpecialValid = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        userController = new UserController(getApplicationContext());

        usernameEditText = findViewById(R.id.username_edit_text);
        emailEditText = findViewById(R.id.email_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);
        confirmPasswordEditText = findViewById(R.id.confirm_password_edit_text);
        signUpButton = findViewById(R.id.sign_up_button);
        loadingIndicator = findViewById(R.id.loading_indicator);
        loginTextView = findViewById(R.id.login_text_view);
        requirementLength = findViewById(R.id.requirement_length);
        requirementUppercase = findViewById(R.id.requirement_uppercase);
        requirementNumber = findViewById(R.id.requirement_number);
        requirementSpecial = findViewById(R.id.requirement_special);

        signUpButton.setOnClickListener(v -> attemptSignUp());
        String loginText = "Already have an account? <font color='#3344DD'>Login here!</font>";
        loginTextView.setText(Html.fromHtml(loginText, Html.FROM_HTML_MODE_LEGACY));

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
        if (!(isLengthValid && isUppercaseValid && isNumberValid && isSpecialValid)) {
            Toast.makeText(this, "Password does not meet requirements.", Toast.LENGTH_LONG).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match. Please try again.", Toast.LENGTH_LONG).show();
            return;
        }

        signUpButton.setEnabled(false);
        setLoadingIndicatorVisibility(View.VISIBLE);

        Toast.makeText(SignUpActivity.this, "Contacting authentication server...", Toast.LENGTH_SHORT).show();

        try {
            userController.registerUser(username, email, password, new UserController.RegisterCallback() {
                @Override
                public void onSuccess() {
                    new android.os.Handler(getMainLooper()).post(() -> {
                        Log.d("SignUpActivity", "Registration successful");
                        loadingIndicator.setVisibility(View.GONE);
                        Toast.makeText(SignUpActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
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

    private void setLoadingIndicatorVisibility(int visible) {
        if (loadingIndicator != null) {
            loadingIndicator.setVisibility(visible);
        } else {
            Log.e("SignUpActivity", "loadingIndicator is null when trying to set visibility: " + visible);
        }
    }
}