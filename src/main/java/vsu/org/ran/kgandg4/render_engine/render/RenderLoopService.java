package vsu.org.ran.kgandg4.render_engine.render;

import javafx.util.Duration;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.Animation;
import javafx.scene.canvas.GraphicsContext;

import vsu.org.ran.kgandg4.dependecyIndjection.annotations.Value;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.Autowired;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.Component;

@Component
public class RenderLoopService {
    private Timeline timeline;
    private int currentWidth;
    private int currentHeight;

    @Autowired
    private Scene scene;

    @Value("${render.fps}")
    private int fps;

    public void startRenderLoop(GraphicsContext gc, int width, int height) {
        stopRenderLoop();

        this.currentWidth = width;
        this.currentHeight = height;


        timeline = new Timeline();
        timeline.setCycleCount(Animation.INDEFINITE);

        Duration frameDuration = Duration.millis(1000.0 / fps);
        KeyFrame frame = new KeyFrame(frameDuration, event -> {
           scene.renderFrame(gc, currentWidth , currentHeight);
        });

        timeline.getKeyFrames().add(frame);
        timeline.play();
    }

    public void stopRenderLoop() {
        if (timeline != null) {
            timeline.stop();
            timeline = null;
        }
    }

    public void updateSize(int width, int height) {
        if (width > 0 && height > 0) {
            this.currentWidth = width;
            this.currentHeight = height;
        }
    }

    public boolean isRunning() {
        return timeline != null && timeline.getStatus() == Animation.Status.RUNNING;
    }
}
