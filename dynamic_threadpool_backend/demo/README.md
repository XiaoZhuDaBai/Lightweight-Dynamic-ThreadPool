# 动态线程池Demo

这是一个基于Spring Boot的动态线程池演示项目，通过Nacos配置中心实现线程池参数的动态调整。

## 功能特性

- **动态线程池配置**: 支持运行时动态调整线程池参数，无需重启应用
- **多线程池支持**: 支持配置多个独立的线程池
- **Nacos集成**: 通过Nacos配置中心管理线程池配置
- **监控告警**: 集成Micrometer监控指标，支持告警通知
- **生产者-消费者模式**: 演示典型的生产者-消费者场景

## 快速开始

### 1. 启动Nacos

```bash
# 使用Docker启动Nacos
docker run --name nacos -e MODE=standalone -p 8848:8848 nacos/nacos-server:latest
```

访问: http://localhost:8848/nacos (用户名/密码: nacos/nacos)

### 2. 导入配置

在Nacos控制台创建配置：
- **Data ID**: `dynamic-threadpool-demo.yaml`
- **Group**: `DEFAULT_GROUP`
- **配置格式**: `YAML`
- **配置内容**: 复制 `src/main/resources/dynamic-threadpool-demo.yaml` 的内容

### 3. 启动应用

```bash
# 方式1: 使用Maven
./mvnw spring-boot:run

# 方式2: 直接运行
./mvnw clean package -DskipTests
java -jar target/demo-0.0.1-SNAPSHOT.jar

### 测试邮件通知

启动应用后，可以通过以下接口测试邮件功能：

```bash
# 测试邮件通知功能
GET /dynamic-thread-pool/test/email
```

## API接口

### 生产者线程池

```bash
# 执行单个生产者任务
GET /dynamic-thread-pool/producer/execute?taskName=produce-data-1

# 批量执行生产者任务
GET /dynamic-thread-pool/producer/batch?count=5

# 查看生产者线程池信息
GET /dynamic-thread-pool/producer/info
```

### 消费者线程池

```bash
# 执行单个消费者任务
GET /dynamic-thread-pool/consumer/execute?taskName=consume-data-1

# 批量执行消费者任务
GET /dynamic-thread-pool/consumer/batch?count=5

# 查看消费者线程池信息
GET /dynamic-thread-pool/consumer/info
```

### 订单处理线程池

```bash
# 执行订单处理任务
GET /dynamic-thread-pool/order/execute?orderId=order-001

# 查看订单线程池信息
GET /dynamic-thread-pool/order/info
```

### 用户处理线程池

```bash
# 执行用户处理任务
GET /dynamic-thread-pool/user/execute?userId=user-001

# 查看用户线程池信息
GET /dynamic-thread-pool/user/info
```

### 生产者-消费者模式

```bash
# 模拟完整生产者-消费者流程
GET /dynamic-thread-pool/producer-consumer?count=3
```

## 配置说明

### 线程池配置参数

| 参数 | 说明 | 可动态调整 |
|------|------|-----------|
| corePoolSize | 核心线程数 | ✅ |
| maximumPoolSize | 最大线程数 | ✅ |
| keepAliveTime | 线程存活时间(秒) | ✅ |
| workQueue | 阻塞队列类型 | ❌ |
| rejectedHandler | 拒绝策略 | ❌ |
| queueCapacity | 队列容量 | ✅ |
| allowCoreThreadTimeOut | 允许核心线程超时 | ✅ |

### 阻塞队列类型

- `LinkedBlockingQueue`: 有界/无界链表队列
- `ArrayBlockingQueue`: 有界数组队列
- `SynchronousQueue`: 同步队列
- `ResizableCapacityLinkedBlockingQueue`: 可动态调整容量的链表队列

### 拒绝策略

- `CallerRunsPolicy`: 调用者运行策略
- `AbortPolicy`: 抛出异常策略
- `DiscardPolicy`: 丢弃策略
- `DiscardOldestPolicy`: 丢弃最老任务策略

## 动态配置

在Nacos中修改配置后，应用会自动检测并应用新的线程池参数：

```yaml
executors:
  - threadPoolId: "onethread-producer"
    corePoolSize: 5  # 从2调整为5
    maximumPoolSize: 8  # 从4调整为8
```

## 监控指标

应用暴露了以下Micrometer指标：

```
# 核心线程数
dynamic_thread_pool_core_size{thread_pool_name="onethread-producer"}

# 最大线程数
dynamic_thread_pool_max_size{thread_pool_name="onethread-producer"}

# 当前活跃线程数
dynamic_thread_pool_active_size{thread_pool_name="onethread-producer"}

# 当前线程池大小
dynamic_thread_pool_pool_size{thread_pool_name="onethread-producer"}

# 队列当前大小
dynamic_thread_pool_queue_size{thread_pool_name="onethread-producer"}
```

访问监控端点：http://localhost:8080/actuator/prometheus

## 项目结构

```
demo/
├── src/main/java/com/example/demo/
│   ├── config/
│   │   └── DynamicThreadPoolConfiguration.java  # 线程池配置
│   ├── DynamicThreadPoolController.java         # REST控制器
│   ├── DynamicThreadPoolDemoService.java        # 业务服务
│   └── DemoApplication.java                     # 主启动类
├── src/main/resources/
│   ├── application.yaml                         # 应用配置
│   └── dynamic-threadpool-demo.yaml             # 默认线程池配置
└── pom.xml                                      # Maven配置
```

## 注意事项

1. **Nacos连接**: 确保Nacos服务正常运行，配置正确的连接信息
2. **配置格式**: 配置文件必须是有效的YAML格式
3. **线程池ID**: 每个线程池的`threadPoolId`必须唯一
4. **动态参数**: 只有`corePoolSize`、`maximumPoolSize`、`keepAliveTime`、`queueCapacity`支持动态调整
5. **队列容量**: 对于`SynchronousQueue`，容量应设置为0

## 故障排除

### 常见问题

1. **配置不生效**: 检查Data ID和Group是否正确
2. **连接失败**: 确认Nacos服务地址和认证信息
3. **指标为空**: 等待监控采集间隔或检查配置
4. **告警不发送**: 检查通知平台配置和接收人信息

### 调试模式

启用DEBUG日志查看详细运行信息：

```yaml
logging:
  level:
    com.xiaozhu: DEBUG
    com.example.demo: DEBUG
```
