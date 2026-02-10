package com.xiaozhu;

import java.lang.annotation.*;

/**
 * @author XiaoZhuDaBai
 * @version 1.0
 * @date 2025/8/21 16:25
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DynamicThreadPool {

}
