package com.coeuy.osp.rmq.adepts.common;

import com.coeuy.osp.rmq.adepts.builder.MessageQueueBuilder;
import com.coeuy.osp.rmq.adepts.consumer.MessageProcess;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import static org.springframework.amqp.core.ExchangeTypes.DIRECT;

/**
 * <p> 初始化监听 </p>
 *
 * @author Yarnk
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InitListener {

    private final MessageQueueBuilder messageBrokerBuilder;

    /**
     * 初始化监听
     *
     * @param exchange       路由
     * @param type           路由类型
     * @param queue          队列
     * @param routingKey     路由键
     * @param threadCount    线程数量
     * @param intervalMils   执行时间间隔（）
     * @param messageProcess 业务处理类
     * @param <T>            消息类型
     */
    @Async
    @SneakyThrows
    public <T> void init(String exchange, ExchangeType type, String queue, String routingKey, int threadCount, int intervalMils, boolean delayed, MessageProcess<T> messageProcess) {
        // 开启监听
        ThreadPoolConsumerBuilder<T> process = new ThreadPoolConsumerBuilder<T>()
                .setCoreSize(threadCount)
                .setIntervalMils(intervalMils)
                .setExchange(exchange)
                .setRoutingKey(routingKey)
                .setQueue(queue)
                .setType(type)
                .setDelayed(delayed)
                .setMessageBrokerBuilder(messageBrokerBuilder)
                .setMessageProcess(messageProcess);
        ThreadPoolConsumer<T> build = process.build();
        build.start();
    }

    @SneakyThrows
    public <T> void init(String exchange, String queue, String routingKey, MessageProcess<T> messageProcess, int threadCount) {
        // 使用 [默认线程参数]
        init(exchange, ExchangeType.DIRECT, queue, routingKey, threadCount, Constants.DEFAULT_INTERVAL_MILS, false, messageProcess);
    }

    @SneakyThrows
    public <T> void init(String queue, MessageProcess<T> messageProcess, int threadCount,int interval) {
        // 使用 [默认线程参数]
        init(Constants.DEFAULT_EXCHANGE_NAME, ExchangeType.DIRECT, queue, Constants.DEFAULT_EXCHANGE_NAME+queue, threadCount,interval, false, messageProcess);
    }


    @SneakyThrows
    public <T> void initPool(String queue, MessageProcess<T> messageProcess, int core,int max) {
        // 开启监听
       initPool(Constants.DEFAULT_EXCHANGE_NAME,ExchangeType.DIRECT,queue,Constants.DEFAULT_EXCHANGE_NAME+queue,messageProcess,core,max,Constants.DEFAULT_INTERVAL_MILS);
    }
    
    @SneakyThrows
    public <T> void initPool(String exchange ,ExchangeType exchangeType,String queue,String rk, MessageProcess<T> messageProcess, int core,int max,int interval) {
        // 开启监听
        ThreadPoolConsumerBuilder<T> process = new ThreadPoolConsumerBuilder<T>()
                .setCoreSize(core)
                .setIntervalMils(interval)
                .setExchange(exchange)
                .setMaxSize(max)
                .setRoutingKey(rk)
                .setQueue(queue)
                .setType(exchangeType)
                .setDelayed(false)
                .setMessageBrokerBuilder(messageBrokerBuilder)
                .setMessageProcess(messageProcess);
        ThreadPoolConsumer<T> build = process.build();
        build.start();
    }
    
    @SneakyThrows
    public <T> void initPool(String queue, MessageProcess<T> messageProcess, int core,int max,int interval) {
        // 开启监听
        
       initPool(Constants.DEFAULT_EXCHANGE_NAME,ExchangeType.DIRECT,queue,Constants.DEFAULT_EXCHANGE_NAME+queue,messageProcess,core,max,interval);
    }
    
    
    @SneakyThrows
    public <T> void init(String exchange, String queue, MessageProcess<T> messageProcess) {
        // 使用  [默认线程参数]  [默认RoutingKey=queue]  [默认路由类型]
        init(exchange, queue, exchange + queue, messageProcess, Constants.DEFAULT_THREAD_COUNT);
    }

    @SneakyThrows
    public <T> void init(String exchange, String queue, MessageProcess<T> messageProcess, int threadCount) {
        // 使用  [默认线程参数]  [默认RoutingKey=queue]  [默认路由类型]
        init(exchange, queue, exchange + queue, messageProcess, threadCount);
    }

    @SneakyThrows
    public <T> void init(String queue, MessageProcess<T> messageProcess, int threadCount) {
        // 简单使用
        init(Constants.DEFAULT_EXCHANGE_NAME, queue, messageProcess, threadCount);
    }

    @SneakyThrows
    public <T> void init(String queue, MessageProcess<T> messageProcess) {
        // 简单使用
        init(Constants.DEFAULT_EXCHANGE_NAME, queue, messageProcess);
    }

    @SneakyThrows
    public <T> void initDelay(String exchange, String queue, String routingKey, boolean delayed, MessageProcess<T> messageProcess, int threadCount) {
        // 使用 [默认线程参数]
        init(exchange, ExchangeType.DIRECT, queue, routingKey, threadCount, Constants.DEFAULT_INTERVAL_MILS, delayed, messageProcess);
    }

    @SneakyThrows
    public <T> void initDelay(String queue, MessageProcess<T> messageProcess) {
        // 监听延时
        String exchangeName = Constants.DEFAULT_DELAY_EXCHANGE_NAME;
        initDelay(exchangeName, queue, exchangeName + queue, true, messageProcess, Constants.DEFAULT_THREAD_COUNT);
    }

}
