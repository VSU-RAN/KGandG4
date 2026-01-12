package vsu.org.ran.kgandg4.dependecyIndjection;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DIContainer {
    private final Map<Class<?>, Object> beans = new HashMap<>();
    private final String basePackage = "vsu.org.ran.kgandg4";


    public void initialize() {
        List<Class<?>> componentClasses = ClassScanner.findComponentClasses(basePackage);
        if (componentClasses.isEmpty()) {
            return;
        }
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


}
