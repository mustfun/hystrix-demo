package com.dzy.learn.annotation;

import com.dzy.learn.enums.HystrixType;

import java.lang.annotation.*;

/**
 * @author dengzhiyuan
 * @version 1.0
 * @date 2017/8/9
 * @since 1.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface Hystrix {
    /**
     * 断路器的配置
     */
    String config();

    HystrixType type() default HystrixType.SUCCESS;
}
