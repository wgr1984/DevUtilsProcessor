package de.wr.libdevutils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/*
 * Created by wolfgangreithmeier on 17.04.17.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.SOURCE)
public @interface Debug {
    boolean allowNonPrivate() default false;
    String methodPattern() default ".*[Dd]ebug.*";
}
