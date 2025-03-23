package me.adversing.nihil.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;

public final class ReflectionUtils {

    private ReflectionUtils() {}

    public static Optional<Field> findField(Class<?> clazz, String fieldName) {
        Class<?> searchClass = clazz;
        while (searchClass != null && searchClass != Object.class) {
            try {
                Field field = searchClass.getDeclaredField(fieldName);
                return Optional.of(field);
            } catch (NoSuchFieldException e) {
                searchClass = searchClass.getSuperclass();
            }
        }
        return Optional.empty();
    }

    public static Optional<Method> findMethod(Class<?> clazz, String methodName, Class<?>... paramTypes) {
        Class<?> searchClass = clazz;
        while (searchClass != null && searchClass != Object.class) {
            try {
                Method method = searchClass.getDeclaredMethod(methodName, paramTypes);
                return Optional.of(method);
            } catch (NoSuchMethodException e) {
                searchClass = searchClass.getSuperclass();
            }
        }
        return Optional.empty();
    }

    public static Optional<Method> findCompatibleSetter(Class<?> clazz, String setterName, Class<?> paramType) {
        return Arrays.stream(clazz.getMethods())
                .filter(m -> m.getName().equals(setterName))
                .filter(m -> m.getParameterCount() == 1)
                .filter(m -> m.getParameterTypes()[0].isAssignableFrom(paramType))
                .findFirst();
    }

    public static <T> T createInstance(Class<T> clazz) throws ReflectiveOperationException {
        Constructor<T> constructor = clazz.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance();
    }

    public static Object getFieldValue(Field field, Object target) throws IllegalAccessException {
        boolean wasAccessible = field.canAccess(target);
        try {
            field.setAccessible(true);
            return field.get(target);
        } finally {
            if (!wasAccessible) {
                field.setAccessible(false);
            }
        }
    }

    public static void setFieldValue(Field field, Object target, Object value) throws IllegalAccessException {
        boolean wasAccessible = field.canAccess(target);
        try {
            field.setAccessible(true);
            field.set(target, value);
        } finally {
            if (!wasAccessible) {
                field.setAccessible(false);
            }
        }
    }
}