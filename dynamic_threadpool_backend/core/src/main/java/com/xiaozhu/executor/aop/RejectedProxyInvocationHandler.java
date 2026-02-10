package com.xiaozhu.executor.aop;

/**
 * @author XiaoZhuDaBai
 * @version 1.0
 * @date 2025/8/16 16:59
 */

import lombok.AllArgsConstructor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 动态代理实现拒绝策略代理器
 */
@AllArgsConstructor
public class RejectedProxyInvocationHandler implements InvocationHandler {

    private final Object target;
    // 记录拒绝策略调用次数
    private final AtomicLong rejectCount;
    private static final String REJECT_METHOD = "rejectedExecution";

    //todo
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (REJECT_METHOD.equals(method.getName()) &&
                args != null &&
                args.length == 2 &&
                args[0] instanceof Runnable &&
                args[1] instanceof ThreadPoolExecutor) {
            // 拒绝策略调用次数加1
            rejectCount.incrementAndGet();
        }

        if (method.getName().equals("toString") && method.getParameterCount() == 0) {
            return target.getClass().getSimpleName();
        }

        try {
            return method.invoke(target, args);
        } catch (InvocationTargetException ex) {
            throw ex.getCause();
        }
    }
}
