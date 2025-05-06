package io.github.kurrycat.mpkmod.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({})
public @interface Option {
    /**
     * Marks a field as an Option. The field has to be public.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface Field {
        String category() default "";

        String displayName() default "";

        String description() default "";

        boolean showInOptionList() default true;
    }

    /**
     * Marks a method as an Option ChangeListener. The method has to be public.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface ChangeListener {
        String field();
    }
}
