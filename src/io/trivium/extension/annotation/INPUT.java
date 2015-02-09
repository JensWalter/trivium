package io.trivium.extension.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface INPUT {
    String typeId();
    String condition() default "";
}
