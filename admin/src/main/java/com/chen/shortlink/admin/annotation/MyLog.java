package com.chen.shortlink.admin.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface MyLog {

    /**
     * 模块标题
     * @return
     */
    String title() default "";

    /**
     * 日志内容
     * @return
     */
    String content() default "";
}
