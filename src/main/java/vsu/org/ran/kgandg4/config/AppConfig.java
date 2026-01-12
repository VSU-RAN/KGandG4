package vsu.org.ran.kgandg4.config;

import vsu.org.ran.kgandg4.dependecyIndjection.annotations.Component;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.Value;

@Component
public class AppConfig {
    @Value("${application.name:3D Viewer}")
    private String applicationName;

    @Value("${window.width:1600}")
    private int windowWidth;

    @Value("${window.height:900}")
    private int windowHeight;


    public String getApplicationName() { return applicationName; }
    public int getWindowWidth() { return windowWidth; }
    public int getWindowHeight() { return windowHeight; }
}