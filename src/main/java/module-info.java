module vsu.org.ran.kgandg4 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires vecmath;

    opens vsu.org.ran.kgandg4 to javafx.fxml;
    exports vsu.org.ran.kgandg4;
}