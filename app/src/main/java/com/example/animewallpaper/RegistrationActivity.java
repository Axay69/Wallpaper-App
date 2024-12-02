package com.example.animewallpaper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.animewallpaper.databinding.ActivityRegistrationBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegistrationActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private ActivityRegistrationBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the activity to full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        // Hide action bar if present
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        binding = ActivityRegistrationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();

        // Navigate to LoginActivity
        binding.txtLogin.setOnClickListener(view -> {
            startActivity(new Intent(RegistrationActivity.this, LoginActivity.class));
            finish();
        });

        binding.btnRegister.setOnClickListener(view -> {
            String username = binding.edtUsernameRegister.getText().toString();
            String email = binding.edtEmailIdRegister.getText().toString();
            String pass = binding.edtPasswordRegister.getText().toString();
            String conPass = binding.edtConfPasswordRegister.getText().toString();

            // Validation
            if (username.isEmpty() || email.isEmpty() || pass.isEmpty() || conPass.isEmpty()) {
                Toast.makeText(RegistrationActivity.this, "Fill all the details first", Toast.LENGTH_SHORT).show();
                return;
            } else if (pass.length() < 8) {
                Toast.makeText(RegistrationActivity.this, "Password must have at least 8 characters", Toast.LENGTH_SHORT).show();
                return;
            } else if (!pass.equals(conPass)) {
                Toast.makeText(RegistrationActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            binding.progressBarRegister.setVisibility(View.VISIBLE);

            // Register the user in Firebase Authentication
            firebaseAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                    String userId = firebaseUser != null ? firebaseUser.getUid() : null;

                    if (userId != null) {
                        sendEmailVerification(firebaseUser);

                        FirebaseFirestore db = FirebaseFirestore.getInstance();

                        // Prepare profile data for Firestore
                        Map<String, Object> profileMap = new HashMap<>();
                        profileMap.put("UserId", userId);
                        profileMap.put("UserName", username);
                        profileMap.put("UserEmailId", email);
                        profileMap.put("UserPassword", pass);

                        // Save profile data in Firestore under the user's profile subcollection
                        db.collection("users").document(userId)
                                .collection("profile").document("user_profile").set(profileMap)
                                .addOnCompleteListener(firestoreTask -> {
                                    if (firestoreTask.isSuccessful()) {
                                        binding.progressBarRegister.setVisibility(View.GONE);
                                        Toast.makeText(RegistrationActivity.this, "User registered successfully. Please verify your email", Toast.LENGTH_SHORT).show();
                                        firebaseAuth.signOut(); // Log out the user after registration
                                        startActivity(new Intent(RegistrationActivity.this, LoginActivity.class));
                                        finish();
                                    } else {
                                        binding.progressBarRegister.setVisibility(View.GONE);
                                        Toast.makeText(RegistrationActivity.this, "Error occurred while saving user data", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        binding.progressBarRegister.setVisibility(View.GONE);
                        Toast.makeText(RegistrationActivity.this, "Failed to get user ID", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    binding.progressBarRegister.setVisibility(View.GONE);
                    Toast.makeText(RegistrationActivity.this, "Failed to register user " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void sendEmailVerification(FirebaseUser firebaseUser) {
        firebaseUser.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(RegistrationActivity.this, "Verification email sent. Please check your inbox.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(RegistrationActivity.this, "Failed to send verification email.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
