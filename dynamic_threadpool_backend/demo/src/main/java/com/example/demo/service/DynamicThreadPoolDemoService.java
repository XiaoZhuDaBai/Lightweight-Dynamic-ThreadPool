package com.example.demo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * 动态线程池演示服务
 */
@Slf4j
@Service
public class DynamicThreadPoolDemoService {

    private final Executor onethreadProducer;

    private final Executor onethreadConsumer;

    public DynamicThreadPoolDemoService(
            @Qualifier("onethreadProducer") Executor onethreadProducer,
            @Qualifier("onethreadConsumer") Executor onethreadConsumer) {
        this.onethreadProducer = onethreadProducer;
        this.onethreadConsumer = onethreadConsumer;
    }

    /**
     * 使用生产者线程池执行异步任务
     * @param taskName 任务名称
     */
    public void executeProducerTask(String taskName) {
        CompletableFuture.runAsync(() -> {
            log.info("生产者线程池开始执行任务: {}", taskName);
            try {
                // 模拟生产者任务执行时间
                Thread.sleep(1500);
                log.info("生产者任务 {} 执行完成", taskName);
            } catch (InterruptedException e) {
                log.error("生产者任务 {} 被中断", taskName, e);
                Thread.currentThread().interrupt();
            }
        }, onethreadProducer).exceptionally(throwable -> {
            log.error("生产者任务 {} 执行异常", taskName, throwable);
            return null;
        });
    }

    /**
     * 使用消费者线程池执行异步任务
     * @param taskName 任务名称
     */
    public void executeConsumerTask(String taskName) {
        CompletableFuture.runAsync(() -> {
            log.info("消费者线程池开始执行任务: {}", taskName);
            try {
                // 模拟消费者任务执行时间
                Thread.sleep(1000);
                log.info("消费者任务 {} 执行完成", taskName);
            } catch (InterruptedException e) {
                log.error("消费者任务 {} 被中断", taskName, e);
                Thread.currentThread().interrupt();
            }
        }, onethreadConsumer).exceptionally(throwable -> {
            log.error("消费者任务 {} 执行异常", taskName, throwable);
            return null;
        });
    }

    /**
     * 批量执行生产者任务，演示线程池的并发能力
     * @param taskCount 任务数量
     */
    public void executeBatchProducerTasks(int taskCount) {
        log.info("生产者线程池开始批量执行 {} 个任务", taskCount);

        CompletableFuture<Void>[] futures = new CompletableFuture[taskCount];
        for (int i = 0; i < taskCount; i++) {
            final int taskId = i;
            futures[i] = CompletableFuture.runAsync(() -> {
                log.info("执行生产者批量任务 {}", taskId);
                try {
                    // 模拟不同执行时间的任务
                    Thread.sleep(800 + (taskId % 2) * 400);
                    log.info("生产者批量任务 {} 完成", taskId);
                } catch (InterruptedException e) {
                    log.error("生产者批量任务 {} 被中断", taskId, e);
                    Thread.currentThread().interrupt();
                }
            }, onethreadProducer);
        }

        // 等待所有任务完成
        CompletableFuture.allOf(futures).thenRun(() -> {
            log.info("所有生产者批量任务执行完成");
        }).exceptionally(throwable -> {
            log.error("生产者批量任务执行异常", throwable);
            return null;
        });
    }

    /**
     * 批量执行消费者任务，演示线程池的并发能力
     * @param taskCount 任务数量
     */
    public void executeBatchConsumerTasks(int taskCount) {
        log.info("消费者线程池开始批量执行 {} 个任务", taskCount);

        CompletableFuture<Void>[] futures = new CompletableFuture[taskCount];
        for (int i = 0; i < taskCount; i++) {
            final int taskId = i;
            futures[i] = CompletableFuture.runAsync(() -> {
                log.info("执行消费者批量任务 {}", taskId);
                try {
                    // 模拟不同执行时间的任务
                    Thread.sleep(600 + (taskId % 3) * 300);
                    log.info("消费者批量任务 {} 完成", taskId);
                } catch (InterruptedException e) {
                    log.error("消费者批量任务 {} 被中断", taskId, e);
                    Thread.currentThread().interrupt();
                }
            }, onethreadConsumer);
        }

        // 等待所有任务完成
        CompletableFuture.allOf(futures).thenRun(() -> {
            log.info("所有消费者批量任务执行完成");
        }).exceptionally(throwable -> {
            log.error("消费者批量任务执行异常", throwable);
            return null;
        });
    }

    /**
     * 模拟生产者-消费者模式
     * 先用生产者线程池生产数据，然后用消费者线程池消费数据
     * @param dataCount 数据数量
     */
    public void simulateProducerConsumer(int dataCount) {
        log.info("开始模拟生产者-消费者模式，数据量: {}", dataCount);

        // 生产者阶段
        CompletableFuture<Void>[] producerFutures = new CompletableFuture[dataCount];
        for (int i = 0; i < dataCount; i++) {
            final int dataId = i;
            producerFutures[i] = CompletableFuture.runAsync(() -> {
                log.info("生产者生产数据 {}", dataId);
                try {
                    Thread.sleep(500);
                    log.info("数据 {} 生产完成", dataId);
                } catch (InterruptedException e) {
                    log.error("生产数据 {} 时被中断", dataId, e);
                    Thread.currentThread().interrupt();
                }
            }, onethreadProducer);
        }

        // 等待生产完成，然后开始消费
        CompletableFuture.allOf(producerFutures).thenRun(() -> {
            log.info("所有数据生产完成，开始消费...");

            // 消费者阶段
            CompletableFuture<Void>[] consumerFutures = new CompletableFuture[dataCount];
            for (int i = 0; i < dataCount; i++) {
                final int dataId = i;
                consumerFutures[i] = CompletableFuture.runAsync(() -> {
                    log.info("消费者消费数据 {}", dataId);
                    try {
                        Thread.sleep(400);
                        log.info("数据 {} 消费完成", dataId);
                    } catch (InterruptedException e) {
                        log.error("消费数据 {} 时被中断", dataId, e);
                        Thread.currentThread().interrupt();
                    }
                }, onethreadConsumer);
            }

            // 等待消费完成
            CompletableFuture.allOf(consumerFutures).thenRun(() -> {
                log.info("生产者-消费者模式执行完成");
            }).exceptionally(throwable -> {
                log.error("消费者阶段执行异常", throwable);
                return null;
            });
        }).exceptionally(throwable -> {
            log.error("生产者阶段执行异常", throwable);
            return null;
        });
    }
}
