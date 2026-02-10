package com.xiaozhu.enable;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author XiaoZhuDaBai
 * @version 1.0
 * @date 2025/8/21 16:27
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(MarkerConfiguration.class)
public @interface EnableXiaozhuThread {

}
