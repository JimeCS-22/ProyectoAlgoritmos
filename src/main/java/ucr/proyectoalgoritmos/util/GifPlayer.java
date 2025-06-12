package ucr.proyectoalgoritmos.util;

import javafx.animation.AnimationTimer;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class GifPlayer {
    private ImageView imageView;
    private Image[] frames;
    private int currentFrame = 0;
    private long lastTime = 0;
    private int delayMillis;

    public GifPlayer(String basePath, int frameCount, int delayMillis) {
        this.delayMillis = delayMillis;
        frames = new Image[frameCount];

        // Cargar todos los frames
        for (int i = 0; i < frameCount; i++) {
            frames[i] = new Image(getClass().getResourceAsStream(basePath + i + ".png"));
        }

        imageView = new ImageView(frames[0]);

        // Animación
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (lastTime == 0) {
                    lastTime = now;
                    return;
                }

                if ((now - lastTime) / 1_000_000 >= delayMillis) {
                    currentFrame = (currentFrame + 1) % frames.length;
                    imageView.setImage(frames[currentFrame]);
                    lastTime = now;
                }
            }
        }.start();
    }

    public ImageView getView() {
        return imageView;
    }
}
