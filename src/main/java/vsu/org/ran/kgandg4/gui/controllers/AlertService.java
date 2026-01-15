package vsu.org.ran.kgandg4.gui.controllers;

import java.util.Optional;

import javafx.stage.Window;
import javafx.scene.control.Alert;
import javafx.application.Platform;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import vsu.org.ran.kgandg4.gui.PanelManager;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.Autowired;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.Component;

@Component
public class AlertService {
    @Autowired
    private PanelManager panelManager;

    public void showInfo(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);

            Window mainWindow = panelManager.getMainWindow();
            if (mainWindow != null) {
                alert.initOwner(mainWindow);
            }

            alert.showAndWait();
        });
    }

    public void showError(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);

            Window mainWindow = panelManager.getMainWindow();
            if (mainWindow != null) {
                alert.initOwner(mainWindow);
            }

            alert.showAndWait();
        });
    }

    /**
     * Показывает диалог подтверждения (OK/Cancel)
     * @return true если пользователь нажал OK, false если Cancel
     */
    public boolean showConfirmation(String title, String message) {
        final Boolean[] result = new Boolean[1];

        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);

            Window mainWindow = panelManager != null ? panelManager.getMainWindow() : null;
            if (mainWindow != null) {
                alert.initOwner(mainWindow);
            }

            ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
            ButtonType cancelButton = new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(okButton, cancelButton);

            // Ждем результат
            Optional<ButtonType> buttonResult = alert.showAndWait();
            result[0] = buttonResult.isPresent() && buttonResult.get() == okButton;
        });

        while (result[0] == null) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }

        return result[0];
    }
}
