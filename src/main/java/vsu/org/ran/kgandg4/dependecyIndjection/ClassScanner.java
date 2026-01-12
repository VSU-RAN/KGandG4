package vsu.org.ran.kgandg4.dependecyIndjection;

import vsu.org.ran.kgandg4.dependecyIndjection.annotations.Component;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarFile;

public class ClassScanner {
    private static final String PACKAGE_SEPARATOR = ".";
    private static final String DIRECTORIES_SEPARATOR = "/";
    private static final String JAR_SEPARATOR = "!";
    private static final String ANONIM_CLASS_SEPARATOR = "$";

    private static final String FILE_PROTOCOL = "file";
    private static final String JAR_PROTOCOL = "jar";

    private static final String CLASS_POSTFIX = ".class";
    private static final String JAR_PREFIX = "file:";


    public static List<Class<?>> findClasses(String packageName) {
        List<Class<?>> classes = new ArrayList<>();
        String scannedPath = packageName.replace(PACKAGE_SEPARATOR, DIRECTORIES_SEPARATOR);

        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            Enumeration<URL> resources = classLoader.getResources(scannedPath);

            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();

                if (resource.getProtocol().equalsIgnoreCase(FILE_PROTOCOL)) {
                    scanFile(resource, scannedPath, classes);
                }

                else if (resource.getProtocol().equalsIgnoreCase(JAR_PROTOCOL)) {
                    scanJar(resource, scannedPath, classes);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при сканировании пакета: " + packageName, e);
        }

        return classes;
    }

    private static void scanFile(URL fileUrl, String scannedPath, List<Class<?>> classes) {
        try {
            File directory = new File(fileUrl.toURI());

            if (!directory.exists()) {
                System.err.println("Директория не существует: " + directory);
                return;
            }

            scanDirectory(directory, scannedPath, classes);

        } catch (URISyntaxException e) {
            System.err.println("Некорректный URL: " + fileUrl);
        }
    }

    private static void scanJar(URL jarUrl, String scannedPath, List<Class<?>> classes) {
        String jarPath = extractJarPath(jarUrl);

        try (JarFile jar = new JarFile(jarPath)) {
            jar.stream()
                    .filter(entry -> !entry.isDirectory())
                    .filter(entry -> entry.getName().startsWith(scannedPath + "/"))
                    .filter(entry -> entry.getName().endsWith(CLASS_POSTFIX))
                    .forEach(entry -> {
                        String className = entry.getName()
                                .replace(DIRECTORIES_SEPARATOR, PACKAGE_SEPARATOR)
                                .replace(CLASS_POSTFIX, "");
                        loadClass(className, classes);
                    });
        } catch (IOException e) {
            System.err.println("Не удалось прочитать JAR файл: " + jarPath);
        }
    }

    private static void scanDirectory(File directory, String scannedPath, List<Class<?>> classes) {
        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                String subPackageName = scannedPath.isEmpty() ? file.getName() : scannedPath + PACKAGE_SEPARATOR + file.getName();

                scanDirectory(file, subPackageName, classes);
            }
            else if (file.getName().endsWith(CLASS_POSTFIX)) {
                if (!file.getName().contains(ANONIM_CLASS_SEPARATOR)) {
                    String className = scannedPath.replace(DIRECTORIES_SEPARATOR, PACKAGE_SEPARATOR) +
                            PACKAGE_SEPARATOR + file.getName().substring(0, file.getName().length() - CLASS_POSTFIX.length());

                    loadClass(className, classes);
                }
            }
        }
    }

    private static void loadClass(String className, List<Class<?>> classes) {
        try {
            Class<?> clazz = Class.forName(className);
            classes.add(clazz);
        } catch (ClassNotFoundException e) {
            System.err.println("Класс не найден: " + className);
        }
    }

    public static List<Class<?>> findComponentClasses(String packageName) {
        List<Class<?>> allClasses = findClasses(packageName);
        List<Class<?>> componentClasses = new ArrayList<>();

        for (Class<?> clazz : allClasses) {
            if (clazz.isAnnotationPresent(Component.class)) {
                componentClasses.add(clazz);
            }
        }

        return componentClasses;
    }

    private static String extractJarPath(URL jarUrl) {
        String path = jarUrl.getFile();

        int separatorIndex = path.indexOf(JAR_SEPARATOR);
        if (separatorIndex != -1) {
            path = path.substring(0, separatorIndex);
        }

        if (path.startsWith(JAR_PREFIX)) {
            path = path.substring(JAR_PREFIX.length());
        }

        return path;
    }
}
