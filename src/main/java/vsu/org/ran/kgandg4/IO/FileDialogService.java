package vsu.org.ran.kgandg4.IO;

import javafx.stage.FileChooser;
import javafx.stage.Window;
import vsu.org.ran.kgandg4.dependecyIndjection.Component;

import java.io.File;
import java.util.Optional;

@Component
public class FileDialogService {

    public Optional<File> showOpenModelDialog(Window ownerWindow) {
        FileChooser fileChooser = createFileChooser("Load Model", "*.obj");
        return Optional.ofNullable(fileChooser.showOpenDialog(ownerWindow));
    }

    public Optional<File> showSaveModelDialog(Window ownerWindow) {
        FileChooser fileChooser = createFileChooser("Save Model", "*.obj");
        return Optional.ofNullable(fileChooser.showSaveDialog(ownerWindow));
    }

    public Optional<File> showOpenTextureDialog(Window ownerWindow) {
        FileChooser fileChooser = createFileChooser("Load Texture",
                "*.png", "*.jpg", "*.jpeg", "*.bmp");
        return Optional.ofNullable(fileChooser.showOpenDialog(ownerWindow));
    }

    private FileChooser createFileChooser(String title, String... extensions) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);

        StringBuilder filterDescription = new StringBuilder();
        for (int i = 0; i < extensions.length; i++) {
            if (i > 0) filterDescription.append(", ");
            filterDescription.append(extensions[i]);
        }

        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Files (" + filterDescription + ")", extensions)
        );

        File projectDir = new File(System.getProperty("user.dir"));
        File modelsDir = new File(projectDir, "3DModels");
        if (modelsDir.exists()) {
            fileChooser.setInitialDirectory(modelsDir);
        } else {
            fileChooser.setInitialDirectory(projectDir);
        }

        return fileChooser;
    }
}