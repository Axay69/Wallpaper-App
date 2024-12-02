package com.example.animewallpaper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.animewallpaper.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser == null) {
            Toast.makeText(this, "User is Null", Toast.LENGTH_LONG).show();
            return;
        }

        binding.btmnMainActivity.setOnNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            String fragmentTag = "";

            switch (item.getItemId()) {
                case R.id.item_home:
                    fragmentTag = "HOME_FRAGMENT";
                    selectedFragment = getSupportFragmentManager().findFragmentByTag(fragmentTag);
                    if (selectedFragment == null) {
                        selectedFragment = new HomeFragment();
                    }
                    break;

                case R.id.item_category:
                    fragmentTag = "CATEGORY_FRAGMENT";
                    selectedFragment = getSupportFragmentManager().findFragmentByTag(fragmentTag);
                    if (selectedFragment == null) {
                        selectedFragment = new CategoryFragment();
                    }
                    break;

                case R.id.item_like:
                    fragmentTag = "LIKE_FRAGMENT";
                    selectedFragment = getSupportFragmentManager().findFragmentByTag(fragmentTag);
                    if (selectedFragment == null) {
                        selectedFragment = new LikeFragment();
                    }
                    break;

                case R.id.item_download:
                    fragmentTag = "DOWNLOAD_FRAGMENT";
                    selectedFragment = getSupportFragmentManager().findFragmentByTag(fragmentTag);
                    if (selectedFragment == null) {
                        selectedFragment = new DownloadFragment();
                    }
                    break;

                case R.id.item_profile:
                    fragmentTag = "PROFILE_FRAGMENT";
                    selectedFragment = getSupportFragmentManager().findFragmentByTag(fragmentTag);
                    if (selectedFragment == null) {
                        selectedFragment = new ProfileFragment();
                    }
                    break;
            }

            if (selectedFragment != null) {
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

                // Hide other fragments
                for (Fragment fragment : getSupportFragmentManager().getFragments()) {
                    if (fragment.isVisible() && !fragment.equals(selectedFragment)) {
                        fragmentTransaction.hide(fragment);
                    }
                }

                if (!selectedFragment.isAdded()) {
                    fragmentTransaction.add(R.id.containerHomePage, selectedFragment, fragmentTag);
                } else {
                    fragmentTransaction.show(selectedFragment);
                }

                fragmentTransaction.commit();
            }

            return true;
        });

        binding.btmnMainActivity.setSelectedItemId(R.id.item_home);
    }


}
