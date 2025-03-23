package me.adversing.nihil.annotation;

import me.adversing.nihil.intf.IPropertyHandler;
import me.adversing.nihil.handler.DefaultPropertyHandler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark and configure properties for updating.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface UpdateProperty {

    /**
     * The name of the target property if different from the source property.
     */
    String targetProperty() default "";

    /**
     * Class of the handler to use for custom property mapping.
     */
    Class<? extends IPropertyHandler> handler() default DefaultPropertyHandler.class;

    /**
     * Whether to include this property in updates when null.
     */
    boolean includeNull() default false;
}