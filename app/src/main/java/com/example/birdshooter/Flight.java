package com.example.birdshooter;
// <--- Updated

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import java.util.ArrayList;
import java.util.List;

public class Flight {
    // ... (rest of the code is the same)

    private boolean isGoingUp = false;
    private boolean isShooting = false;
    private int x, y, width, height;
    private int flyCounter = 0, shootCounter = 0;
    private final List<Bitmap> flyFrames;
    private final List<Bitmap> shootFrames;
    private final Bitmap dead;
    private final Rect collisionShape;
    private final GameView gameView;

    Flight(GameView gameView, int screenY, Resources res, float screenRatioX, float screenRatioY) {
        this.gameView = gameView;

        flyFrames = new ArrayList<>();
        shootFrames = new ArrayList<>();

        Bitmap flight1 = BitmapFactory.decodeResource(res, R.drawable.fly1);
        if (flight1 == null) {
            throw new RuntimeException("Failed to decode R.drawable.flight1");
        }

        int baseWidth = flight1.getWidth() / 4;
        int baseHeight = flight1.getHeight() / 4;
        this.width = (int) (baseWidth * screenRatioX);
        this.height = (int) (baseHeight * screenRatioY);

        flyFrames.add(loadAndScale(res, R.drawable.fly1, width, height));
        flyFrames.add(loadAndScale(res, R.drawable.fly2, width, height));

        shootFrames.add(loadAndScale(res, R.drawable.shoot1, width, height));
        shootFrames.add(loadAndScale(res, R.drawable.shoot2, width, height));
        shootFrames.add(loadAndScale(res, R.drawable.shoot3, width, height));
        shootFrames.add(loadAndScale(res, R.drawable.shoot4, width, height));
        shootFrames.add(loadAndScale(res, R.drawable.shoot5, width, height));

        dead = loadAndScale(res, R.drawable.dead, width, height);

        y = screenY / 2;
        x = (int) (64 * screenRatioX);
        collisionShape = new Rect();
    }

    // ... (loadAndScale, getBitmap, getDead, getCollisionShape, recycle, getters/setters)

    private Bitmap loadAndScale(Resources res, int drawableId, int width, int height) {
        Bitmap bitmap = BitmapFactory.decodeResource(res, drawableId);
        if (bitmap == null) {
            throw new RuntimeException("Failed to decode resource: " + drawableId);
        }
        return Bitmap.createScaledBitmap(bitmap, width, height, true);
    }

    public Bitmap getBitmap() {
        if (isShooting) {
            Bitmap frame = shootFrames.get(shootCounter);
            if (shootCounter == 0) {
                gameView.newBullet();
            }
            shootCounter++;
            if (shootCounter >= shootFrames.size()) {
                shootCounter = 0;
                isShooting = false;
            }
            return frame;
        }

        Bitmap frame = flyFrames.get(flyCounter);
        flyCounter = (flyCounter + 1) % flyFrames.size();
        return frame;
    }

    public Bitmap getDead() { return dead; }

    public Rect getCollisionShape() {
        collisionShape.set(x, y, x + width, y + height);
        return collisionShape;
    }

    public void recycle() {
        // ... (recycle all bitmaps)
    }

    // ... (Getters and Setters)
    public int getX() { return x; }
    public int getY() { return y; }
    public void setY(int y) { this.y = y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public boolean isGoingUp() { return isGoingUp; }
    public void setGoingUp(boolean goingUp) { isGoingUp = goingUp; }
    public void setShooting() {
        if (!isShooting) {
            isShooting = true;
            shootCounter = 0;
        }
    }
}
