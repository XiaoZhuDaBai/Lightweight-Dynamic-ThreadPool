# Xiaozhu 动态线程池框架

Xiaozhu 动态线程池框架是一款基于 Spring Boot 的轻量级动态线程池管理解决方案，为业务系统提供在线程池治理方面的全面支持。

## 简介
### 项目结构
项目采用多模块Maven架构设计，模块职责清晰分离：

```plain
dynamic_threadpool_backend/
├── core/                    # 核心功能模块
├── spring-base/             # Spring基础配置模块
├── starter/                 # 启动器模块
│   ├── common-stater/       # 通用启动器
│   └── nacos-starter/       # Nacos集成启动器
└── demo/                    # 示例项目
```

#### 架构概览
<!-- 这是一张图片，ocr 内容为： -->
![](https://cdn.nlark.com/yuque/0/2026/png/62242460/1769674993943-e1d0817c-5b09-41ba-8008-f37dc371fadb.png)

#### 工作流程图
<!-- 这是一张图片，ocr 内容为： -->
![](https://cdn.nlark.com/yuque/0/2026/png/62242460/1769358618594-22b5208b-51dc-4c89-bd9a-c58f311c44a3.png)

#### 模块说明
+ **core**: 核心业务逻辑，包含线程池实现、监控、告警、通知等核心组件
+ **spring-base**: Spring基础配置，提供`@EnableXiaozhuThread`注解和基础Bean
+ **common-stater**: 通用启动器，提供自动配置、AOP切面和基础功能
+ **nacos-starter**: Nacos启动器，基于common-stater增加Nacos配置中心支持
+ **demo**: 示例项目，展示框架的使用方法

**模块职责说明**：

| 模块 | 职责 | 依赖关系 |
| --- | --- | --- |
| **core** | 核心业务逻辑，无Spring依赖 | 无 |
| **spring-base** | Spring集成层，提供注解支持 | core |
| **common-stater** | 通用启动器，提供基础功能 | spring-base |
| **nacos-starter** | Nacos集成，提供配置中心支持 | common-stater |


## 用户指南
### 为什么需要动态线程池
在实际生产环境中，线程池参数配置不当往往会导致严重的问题：

+ **资源浪费**: 线程池参数设置过大，造成服务器资源浪费
+ **性能瓶颈**: 参数设置过小，在高并发场景下无法满足业务需求
+ **缺乏监控**: 无法实时了解线程池运行状态，问题发现不及时
+ **配置僵化**: 运行时无法动态调整参数，只能通过重启应用解决
+ **告警缺失**: 当线程池出现问题时，无法及时得到通知

Xiaozhu 框架通过以下方式解决这些问题：

+ **动态配置**: 支持运行时动态修改线程池参数，无需重启应用
+ **实时监控**: 提供丰富的监控指标，帮助了解线程池运行状态
+ **智能告警**: 内置多种告警策略，及时发现并通知问题
+ **易于集成**: 基于 Spring Boot，提供开箱即用的解决方案

### 架构设计
Xiaozhu 框架采用分层架构设计，确保各模块职责清晰，易于维护和扩展：

#### 整体架构
<!-- 这是一张图片，ocr 内容为： -->
![](https://cdn.nlark.com/yuque/0/2026/png/62242460/1769675377159-57fade3d-cbd2-4b53-a86c-95089782b0ad.png)

**核心功能模块**：

+ **监控模块**: 实时监控线程池运行状态，收集性能指标
+ **告警模块**: 支持多种告警策略，及时发现并通知问题
+ **配置管理模块**: 动态配置管理，支持本地和远程配置
+ **核心线程池模块**: XiaozhuThreadExecutor、动态参数调整、优雅关闭

### 快速开始
#### 环境要求
+ JDK 17+
+ Maven 3.6+
+ Spring Boot 3.0+
+ Spring Cloud 2022.0+
+ Spring Cloud Alibaba 2022.0+ （使用 Nacos 时需要）

#### 引入依赖
根据你的需求选择合适的启动器：

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

#### 启用框架
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

#### 基础配置
在 `application.yaml` 中添加配置：

```yaml
server:
  port: 8080

spring:
  application:
    name: your-app-name
  profiles:
    active: dev

xiaozhu:
  dynamicPool: true
  monitorConfig:
    enable: true
    collectType: micrometer
    collectInterval: 5
```

> ⚠️ **重要提醒**: 线程池的队列类型不支持热更新。如需修改队列类型，必须重启应用。支持热更新的参数包括核心线程数、最大线程数、空闲存活时间、拒绝策略等。
>

#### 创建线程池
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

> 📝 **注意**: 线程池的队列类型在创建时确定，后续不支持修改。如需更改队列类型，请修改代码中的配置并重启应用。
>

#### 验证功能
启动应用后，访问监控端点：

```bash
curl http://localhost:8080/dynamic-threadpool/info/business-thread-pool
```

### 通知报警
Xiaozhu 框架支持多种通知渠道，确保问题能够及时被发现和处理。

#### 支持的通知类型
+ **邮件通知**: 基于 JavaMail，支持主流邮箱服务
+ **企业微信**: 集成企业微信机器人（预留接口）
+ **钉钉**: 集成钉钉机器人（预留接口）
+ **扩展支持**: 可自定义通知实现

#### 配置邮件通知
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

#### 告警触发条件
+ 队列使用率超过阈值
+ 活跃线程率超过阈值
+ 任务执行时间超长
+ 线程池拒绝任务

#### 自定义通知
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

#### Nacos 配置示例
在使用 `nacos-starter` 时，创建 Nacos 配置文件：

```yaml
xiaozhu:
  dynamicPool: true
  monitorConfig:
    enable: true
    collectType: micrometer
    collectInterval: 5
  notifyPlatforms:
    platform: EMAIL
    email:
      account: "your-email@example.com"
      auth: "your-email-auth-code"
      receivers: "admin@example.com"
      smtpHost: "smtp.qq.com"
      smtpPort: 465
  executors:
    - threadPoolId: "business-thread-pool"
      corePoolSize: 4
      maximumPoolSize: 8
      keepAliveTime: 60
      workQueue: "LinkedBlockingQueue"
      rejectedHandler: "CallerRunsPolicy"
      queueCapacity: 100
      allowCoreThreadTimeOut: false
      notify:
        receivers: "admin@example.com"
        interval: 5
      alarm:
        enable: true
        queueThreshold: 80
        activeThreshold: 80
```

## 运维指南
### 监控指标


Xiaozhu 框架提供全面的线程池监控指标：

#### 基础指标
+ 核心线程数、最大线程数、活跃线程数
+ 队列类型、容量、当前大小
+ 已完成任务数、拒绝任务数

#### 性能指标
+ 线程池利用率
+ 队列使用率
+ 任务执行时间

### 告警配置
#### 告警类型
+ **队列告警**: 队列使用率超过设定阈值
+ **线程告警**: 活跃线程率超过设定阈值
+ **拒绝告警**: 任务拒绝次数过多
+ **配置告警**: 线程池配置变更通知

#### 配置示例
```yaml
executors:
  - threadPoolId: "business-pool"
    alarm:
      enable: true
      queueThreshold: 80    # 队列使用率阈值
      activeThreshold: 80   # 活跃线程率阈值
```

#### 告警示例图
<!-- 这是一张图片，ocr 内容为： -->
![](https://cdn.nlark.com/yuque/0/2026/jpeg/62242460/1769358947168-07650a08-9314-4cf8-8658-7a6638819731.jpeg)

### 配置管理
#### 本地配置
使用 `common-stater` 时，配置直接写在 `application.yaml` 中：

```yaml
xiaozhu:
  executors:
    - threadPoolId: "pool-1"
      corePoolSize: 4
      maximumPoolSize: 8
```

#### Nacos 配置
使用 `nacos-starter` 时，在 Nacos 创建配置文件：

配置文件命名：`${spring.application.name}-${spring.profiles.active}.yaml`

配置变更后自动生效，无需重启应用。

#### 配置限制和注意事项
**支持热更新的参数**：

+ `corePoolSize`: 核心线程数
+ `maximumPoolSize`: 最大线程数
+ `keepAliveTime`: 线程空闲存活时间
+ `queueCapacity`: 队列容量（仅对有界队列有效）
+ `allowCoreThreadTimeOut`: 是否允许核心线程超时
+ `rejectedHandler`: 拒绝策略
+ 告警配置：阈值、通知设置等

**不支持热更新的参数**：

+ `workQueue`: 队列类型
+ `threadPoolId`: 线程池ID

> ⚠️ **重要说明**: 队列类型(`workQueue`)不支持热更新。这是因为不同的队列类型具有不同的数据结构和行为特性，ThreadPoolExecutor 在创建时会根据队列类型进行特定的初始化。一旦创建完成，队列类型无法在运行时更改。如需修改队列类型，必须重启应用。
>

**配置生效时间**：

+ 支持热更新的参数：配置变更后立即生效
+ 不支持热更新的参数：变更会被忽略，并记录警告日志

### 故障排查
#### 常见问题
1. **配置不生效**
    - 检查配置文件名格式
    - 确认线程池ID匹配
    - 查看应用日志
2. **邮件发送失败**
    - 验证邮箱配置
    - 检查网络连接
    - 确认SMTP设置
3. **监控数据异常**
    - 检查 Micrometer 配置
    - 确认监控间隔设置
    - 查看日志输出

#### 日志分析
关键日志模式：

+ `线程池注册成功`: 确认线程池正常启动
+ `配置更新完成`: 验证动态配置生效
+ `告警通知发送`: 检查告警功能正常
+ `监控数据采集`: 确认监控运行状态

#### 分包使用指南
Xiaozhu 框架采用渐进式的分包设计，你可以根据项目需求选择合适的依赖：

**场景一：基础使用**

```xml
<dependency>
    <groupId>xiaozhu.dabai</groupId>
    <artifactId>common-stater</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>

```

适用于单体应用或配置相对稳定的场景。

**场景二：配置中心支持（推荐）**

```xml
<dependency>
    <groupId>xiaozhu.dabai</groupId>
    <artifactId>nacos-starter</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>

```

适用于分布式应用或需要动态配置的场景。

**功能对比**：

| 功能特性 | common-stater | nacos-starter |
| --- | --- | --- |
| 基础线程池功能 | ✅ | ✅ |
| 动态参数修改 | ✅ (重启生效) | ✅ (即时生效) |
| 配置监听 | ❌ | ✅ |
| Nacos依赖 | ❌ | ✅ |
| 配置方式 | application.yaml | Nacos配置中心 |


**模块组成**：

```plain
nacos-starter
├── common-stater
│   ├── spring-base
│   │   ├── core (纯业务逻辑)
│   │   └── Spring集成注解
│   └── 自动配置 + AOP功能
└── Nacos配置支持
```

## 开发者手册
### 架构设计
项目采用模块化设计，各模块职责清晰分离：

```plain
┌─────────────────────────────────────┐
│         demo (示例项目)              │
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
        │    spring-base    │
        │  (Spring基础配置) │
        └─────────┬─────────┘
                  │
                  ▼
        ┌─────────┴─────────┐
        │       core        │
        │    (核心功能)     │
        └───────────────────┘
```

#### 模块职责
+ **core**: 核心业务逻辑实现
    - `XiaozhuThreadExecutor`: 增强的线程池执行器
    - `XiaozhuThreadRegistry`: 线程池注册中心
    - `ThreadPoolMonitor`: 监控组件
    - `ThreadPoolAlarmChecker`: 告警检查器
    - 通知服务实现（邮件等）
+ **spring-base**: Spring集成层
    - `@EnableXiaozhuThread`: 启用注解
    - `@DynamicThreadPool`: 线程池标记注解
    - 基础Bean配置
+ **common-stater**: 通用启动器
    - 自动配置类 `CommonAutoConfiguration`
    - AOP切面 `DynamicThreadPoolAspect`（自动注册线程池）
    - 配置监听器 `DynamicThreadPoolRefreshListener`
    - 提供基础的动态配置功能
+ **nacos-starter**: Nacos集成启动器
    - 继承common-stater的所有功能
    - 添加Nacos配置中心支持
    - `NacosDynamicThreadPoolConfiguration`: Nacos配置处理器

### 核心组件
#### XiaozhuThreadExecutor
增强的线程池执行器，提供动态配置支持：

```java
public class XiaozhuThreadExecutor extends ThreadPoolExecutor {

    private final String threadPoolId;

    // 支持动态修改核心参数
    public void updateCorePoolSize(int corePoolSize) {
        super.setCorePoolSize(corePoolSize);
    }

    // 提供丰富的监控信息
    public ThreadPoolRuntimeInfo getRuntimeInfo() {
        return ThreadPoolRuntimeInfo.builder()
            .threadPoolId(threadPoolId)
            .corePoolSize(getCorePoolSize())
            .maximumPoolSize(getMaximumPoolSize())
            .activeCount(getActiveCount())
            .queueSize(getQueue().size())
            .build();
    }
}
```

#### 线程池注册中心
`XiaozhuThreadRegistry` 管理所有动态线程池实例：

```java
public class XiaozhuThreadRegistry {

    private static final Map<String, ThreadPoolExecutorHolder> HOLDER_REGISTRY =
        new ConcurrentHashMap<>();

    public static void register(String threadPoolId, ThreadPoolExecutor executor,
                               ThreadPoolExecutorProperties properties) {
        ThreadPoolExecutorHolder holder = new ThreadPoolExecutorHolder(threadPoolId, executor, properties);
        HOLDER_REGISTRY.put(threadPoolId, holder);
    }

    public static ThreadPoolExecutorHolder getHolder(String threadPoolId) {
        return HOLDER_REGISTRY.get(threadPoolId);
    }
}
```

#### 配置监听器
`DynamicThreadPoolRefreshListener` 处理配置变更：

```java
@Component
public class DynamicThreadPoolRefreshListener implements ApplicationListener<ThreadPoolConfigUpdateEvent> {

    @Override
    public void onApplicationEvent(ThreadPoolConfigUpdateEvent event) {
        updateThreadPoolFromRemoteConfig(event.getProperties());
    }
}
```

### 扩展开发
#### 自定义通知方式
实现 `NotifierService` 接口：

```java
@Service
public class CustomNotifier implements NotifierService {

    @Override
    public void send(ThreadPoolNotifyDTO notifyDTO) {
        // 自定义通知逻辑
        System.out.println("Custom notification: " + notifyDTO.getMessage());
    }

    @Override
    public String notifyType() {
        return "CUSTOM";
    }
}
```

#### 自定义队列类型
扩展 `BlockingQueueTypeEnum`：

```java
public enum BlockingQueueTypeEnum {

    LINKED_BLOCKING_QUEUE("LinkedBlockingQueue"),
    SYNCHRONOUS_QUEUE("SynchronousQueue"),
    // 添加自定义队列类型
    CUSTOM_QUEUE("com.example.CustomBlockingQueue");

    private final String className;

    BlockingQueueTypeEnum(String className) {
        this.className = className;
    }
}
```

#### 自定义拒绝策略
实现 `RejectedExecutionHandler`：

```java
public class CustomRejectedHandler implements RejectedExecutionHandler {

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        // 自定义拒绝策略逻辑
        System.out.println("Task rejected, executing in caller thread");
        r.run(); // 在调用者线程中执行
    }
}
```

## 常见问题
### 配置相关
**Q: Nacos 配置为什么不生效？**

A: 检查以下几点：

+ 使用 `nacos-starter` 依赖
+ 配置文件名格式：`${spring.application.name}-${spring.profiles.active}.yaml`
+ 线程池ID要与配置中的 `threadPoolId` 一致
+ Nacos连接配置正确

**Q: 邮件通知发送失败？**

A: 检查邮箱配置：

+ 发件人邮箱和授权码正确
+ SMTP服务器和端口配置正确
+ 网络能访问SMTP服务器

### 功能相关
**Q: 支持哪些队列类型？**

A: 支持的队列类型：

+ `SynchronousQueue`: 同步队列
+ `LinkedBlockingQueue`: 链表阻塞队列
+ `ArrayBlockingQueue`: 数组阻塞队列
+ `ResizableCapacityLinkedBlockingQueue`: 可调整容量的链表队列

**Q: 如何实现优雅关闭？**

A: 框架默认支持优雅关闭，应用关闭时会等待线程池任务完成。如需自定义等待时间，可配置相关参数。

**Q: 如何监控线程池状态？**

A: 多种监控方式：

+ REST API: `/dynamic-threadpool/info/{threadPoolId}`
+ 应用日志：查看监控数据输出
+ Micrometer指标：集成监控系统
+ 告警通知：配置阈值自动告警

### 开发相关
**Q: 如何打包发布？**

A: 多模块项目打包：

```bash
# 编译所有模块
mvn clean compile

# 打包
mvn clean package

# 安装到本地仓库
mvn clean install
```

各模块说明：

+ `core`: 核心功能
+ `spring-base`: Spring集成
+ `common-stater`: 通用启动器
+ `nacos-starter`: Nacos启动器

**Q: 为什么队列类型不能热更新？**

A: ThreadPoolExecutor 的队列类型在创建时就已经确定，不支持运行时更改。这是因为：

1. **数据结构差异**: 不同队列类型（如 LinkedBlockingQueue、SynchronousQueue）具有不同的内部实现和行为特性
2. **线程安全考虑**: 更改队列类型可能导致正在执行的任务出现不可预期的行为
3. **JVM 限制**: ThreadPoolExecutor 的设计不支持在运行时替换内部队列

解决方案：如需更改队列类型，请修改代码配置并重启应用。

**Q: 哪些配置参数支持热更新？**

A: 支持热更新的参数包括：

+ 核心线程数 (`corePoolSize`)
+ 最大线程数 (`maximumPoolSize`)
+ 空闲存活时间 (`keepAliveTime`)
+ 队列容量 (`queueCapacity`，仅对有界队列有效）
+ 拒绝策略 (`rejectedHandler`)
+ 告警配置

不支持热更新的参数：

+ 队列类型 (`workQueue`)
+ 线程池ID (`threadPoolId`)

## 贡献指南
### 开发环境
+ JDK 17+
+ Maven 3.6+
+ IntelliJ IDEA (推荐)

### 快速开始
```bash
# 克隆项目
git clone https://github.com/xiaozhu/dynamic-threadpool.git

# 编译项目
mvn clean compile

# 运行示例
cd demo && mvn spring-boot:run
```

### 代码规范
+ 遵循阿里巴巴 Java 开发规范
+ 使用 Lombok 简化代码
+ 保持良好的注释习惯
+ 提交前运行测试用例

### 提交规范
+ `feat`: 新功能
+ `fix`: 修复bug
+ `docs`: 文档更新
+ `style`: 代码格式调整
+ `refactor`: 代码重构
+ `test`: 测试相关
+ `chore`: 其他修改

### 联系我们
+ 邮箱：1105774747@qq.com

---

Xiaozhu 动态线程池框架致力于为 Java 开发者提供简单易用的线程池治理解决方案。如果这个项目对你有帮助，请给我们一个 Star！

