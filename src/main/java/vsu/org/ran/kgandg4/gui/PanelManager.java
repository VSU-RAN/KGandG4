package vsu.org.ran.kgandg4.gui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import vsu.org.ran.kgandg4.Simple3DViewer;
import vsu.org.ran.kgandg4.dependecyIndjection.DIContainer;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.Component;
import vsu.org.ran.kgandg4.gui.controllers.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Component
public class PanelManager {
    private final DIContainer DIContainer;

    private final Map<String, PanelStorageEntry<?>> storage = new HashMap<>();
    private String currentPanelId = null;
    private VBox currentContainer = null;

    private Window mainWindow;

    private static class PanelStorageEntry<T> {
        public final Parent view;
        public final T controller;
        public final String panelId;

        public PanelStorageEntry(String panelId, Parent view, T controller) {
            this.panelId = panelId;
            this.view = view;
            this.controller = controller;
        }
    }

    public PanelManager() {
        this.DIContainer = Simple3DViewer.getDiContainer();
    }

    public void initializeMainWindow(Window window) {
        this.mainWindow = window;
    }

    public Window getMainWindow() {
        return mainWindow;
    }

    public <T> void loadPanel(String panelId, String fxmlPath, Class<T> controllerType) {
        if (!storage.containsKey(panelId)) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                loader.setControllerFactory(DIContainer::getBean);

                Parent view = loader.load();
                Object controller = loader.getController();

                if (!controllerType.isInstance(controller)) {
                    throw new RuntimeException("Неверный тип контроллера");
                }

                T typedController = controllerType.cast(controller);
                PanelStorageEntry<T> entry = new PanelStorageEntry<>(panelId, view, typedController);

                storage.put(panelId, entry);
            } catch (IOException e) {
                throw new RuntimeException("Ошибка загрузки: " + panelId, e);
            }
        }
    }

    public void showPanel(String panelId, VBox container) {
        PanelStorageEntry<?> entry = storage.get(panelId);

        if (entry == null) {
            throw new IllegalArgumentException("Панель не загружена: " + panelId);
        }

        if (currentPanelId != null && !currentPanelId.equals(panelId)) {
            hidePanel(currentPanelId, container);
        }

        if (panelId.equals(currentPanelId)) {
            return;
        }

        container.getChildren().setAll(entry.view);
        container.setVisible(true);
        container.setManaged(true);

        currentPanelId = panelId;
        currentContainer = container;

        notifyControllerShown(entry.controller);
    }

    public void hidePanel(String panelId, VBox container) {
        if (panelId.equals(currentPanelId)) {
            hideCurrentPanel();
        }
    }

    private void hideCurrentPanel() {
        if (currentPanelId != null && currentContainer != null) {
            currentContainer.getChildren().clear();
            currentContainer.setVisible(false);
            currentContainer.setManaged(false);
            currentPanelId = null;
            currentContainer = null;
        }
    }

    public void togglePanel(String panelId, VBox container) {
        if (panelId.equals(currentPanelId)) {
            hidePanel(panelId, container);
        } else {
            showPanel(panelId, container);
        }
    }

    private <T> void notifyControllerShown(T controller) {
        if (controller instanceof PanelController) {
            ((PanelController) controller).onPanelShow();
        }
    }

    public Parent getView(String panelId) {
        PanelStorageEntry<?> entry = storage.get(panelId);
        if (entry == null) {
            throw new IllegalArgumentException("Панель не загружена: " + panelId);
        }
        return entry.view;
    }

    public boolean isPanelOpen(String panelId) {
        return panelId.equals(currentPanelId);
    }

    public String getCurrentPanelId() {
        return currentPanelId;
    }


    public <T> T getController(String panelId, Class<T> type) {
        PanelStorageEntry<?> entry = storage.get(panelId);

        if (entry == null) {
            throw new IllegalArgumentException("Панель не загружена: " + panelId);
        }

        if (!type.isInstance(entry.controller)) {
            throw new ClassCastException("Контроллер панели " + panelId + " не является экземпляром " + type.getName());
        }

        return type.cast(entry.controller);
    }

    public void preloadAll() {
        loadPanel("camera", "/vsu/org/ran/kgandg4/camera-panel.fxml", CameraPanelController.class);
        loadPanel("model", "/vsu/org/ran/kgandg4/model-panel.fxml", ModelPanelController.class);
        loadPanel("render", "/vsu/org/ran/kgandg4/render-panel.fxml", RenderPanelController.class);
        loadPanel("edit", "/vsu/org/ran/kgandg4/edit-panel.fxml", EditPanelController.class);
        loadPanel("transform", "/vsu/org/ran/kgandg4/transform-panel.fxml", TransformPanelController.class);
    }
}
