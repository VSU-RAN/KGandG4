package vsu.org.ran.kgandg4.render_engine;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.Component;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.Value;

@Component
public class RenderLoopService {
    private Timeline timeline;
    private Runnable renderTask;

    @Value("${render.fps}")
    private int fps;

    public void startRenderLoop(Runnable renderTask) {
        this.renderTask = renderTask;
        stopRenderLoop();

        timeline = new Timeline();
        timeline.setCycleCount(Animation.INDEFINITE);

        Duration frameDuration = Duration.millis(1000.0 / fps);
        KeyFrame frame = new KeyFrame(frameDuration, event -> {
            if (renderTask != null) {
                renderTask.run();
            }
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
}
