package com.example.demo;

import com.xiaozhu.config.BootstrapConfigProperties;
import com.xiaozhu.enable.EnableXiaozhuThread;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@EnableXiaozhuThread
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

}
