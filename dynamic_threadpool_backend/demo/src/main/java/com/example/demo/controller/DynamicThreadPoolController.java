package com.example.demo.controller;

import com.example.demo.service.DynamicThreadPoolDemoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 动态线程池演示控制器
 */
@Slf4j
@RestController
@RequestMapping("/dynamic-thread-pool")
public class DynamicThreadPoolController {

    private final DynamicThreadPoolDemoService demoService;

    private final Executor producerExecutor;

    private final Executor consumerExecutor;

    private final Executor orderExecutor;

    private final Executor userExecutor;

    public DynamicThreadPoolController(
            DynamicThreadPoolDemoService demoService,
            @Qualifier("onethreadProducer") Executor producerExecutor,
            @Qualifier("onethreadConsumer") Executor consumerExecutor,
            @Qualifier("orderExecutor") Executor orderExecutor,
            @Qualifier("userExecutor") Executor userExecutor) {
        this.demoService = demoService;
        this.producerExecutor = producerExecutor;
        this.consumerExecutor = consumerExecutor;
        this.orderExecutor = orderExecutor;
        this.userExecutor = userExecutor;
    }

    /**
     * 执行生产者任务
     * @param taskName 任务名称
     * @return 响应结果
     */
    @GetMapping("/producer/execute")
    public String executeProducerTask(@RequestParam(defaultValue = "producer-task") String taskName) {
        log.info("接收到生产者任务请求: {}", taskName);
        demoService.executeProducerTask(taskName);
        return String.format("生产者任务 '%s' 已提交到动态线程池执行", taskName);
    }

    /**
     * 执行消费者任务
     * @param taskName 任务名称
     * @return 响应结果
     */
    @GetMapping("/consumer/execute")
    public String executeConsumerTask(@RequestParam(defaultValue = "consumer-task") String taskName) {
        log.info("接收到消费者任务请求: {}", taskName);
        demoService.executeConsumerTask(taskName);
        return String.format("消费者任务 '%s' 已提交到动态线程池执行", taskName);
    }

    /**
     * 执行批量生产者任务
     * @param count 任务数量
     * @return 响应结果
     */
    @GetMapping("/producer/batch")
    public String executeBatchProducerTasks(@RequestParam(defaultValue = "5") int count) {
        log.info("接收到批量生产者任务请求，任务数量: {}", count);
        demoService.executeBatchProducerTasks(count);
        return String.format("已提交 %d 个生产者任务到动态线程池执行", count);
    }

    /**
     * 执行批量消费者任务
     * @param count 任务数量
     * @return 响应结果
     */
    @GetMapping("/consumer/batch")
    public String executeBatchConsumerTasks(@RequestParam(defaultValue = "5") int count) {
        log.info("接收到批量消费者任务请求，任务数量: {}", count);
        demoService.executeBatchConsumerTasks(count);
        return String.format("已提交 %d 个消费者任务到动态线程池执行", count);
    }

    /**
     * 模拟生产者-消费者模式
     * @param count 数据数量
     * @return 响应结果
     */
    @GetMapping("/producer-consumer")
    public String simulateProducerConsumer(@RequestParam(defaultValue = "3") int count) {
        log.info("接收到生产者-消费者模式请求，数据量: {}", count);
        demoService.simulateProducerConsumer(count);
        return String.format("已启动生产者-消费者模式，数据量: %d", count);
    }

    /**
     * 获取生产者线程池信息
     * @return 线程池状态信息
     */
    @GetMapping("/producer/info")
    public String getProducerThreadPoolInfo() {
        if (producerExecutor instanceof java.util.concurrent.ThreadPoolExecutor) {
            java.util.concurrent.ThreadPoolExecutor executor = (java.util.concurrent.ThreadPoolExecutor) producerExecutor;
            return String.format("生产者线程池状态 - 核心线程数: %d, 最大线程数: %d, 当前线程数: %d, 活跃线程数: %d, 队列大小: %d",
                    executor.getCorePoolSize(),
                    executor.getMaximumPoolSize(),
                    executor.getPoolSize(),
                    executor.getActiveCount(),
                    executor.getQueue().size());
        }
        return "无法获取生产者线程池详细信息";
    }

    /**
     * 获取消费者线程池信息
     * @return 线程池状态信息
     */
    @GetMapping("/consumer/info")
    public String getConsumerThreadPoolInfo() {
        if (consumerExecutor instanceof java.util.concurrent.ThreadPoolExecutor) {
            java.util.concurrent.ThreadPoolExecutor executor = (java.util.concurrent.ThreadPoolExecutor) consumerExecutor;
            return String.format("消费者线程池状态 - 核心线程数: %d, 最大线程数: %d, 当前线程数: %d, 活跃线程数: %d, 队列大小: %d",
                    executor.getCorePoolSize(),
                    executor.getMaximumPoolSize(),
                    executor.getPoolSize(),
                    executor.getActiveCount(),
                    executor.getQueue().size());
        }
        return "无法获取消费者线程池详细信息";
    }

    /**
     * 执行订单处理任务
     * @param orderId 订单ID
     * @return 响应结果
     */
    @GetMapping("/order/execute")
    public String executeOrderTask(@RequestParam(defaultValue = "order-001") String orderId) {
        log.info("接收到订单处理请求: {}", orderId);
        // 模拟订单处理逻辑
        orderExecutor.execute(() -> {
            log.info("开始处理订单: {}", orderId);
            try {
                Thread.sleep(200);
                log.info("订单 {} 处理完成", orderId);
            } catch (InterruptedException e) {
                log.error("订单 {} 处理被中断", orderId, e);
                Thread.currentThread().interrupt();
            }
        });
        return String.format("订单 '%s' 已提交到动态线程池处理", orderId);
    }

    /**
     * 执行用户处理任务
     * @param userId 用户ID
     * @return 响应结果
     */
    @GetMapping("/user/execute")
    public String executeUserTask(@RequestParam(defaultValue = "user-001") String userId) {
        log.info("接收到用户处理请求: {}", userId);
        // 模拟用户处理逻辑
        userExecutor.execute(() -> {
            log.info("开始处理用户: {}", userId);
            try {
                Thread.sleep(150);
                log.info("用户 {} 处理完成", userId);
            } catch (InterruptedException e) {
                log.error("用户 {} 处理被中断", userId, e);
                Thread.currentThread().interrupt();
            }
        });
        return String.format("用户 '%s' 已提交到动态线程池处理", userId);
    }

    /**
     * 获取订单线程池信息
     * @return 线程池状态信息
     */
    @GetMapping("/order/info")
    public String getOrderThreadPoolInfo() {
        if (orderExecutor instanceof ThreadPoolExecutor executor) {
            return String.format("订单线程池状态 - 核心线程数: %d, 最大线程数: %d, 当前线程数: %d, 活跃线程数: %d, 队列大小: %d",
                    executor.getCorePoolSize(),
                    executor.getMaximumPoolSize(),
                    executor.getPoolSize(),
                    executor.getActiveCount(),
                    executor.getQueue().size());
        }
        return "无法获取订单线程池详细信息";
    }

    /**
     * 获取用户线程池信息
     * @return 线程池状态信息
     */
    @GetMapping("/user/info")
    public String getUserThreadPoolInfo() {
        if (userExecutor instanceof java.util.concurrent.ThreadPoolExecutor) {
            java.util.concurrent.ThreadPoolExecutor executor = (java.util.concurrent.ThreadPoolExecutor) userExecutor;
            return String.format("用户线程池状态 - 核心线程数: %d, 最大线程数: %d, 当前线程数: %d, 活跃线程数: %d, 队列大小: %d",
                    executor.getCorePoolSize(),
                    executor.getMaximumPoolSize(),
                    executor.getPoolSize(),
                    executor.getActiveCount(),
                    executor.getQueue().size());
        }
        return "无法获取用户线程池详细信息";
    }

    /**
     * 测试邮件发送功能
     * @return 测试结果
     */
    @GetMapping("/test/email")
    public String testEmailNotification() {
        try {
            // 这里可以手动触发邮件发送来测试HTML格式是否正确
            log.info("邮件通知功能测试 - HTML格式模板已配置");
            return "邮件通知功能测试完成！请检查日志中的HTML模板格式。模板已从Markdown转换为HTML格式，包含样式和颜色。";
        } catch (Exception e) {
            log.error("邮件测试失败", e);
            return "邮件测试失败: " + e.getMessage();
        }
    }

    // ===========================================
    // 压测接口
    // ===========================================

    /**
     * 固定任务数量压测 - 生产者线程池
     * @param taskCount 任务总数
     * @param taskDurationMs 每个任务执行时间(毫秒)
     * @return 压测结果
     */
    @GetMapping("/stress/producer/fixed-tasks")
    public String stressProducerFixedTasks(
            @RequestParam(defaultValue = "1000") int taskCount,
            @RequestParam(defaultValue = "100") int taskDurationMs) {

        log.info("开始生产者线程池固定任务数量压测 - 任务数: {}, 每个任务耗时: {}ms", taskCount, taskDurationMs);

        long startTime = System.currentTimeMillis();
        AtomicInteger completedTasks = new AtomicInteger(0);
        AtomicInteger failedTasks = new AtomicInteger(0);

        CompletableFuture<Void>[] futures = new CompletableFuture[taskCount];

        for (int i = 0; i < taskCount; i++) {
            final int taskId = i;
            futures[i] = CompletableFuture.runAsync(() -> {
                try {
                    // 模拟业务处理时间
                    Thread.sleep(taskDurationMs);
                    completedTasks.incrementAndGet();
                } catch (InterruptedException e) {
                    failedTasks.incrementAndGet();
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    failedTasks.incrementAndGet();
                    log.error("任务 {} 执行异常", taskId, e);
                }
            }, producerExecutor);
        }

        // 等待所有任务完成
        CompletableFuture.allOf(futures).join();
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        double qps = (double) taskCount / (duration / 1000.0);

        return String.format("生产者线程池压测完成 - 总任务数: %d, 完成: %d, 失败: %d, 耗时: %dms, QPS: %.2f",
                taskCount, completedTasks.get(), failedTasks.get(), duration, qps);
    }

    /**
     * 持续时间压测 - 生产者线程池
     * @param durationSeconds 压测持续时间(秒)
     * @param qpsTarget 目标QPS
     * @return 压测结果
     */
    @GetMapping("/stress/producer/duration")
    public String stressProducerDuration(
            @RequestParam(defaultValue = "60") int durationSeconds,
            @RequestParam(defaultValue = "50") int qpsTarget) {

        log.info("开始生产者线程池持续时间压测 - 持续时间: {}秒, 目标QPS: {}", durationSeconds, qpsTarget);

        long startTime = System.currentTimeMillis();
        long endTime = startTime + (durationSeconds * 1000L);

        AtomicLong submittedTasks = new AtomicLong(0);
        AtomicLong completedTasks = new AtomicLong(0);
        AtomicLong failedTasks = new AtomicLong(0);

        // 控制QPS的调度器
        CompletableFuture<Void> stressFuture = CompletableFuture.runAsync(() -> {
            long intervalMs = 1000 / qpsTarget; // 每个任务的间隔时间

            while (System.currentTimeMillis() < endTime) {
                try {
                    long taskId = submittedTasks.incrementAndGet();

                    CompletableFuture.runAsync(() -> {
                        try {
                            // 模拟业务处理时间 (50-150ms随机)
                            Thread.sleep(50 + (long)(Math.random() * 100));
                            completedTasks.incrementAndGet();
                        } catch (InterruptedException e) {
                            failedTasks.incrementAndGet();
                            Thread.currentThread().interrupt();
                        } catch (Exception e) {
                            failedTasks.incrementAndGet();
                        }
                    }, producerExecutor);

                    // 控制QPS
                    Thread.sleep(intervalMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });

        stressFuture.join();
        long actualDuration = System.currentTimeMillis() - startTime;
        double actualQps = (double) completedTasks.get() / (actualDuration / 1000.0);

        return String.format("生产者线程池持续时间压测完成 - 提交任务: %d, 完成: %d, 失败: %d, 实际耗时: %dms, 实际QPS: %.2f",
                submittedTasks.get(), completedTasks.get(), failedTasks.get(), actualDuration, actualQps);
    }

    /**
     * 队列容量压测 - 测试线程池队列满载情况
     * @param concurrentTasks 并发任务数
     * @param taskDurationMs 任务执行时间
     * @return 压测结果
     */
    @GetMapping("/stress/producer/queue-capacity")
    public String stressProducerQueueCapacity(
            @RequestParam(defaultValue = "100") int concurrentTasks,
            @RequestParam(defaultValue = "2000") int taskDurationMs) {

        log.info("开始生产者线程池队列容量压测 - 并发任务数: {}, 任务耗时: {}ms", concurrentTasks, taskDurationMs);

        long startTime = System.currentTimeMillis();
        AtomicInteger completedTasks = new AtomicInteger(0);
        AtomicInteger failedTasks = new AtomicInteger(0);

        CompletableFuture<Void>[] futures = new CompletableFuture[concurrentTasks];

        for (int i = 0; i < concurrentTasks; i++) {
            final int taskId = i;
            futures[i] = CompletableFuture.runAsync(() -> {
                try {
                    // 较长的执行时间，测试队列积压
                    Thread.sleep(taskDurationMs);
                    completedTasks.incrementAndGet();
                } catch (InterruptedException e) {
                    failedTasks.incrementAndGet();
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    failedTasks.incrementAndGet();
                    log.error("队列容量压测任务 {} 执行异常", taskId, e);
                }
            }, producerExecutor);
        }

        // 等待所有任务完成
        CompletableFuture.allOf(futures).join();
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        return String.format("生产者线程池队列容量压测完成 - 并发任务数: %d, 完成: %d, 失败: %d, 总耗时: %dms",
                concurrentTasks, completedTasks.get(), failedTasks.get(), duration);
    }

    /**
     * 混合负载压测 - 同时压测所有线程池
     * @param taskCount 每个线程池的任务数
     * @param taskDurationMs 任务执行时间
     * @return 压测结果
     */
    @GetMapping("/stress/mixed-load")
    public String stressMixedLoad(
            @RequestParam(defaultValue = "200") int taskCount,
            @RequestParam(defaultValue = "300") int taskDurationMs) {

        log.info("开始混合负载压测 - 每个线程池任务数: {}, 任务耗时: {}ms", taskCount, taskDurationMs);

        long startTime = System.currentTimeMillis();

        // 并行执行所有线程池的压测
        CompletableFuture<String> producerFuture = CompletableFuture.supplyAsync(() ->
                stressSinglePool("生产者", producerExecutor, taskCount, taskDurationMs));

        CompletableFuture<String> consumerFuture = CompletableFuture.supplyAsync(() ->
                stressSinglePool("消费者", consumerExecutor, taskCount, taskDurationMs));

        CompletableFuture<String> orderFuture = CompletableFuture.supplyAsync(() ->
                stressSinglePool("订单", orderExecutor, taskCount, taskDurationMs));

        CompletableFuture<String> userFuture = CompletableFuture.supplyAsync(() ->
                stressSinglePool("用户", userExecutor, taskCount, taskDurationMs));

        // 等待所有压测完成
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                producerFuture, consumerFuture, orderFuture, userFuture);

        allFutures.join();
        long endTime = System.currentTimeMillis();
        long totalDuration = endTime - startTime;

        try {
            String result = String.format("混合负载压测完成 - 总耗时: %dms\n", totalDuration) +
                    "生产者: " + producerFuture.get() + "\n" +
                    "消费者: " + consumerFuture.get() + "\n" +
                    "订单: " + orderFuture.get() + "\n" +
                    "用户: " + userFuture.get();

            return result;
        } catch (Exception e) {
            log.error("获取混合负载压测结果失败", e);
            return "混合负载压测失败: " + e.getMessage();
        }
    }

    /**
     * 渐增负载压测 - 逐步增加并发度
     * @param maxConcurrency 最大并发数
     * @param stepDurationSeconds 每阶段持续时间(秒)
     * @return 压测结果
     */
    @GetMapping("/stress/producer/ramp-up")
    public String stressProducerRampUp(
            @RequestParam(defaultValue = "20") int maxConcurrency,
            @RequestParam(defaultValue = "10") int stepDurationSeconds) {

        log.info("开始生产者线程池渐增负载压测 - 最大并发: {}, 每阶段时长: {}秒", maxConcurrency, stepDurationSeconds);

        StringBuilder result = new StringBuilder();
        result.append("渐增负载压测结果:\n");

        for (int concurrency = 1; concurrency <= maxConcurrency; concurrency++) {
            log.info("当前并发度: {}", concurrency);

            long stepStartTime = System.currentTimeMillis();
            AtomicInteger stepCompleted = new AtomicInteger(0);
            AtomicInteger stepFailed = new AtomicInteger(0);

            // 在指定时间内持续提交任务
            long stepEndTime = stepStartTime + (stepDurationSeconds * 1000L);

            while (System.currentTimeMillis() < stepEndTime) {
                for (int i = 0; i < concurrency; i++) {
                    CompletableFuture.runAsync(() -> {
                        try {
                            Thread.sleep(100 + (long)(Math.random() * 200)); // 100-300ms随机执行时间
                            stepCompleted.incrementAndGet();
                        } catch (InterruptedException e) {
                            stepFailed.incrementAndGet();
                            Thread.currentThread().interrupt();
                        } catch (Exception e) {
                            stepFailed.incrementAndGet();
                        }
                    }, producerExecutor);
                }

                // 短暂等待，避免提交过于频繁
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }

            long stepDuration = System.currentTimeMillis() - stepStartTime;
            double stepQps = (double) stepCompleted.get() / (stepDuration / 1000.0);

            result.append(String.format("并发度 %d: 完成 %d, 失败 %d, QPS %.2f\n",
                    concurrency, stepCompleted.get(), stepFailed.get(), stepQps));

            // 检查线程池状态
            if (producerExecutor instanceof java.util.concurrent.ThreadPoolExecutor) {
                java.util.concurrent.ThreadPoolExecutor executor = (java.util.concurrent.ThreadPoolExecutor) producerExecutor;
                result.append(String.format("  线程池状态 - 活跃: %d, 队列: %d\n",
                        executor.getActiveCount(), executor.getQueue().size()));
            }
        }

        return result.toString();
    }

    /**
     * 辅助方法：单线程池压测
     */
    private String stressSinglePool(String poolName, Executor executor, int taskCount, int taskDurationMs) {
        AtomicInteger completed = new AtomicInteger(0);
        AtomicInteger failed = new AtomicInteger(0);

        CompletableFuture<Void>[] futures = new CompletableFuture[taskCount];

        for (int i = 0; i < taskCount; i++) {
            final int taskId = i;
            futures[i] = CompletableFuture.runAsync(() -> {
                try {
                    Thread.sleep(taskDurationMs);
                    completed.incrementAndGet();
                } catch (InterruptedException e) {
                    failed.incrementAndGet();
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    failed.incrementAndGet();
                    log.error("{}池任务 {} 执行异常", poolName, taskId, e);
                }
            }, executor);
        }

        CompletableFuture.allOf(futures).join();
        return String.format("完成 %d, 失败 %d", completed.get(), failed.get());
    }
}
