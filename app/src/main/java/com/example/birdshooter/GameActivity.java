package com.example.birdshooter;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;

public class GameActivity extends AppCompatActivity {

    private GameView gameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // --- Modern Fullscreen Mode ---
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        WindowInsetsControllerCompat controller = new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
        controller.hide(WindowInsetsCompat.Type.systemBars());
        controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);

        // --- Get Screen Size ---
        Point point = new Point();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            Display display = getDisplay();
            if (display != null) {
                display.getRealMetrics(displayMetrics);
                point.x = displayMetrics.widthPixels;
                point.y = displayMetrics.heightPixels;
            }
        } else {
            getWindowManager().getDefaultDisplay().getRealSize(point);
        }

        gameView = new GameView(this, point.x, point.y);
        setContentView(gameView);
    }

    // ... (onPause and onResume methods)
    @Override
    protected void onPause() {
        super.onPause();
        gameView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        gameView.resume();
    }
}
