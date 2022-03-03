# 使用说明 rmq-adepts-starter

## 新特性

1. 支持回调发送结果
2. 支持缓存重试
3. 支持TOPIC消息
4. 支持单个队列多线程消费（可自定线程数）
5. 支持手动确认消费
6. 多线程监听
7. 优雅停机
8. 延时队列（需要rabbitmq插件支持 ， 延时队列在 3.6 版本及以上支持 http://www.rabbitmq.com/community-plugins.html 下载
   rabbitmq_delayed_message_exchange 插件）
9. 消息发送工具类SmsListener支持直接调用静态方法

## 导入依赖

```xml

<dependency>
    <groupId>com.coeuy</groupId>
    <artifactId>rmq-adepts-starter</artifactId>
    <version>1.0.0</version>
    <scope>compile</scope>
</dependency>
```

## 配置详情

基于 starter amqp

```yaml 
spring: 
  rabbitmq:
    # rabbitmq服务地址 集群用‘,’隔开
    address: ip:port
    # rabbit用户名 分环境使用 (zhiantech_dev,zhiantech_test,zhiantech_stage)
    username: test
    # rabbit密码
    password: 123456
    # 空间名称 分环境使用 (virtual_dev,virtual_test,...)
    virtual-host: virtual
```

## 简单使用

### 发送消息

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

> 注意： MessageProducer需要在Spring bean @Component 下才生效


2.单例工具类调用方式

```java
public class UserService {

    public sendMessage() {
        SimpleSender.send("hello", "你好！");
    }
}
```

### 订阅消息

```java

@Slf4j
@Component
public class HelloMessageProcess implements MessageProcess<String> {

    // 1. 导入监听初始化类
    @Resource
    InitListener initListener;

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

#### 发送消息时 Object 类定义

> 发送Message Obj时 Bbj必须含有全参构造方法，否则Jackson序列化解析失败
  
    

