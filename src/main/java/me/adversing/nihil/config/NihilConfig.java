package me.adversing.nihil.config;

import java.util.HashSet;
import java.util.Set;

public class NihilConfig {
    private AccessStrategy accessStrategy;
    private boolean deepCopy;
    private boolean ignoreNull;
    private boolean includeTransient;
    private final Set<String> ignoredProperties;

    private NihilConfig() {
        this.accessStrategy = AccessStrategy.AUTO;
        this.deepCopy = true;
        this.ignoreNull = true;
        this.includeTransient = false;
        this.ignoredProperties = new HashSet<>();
    }

    /**
     * Creates a default configuration.
     *
     * @return Default configuration
     */
    public static NihilConfig defaults() {
        return new NihilConfig();
    }

    public static Builder builder() {
        return new Builder();
    }

    public AccessStrategy getAccessStrategy() {
        return accessStrategy;
    }

    public boolean isDeepCopy() {
        return deepCopy;
    }

    public boolean isIgnoreNull() {
        return ignoreNull;
    }

    public boolean isIncludeTransient() {
        return includeTransient;
    }

    public Set<String> getIgnoredProperties() {
        return ignoredProperties;
    }

    /**
     * Access strategy for updating properties.
     */
    public enum AccessStrategy {
        /**
         * Use setters if available, otherwise direct field access.
         */
        AUTO,

        /**
         * Use only setter methods.
         */
        METHOD,

        /**
         * Use only direct field access.
         */
        FIELD
    }

    public static class Builder {
        private final NihilConfig config;

        private Builder() {
            this.config = new NihilConfig();
        }

        /**
         * Sets the access strategy.
         *
         * @param accessStrategy The access strategy
         * @return This builder for chaining
         */
        public Builder withAccessStrategy(AccessStrategy accessStrategy) {
            config.accessStrategy = accessStrategy;
            return this;
        }

        /**
         * Sets whether deep copy is enabled.
         *
         * @param deepCopy True to enable deep copy
         * @return This builder for chaining
         */
        public Builder withDeepCopy(boolean deepCopy) {
            config.deepCopy = deepCopy;
            return this;
        }

        /**
         * Sets whether null values should be ignored.
         *
         * @param ignoreNull True to ignore null values
         * @return This builder for chaining
         */
        public Builder withIgnoreNull(boolean ignoreNull) {
            config.ignoreNull = ignoreNull;
            return this;
        }

        /**
         * Sets whether transient fields should be included.
         *
         * @param includeTransient True to include transient fields
         * @return This builder for chaining
         */
        public Builder withIncludeTransient(boolean includeTransient) {
            config.includeTransient = includeTransient;
            return this;
        }

        /**
         * Adds a property to ignore.
         *
         * @param property The property name to ignore
         * @return This builder for chaining
         */
        public Builder ignoreProperty(String property) {
            config.ignoredProperties.add(property);
            return this;
        }

        /**
         * Builds the configuration.
         *
         * @return The configuration
         */
        public NihilConfig build() {
            return config;
        }
    }
}