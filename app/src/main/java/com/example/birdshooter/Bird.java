package com.example.birdshooter;


import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;

import java.util.ArrayList;
import java.util.List;

public class Bird {

    // --- Properties ---
    private int speed = 20;
    private boolean wasShot = true;
    private int x = 0;
    private int y;
    private final int width;
    private final int height;

    // --- Animation ---
    private int currentFrame = 0;
    private final List<Bitmap> birdFrames;

    // --- Collision ---
    // Create one Rect and reuse it to avoid garbage collection
    private final Rect collisionShape;

    /**
     * Constructor
     * @param res Resources object to load bitmaps
     * @param screenRatioX The horizontal screen ratio
     * @param screenRatioY The vertical screen ratio
     */
    Bird(Resources res, float screenRatioX, float screenRatioY) {

        birdFrames = new ArrayList<>();

        // Load one bitmap just to get the base dimensions
        Bitmap bird1 = BitmapFactory.decodeResource(res, R.drawable.bird1);
        if (bird1 == null) {
            throw new RuntimeException("Failed to decode R.drawable.bird1");
        }

        // --- Calculate Dimensions ---
        int baseWidth = bird1.getWidth() / 6;
        int baseHeight = bird1.getHeight() / 6;
        this.width = (int) (baseWidth * screenRatioX);
        this.height = (int) (baseHeight * screenRatioY);

        // --- Load and Scale All Frames ---
        birdFrames.add(loadAndScale(res, R.drawable.bird1, width, height));
        birdFrames.add(loadAndScale(res, R.drawable.bird2, width, height));
        birdFrames.add(loadAndScale(res, R.drawable.bird3, width, height));
        birdFrames.add(loadAndScale(res, R.drawable.bird4, width, height));

        // Set initial position off-screen (top)
        y = -height;

        // Initialize the single, reusable collision rectangle
        collisionShape = new Rect();
    }

    /**
     * Helper method to load a bitmap and scale it with filtering.
     */
    private Bitmap loadAndScale(Resources res, int drawableId, int width, int height) {
        Bitmap bitmap = BitmapFactory.decodeResource(res, drawableId);
        if (bitmap == null) {
            throw new RuntimeException("Failed to decode resource: " + drawableId);
        }
        // Use 'true' for filtering to get a smoother image
        return Bitmap.createScaledBitmap(bitmap, width, height, true);
    }

    /**
     * Gets the current animation frame and advances to the next.
     * @return The current Bitmap frame for the bird.
     */
    public Bitmap getBitmap() {
        // Get the current frame
        Bitmap frame = birdFrames.get(currentFrame);

        // Advance the frame counter, looping back to 0
        currentFrame = (currentFrame + 1) % birdFrames.size();

        return frame;
    }

    /**
     * Gets the bird's collision shape.
     * This method reuses the same Rect object to prevent memory churn.
     * @return The Rect object representing the bird's bounds.
     */
    public Rect getCollisionShape() {
        // Update the existing Rect's coordinates instead of creating a new one
        collisionShape.set(x, y, x + width, y + height);
        return collisionShape;
    }

    /**
     * Call this when the bird is no longer needed to free up memory.
     */
    public void recycle() {
        for (Bitmap bitmap : birdFrames) {
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
        }
        birdFrames.clear();
    }

    // --- Getters and Setters ---
    public int getSpeed() { return speed; }
    public void setSpeed(int speed) { this.speed = speed; }

    public boolean getWasShot() { return wasShot; }
    public void setWasShot(boolean wasShot) { this.wasShot = wasShot; }

    public int getX() { return x; }
    public void setX(int x) { this.x = x; }

    public int getY() { return y; }
    public void setY(int y) { this.y = y; }

    public int getWidth() { return width; }
    public int getHeight() { return height; }
}
