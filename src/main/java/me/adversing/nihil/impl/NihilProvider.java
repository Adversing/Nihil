package me.adversing.nihil.impl;

import me.adversing.nihil.Nihil;
import me.adversing.nihil.intf.INihilProvider;
import me.adversing.nihil.config.NihilConfig;

/**
 * Default provider implementation for the NihilImpl.
 */
public class NihilProvider implements INihilProvider {

    @Override
    public Nihil create(NihilConfig config) {
        return new NihilImpl(config);
    }
}