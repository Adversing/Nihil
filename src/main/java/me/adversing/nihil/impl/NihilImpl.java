package me.adversing.nihil.impl;

import me.adversing.nihil.Nihil;
import me.adversing.nihil.config.NihilConfig;
import me.adversing.nihil.intf.IPropertyHandler;
import me.adversing.nihil.annotation.Dependency;
import me.adversing.nihil.annotation.UpdateProperty;
import me.adversing.nihil.exception.PropertyUpdateException;
import me.adversing.nihil.handler.DefaultPropertyHandler;
import me.adversing.nihil.util.ReflectionUtils;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Default implementation of Nihil using Java Reflection.
 */
public class NihilImpl implements Nihil {

    private final NihilConfig config;
    private final Map<String, Method> methodCache = new ConcurrentHashMap<>();
    private final Map<String, Field> fieldCache = new ConcurrentHashMap<>();

    public NihilImpl(NihilConfig config) {
        this.config = config;
    }

    @Override
    public <T, S> T update(T target, S source) {
        return update(target, source, Map.of());
    }

    @Override
    public <T, S> T update(T target, S source, Map<Class<?>, Object> dependencies) {
        if (target == null || source == null) {
            return target;
        }

        Class<?> sourceClass = source.getClass();
        Class<?> targetClass = target.getClass();

        switch (config.getAccessStrategy()) {
            case METHOD -> processViaSetters(target, source, dependencies, sourceClass, targetClass);
            case FIELD -> processViaFieldAccess(target, source, dependencies, sourceClass, targetClass);
            case AUTO -> {
                // try setters first, then fall back to field access if needed
                Set<String> processedProperties = processViaSetters(target, source, dependencies, sourceClass, targetClass);
                processViaFieldAccess(target, source, dependencies, sourceClass, targetClass, processedProperties);
            }
        }

        return target;
    }

    @Override
    public <T> UpdaterBuilder<T> forTarget(T target) {
        return new NihilImplBuilderBuilder<>(target, this);
    }

    private <T, S> Set<String> processViaSetters(T target, S source, Map<Class<?>, Object> dependencies,
                                                 Class<?> sourceClass, Class<?> targetClass) {
        return Arrays.stream(sourceClass.getDeclaredFields())
                .filter(this::shouldProcessField)
                .map(field -> {
                    try {
                        field.setAccessible(true);
                        Object value = ReflectionUtils.getFieldValue(field, source);

                        if (shouldUpdateValue(value, field)) {
                            String propertyName = field.getName();
                            UpdateProperty annotation = field.getAnnotation(UpdateProperty.class);

                            if (annotation != null && !annotation.targetProperty().isEmpty()) {
                                propertyName = annotation.targetProperty();
                            }

                            // handle custom property mapping (if needed)
                            if (annotation != null && !annotation.handler().equals(DefaultPropertyHandler.class)) {
                                value = processWithHandler(value, annotation.handler(), dependencies);
                            }

                            // invoke setter method
                            String setterName = "set" + capitalizeFirstLetter(propertyName);
                            Method setter = findSetter(targetClass, setterName, value.getClass());

                            if (setter != null) {
                                setter.invoke(target, value);
                                return propertyName;
                            }
                        }
                    } catch (Exception e) {
                        throw new PropertyUpdateException("Error updating property: " + field.getName(), e);
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private <T, S> void processViaFieldAccess(T target, S source, Map<Class<?>, Object> dependencies,
                                              Class<?> sourceClass, Class<?> targetClass) {
        processViaFieldAccess(target, source, dependencies, sourceClass, targetClass, Set.of());
    }

    private <T, S> void processViaFieldAccess(T target, S source, Map<Class<?>, Object> dependencies,
                                              Class<?> sourceClass, Class<?> targetClass,
                                              Set<String> skipProperties) {
        Arrays.stream(sourceClass.getDeclaredFields())
                .filter(this::shouldProcessField)
                .forEach(sourceField -> {
                    try {
                        String propertyName = sourceField.getName();
                        UpdateProperty annotation = sourceField.getAnnotation(UpdateProperty.class);

                        if (annotation != null && !annotation.targetProperty().isEmpty()) {
                            propertyName = annotation.targetProperty();
                        }

                        if (skipProperties.contains(propertyName)) {
                            return;
                        }

                        sourceField.setAccessible(true);
                        Object value = ReflectionUtils.getFieldValue(sourceField, source);

                        if (shouldUpdateValue(value, sourceField)) {
                            if (annotation != null && !annotation.handler().equals(DefaultPropertyHandler.class)) {
                                value = processWithHandler(value, annotation.handler(), dependencies);
                            }

                            Field targetField = findField(targetClass, propertyName);
                            if (targetField != null) {
                                ReflectionUtils.setFieldValue(targetField, target, value);
                            }
                        }
                    } catch (Exception e) {
                        throw new PropertyUpdateException("Error updating field: " + sourceField.getName(), e);
                    }
                });
    }

    @SuppressWarnings("unchecked")
    private <V> Object processWithHandler(Object value, Class<? extends IPropertyHandler> handlerClass,
                                          Map<Class<?>, Object> dependencies) throws Exception {
        IPropertyHandler<V> handler = (IPropertyHandler<V>) ReflectionUtils.createInstance(handlerClass);

        // dependency injection
        for (Field field : handlerClass.getDeclaredFields()) {
            Dependency injection = field.getAnnotation(Dependency.class);
            if (injection != null && dependencies.containsKey(field.getType())) {
                ReflectionUtils.setFieldValue(field, handler, dependencies.get(field.getType()));
            }
        }

        return handler.process((V) value);
    }

    /**
     * Determines if a field should be processed based on configuration.
     */
    private boolean shouldProcessField(Field field) {
        if (config.getIgnoredProperties().contains(field.getName())) {
            return false;
        }

        int modifiers = field.getModifiers();

        if (Modifier.isStatic(modifiers) || Modifier.isFinal(modifiers)) {
            return false;
        }

        return !Modifier.isTransient(modifiers) || config.isIncludeTransient();
    }

    /**
     * Determines if a value should be used for updating based on configuration and annotations.
     */
    private boolean shouldUpdateValue(Object value, Field field) {
        if (value == null) {
            UpdateProperty annotation = field.getAnnotation(UpdateProperty.class);
            return annotation != null && annotation.includeNull() || !config.isIgnoreNull();
        }
        return true;
    }

    /**
     * Finds a setter method for a property.
     */
    private Method findSetter(Class<?> clazz, String setterName, Class<?> paramType) {
        String cacheKey = clazz.getName() + "#" + setterName + "#" + paramType.getName();

        return methodCache.computeIfAbsent(cacheKey, key -> {
            Optional<Method> exactMethod = ReflectionUtils.findMethod(clazz, setterName, paramType);
            return exactMethod.orElseGet(() ->
                    ReflectionUtils.findCompatibleSetter(clazz, setterName, paramType).orElse(null)
            );
        });
    }

    /**
     * Finds a field in a class.
     */
    private Field findField(Class<?> clazz, String fieldName) {
        String cacheKey = clazz.getName() + "#" + fieldName;

        return fieldCache.computeIfAbsent(cacheKey, key ->
                ReflectionUtils.findField(clazz, fieldName).orElse(null)
        );
    }

    // this should be moved to a utility class
    private String capitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return Character.toUpperCase(input.charAt(0)) + input.substring(1);
    }

    private static class NihilImplBuilderBuilder<T> implements UpdaterBuilder<T> {
        private final T target;
        private final NihilImpl nihil;
        private final Map<Class<?>, Object> dependencies = new ConcurrentHashMap<>();
        private final Map<String, String> propertyMappings = new ConcurrentHashMap<>();
        private final Map<String, Function<Object, Object>> transformers = new ConcurrentHashMap<>();

        NihilImplBuilderBuilder(T target, NihilImpl nihil) {
            this.target = target;
            this.nihil = nihil;
        }

        @Override
        public <D> UpdaterBuilder<T> withDependency(Class<D> type, D dependency) {
            dependencies.put(type, dependency);
            return this;
        }

        @Override
        public UpdaterBuilder<T> withMapping(String sourceProperty, String targetProperty) {
            propertyMappings.put(sourceProperty, targetProperty);
            return this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <V> UpdaterBuilder<T> withTransformer(String property, Function<V, Object> transformer) {
            transformers.put(property, (Function<Object, Object>) transformer);
            return this;
        }

        @Override
        public <S> T update(S source) {
            if (source == null || target == null) {
                return target;
            }

            // if we have custom mappings or transformers, we need to create a proxy
            if (!propertyMappings.isEmpty() || !transformers.isEmpty()) {
                S proxiedSource = createSourceProxy(source);
                return nihil.update(target, proxiedSource, dependencies);
            }

            return nihil.update(target, source, dependencies);
        }

        /**
         * Creates a dynamic proxy for the source object to apply transformations
         * and property mappings.
         *
         * @param source The source object to proxy
         * @param <S>    The source type
         * @return A proxy of the source with transformations applied
         */
        @SuppressWarnings("unchecked")
        private <S> S createSourceProxy(S source) {
            Class<?> sourceClass = source.getClass();

            InvocationHandler handler = new SourceObjectInvocationHandler<>(source, transformers, propertyMappings);

            if (sourceClass.isInterface()) {
                // if the source is an interface, just use Java's built-in Proxy
                return (S) Proxy.newProxyInstance(
                        sourceClass.getClassLoader(),
                        new Class<?>[]{sourceClass},
                        handler
                );
            } else {
                return createClassProxy(source);
            }
        }

        /**
         * Creates a proxy for a concrete class (not an interface).
         * This implementation analyzes the source object and creates a wrapper
         * that delegates all methods and applies transformations.
         *
         * @param source The source object
         * @param <S>    The source type
         * @return A proxy of the source object
         */
        @SuppressWarnings("unchecked")
        private <S> S createClassProxy(S source) {
            Class<S> sourceClass = (Class<S>) source.getClass();

            // i'll use a wrapper approach that delegates to the original object but applies transformations
            try {
                return new SourceObjectWrapper<>(source, transformers, propertyMappings).createProxy();
            } catch (Exception e) {
                throw new PropertyUpdateException("Failed to create proxy for class: " + sourceClass.getName(), e);
            }
        }
    }

    /**
     * Invocation handler that intercepts method calls on the proxy to apply transformations.
     */
    private record SourceObjectInvocationHandler<S>(S target, Map<String, Function<Object, Object>> transformers,
                                                    Map<String, String> propertyMappings) implements InvocationHandler {

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String methodName = method.getName();
            // object-inherited methods are not intercepted
            if (methodName.equals("equals") || methodName.equals("hashCode") || methodName.equals("toString")) {
                return method.invoke(target, args);
            }

            if (methodName.startsWith("get") && methodName.length() > 3 && args == null || args.length == 0) {
                String propertyName = Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
                Object result = method.invoke(target, args);

                if (transformers.containsKey(propertyName) && result != null) {
                    return transformers.get(propertyName).apply(result);
                }

                return result;
            }

            // for non-getter methods, just delegate to the target
            return method.invoke(target, args);
        }
    }

    /**
     * Wrapper that provides dynamic access to a source object while applying transformations.
     * This handles the case where the source is a concrete class rather than an interface.
     */
    private static class SourceObjectWrapper<S> {
        private final S target;
        private final Map<String, Function<Object, Object>> transformers;
        private final Map<String, String> propertyMappings;
        private final Class<S> targetClass;

        @SuppressWarnings("unchecked")
        public SourceObjectWrapper(S target,
                                   Map<String, Function<Object, Object>> transformers,
                                   Map<String, String> propertyMappings) {
            this.target = target;
            this.transformers = transformers;
            this.propertyMappings = propertyMappings;
            this.targetClass = (Class<S>) target.getClass();
        }

        /**
         * Creates a proxy that wraps the target object and applies transformations.
         *
         * @return A proxy that applies transformations
         */
        @SuppressWarnings("unchecked")
        public S createProxy() {
            try {
                // create a dynamic proxy that implements all the target's interfaces
                Class<?>[] interfaces = getAllInterfaces(targetClass);

                if (interfaces.length > 0) {
                    // if the class implements interfaces, we can use a standard proxy
                    InvocationHandler handler = new SourceObjectInvocationHandler<>(
                            target, transformers, propertyMappings
                    );

                    return (S) Proxy.newProxyInstance(
                            targetClass.getClassLoader(),
                            interfaces,
                            handler
                    );
                } else {
                    // for classes without interfaces, we need an advanced approach
                    return createTransformingDelegate();
                }
            } catch (Exception e) {
                throw new PropertyUpdateException("Failed to create proxy", e);
            }
        }

        /**
         * Creates a delegate object that wraps the target while applying transformations.
         * This is used for classes that don't implement interfaces.
         *
         * @return A proxy object that delegates to the target
         */
        private S createTransformingDelegate() {
            return new TransformingDelegate<>(target, transformers).asProxy();
        }

        /**
         * Gets all interfaces implemented by a class and its superclasses.
         *
         * @param cls The class to analyze
         * @return Array of all interfaces
         */
        private Class<?>[] getAllInterfaces(Class<?> cls) {
            Set<Class<?>> interfaces = collectInterfaces(cls);
            return interfaces.toArray(new Class<?>[0]);
        }

        /**
         * Recursively collects all interfaces implemented by a class and its superclasses.
         *
         * @param cls The class to analyze
         * @return Set of interfaces
         */
        private Set<Class<?>> collectInterfaces(Class<?> cls) {
            Set<Class<?>> interfaces = Arrays.stream(cls.getInterfaces())
                    .collect(Collectors.toSet());

            Class<?> superClass = cls.getSuperclass();
            if (superClass != null && superClass != Object.class) {
                interfaces.addAll(collectInterfaces(superClass));
            }

            return interfaces;
        }
    }

    /**
     * A special delegating wrapper for cases where the source object's class
     * doesn't implement any interfaces and can't be directly proxied.
     */
    private static class TransformingDelegate<S> {
        private final S delegate;
        private final Map<String, Function<Object, Object>> transformers;
        private final Map<String, Object> transformedValues = new ConcurrentHashMap<>();

        public TransformingDelegate(S delegate,
                                    Map<String, Function<Object, Object>> transformers) {
            this.delegate = delegate;
            this.transformers = transformers;

            precomputeTransformedValues();
        }

        /**
         * Precomputes transformed values for all properties with transformers.
         */
        private void precomputeTransformedValues() {
            Class<?> delegateClass = delegate.getClass();

            // process all getters
            Arrays.stream(delegateClass.getMethods())
                    .filter(this::isGetter)
                    .forEach(getter -> {
                        try {
                            String propertyName = extractPropertyName(getter.getName());
                            if (transformers.containsKey(propertyName)) {
                                Object originalValue = getter.invoke(delegate);
                                if (originalValue != null) {
                                    transformedValues.put(propertyName,
                                            transformers.get(propertyName).apply(originalValue));
                                }
                            }
                        } catch (Exception e) {
                            // skip on error
                        }
                    });

            // also process fields directly if needed
            Arrays.stream(delegateClass.getDeclaredFields())
                    .forEach(field -> {
                        try {
                            String fieldName = field.getName();
                            if (transformers.containsKey(fieldName) && !transformedValues.containsKey(fieldName)) {
                                field.setAccessible(true);
                                Object value = ReflectionUtils.getFieldValue(field, delegate);
                                if (value != null) {
                                    transformedValues.put(fieldName,
                                            transformers.get(fieldName).apply(value));
                                }
                            }
                        } catch (Exception e) {
                            // skip on error
                        }
                    });
        }

        /**
         * Returns this object as a proxy that applies transformations.
         *
         * @return The proxy object
         */
        @SuppressWarnings("unchecked")
        public S asProxy() {
            return (S) this;
        }

        /**
         * Checks if a method is a getter.
         *
         * @param method The method to check
         * @return True if the method is a getter
         */
        private boolean isGetter(Method method) {
            String name = method.getName();
            // this heuristic is not perfect, but it's good enough for now. It does not work for records datatypes as 
            // their getters are not prefixed with "get" or "is".
            return (name.startsWith("get") && name.length() > 3 && method.getParameterCount() == 0 &&
                    method.getReturnType() != void.class) ||
                    (name.startsWith("is") && name.length() > 2 && method.getParameterCount() == 0 &&
                            method.getReturnType() == boolean.class);
        }

        /**
         * Extracts the property name from a getter method name.
         *
         * @param methodName The method name
         * @return The property name
         */
        private String extractPropertyName(String methodName) {
            // the same here
            if (methodName.startsWith("get") && methodName.length() > 3) {
                return Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
            } else if (methodName.startsWith("is") && methodName.length() > 2) {
                return Character.toLowerCase(methodName.charAt(2)) + methodName.substring(3);
            }
            return methodName;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof TransformingDelegate) {
                return delegate.equals(((TransformingDelegate<?>) obj).delegate);
            }
            return delegate.equals(obj);
        }

        @Override
        public int hashCode() {
            return delegate.hashCode();
        }

        @Override
        public String toString() {
            return "TransformingDelegate[" + delegate + "]";
        }
    }
}