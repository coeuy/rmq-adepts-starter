# 使用说明 rmq-adepts-starter

## 特性

1. 支持自定义回调发送结果
2. 支持自定义缓存重试
3. 支持TOPIC消息
4. 支持单个队列多线程消费（可自定线程数）
5. 支持手动确认消费/发送确认
6. 优雅停机
7. 延时队列（需要rabbitmq插件支持 ， 延时队列在 3.6 版本及以上支持 http://www.rabbitmq.com/community-plugins.html 下载
   rabbitmq_delayed_message_exchange 插件）
8. 注解声明消费

#### [Springboot例子](https://github.com/yarnk/rmq-adepts-example) 
#### [交流社区](https://chat.coeuy.com) 
## 开始使用

### 1. 依赖导入
```xml

<dependency>
   <groupId>com.coeuy</groupId>
   <artifactId>rmq-adepts-starter</artifactId>
   <version>last-version</version>
   <scope>compile</scope>
</dependency>
```

### 2. 配置详情

基于 starter amqp

```yaml 
spring: 
  rabbitmq:
    # rabbitmq服务地址 集群用‘,’隔开
    addresses: ip:port
    # rabbit用户名 
    username: test
    # rabbit密码
    password: 123456
    # 空间名称 
    virtual-host: virtual
# rmq adeptes 配置
rmq-adepts:
  # mq channels 最大连接数 ，注意 init的时候
  connection-limit: 2000 
  # 打开可看一些关键日志
  debug: true

```

### 3.发送消息

> 消息的发送方只需要写以下代码，就可以实现消息的发送！ 发送更多类型请查看方法注释。

提供了以下2种方式：

1.Spring @Resource 引入

```java
public class UserService {

    @Resource
    private MessageProducer messageProducer;

    public void Send() {

        //普通消息队列
        messageProducer.sendMessage("hello", "你好！");

        //延迟消息队列 毫秒(需要rabbitmq服务插件支持)
        messageProducer.sendDelayMessage("hello", "你好！", 5000);
    }
}
```

2.工具类调用方式

```java
public class UserService {

    public sendMessage() {
        SimpleSender.send("hello", "你好！");
    }
}
```

### 4. 订阅（消费）消息

```java
@Slf4j
@Component
public class HelloMessageProcess implements MessageProcess<String> {

    /**
     *  1. 导入监听初始化类
     */
    @Resource
    private InitListener initListener;

    /**
     * 2. 启动时初始化监听
     */
    @PostConstruct
    public void init() {
        // 2.1 指定初始化参数（这里做了最简单的使用，更多参数自己点进去看）
        initListener.init("hello", this);
    }

    /**
     * 3. 接收消息进行业务操作
     */
    @Override
    public MessageResult process(String message) {
        log.info("快看！ 我收到消息了 {}", message);
        System.out.println(message);

        // 4. 返回消息消费确认 
        return new MessageResult(true, "OK");
    }
}

```

### 5. 多线程消费(大批量无序异步处理场景)
```java

@Slf4j
@Component
public class HelloMessageProcess implements MessageProcess<String> {

   /**
    *  1. 导入监听初始化类
    */
   @Resource
   private InitListener initListener;

   /**
    * 2. 启动时初始化多线程监听
    */
   @PostConstruct
   public void init() {
      // 2.1 初始化线程池最小5 最大100 的监听任务
      initListener.init("hello", this, 5, 100);
   }

   /**
    * 3. 接收消息进行业务操作
    */
   @Override
   public MessageResult process(String message) {
      log.info("快看！ 我收到消息了 {}", message);
      System.out.println(message);

      // 4. 返回消息消费确认 
      return new MessageResult(true, "OK");
   }
}

```

#### 注意事项：

1. 发送消息时 Object 类定义

> 发送Message Obj时 Bbj必须含有全参构造方法，否则Jackson序列化解析失败

#### 常见问题
-
