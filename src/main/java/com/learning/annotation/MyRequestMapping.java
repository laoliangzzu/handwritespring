package com.learning.annotation;

import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.Mapping;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义RequestMapping
 * @author bxd
 * @date 2019/5/27.
 */
@Retention(RetentionPolicy.RUNTIME)
@Mapping
@Target({ElementType.TYPE,ElementType.METHOD})
@Component
public @interface MyRequestMapping {
    String name();
}
