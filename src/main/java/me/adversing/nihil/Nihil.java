package me.adversing.nihil;

import me.adversing.nihil.config.NihilConfig;
import me.adversing.nihil.impl.NihilImpl;
import me.adversing.nihil.intf.INihilProvider;

import java.util.Map;
import java.util.ServiceLoader;
import java.util.function.Function;

/**
 * Core API for updating objects with non-null values.
 * Framework-agnostic and designed for extensibility.
 * @author Adversing
 */
public interface Nihil {

    /**
     * Updates the target object with non-null values from the source object.
     *
     * @param target The object to update
     * @param source The object containing new values
     * @param <T> Target type
     * @param <S> Source type
     * @return The updated target object
     */
    <T, S> T update(T target, S source);

    /**
     * Updates the target object with non-null values from the source object,
     * with additional dependencies for complex property handling.
     *
     * @param target The object to update
     * @param source The object containing new values
     * @param dependencies Additional objects needed for complex property handling
     * @param <T> Target type
     * @param <S> Source type
     * @return The updated target object
     */
    <T, S> T update(T target, S source, Map<Class<?>, Object> dependencies);

    /**
     * Creates a builder for configuring an update operation.
     *
     * @param target The object to update
     * @param <T> Target type
     * @return An UpdaterBuilder for configuration
     */
    <T> UpdaterBuilder<T> forTarget(T target);

    /**
     * Factory method to create a Nihil instance.
     *
     * @return A Nihil implementation
     */
    static Nihil create() {
        return create(NihilConfig.defaults());
    }

    /**
     * Factory method to create a Nihil instance with custom configuration.
     *
     * @param config The configuration for the updater
     * @return A Nihil implementation
     */
    static Nihil create(NihilConfig config) {
        return ServiceLoader.load(INihilProvider.class)
                .findFirst()
                .map(provider -> provider.create(config))
                .orElseGet(() -> new NihilImpl(config));
    }

    /**
     * Builder for configuring update operations.
     */
    interface UpdaterBuilder<T> {
        /**
         * Add a dependency for property handlers.
         *
         * @param type The type of the dependency
         * @param dependency The dependency instance
         * @param <D> The dependency type
         * @return This builder for chaining
         */
        <D> UpdaterBuilder<T> withDependency(Class<D> type, D dependency);

        /**
         * Add a custom property mapping.
         *
         * @param sourceProperty Source property name
         * @param targetProperty Target property name
         * @return This builder for chaining
         */
        UpdaterBuilder<T> withMapping(String sourceProperty, String targetProperty);

        /**
         * Add a custom property transformer.
         *
         * @param property Property name
         * @param transformer Function to transform the property value
         * @param <V> The property value type
         * @return This builder for chaining
         */
        <V> UpdaterBuilder<T> withTransformer(String property, Function<V, Object> transformer);

        /**
         * Perform the update operation.
         *
         * @param source The source object containing new values
         * @param <S> Source type
         * @return The updated target object
         */
        <S> T update(S source);
    }
}