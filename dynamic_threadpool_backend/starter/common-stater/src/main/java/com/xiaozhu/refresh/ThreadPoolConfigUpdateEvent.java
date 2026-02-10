package com.xiaozhu.refresh;

import com.xiaozhu.config.BootstrapConfigProperties;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

/**
 * @author XiaoZhuDaBai
 * @version 1.0
 * @date 2025/8/22 16:42
 */
@Getter
@Setter
public class ThreadPoolConfigUpdateEvent extends ApplicationEvent {

    private BootstrapConfigProperties bootstrapConfigProperties;

    public ThreadPoolConfigUpdateEvent(Object source, BootstrapConfigProperties bootstrapConfigProperties) {
        super(source);
        this.bootstrapConfigProperties = bootstrapConfigProperties;
    }
}