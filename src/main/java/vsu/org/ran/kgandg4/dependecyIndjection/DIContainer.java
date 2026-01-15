package vsu.org.ran.kgandg4.dependecyIndjection;

import vsu.org.ran.kgandg4.config.PropertyResolver;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.Value;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.Autowired;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.PostConstruct;

import java.util.*;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

public class DIContainer {
    private final Map<Class<?>, Object> beans = new HashMap<>();
    private final String basePackage;
    private PropertyResolver propertyResolver;

    public DIContainer() {
        this.basePackage = loadBasePackage();
    }

    public void initialize() {
        propertyResolver = new PropertyResolver();
        beans.put(PropertyResolver.class, propertyResolver);

        List<Class<?>> componentClasses = ClassScanner.findComponentClasses(basePackage);
        createAllBeans(componentClasses);
        injectAllDependicies();
    }

    private void createAllBeans(List<Class<?>> classes) {
        for (Class<?> clazz : classes) {
            try {
                Object bean = clazz.getDeclaredConstructor().newInstance();
                beans.put(clazz, bean);

                for (Class<?> iface : clazz.getInterfaces()) {
                    if (!beans.containsKey(iface)) {
                        beans.put(iface, bean);
                    }
                }

            } catch (Exception e) {
                System.err.println("Ошибка создания " + clazz.getSimpleName() + ": " + e.getMessage());
            }
        }
    }

    private void injectAllDependicies() {
        for (Object bean: new ArrayList<>(beans.values())) {
            injectDependecies(bean);
            invokePostConstruct(bean);
        }
    }

    private void injectDependecies(Object bean) {
        Class<?> clazz = bean.getClass();
        while (clazz != null) {
            for (Field field: clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(Autowired.class)) {
                    injectAutoWired(field, bean);
                }
                else if (field.isAnnotationPresent(Value.class)) {
                    injectValue(field, bean);
                }
            }
            clazz = clazz.getSuperclass();
        }
    }

    private void invokePostConstruct(Object bean) {
        Class<?> clazz = bean.getClass();
        for (Method method: clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(PostConstruct.class)) {
                try {
                    method.setAccessible(true);
                    method.invoke(bean);
                } catch (InvocationTargetException | IllegalAccessException  e) {
                    System.err.println("Ошибка в @PostConstruct методе: " + e.getMessage());
                }
            }
        }
    }

    private void injectAutoWired(Field field, Object bean) {
        field.setAccessible(true);

        Object dependency = findCompatibleBean(field.getType());
        if (dependency != null) {

            try {
                field.set(bean, dependency);

            } catch (IllegalAccessException e) {
                System.err.println("Ошибка внедрения в " + bean.getClass().getSimpleName() + ": " + e.getMessage());
            }
        }
    }

    private void injectValue(Field field, Object bean) {
        field.setAccessible(true);
        try {
            Value valueAnnotation = field.getAnnotation(Value.class);
            String expression = valueAnnotation.value();
            String resolvedValue =  propertyResolver.resolveValue(expression);

            if (resolvedValue == null || resolvedValue.isEmpty()) {
                resolvedValue = valueAnnotation.defaultValue();
            }

            Object value = convertValue(field.getType(), resolvedValue);
            field.set(bean, value);

        } catch (IllegalAccessException e) {
            System.err.println("Ошибка внедрения @Value: " + e.getMessage());
        }
    }
    private Object findCompatibleBean(Class<?> type) {
        if (beans.containsKey(type)) {
            return beans.get(type);
        }

        List<Object> candidates = new ArrayList<>();

        for (Map.Entry<Class<?>, Object> bean: beans.entrySet()) {
            if (type.isAssignableFrom(bean.getKey())) {
                candidates.add(bean.getValue());
            }
        }

        if (candidates.isEmpty()) return null;

        if (candidates.size() > 1) {
            System.err.println("Найдено " + candidates.size() + " кандидатов для " + type.getSimpleName() + ". Берем первый.");
        }

        return candidates.get(0);
    }

    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<T> type) {
        if (beans.containsKey(type)) {
            return (T) beans.get(type);
        }

        for (Map.Entry<Class<?>, Object> bean: beans.entrySet()) {
            if (type.isAssignableFrom(bean.getKey())) {
                return (T) bean.getValue();
            }
        }

        return null;
    }

    private Object convertValue(Class<?> targetType, String stringValue) {
        if (stringValue == null) return null;

        if (targetType == String.class) {
            return stringValue;
        } else if (targetType == int.class || targetType == Integer.class) {
            return Integer.parseInt(stringValue);
        } else if (targetType == long.class || targetType == Long.class) {
            return Long.parseLong(stringValue);
        } else if (targetType == double.class || targetType == Double.class) {
            return Double.parseDouble(stringValue);
        } else if (targetType == float.class || targetType == Float.class) {
            return Float.parseFloat(stringValue);
        } else if (targetType == boolean.class || targetType == Boolean.class) {
            return Boolean.parseBoolean(stringValue);
        } else if (targetType == javafx.scene.paint.Color.class) {
            return javafx.scene.paint.Color.web(stringValue);
        } else {
            throw new IllegalArgumentException("Unsupported type: " + targetType);
        }
    }

    private String loadBasePackage() {
        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream("app.properties")) {

            if (input != null) {
                Properties props = new Properties();
                props.load(input);
                String packageFromProps = props.getProperty("base.package");
                if (packageFromProps != null && !packageFromProps.isEmpty()) {
                    return packageFromProps;
                }
            }

        } catch (IOException e) {
        }
        return "vsu.org.ran.kgandg4";
    }


}
