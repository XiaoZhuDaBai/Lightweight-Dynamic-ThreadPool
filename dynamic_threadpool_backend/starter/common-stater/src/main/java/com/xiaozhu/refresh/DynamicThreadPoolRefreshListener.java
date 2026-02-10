package com.xiaozhu.refresh;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import com.xiaozhu.config.BootstrapConfigProperties;
import com.xiaozhu.executor.ThreadPoolExecutorHolder;
import com.xiaozhu.executor.ThreadPoolExecutorProperties;
import com.xiaozhu.executor.XiaozhuThreadRegistry;
import com.xiaozhu.executor.myenum.BlockingQueueTypeEnum;
import com.xiaozhu.executor.myenum.RejectedPolicyTypeEnum;
import com.xiaozhu.executor.resize.ResizableCapacityLinkedBlockingQueue;
import com.xiaozhu.notification.dto.ThreadPoolConfigChangeDTO;
import com.xiaozhu.notification.service.NotifierDispatcher;
import com.xiaozhu.support.ApplicationContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.xiaozhu.text.text.CHANGE_DELIMITER;
import static com.xiaozhu.text.text.CHANGE_THREAD_POOL_TEXT;

/**
 * 监听动态线程池配置更新事件
 * @author XiaoZhuDaBai
 * @version 1.0
 * @date 2025/8/22 17:28
 */
@Slf4j
@RequiredArgsConstructor
public class DynamicThreadPoolRefreshListener implements ApplicationListener<ThreadPoolConfigUpdateEvent> {
    private final NotifierDispatcher dispatcher;

    @Override
    public void onApplicationEvent(ThreadPoolConfigUpdateEvent event) {
        log.info("接收到线程池配置更新事件");
        BootstrapConfigProperties refresherProperties = event.getBootstrapConfigProperties();

        // 检查远程配置文件是否包含线程池配置
        if (CollUtil.isEmpty(refresherProperties.getExecutors())) {
            log.warn("远程配置中没有找到线程池配置");
            return;
        }
        log.info("开始处理 {} 个线程池配置更新", refresherProperties.getExecutors().size());

        // 刷新动态线程池对象核心参数
        for (ThreadPoolExecutorProperties remoteProperties : refresherProperties.getExecutors()) {
            // 检查线程池配置是否发生变化（与当前内存中的配置对比）
            boolean changed = hasThreadPoolConfigChanged(remoteProperties);
            if (!changed) {
                continue;
            }

            // 将远程配置应用到线程池，更新相关参数
            updateThreadPoolFromRemoteConfig(remoteProperties);

            // 线程池参数变更后进行日志打印
            String threadPoolId = remoteProperties.getThreadPoolId();
            ThreadPoolExecutorHolder holder = XiaozhuThreadRegistry.getHolder(threadPoolId);
            ThreadPoolExecutorProperties originalProperties = holder.getExecutorProperties();
            holder.setExecutorProperties(remoteProperties);

            // 发送线程池配置变更消息通知
            sendThreadPoolConfigChangeMessage(originalProperties, remoteProperties);

            // 打印线程池配置变更日志
            log.info(CHANGE_THREAD_POOL_TEXT,
                    threadPoolId,
                    String.format(CHANGE_DELIMITER, originalProperties.getCorePoolSize(), remoteProperties.getCorePoolSize()),
                    String.format(CHANGE_DELIMITER, originalProperties.getMaximumPoolSize(), remoteProperties.getMaximumPoolSize()),
                    String.format(CHANGE_DELIMITER, originalProperties.getQueueCapacity(), remoteProperties.getQueueCapacity()),
                    String.format(CHANGE_DELIMITER, originalProperties.getKeepAliveTime(), remoteProperties.getKeepAliveTime()),
                    String.format(CHANGE_DELIMITER, originalProperties.getRejectedHandler(), remoteProperties.getRejectedHandler()),
                    String.format(CHANGE_DELIMITER, originalProperties.getAllowCoreThreadTimeOut(), remoteProperties.getAllowCoreThreadTimeOut()));
        }
    }

    // 更新线程池参数
    private void updateThreadPoolFromRemoteConfig(ThreadPoolExecutorProperties remoteProperties) {
        String threadPoolId = remoteProperties.getThreadPoolId();
        log.info("开始更新线程池参数: {}", threadPoolId);

        ThreadPoolExecutorHolder holder = XiaozhuThreadRegistry.getHolder(threadPoolId);
        ThreadPoolExecutor executor = holder.getExecutor();
        ThreadPoolExecutorProperties originalProperties = holder.getExecutorProperties();

        log.info("线程池 {} 当前状态 - 核心: {}, 最大: {}, 活跃: {}",
                threadPoolId, executor.getCorePoolSize(), executor.getMaximumPoolSize(), executor.getActiveCount());

        // 检查队列类型是否兼容，避免IllegalArgumentException
        if (!isQueueTypeCompatible(remoteProperties, executor)) {
            log.warn("线程池 {} 队列类型变更需要重启应用生效，跳过参数更新。当前队列: {}, 配置队列: {}",
                    threadPoolId, executor.getQueue().getClass().getSimpleName(), remoteProperties.getWorkQueue());
            return;
        }

        Integer remoteCorePoolSize = remoteProperties.getCorePoolSize();
        Integer remoteMaximumPoolSize = remoteProperties.getMaximumPoolSize();

        if (remoteCorePoolSize != null && remoteMaximumPoolSize != null) {
            log.info("更新线程池 {} 核心线程数: {} -> {}, 最大线程数: {} -> {}",
                    threadPoolId, executor.getCorePoolSize(), remoteCorePoolSize,
                    executor.getMaximumPoolSize(), remoteMaximumPoolSize);

            try {
                // jdk17+ 对 corePoolSize 和 maximumPoolSize 的限制
                if (remoteCorePoolSize > remoteMaximumPoolSize) {
                    // 先提高最大容量
                    executor.setMaximumPoolSize(remoteMaximumPoolSize);
                    executor.setCorePoolSize(remoteCorePoolSize);
                } else {
                    executor.setCorePoolSize(remoteCorePoolSize);
                    executor.setMaximumPoolSize(remoteMaximumPoolSize);
                }

                log.info("线程池 {} 参数更新完成 - 核心: {}, 最大: {}",
                        threadPoolId, executor.getCorePoolSize(), executor.getMaximumPoolSize());
            } catch (IllegalArgumentException e) {
                log.error("线程池 {} 参数更新失败，队列类型不兼容: {}", threadPoolId, e.getMessage());
                return;
            }
        } else {
            try {
                if (remoteCorePoolSize != null) {
                    log.info("单独更新线程池 {} 核心线程数: {} -> {}", threadPoolId, executor.getCorePoolSize(), remoteCorePoolSize);
                    executor.setCorePoolSize(remoteCorePoolSize);
                }
                if (remoteMaximumPoolSize != null) {
                    log.info("单独更新线程池 {} 最大线程数: {} -> {}", threadPoolId, executor.getMaximumPoolSize(), remoteMaximumPoolSize);
                    executor.setMaximumPoolSize(remoteMaximumPoolSize);
                }
            } catch (IllegalArgumentException e) {
                log.error("线程池 {} 参数更新失败: {}", threadPoolId, e.getMessage());
                return;
            }
        }

        // 允许超时设置
        if (remoteProperties.getAllowCoreThreadTimeOut() != null &&
                !Objects.equals(remoteProperties.getAllowCoreThreadTimeOut(), originalProperties.getAllowCoreThreadTimeOut())) {
            executor.allowCoreThreadTimeOut(remoteProperties.getAllowCoreThreadTimeOut());
        }
        // 拒绝策略设置
        if (remoteProperties.getRejectedHandler() != null &&
                !Objects.equals(remoteProperties.getRejectedHandler(), originalProperties.getRejectedHandler())) {
            RejectedExecutionHandler handler = RejectedPolicyTypeEnum.createPolicy(remoteProperties.getRejectedHandler());
            executor.setRejectedExecutionHandler(handler);
        }

        // 存活时间设置
        if (remoteProperties.getKeepAliveTime() != null &&
                !Objects.equals(remoteProperties.getKeepAliveTime(), originalProperties.getKeepAliveTime())) {
            executor.setKeepAliveTime(remoteProperties.getKeepAliveTime(), TimeUnit.SECONDS);
        }

        //todo 更新队列容量(扩展数组阻塞队列)
        if (isQueueCapacityChanged(originalProperties, remoteProperties, executor)) {
            BlockingQueue<Runnable> queue = executor.getQueue();
            ResizableCapacityLinkedBlockingQueue<?> resizableQueue = null;
            if (queue instanceof ResizableCapacityLinkedBlockingQueue<?>) {
                resizableQueue = (ResizableCapacityLinkedBlockingQueue<?>) queue;
            }

            assert resizableQueue != null;
            resizableQueue.setCapacity(remoteProperties.getQueueCapacity());
        }
    }

    /**
     * 发送动态线程池配置更新消息
     * @param originalProperties
     * @param remoteProperties
     */
    @SneakyThrows
    private void sendThreadPoolConfigChangeMessage(ThreadPoolExecutorProperties originalProperties,
                                                   ThreadPoolExecutorProperties remoteProperties) {
        Environment environment = ApplicationContextHolder.getBean(Environment.class);
        String activeProfile = environment.getProperty("spring.profiles.active", "dev");
        String applicationName = environment.getProperty("spring.application.name");

        // 储存变更前后的数据
        Map<String, ThreadPoolConfigChangeDTO.ChangePair<?>> changes = new HashMap<>();
        changes.put("corePoolSize", new ThreadPoolConfigChangeDTO.ChangePair<>(originalProperties.getCorePoolSize(), remoteProperties.getCorePoolSize()));
        changes.put("maximumPoolSize", new ThreadPoolConfigChangeDTO.ChangePair<>(originalProperties.getMaximumPoolSize(), remoteProperties.getMaximumPoolSize()));
        changes.put("queueCapacity", new ThreadPoolConfigChangeDTO.ChangePair<>(originalProperties.getQueueCapacity(), remoteProperties.getQueueCapacity()));
        changes.put("rejectedHandler", new ThreadPoolConfigChangeDTO.ChangePair<>(originalProperties.getRejectedHandler(), remoteProperties.getRejectedHandler()));
        changes.put("keepAliveTime", new ThreadPoolConfigChangeDTO.ChangePair<>(originalProperties.getKeepAliveTime(), remoteProperties.getKeepAliveTime()));

        // 获取接收人邮箱，优先级：线程池配置 > 全局邮件配置 > 默认值
        String receives = null;

        // 优先从线程池配置获取
        if (remoteProperties.getNotify() != null) {
            receives = remoteProperties.getNotify().getReceivers();
        }

        // 如果线程池配置中没有，则从全局邮件配置中获取
        if (receives == null || receives.trim().isEmpty()) {
            BootstrapConfigProperties.NotifyPlatformsConfig notifyConfig =
                BootstrapConfigProperties.getInstance().getNotifyPlatforms();
            if (notifyConfig != null && notifyConfig.getEmail() != null) {
                receives = notifyConfig.getEmail().getReceivers();
            }
        }

        // 如果还是没有，则使用发件人邮箱作为默认接收者
        if (receives == null || receives.trim().isEmpty()) {
            BootstrapConfigProperties.NotifyPlatformsConfig notifyConfig =
                BootstrapConfigProperties.getInstance().getNotifyPlatforms();
            if (notifyConfig != null && notifyConfig.getEmail() != null) {
                receives = notifyConfig.getEmail().getAccount();
            }
        }

        // 如果还是没有，则使用硬编码的默认值
        if (receives == null || receives.trim().isEmpty()) {
            receives = "1105774747@qq.com";
        }

        ThreadPoolConfigChangeDTO configChangeDTO = ThreadPoolConfigChangeDTO.builder()
                .activeProfile(activeProfile)
                .identify(InetAddress.getLocalHost().getHostAddress())
                .applicationName(applicationName)
                .threadPoolId(originalProperties.getThreadPoolId())
                .receives(receives)
                .workQueue(originalProperties.getWorkQueue())
                .changes(changes)
                .updateTime(DateUtil.now())
                .build();
        dispatcher.sendChangeMessage(configChangeDTO);
    }

    /**
     *
     * @param remoteProperties
     * @return
     */
    private boolean hasThreadPoolConfigChanged(ThreadPoolExecutorProperties remoteProperties) {
        String threadPoolId = remoteProperties.getThreadPoolId();
        log.info("检查线程池配置变化: {}", threadPoolId);

        ThreadPoolExecutorHolder holder = XiaozhuThreadRegistry.getHolder(threadPoolId);
        if (holder == null) {
            log.warn("没有找到该线程池的id: {}, 注册表中的线程池: {}", threadPoolId,
                    XiaozhuThreadRegistry.getAllHolders().stream()
                            .map(ThreadPoolExecutorHolder::getThreadPoolId)
                            .toList());
            return false;
        }
        log.info("找到线程池: {}", threadPoolId);

        ThreadPoolExecutor executor = holder.getExecutor();
        ThreadPoolExecutorProperties originalProperties = holder.getExecutorProperties();

        return hasDifference(originalProperties, remoteProperties, executor);
    }

    /**
     * 对比当前文件和远程文件参数是否有变化
     * @param originalProperties
     * @param remoteProperties
     * @param executor
     * @return
     */
    private boolean hasDifference(ThreadPoolExecutorProperties originalProperties,
                                  ThreadPoolExecutorProperties remoteProperties,
                                  ThreadPoolExecutor executor) {
        return isChanged(originalProperties.getCorePoolSize(), remoteProperties.getCorePoolSize())
                || isChanged(originalProperties.getMaximumPoolSize(), remoteProperties.getMaximumPoolSize())
                || isChanged(originalProperties.getKeepAliveTime(), remoteProperties.getKeepAliveTime())
                || isChanged(originalProperties.getAllowCoreThreadTimeOut(), remoteProperties.getAllowCoreThreadTimeOut())
                || isChanged(originalProperties.getRejectedHandler(), remoteProperties.getRejectedHandler())
                // 是否改变阻塞队列容量
                || isQueueCapacityChanged(originalProperties, remoteProperties, executor);
    }

    private <T> boolean isChanged(T before, T after) {
        return after != null && !Objects.equals(before, after);
    }

    private boolean isQueueCapacityChanged(ThreadPoolExecutorProperties originalProperties,
                                  ThreadPoolExecutorProperties remoteProperties,
                                  ThreadPoolExecutor executor) {
        Integer original = originalProperties.getQueueCapacity();
        Integer remote = remoteProperties.getQueueCapacity();
        BlockingQueue<?> queue = executor.getQueue();

        return remote != null && !Objects.equals(original, remote) &&
                // 队列类型为 ResizableCapacityLinkedBlockingQueue
                Objects.equals(BlockingQueueTypeEnum.RESIZABLE_CAPACITY_LINKED_BLOCKING_QUEUE.getName(), queue.getClass().getSimpleName());
    }

    /**
     * 检查队列类型是否兼容，避免动态更新时的IllegalArgumentException
     */
    private boolean isQueueTypeCompatible(ThreadPoolExecutorProperties remoteProperties, ThreadPoolExecutor executor) {
        String remoteWorkQueue = remoteProperties.getWorkQueue();
        if (remoteWorkQueue == null) {
            return true; // 没有指定队列类型，认为是兼容的
        }

        BlockingQueue<?> currentQueue = executor.getQueue();
        String currentQueueType = currentQueue.getClass().getSimpleName();

        // 队列类型匹配检查
        return Objects.equals(remoteWorkQueue, currentQueueType) ||
               // 允许从 LinkedBlockingQueue 到 ResizableCapacityLinkedBlockingQueue 的转换
               (Objects.equals(remoteWorkQueue, BlockingQueueTypeEnum.RESIZABLE_CAPACITY_LINKED_BLOCKING_QUEUE.getName()) &&
                Objects.equals(currentQueueType, BlockingQueueTypeEnum.LINKED_BLOCKING_QUEUE.getName()));
    }

}
