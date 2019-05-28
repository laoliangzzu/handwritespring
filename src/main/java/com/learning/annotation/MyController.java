package com.learning.annotation;

import java.lang.annotation.*;

/**
 * 自定义Controller
 * @author bxd
 * @date 2019/5/27.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyController {

}
