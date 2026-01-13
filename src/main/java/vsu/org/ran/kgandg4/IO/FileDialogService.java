package vsu.org.ran.kgandg4.IO;

import javafx.stage.FileChooser;
import javafx.stage.Window;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.Component;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.Value;

import java.io.File;
import java.util.Optional;

@Component
public class FileDialogService {

    @Value("${filechooser.paths.models:3DModels}")
    private String modelsPath;

    @Value("${filechooser.paths.textures:textures}")
    private String texturesPath;

    @Value("${filechooser.paths.saves:saves}")
    private String savesPath;

    // Системные пути (опционально)
    @Value("${base.package}")
    private String userDir;

    public Optional<File> showOpenModelDialog(Window ownerWindow) {
        return showDialog(DialogType.LOAD_MODEL, ownerWindow);
    }

    public Optional<File> showSaveModelDialog(Window ownerWindow) {
        return showDialog(DialogType.SAVE_MODEL, ownerWindow);
    }

    public Optional<File> showOpenTextureDialog(Window ownerWindow) {
        return showDialog(DialogType.LOAD_TEXTURE, ownerWindow);
    }

    private Optional<File> showDialog(DialogType type, Window ownerWindow) {
        FileChooser fileChooser = createFileChooser(type);

        File initialDir = getInitialDirectory(type);
        fileChooser.setInitialDirectory(initialDir);


        if (type.isSaveDialog()) {
            fileChooser.setInitialFileName(type.getDefaultFileName());
        }

        // Показываем диалог
        File selectedFile = type.isSaveDialog()
                ? fileChooser.showSaveDialog(ownerWindow)
                : fileChooser.showOpenDialog(ownerWindow);

        return Optional.ofNullable(selectedFile);
    }

    private FileChooser createFileChooser(DialogType type) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(type.getTitle());

        for (FileChooser.ExtensionFilter filter : type.getFilters()) {
            fileChooser.getExtensionFilters().add(filter);
        }

        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("All files", "*.*")
        );

        return fileChooser;
    }

    private File getInitialDirectory(DialogType type) {
        String configPath = getConfigPathForType(type);
        File dir = resolvePath(configPath);

        // Если директория не существует, используем user.dir
        return dir.exists() ? dir : new File(System.getProperty("user.dir"));
    }

    private String getConfigPathForType(DialogType type) {
        return switch (type) {
            case LOAD_MODEL, SAVE_MODEL -> modelsPath;
            case LOAD_TEXTURE -> texturesPath;
            default -> savesPath;
        };
    }

    public File resolvePath(String configPath) {
        String resolved = configPath
                .replace("${user.dir}", System.getProperty("user.dir"))
                .replace("${user.home}", System.getProperty("user.home"))
                .replace("//", "/")
                .replace("\\", File.separator);

        if (!new File(resolved).isAbsolute()) {
            resolved = System.getProperty("user.dir") + File.separator + resolved;
        }

        return new File(resolved);
    }
}