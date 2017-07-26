package org.springframework.security.access.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
/**
 * For compatible with existence source code
 */
public @interface Secured {
    String[] value();
}