package com.example.animewallpaper;

import androidx.appcompat.app.AppCompatActivity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import com.example.animewallpaper.Extra.WallpaperChangeReceiver;
import com.example.animewallpaper.databinding.ActivitySettingBinding;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Calendar;

public class SettingActivity extends AppCompatActivity {

    ActivitySettingBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setOnClickListeners();
    }

    private void setOnClickListeners() {
        binding.wallpaperHistoryButton.setOnClickListener(v -> startActivity(new Intent(SettingActivity.this, SetAsWallpaperActivity.class)));

        binding.wallpaperSchedulingButton.setOnClickListener(v -> showWallpaperScheduleDialog());

        binding.shareAppButton.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            String shareBody = "Check out this awesome wallpaper app! Download it from Demo link\n\nhttps://play.google.com/store/apps/details?id=com.google.android.apps.wallpaper&hl=en_IN";
            String shareSubject = "Wallpaper App";
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, shareSubject);
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
            startActivity(Intent.createChooser(shareIntent, "Share via"));
        });

        binding.logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(SettingActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            Toast.makeText(SettingActivity.this, "Logging out", Toast.LENGTH_SHORT).show();
            finish();
        });

        binding.aboutUsButton.setOnClickListener(v -> startActivity(new Intent(SettingActivity.this, AboutUsActivity.class)));
    }

    private void showWallpaperScheduleDialog() {
        String[] scheduleOptions = {"Off", "Every 2 minutes", "Every 5 minutes", "Every hour", "Every day", "Every week"};

        new AlertDialog.Builder(this)
                .setTitle("Set Wallpaper Schedule")
                .setSingleChoiceItems(scheduleOptions, -1, (dialog, which) -> {
                    String selectedOption = scheduleOptions[which];
                    saveSchedulePreference(selectedOption); // Save the selected option
                    scheduleWallpaperChange(selectedOption); // Schedule the task
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void saveSchedulePreference(String schedule) {
        SharedPreferences prefs = getSharedPreferences("wallpaper_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("schedule", schedule);
        editor.apply();
    }

    public void scheduleWallpaperChange(String scheduleOption) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                // Prompt the user to enable the exact alarm permission
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
            }
        }

        Intent intent = new Intent(this, WallpaperChangeReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        // Cancel any existing alarms
        alarmManager.cancel(pendingIntent);

        // Determine the interval based on the user's choice
        long interval = 0; // default for "Off"
        switch (scheduleOption) {
            case "Every 2 minutes":
                interval = 2 * 60 * 1000; // 2 minutes in milliseconds
                break;
            case "Every 5 minutes":
                interval = 5 * 60 * 1000; // 5 minutes in milliseconds
                break;
            case "Every hour":
                interval = AlarmManager.INTERVAL_HOUR;
                break;
            case "Every day":
                interval = AlarmManager.INTERVAL_DAY;
                break;
            case "Every week":
                interval = AlarmManager.INTERVAL_DAY * 7;
                break;
        }

        if (interval > 0) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            // Adjust time for non-minute intervals like day or week
            if (!scheduleOption.equals("Every 2 minutes") && !scheduleOption.equals("Every 5 minutes") && !scheduleOption.equals("Every hour")) {
                calendar.set(Calendar.HOUR_OF_DAY, 8);  // Fixed time (e.g., 8 AM)
                if (Calendar.getInstance().after(calendar)) {
                    calendar.add(Calendar.DAY_OF_MONTH, 1);
                }
            }

            // Use setExactAndAllowWhileIdle for precise alarms
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            Toast.makeText(this, "Wallpaper scheduling set: " + scheduleOption, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Wallpaper scheduling turned off", Toast.LENGTH_SHORT).show();
        }
    }
}
