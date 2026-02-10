package com.xiaozhu.executor.myenum;

import com.xiaozhu.executor.resize.ResizableCapacityLinkedBlockingQueue;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;

/**
 * @author XiaoZhuDaBai
 * @version 1.0
 * @date 2025/8/16 17:06
 */
@Getter
public enum BlockingQueueTypeEnum {

    LINKED_BLOCKING_QUEUE("LinkedBlockingQueue") {
        @Override
        public <T> BlockingQueue<T> of(Integer capacity) {
            return new LinkedBlockingQueue<>(capacity);
        }

        @Override
        public <T> BlockingQueue<T> of() {
            return new LinkedBlockingQueue<>();
        }
    },
    ARRAY_BLOCKING_QUEUE("ArrayBlockingQueue") {
        @Override
        public <T> BlockingQueue<T> of(Integer capacity) {
            return new ArrayBlockingQueue<>(capacity);
        }

        @Override
        public <T> BlockingQueue<T> of() {
            return new ArrayBlockingQueue<>(DEFAULT_CAPACITY);
        }
    },
    SYNCHRONOUS_QUEUE("SynchronousQueue") {
        @Override
        public <T> BlockingQueue<T> of(Integer capacity) {
            return new SynchronousQueue<>();
        }

        @Override
        public <T> BlockingQueue<T> of() {
            return new SynchronousQueue<>();
        }
    },
    /**
     * todo 补充阻塞队列类型
     */
    RESIZABLE_CAPACITY_LINKED_BLOCKING_QUEUE("ResizableCapacityLinkedBlockingQueue") {
        @Override
        <T> BlockingQueue<T> of(Integer capacity) {
            return new ResizableCapacityLinkedBlockingQueue<>(capacity);
        }

        @Override
        <T> BlockingQueue<T> of() {
            return new ResizableCapacityLinkedBlockingQueue<>();
        }
    };


    private final String name;
    private static final int DEFAULT_CAPACITY = 1314;

    BlockingQueueTypeEnum(String name) {
        this.name = name;
    }
    abstract <T> BlockingQueue<T> of(Integer capacity);
    // 默认容量
    abstract <T> BlockingQueue<T> of();

    /**
     * 根据名称设置注册表
     */
    private static final Map<String, BlockingQueueTypeEnum> NAME_TO_ENUM_MAP;

    static {
        final BlockingQueueTypeEnum[] values = BlockingQueueTypeEnum.values();
        NAME_TO_ENUM_MAP = new HashMap<>(values.length);
        for (BlockingQueueTypeEnum value : values) {
            NAME_TO_ENUM_MAP.put(value.name, value);
        }
    }

    public static <T> BlockingQueue<T> createBlockingQueue(String blockingQueueName, Integer capacity) {
        BlockingQueue<T> blockingQueue = of(blockingQueueName, capacity);
        if (blockingQueue != null) {
            return blockingQueue;
        }

        throw new IllegalArgumentException("找不到匹配的阻塞队列： " + blockingQueueName);
    }

    //这里有有界队列和无界队列的判断
    private static <T> BlockingQueue<T> of(String blockingQueueName, Integer capacity) {
        BlockingQueueTypeEnum blockingQueueTypeEnum = NAME_TO_ENUM_MAP.get(blockingQueueName);
        if (blockingQueueTypeEnum == null) {
            return null;
        }
        return Objects.isNull(capacity) ? blockingQueueTypeEnum.of() : blockingQueueTypeEnum.of(capacity);
    }
}
