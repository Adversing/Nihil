package me.adversing.nihil.intf;

import me.adversing.nihil.Nihil;
import me.adversing.nihil.config.NihilConfig;

/**
 * Service provider interface for obtaining Nihil implementations.
 * Allows for custom implementations to be plugged in via Java's ServiceLoader.
 */
public interface INihilProvider {

    /**
     * Creates a new Nihil implementation.
     *
     * @param config The configuration for the updater
     * @return A Nihil implementation
     */
    Nihil create(NihilConfig config);
}