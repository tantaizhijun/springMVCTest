package com.me.TestOther;

import java.lang.annotation.*;

/**
 * 自定义注解
 */
@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyMethod {
    String value() default "";
}
