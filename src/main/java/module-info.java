module vsu.org.ran.kgandg4 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires MathLibrary;

    opens vsu.org.ran.kgandg4 to javafx.fxml;
    opens vsu.org.ran.kgandg4.gui to javafx.fxml;
    opens vsu.org.ran.kgandg4.gui.controllers to javafx.fxml;
    opens vsu.org.ran.kgandg4.render_engine.render to javafx.fxml;
    opens vsu.org.ran.kgandg4.model to javafx.fxml;
    opens vsu.org.ran.kgandg4.camera to javafx.fxml;
    opens vsu.org.ran.kgandg4.render_engine to javafx.fxml;


    exports vsu.org.ran.kgandg4;
    exports vsu.org.ran.kgandg4.gui;
    exports vsu.org.ran.kgandg4.gui.controllers;
    exports vsu.org.ran.kgandg4.render_engine.render;
    exports vsu.org.ran.kgandg4.model;
    exports vsu.org.ran.kgandg4.camera;
    exports vsu.org.ran.kgandg4.render_engine;
    exports vsu.org.ran.kgandg4.model.models;
    opens vsu.org.ran.kgandg4.model.models to javafx.fxml;
}