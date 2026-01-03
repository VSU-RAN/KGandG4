module vsu.org.ran.kgandg4 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires vecmath;
    requires MathLibrary;


    opens vsu.org.ran.kgandg4 to javafx.fxml;
    opens vsu.org.ran.kgandg4.model to org.junit.jupiter, org.junit.platform.commons;
    exports vsu.org.ran.kgandg4;
    exports vsu.org.ran.kgandg4.objReader;
    opens vsu.org.ran.kgandg4.objReader to javafx.fxml;
}