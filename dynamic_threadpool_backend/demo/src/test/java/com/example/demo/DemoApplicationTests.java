package com.example.demo;

import com.xiaozhu.refresh.NacosCloudRefresherHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DemoApplicationTests {

    @Autowired(required = false)
    private NacosCloudRefresherHandler nacosCloudRefresherHandler;

    @Test
    void contextLoads() {
        // 测试应用上下文是否能正常加载
        System.out.println("NacosCloudRefresherHandler exists: " + (nacosCloudRefresherHandler != null));
    }

}
