package vsu.org.ran.kgandg4.config;

import vsu.org.ran.kgandg4.dependecyIndjection.annotations.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Component
public class PropertyResolver {
    private final Properties properties = new Properties();

    public PropertyResolver() {
        loadProperties();
    }

    private void loadProperties() {
        try(InputStream inputStream = getClass().getClassLoader().getResourceAsStream("app.properties")) {
            properties.load(inputStream);
        } catch (IOException e) {
            System.err.println("Failed to load app.properties: " + e.getMessage());
        }
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public String resolveValue(String expression) {
        if (expression.startsWith("${") && expression.endsWith("}")) {
            String key = expression.substring(2, expression.length() - 1);

            if (key.contains(":")) {
                String[] parts = key.split(":");
                String propertyKey = parts[0].trim();
                String defaultValue = parts[1].trim();
                return getProperty(propertyKey, defaultValue);
            }

            return getProperty(key);
        }
        return expression;
    }
}
