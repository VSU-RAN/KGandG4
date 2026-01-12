package vsu.org.ran.kgandg4.IO;

import javafx.stage.FileChooser;

public enum DialogType {
    LOAD_MODEL("Load 3D Model", false, "model.obj",
            new FileChooser.ExtensionFilter("OBJ Files", "*.obj")),

    SAVE_MODEL("Save 3D Model", true, "model.obj",
            new FileChooser.ExtensionFilter("OBJ Files", "*.obj")),

    LOAD_TEXTURE("Load Texture", false, "texture.png",
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.bmp", "*.tga"),
            new FileChooser.ExtensionFilter("PNG Files", "*.png"),
            new FileChooser.ExtensionFilter("JPEG Files", "*.jpg", "*.jpeg"),
            new FileChooser.ExtensionFilter("BMP Files", "*.bmp"));

    private final String title;
    private final boolean saveDialog;
    private final String defaultFileName;
    private final FileChooser.ExtensionFilter[] filters;

    DialogType(String title, boolean saveDialog, String defaultFileName,
               FileChooser.ExtensionFilter... filters) {
        this.title = title;
        this.saveDialog = saveDialog;
        this.defaultFileName = defaultFileName;
        this.filters = filters;
    }

    public String getTitle() { return title; }
    public boolean isSaveDialog() { return saveDialog; }
    public String getDefaultFileName() { return defaultFileName; }
    public FileChooser.ExtensionFilter[] getFilters() { return filters; }
}