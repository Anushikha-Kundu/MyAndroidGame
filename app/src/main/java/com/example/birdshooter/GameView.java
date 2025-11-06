package com.example.birdshooter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameView extends SurfaceView implements Runnable {

    // --- Core ---
    private Thread thread;
    private boolean isPlaying, isGameOver = false;
    private volatile boolean isReady = false;

    private final int screenX, screenY;
    private final float screenRatioX, screenRatioY;
    private final Activity activity;

    // --- Game Objects (Initialized as null) ---
    private Flight flight;
    private Background background1, background2;
    private Bird[] birds;
    private List<Bullet> bullets;
    private List<Bullet> bulletsTrashList;

    // --- Scoring & Audio ---
    private int score = 0;
    private final SharedPreferences prefs;
    private SoundPool soundPool;
    private int sound;

    // --- Drawing & Timing ---
    private final Paint paint;
    private final Paint loadingPaint;
    private Random random;
    private static final long TARGET_FPS = 60;
    private final long frameTime;

    public GameView(Activity activity, int screenX, int screenY) {
        super(activity);
        this.activity = activity;
        this.screenX = screenX;
        this.screenY = screenY;
        this.screenRatioX = screenX / 1920f;
        this.screenRatioY = screenY / 1080f;

        // Init fast objects
        bullets = new ArrayList<>();
        bulletsTrashList = new ArrayList<>();
        birds = new Bird[4];
        random = new Random();
        prefs = activity.getSharedPreferences("game", Context.MODE_PRIVATE);

        // Init paints
        paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setTextSize((int) (128 * screenRatioX));

        loadingPaint = new Paint();
        loadingPaint.setColor(Color.WHITE);
        loadingPaint.setTextSize(100);
        loadingPaint.setTextAlign(Paint.Align.CENTER);

        frameTime = 1000 / TARGET_FPS;
    }

    private void loadAssets() {
        // --- Load backgrounds first ---
        background1 = new Background(screenX, screenY, getResources());
        background2 = new Background(screenX, screenY, getResources());
        background2.setX(screenX);

        // --- Load other assets ---
        flight = new Flight(this, screenY, getResources(), screenRatioX, screenRatioY);
        for (int i = 0; i < 4; i++) {
            birds[i] = new Bird(getResources(), screenRatioX, screenRatioY);
        }

        // --- Load Sounds ---
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .build();
            soundPool = new SoundPool.Builder()
                    .setAudioAttributes(audioAttributes)
                    .setMaxStreams(5)
                    .build();
        } else {
            soundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
        }
        sound = soundPool.load(activity, R.raw.shoot, 1);

        // --- Finished Loading ---
        isReady = true;
    }

    @Override
    public void run() {
        loadAssets(); // Load assets on this background thread

        while (isPlaying) {
            long startTime = System.currentTimeMillis();
            update();
            draw();
            long timeTaken = System.currentTimeMillis() - startTime;
            long sleepTime = frameTime - timeTaken;
            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void update() {
        if (!isReady || isGameOver) {
            return;
        }

        // --- Update Backgrounds ---
        background1.setX(background1.getX() - (int) (10 * screenRatioX));
        background2.setX(background2.getX() - (int) (10 * screenRatioX));
        if (background1.getX() + background1.getBitmap().getWidth() < 0) {
            background1.setX(screenX);
        }
        if (background2.getX() + background2.getBitmap().getWidth() < 0) {
            background2.setX(screenX);
        }

        // --- Update Player ---
        if (flight.isGoingUp()) {
            flight.setY(flight.getY() - (int) (30 * screenRatioY));
        } else {
            flight.setY(flight.getY() + (int) (30 * screenRatioY));
        }
        if (flight.getY() < 0) flight.setY(0);
        if (flight.getY() > screenY - flight.getHeight()) flight.setY(screenY - flight.getHeight());

        // --- Update Bullets & Collisions ---
        bulletsTrashList.clear();
        for (Bullet bullet : bullets) {
            if (bullet.getX() > screenX) {
                bulletsTrashList.add(bullet);
                continue;
            }
            bullet.setX(bullet.getX() + (int) (50 * screenRatioX));
            for (Bird bird : birds) {
                if (!bird.getWasShot() && Rect.intersects(bird.getCollisionShape(), bullet.getCollisionShape())) {
                    score++;
                    bird.setWasShot(true);
                    bird.setX(-500);
                    bulletsTrashList.add(bullet);
                }
            }
        }
        bullets.removeAll(bulletsTrashList);

        // --- Update Birds & Collisions ---
        for (Bird bird : birds) {
            bird.setX(bird.getX() - bird.getSpeed());
            if (bird.getX() + bird.getWidth() < 0) {
                if (!bird.getWasShot()) {
                    isGameOver = true;
                    return;
                }
                int bound = (int) (25 * screenRatioX);
                bird.setSpeed(random.nextInt(bound));
                if (bird.getSpeed() < (int) (8 * screenRatioX)) bird.setSpeed((int) (8 * screenRatioX));
                bird.setX(screenX);
                bird.setY(random.nextInt(screenY - bird.getHeight()));
                bird.setWasShot(false);
            }
            if (!bird.getWasShot() && Rect.intersects(bird.getCollisionShape(), flight.getCollisionShape())) {
                isGameOver = true;
                return;
            }
        }
    }

    private void draw() {
        if (getHolder().getSurface().isValid()) {
            Canvas canvas = getHolder().lockCanvas();

            // --- Draw Loading Screen ---
            if (!isReady) {
                // Check if background1 is loaded yet
                if (background1 != null) {
                    // Draw the background
                    canvas.drawBitmap(background1.getBitmap(), background1.getX(), background1.getY(), paint);
                } else {
                    // If not, just draw black
                    canvas.drawColor(Color.BLACK);
                }
                // Draw "Loading..." text
                canvas.drawText("Loading...", screenX / 2f, screenY / 2f, loadingPaint);

                getHolder().unlockCanvasAndPost(canvas);
                return;
            }

            // --- Draw Normal Game ---
            canvas.drawBitmap(background1.getBitmap(), background1.getX(), background1.getY(), paint);
            canvas.drawBitmap(background2.getBitmap(), background2.getX(), background2.getY(), paint);
            for (Bird bird : birds) {
                canvas.drawBitmap(bird.getBitmap(), bird.getX(), bird.getY(), paint);
            }
            canvas.drawText(score + "", screenX / 2f, 164, paint);

            if (isGameOver) {
                isPlaying = false;
                canvas.drawBitmap(flight.getDead(), flight.getX(), flight.getY(), paint);
                getHolder().unlockCanvasAndPost(canvas);
                saveIfHighScore();
                waitBeforeExiting();
                return;
            }

            canvas.drawBitmap(flight.getBitmap(), flight.getX(), flight.getY(), paint);
            for (Bullet bullet : bullets) {
                canvas.drawBitmap(bullet.getBitmap(), bullet.getX(), bullet.getY(), paint);
            }

            getHolder().unlockCanvasAndPost(canvas);
        }
    }

    // --- (Rest of the methods are the same) ---

    private void waitBeforeExiting() {
        try {
            Thread.sleep(3000);
            activity.finish();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void saveIfHighScore() {
        if (prefs.getInt("highscore", 0) < score) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("highscore", score);
            editor.apply();
        }
    }

    public void resume() {
        isPlaying = true;
        thread = new Thread(this);
        thread.start();
    }

    public void pause() {
        try {
            isPlaying = false;
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isReady || isGameOver) {
            return false;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (event.getX() < screenX / 2f) {
                    flight.setGoingUp(true);
                } else {
                    flight.setShooting();
                }
                break;
            case MotionEvent.ACTION_UP:
                flight.setGoingUp(false);
                break;
        }
        return true;
    }

    public void newBullet() {
        if (soundPool == null || !isReady) {
            return;
        }
        if (!prefs.getBoolean("isMute", false)) {
            soundPool.play(sound, 1, 1, 0, 0, 1);
        }
        Bullet bullet = new Bullet(getResources(), screenRatioX, screenRatioY);
        bullet.setX(flight.getX() + flight.getWidth());
        bullet.setY(flight.getY() + (flight.getHeight() / 2) - (bullet.getHeight() / 2));
        bullets.add(bullet);
    }
}
// testing