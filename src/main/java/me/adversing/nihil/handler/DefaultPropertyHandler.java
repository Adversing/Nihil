package me.adversing.nihil.handler;

import me.adversing.nihil.intf.IPropertyHandler;

/**
 * Default implementation of IPropertyHandler that returns the value unchanged.
 */
public class DefaultPropertyHandler<T> implements IPropertyHandler<T> {

    @Override
    public Object process(T value) {
        return value;
    }
}