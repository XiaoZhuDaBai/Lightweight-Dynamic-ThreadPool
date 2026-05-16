# Xiaozhu 动态线程池框架

[![](https://img.shields.io/badge/JDK-17%2B-green)](#) [![](https://img.shields.io/badge/Spring%20Boot-3.0%2B-blue)](#) [![](https://img.shields.io/badge/license-MIT-lightgrey)](#) [![](https://img.shields.io/github/stars/xiaozhu/dynamic-threadpool?style=social)](#)

[English](README.md) | 中文

Xiaozhu 动态线程池是一款基于 Spring Boot 的轻量级动态线程池管理解决方案，为业务系统提供在线程池治理方面的全面支持。

## 功能特性

- **动态配置**：支持运行时动态修改线程池参数，无需重启应用
- **实时监控**：提供丰富的监控指标，帮助了解线程池运行状态
- **智能告警**：内置多种告警策略，及时发现并通知问题
- **易于集成**：基于 Spring Boot，提供开箱即用的解决方案
- **Nacos 集成**：支持配置中心，实现配置的集中管理和动态刷新
- **多种队列**：支持 SynchronousQueue、LinkedBlockingQueue、ArrayBlockingQueue、可调整容量队列

## 项目结构

```
dynamic_threadpool_backend/
├── core/                    # 核心功能模块
│   ├── executor/            # 线程池执行器
│   ├── monitor/            # 监控模块
│   ├── alarm/              # 告警模块
│   ├── notification/       # 通知模块
│   └── parser/             # 配置解析
├── spring-base/            # Spring 基础配置模块
├── starter/                # 启动器模块
│   ├── common-stater/      # 通用启动器
│   └── nacos-starter/      # Nacos 集成启动器
└── demo/                   # 示例项目
```

### 架构图

```
┌─────────────────────────────────────┐
│           demo (示例项目)              │
└─────────────────┬───────────────────┘
                  │
                  ▼
        ┌─────────┴─────────┐
        │   nacos-starter   │
        │   (Nacos启动器)   │
        └─────────┬─────────┘
                  │
                  ▼
        ┌─────────┴─────────┐
        │   common-stater   │
        │   (通用启动器)    │
        └─────────┬─────────┘
                  │
                  ▼
        ┌─────────┴─────────┐
        │    spring-base     │
        │  (Spring基础配置) │
        └─────────┬─────────┘
                  │
                  ▼
        ┌─────────┴─────────┐
        │       core        │
        │    (核心功能)     │
        └───────────────────┘
```

### 模块说明

| 模块 | 描述 | 依赖关系 |
|------|------|----------|
| **core** | 核心业务逻辑，包含线程池实现、监控、告警、通知等 | 无 |
| **spring-base** | Spring 集成层，提供注解支持 | core |
| **common-stater** | 通用启动器，提供自动配置和 AOP 支持 | spring-base |
| **nacos-starter** | Nacos 集成，提供配置中心支持 | common-stater |
| **demo** | 示例项目，展示框架使用方法 | nacos-starter |

## 环境要求

- JDK 17+
- Maven 3.6+
- Spring Boot 3.0+
- Spring Cloud 2022.0+
- Spring Cloud Alibaba 2022.0+（使用 Nacos 时需要）

## 快速开始

### 1. 添加依赖

**推荐方式：使用 Nacos 配置中心**

```xml
<dependency>
    <groupId>xiaozhu.dabai</groupId>
    <artifactId>nacos-starter</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

**基础方式：仅使用本地配置**

```xml
<dependency>
    <groupId>xiaozhu.dabai</groupId>
    <artifactId>common-stater</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

### 2. 启用框架

在 Spring Boot 主类上添加注解：

```java
@SpringBootApplication
@EnableXiaozhuThread
public class YourApplication {
    public static void main(String[] args) {
        SpringApplication.run(YourApplication.class, args);
    }
}
```

### 3. 配置线程池

在 `application.yaml` 中添加配置：

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

### 4. 创建线程池

使用 `@DynamicThreadPool` 注解创建线程池：

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

### 5. 验证功能

启动应用后，访问监控端点：

```bash
curl http://localhost:8080/dynamic-threadpool/info/business-thread-pool
```

## 通知告警

Xiaozhu 支持多种通知渠道，确保问题能够及时被发现和处理。

### 邮件通知配置

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

### 告警触发条件

- 队列使用率超过阈值
- 活跃线程率超过阈值
- 任务执行时间超长
- 线程池拒绝任务

## 监控指标

框架提供全面的线程池监控指标：

| 指标 | 说明 |
|------|------|
| core.size | 核心线程数 |
| maximum.size | 最大线程数 |
| active.size | 活跃线程数 |
| queue.size | 当前队列大小 |
| queue.capacity | 队列容量 |
| queue.remaining.capacity | 队列剩余容量 |
| completed.task.count | 已完成任务数 |
| reject.count | 拒绝次数 |

访问监控端点：`http://localhost:8080/actuator/prometheus`

## 热更新支持

### 支持热更新的参数

- `corePoolSize`：核心线程数
- `maximumPoolSize`：最大线程数
- `keepAliveTime`：线程空闲存活时间
- `queueCapacity`：队列容量（仅对有界队列有效）
- `allowCoreThreadTimeOut`：是否允许核心线程超时
- `rejectedHandler`：拒绝策略
- 告警配置

### 不支持热更新的参数

- `workQueue`：队列类型
- `threadPoolId`：线程池 ID

> **注意**：队列类型不支持热更新，因为 ThreadPoolExecutor 在创建时会根据队列类型进行特定初始化。如需修改队列类型，必须重启应用。

## Nacos 配置示例

使用 `nacos-starter` 时，在 Nacos 创建配置文件：

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

## 自定义扩展

### 自定义通知方式

实现 `NotifierService` 接口：

```java
@Service
public class CustomNotifier implements NotifierService {
    @Override
    public void send(ThreadPoolNotifyDTO notifyDTO) {
        // 自定义通知逻辑
    }

    @Override
    public String notifyType() {
        return "CUSTOM";
    }
}
```

## 功能对比

| 功能特性 | common-stater | nacos-starter |
|---------|---------------|---------------|
| 基础线程池功能 | ✅ | ✅ |
| 动态参数修改 | ✅（重启生效） | ✅（即时生效） |
| 配置监听 | ❌ | ✅ |
| Nacos 依赖 | ❌ | ✅ |
| 配置方式 | application.yaml | Nacos 配置中心 |

## 常见问题

**Q: Nacos 配置为什么不生效？**

A: 检查以下几点：
- 使用 `nacos-starter` 依赖
- 配置文件名格式：`${spring.application.name}-${spring.profiles.active}.yaml`
- 线程池 ID 要与配置中的 `threadPoolId` 一致
- Nacos 连接配置正确

**Q: 支持哪些队列类型？**

A: 支持的队列类型：
- `SynchronousQueue`：同步队列
- `LinkedBlockingQueue`：链表阻塞队列
- `ArrayBlockingQueue`：数组阻塞队列
- `ResizableCapacityLinkedBlockingQueue`：可调整容量的链表队列

**Q: 如何监控线程池状态？**

A: 多种监控方式：
- REST API：`/dynamic-threadpool/info/{threadPoolId}`
- Micrometer 指标：集成监控系统
- 告警通知：配置阈值自动告警

## 贡献指南

### 开发环境

- JDK 17+
- Maven 3.6+
- IntelliJ IDEA（推荐）

### 快速开始

```bash
# 克隆项目
git clone https://github.com/xiaozhu/dynamic-threadpool.git

# 编译项目
mvn clean compile

# 运行示例
cd demo && mvn spring-boot:run
```

### 提交规范

- `feat`：新功能
- `fix`：修复 bug
- `docs`：文档更新
- `style`：代码格式调整
- `refactor`：代码重构
- `test`：测试相关
- `chore`：其他修改

## 联系方式

- 邮箱：1105774747@qq.com

---

如果这个项目对你有帮助，请给我们一个 ⭐！
