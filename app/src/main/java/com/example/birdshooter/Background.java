package com.example.birdshooter;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Background {

    private int x = 0;
    private int y = 0;
    private final Bitmap background;

    Background(int screenX, int screenY, Resources res) {

        Bitmap originalBitmap = BitmapFactory.decodeResource(res, R.drawable.background);

        if (originalBitmap == null) {
            throw new RuntimeException("Failed to decode R.drawable.background. Resource is missing or invalid.");
        }

        // Use 'true' for bilinear filtering. This makes the scaled background
        // look smooth instead of blocky/pixelated.
        background = Bitmap.createScaledBitmap(originalBitmap, screenX, screenY, true);
    }

    // --- Getters ---

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Bitmap getBitmap() {
        return background;
    }

    // --- Setters ---
    // You'll need these to move the background for a scrolling effect

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    /**
     * Call this to free memory when the game closes.
     */
    public void recycle() {
        if (background != null && !background.isRecycled()) {
            background.recycle();
        }
    }
}
