package com.example.animewallpaper;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.animewallpaper.databinding.ActivityLoginBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onStart() {
        super.onStart();
        checkCurrentUser();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFullScreenMode();
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();

        setupClickListeners();
    }

    private void setFullScreenMode() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
    }

    private void checkCurrentUser() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            navigateToHome();
        }
    }

    private void setupClickListeners() {
        binding.txtSignUp.setOnClickListener(view -> {
            navigateToRegistration();
        });

        binding.btnLogin.setOnClickListener(view -> {
            handleLogin();
        });

        binding.btnForgotPasswordLogin.setOnClickListener(view -> {
            handlePasswordReset();
        });
    }

    private void handleLogin() {
        String email = binding.edtEmailIdLogin.getText().toString().trim();
        String password = binding.edtPasswordLogin.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            showToast("Fill all the details first");
            return;
        }

        showProgressBar(true);

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    showProgressBar(false);
                    if (task.isSuccessful()) {

                        if (firebaseAuth.getCurrentUser() != null && firebaseAuth.getCurrentUser().isEmailVerified()) {
                            // If email is verified, allow login
                            showToast("Login Successful");
                            navigateToHome();
                        } else {
                            // If email is not verified
                            showToast("Please verify your email address before logging in.");
                            firebaseAuth.signOut(); // Sign out the user
                        }
                    } else {
                        showToast("Failed: " + task.getException().getMessage());
                    }
                });
    }

    private void handlePasswordReset() {
        String email = binding.edtEmailIdLogin.getText().toString().trim();
        if (email.isEmpty()) {
            showToast("Enter your email to reset password");
            return;
        }

        showProgressBar(true);

        firebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    showProgressBar(false);
                    if (task.isSuccessful()) {
                        showToast("Password reset email sent");
                    } else {
                        showToast("Failed: " + task.getException().getMessage());
                    }
                });
    }

    private void navigateToHome() {
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        finish();
    }

    private void navigateToRegistration() {
        startActivity(new Intent(LoginActivity.this, RegistrationActivity.class));
        finish();
    }

    private void showToast(String message) {
        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    private void showProgressBar(boolean show) {
        binding.progressBarLogin.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}
