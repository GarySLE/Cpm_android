package com.ys.cpm.db;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 运行时注解 Column
 */

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Column { //todo kotlin转换

    enum Type {
        TEXT, INTEGER, LONG, BOOLEAN, BLOB,
    }

    Type type() default Type.TEXT;

    boolean unique() default false;

    boolean notnull() default false;
}