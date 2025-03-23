package me.adversing.nihil.intf;

/**
 * Interface for handling custom property transformations.
 *
 * @param <T> The type of value being processed
 */
public interface IPropertyHandler<T> {

    /**
     * Process the source value before setting it on the target.
     *
     * @param value The source value to process
     * @return The processed value to set on the target
     */
    Object process(T value);
}