# Xiaozhu Dynamic ThreadPool Framework

[![](https://img.shields.io/badge/JDK-17%2B-green)](#) [![](https://img.shields.io/badge/Spring%20Boot-3.0%2B-blue)](#) [![](https://img.shields.io/badge/license-MIT-lightgrey)](#) [![](https://img.shields.io/github/stars/xiaozhu/dynamic-threadpool?style=social)](#)

English | [中文](README_ZH.md)

Xiaozhu Dynamic ThreadPool is a lightweight dynamic thread pool management solution based on Spring Boot, providing comprehensive support for thread pool governance in business systems.

## Features

- **Dynamic Configuration**: Modify thread pool parameters at runtime without restarting the application
- **Real-time Monitoring**: Rich monitoring metrics to understand thread pool status
- **Smart Alerting**: Built-in multiple alerting strategies for timely problem detection
- **Easy Integration**: Spring Boot-based, out-of-the-box solution
- **Nacos Integration**: Configuration center support for centralized management
- **Multiple Queue Types**: SynchronousQueue, LinkedBlockingQueue, ArrayBlockingQueue, ResizableCapacityLinkedBlockingQueue

## Architecture

```
┌─────────────────────────────────────┐
│           demo (Demo Project)        │
└─────────────────┬───────────────────┘
                  │
                  ▼
        ┌─────────┴─────────┐
        │   nacos-starter   │
        │  (Nacos Starter)  │
        └─────────┬─────────┘
                  │
                  ▼
        ┌─────────┴─────────┐
        │   common-stater   │
        │ (Common Starter)  │
        └─────────┬─────────┘
                  │
                  ▼
        ┌─────────┴─────────┐
        │    spring-base    │
        │ (Spring Base)     │
        └─────────┬─────────┘
                  │
                  ▼
        ┌─────────┴─────────┐
        │       core        │
        │   (Core Module)   │
        └───────────────────┘
```

### Module Description

| Module | Description | Dependencies |
|--------|-------------|--------------|
| **core** | Core business logic, thread pool implementation, monitoring, alerting, notification | None |
| **spring-base** | Spring integration layer, provides `@EnableXiaozhuThread` annotation | core |
| **common-stater** | Common starter with auto-configuration and AOP support | spring-base |
| **nacos-starter** | Nacos integration for configuration center support | common-stater |
| **demo** | Demo project showing usage examples | nacos-starter |

## Requirements

- JDK 17+
- Maven 3.6+
- Spring Boot 3.0+
- Spring Cloud 2022.0+
- Spring Cloud Alibaba 2022.0+ (when using Nacos)

## Quick Start

### 1. Add Dependencies

**Recommended: Using Nacos Configuration Center**

```xml
<dependency>
    <groupId>xiaozhu.dabai</groupId>
    <artifactId>nacos-starter</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

**Basic: Local Configuration Only**

```xml
<dependency>
    <groupId>xiaozhu.dabai</groupId>
    <artifactId>common-stater</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

### 2. Enable Framework

Add the `@EnableXiaozhuThread` annotation to your Spring Boot main class:

```java
@SpringBootApplication
@EnableXiaozhuThread
public class YourApplication {
    public static void main(String[] args) {
        SpringApplication.run(YourApplication.class, args);
    }
}
```

### 3. Configure Thread Pools

Add configuration in `application.yaml`:

```yaml
server:
  port: 8080

spring:
  application:
    name: your-app-name

xiaozhu:
  dynamicPool: true
  monitorConfig:
    enable: true
    collectType: micrometer
    collectInterval: 5
```

### 4. Create Thread Pool

Use `@DynamicThreadPool` annotation to create a thread pool:

```java
@Configuration
public class ThreadPoolConfig {

    @Bean
    @DynamicThreadPool
    public ThreadPoolExecutor businessExecutor() {
        return ThreadPoolExecutorBuilder.builder()
                .threadPoolId("business-thread-pool")
                .corePoolSize(4)
                .maximumPoolSize(8)
                .keepAliveTime(60L)
                .workQueueType(BlockingQueueTypeEnum.LINKED_BLOCKING_QUEUE)
                .threadFactory("business-executor_")
                .rejectedHandler(new ThreadPoolExecutor.CallerRunsPolicy())
                .dynamicPool()
                .build();
    }
}
```

### 5. Verify

Start the application and access the monitoring endpoint:

```bash
curl http://localhost:8080/dynamic-threadpool/info/business-thread-pool
```

## Notification & Alerting

Xiaozhu supports multiple notification channels:

### Email Configuration

```yaml
xiaozhu:
  notifyPlatforms:
    platform: EMAIL
    email:
      account: "your-email@example.com"
      auth: "your-email-auth-code"
      receivers: "admin@example.com"
      smtpHost: "smtp.qq.com"
      smtpPort: 465
```

### Alert Triggers

- Queue usage rate exceeds threshold
- Active thread rate exceeds threshold
- Task execution time too long
- Task rejection

## Monitoring Metrics

The framework provides comprehensive thread pool metrics:

| Metric | Description |
|--------|-------------|
| core.size | Core pool size |
| maximum.size | Maximum pool size |
| active.size | Active thread count |
| queue.size | Current queue size |
| queue.capacity | Queue capacity |
| queue.remaining.capacity | Remaining queue capacity |
| completed.task.count | Completed task count |
| reject.count | Rejection count |

Access metrics via: `http://localhost:8080/actuator/prometheus`

## Hot-Update Support

### Supported Parameters (Hot-Update)

- `corePoolSize`: Core pool size
- `maximumPoolSize`: Maximum pool size
- `keepAliveTime`: Thread idle time
- `queueCapacity`: Queue capacity (for bounded queues only)
- `allowCoreThreadTimeOut`: Allow core thread timeout
- `rejectedHandler`: Rejection policy
- Alert configuration

### Unsupported Parameters (Requires Restart)

- `workQueue`: Queue type
- `threadPoolId`: Thread pool ID

## Nacos Configuration Example

When using `nacos-starter`, create a Nacos configuration file:

```yaml
xiaozhu:
  dynamicPool: true
  executors:
    - threadPoolId: "business-thread-pool"
      corePoolSize: 4
      maximumPoolSize: 8
      keepAliveTime: 60
      workQueue: "LinkedBlockingQueue"
      queueCapacity: 100
      alarm:
        enable: true
        queueThreshold: 80
        activeThreshold: 80
```

## Custom Extension

### Custom Notification

Implement `NotifierService` interface:

```java
@Service
public class CustomNotifier implements NotifierService {
    @Override
    public void send(ThreadPoolNotifyDTO notifyDTO) {
        // Custom notification logic
    }

    @Override
    public String notifyType() {
        return "CUSTOM";
    }
}
```

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

### Development Setup

```bash
# Clone the project
git clone https://github.com/xiaozhu/dynamic-threadpool.git

# Compile
mvn clean compile

# Run demo
cd demo && mvn spring-boot:run
```

### Commit Message Format

- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation update
- `style`: Code format adjustment
- `refactor`: Code refactoring
- `test`: Test related
- `chore`: Other changes

## Contact

- Email: 1105774747@qq.com

---

If this project is helpful to you, please give us a ⭐!
