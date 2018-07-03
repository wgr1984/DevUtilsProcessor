package de.wr.libdevutils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * annotate the code is only for debug build.
 * <p>
 * <b>force fail</b> if the current build is not debug build.
 */
@Target({
        ElementType.FIELD,
        ElementType.LOCAL_VARIABLE,
        ElementType.METHOD,
        ElementType.PARAMETER,
        ElementType.TYPE})
@Retention(RetentionPolicy.SOURCE)
public @interface DebugOnly {

}
