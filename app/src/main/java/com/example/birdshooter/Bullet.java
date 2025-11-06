package com.example.birdshooter;
 // <--- Updated

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;

public class Bullet {
    // ... (rest of the code is the same)

    private int x, y;
    private final int width;
    private final int height;
    private final Bitmap bullet;
    private final Rect collisionShape;

    Bullet(Resources res, float screenRatioX, float screenRatioY) {

        Bitmap originalBitmap = BitmapFactory.decodeResource(res, R.drawable.bullet);

        if (originalBitmap == null) {
            throw new RuntimeException("Failed to decode R.drawable.bullet. Resource is missing.");
        }

        int baseWidth = originalBitmap.getWidth() / 4;
        int baseHeight = originalBitmap.getHeight() / 4;
        this.width = (int) (baseWidth * screenRatioX);
        this.height = (int) (baseHeight * screenRatioY);

        bullet = Bitmap.createScaledBitmap(originalBitmap, width, height, true);
        collisionShape = new Rect();
    }

    // ... (getCollisionShape, recycle, getters/setters)

    public Rect getCollisionShape() {
        collisionShape.set(x, y, x + width, y + height);
        return collisionShape;
    }

    public void recycle() {
        if (bullet != null && !bullet.isRecycled()) {
            bullet.recycle();
        }
    }

    // ... (Getters and Setters)
    public int getX() { return x; }
    public void setX(int x) { this.x = x; }
    public int getY() { return y; }
    public void setY(int y) { this.y = y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public Bitmap getBitmap() { return bullet; }
}