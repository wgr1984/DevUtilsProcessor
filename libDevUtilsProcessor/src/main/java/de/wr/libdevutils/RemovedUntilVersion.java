package de.wr.libdevutils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/*
 * Created by wolfgangreithmeier on 17.04.17.
 */
@Target({
        ElementType.FIELD,
        ElementType.LOCAL_VARIABLE,
        ElementType.METHOD,
        ElementType.PARAMETER,
        ElementType.TYPE})
@Retention(RetentionPolicy.SOURCE)
public @interface RemovedUntilVersion {
    String value();
}
