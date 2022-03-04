package com.coeuy.osp.rmq.adepts.builder;

import com.coeuy.osp.rmq.adepts.common.*;
import com.coeuy.osp.rmq.adepts.consumer.MessageProcess;
import com.google.common.collect.Sets;
import com.rabbitmq.client.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpApplicationContextClosedException;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.DefaultMessagePropertiesConverter;
import org.springframework.amqp.rabbit.support.MessagePropertiesConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.support.AbstractApplicationContext;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeoutException;


/**
 * @author Yarnk
 */
@Slf4j
public class MessageQueueBuilder {

    private static final Set<String> BIND_CACHE = Sets.newConcurrentHashSet();
    private final RabbitTemplate rabbitTemplate;
    private final RabbitAdmin rabbitAdmin;
    private final ConnectionFactory connectionFactory;
    private final MessageConverter messageConverter;

    public MessageQueueBuilder(RabbitTemplate rabbitTemplate, ConnectionFactory connectionFactory, RabbitAdmin rabbitAdmin, MessageConverter messageConverter) {
        this.rabbitAdmin = rabbitAdmin;
        this.connectionFactory = connectionFactory;
        this.messageConverter = messageConverter;
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * 构建消息发送对象
     *
     * @param exchange     路由
     * @param queue        队列
     * @param exchangeType 类型
     * @param routingKey   绑定
     * @return {MessageSender}
     */
    public MessageSender buildMessageSender(final String exchange, final String queue, final ExchangeType exchangeType, final String toExchange, final String routingKey,boolean tx,boolean retry) {
        // 创建重试缓存
        RetryCache retryCache = new RetryCache();
        // 构造sender对象
        return new MessageSender() {
            @Override
            public MessageResult send(Object message) {
                long time = System.currentTimeMillis();
                return send(time, message);
            }

            @Override
            public MessageResult send(long time, Object message) {
                // 创建Template并设置参数
                rabbitTemplate.setExchange(exchange);
                rabbitTemplate.setRoutingKey(routingKey);
                rabbitTemplate.setChannelTransacted(tx);
                // 绑定普通队列
                declareBindExchange(exchange, queue, exchangeType, toExchange, routingKey, false);
                long id = retryCache.generateId();
                MessageWithTime messageWithTime = new MessageWithTime(id, time, message);
                try {
                    rabbitTemplate.correlationConvertAndSend(message, new CorrelationData(
                            String.valueOf(messageWithTime.getId())));
                    return new MessageResult(true, "消息发送成功");
                } catch (AmqpApplicationContextClosedException e) {
                    log.error("应用关闭 消息发送失败：{} \n", e.getMessage());
                    return new MessageResult(false, "消息发送失败");
                } catch (Exception e) {
                    log.error("消息发送失败", e);
                    if (retry){
                        // 加入重试
                        retryCache.setSender(this);
                        retryCache.add(messageWithTime);
                    }

                    return new MessageResult(false, "消息发送失败");
                }
            }

            @Override
            public MessageResult sendDelayMessage(Object message, int millisecond) {
                long time = System.currentTimeMillis();
                return sendDelayMessage(time, message, millisecond,false,false);
            }

            @Override
            public MessageResult sendDelayMessage(long time, Object messageObject, int millisecond,boolean tx,boolean retry) {
                // 绑定延时队列
                declareBindExchange(exchange, queue + Constants.DEFAULT_QUEUE_DELAY_SUFFIX, exchangeType, toExchange, routingKey + Constants.DEFAULT_QUEUE_DELAY_SUFFIX, true);
                // 绑定死信队列
                declareDeadLetterBindExchange(Constants.DEFAULT_DEAD_LETTER_EXCHANGE,
                        queue + Constants.DEFAULT_DEAD_LETTER_EXCHANGE_SUFFIX,
                        exchangeType.getName(), toExchange, Constants.DEFAULT_DEAD_LETTER_EXCHANGE + queue + Constants.DEFAULT_DEAD_LETTER_EXCHANGE_SUFFIX, true);

                // 创建Template并设置参数
                rabbitTemplate.setExchange(exchange);
                rabbitTemplate.setRoutingKey(routingKey + Constants.DEFAULT_QUEUE_DELAY_SUFFIX);
                long id = retryCache.generateId();
                MessageWithTime messageWithTime = new MessageWithTime(id, time, messageObject);
                try {
                    //执行发送消息到指定队列
                    CorrelationData correlationData = new CorrelationData(
                            String.valueOf(messageWithTime.getId()));
                    rabbitTemplate.convertAndSend(messageObject, message -> {
                        message.getMessageProperties().setHeader("x-delay", millisecond);
                        return message;
                    }, correlationData);
                } catch (Exception e) {
                    log.error("延时消息发送失败", e);
                    if (retry){
                        // 加入重试
                        retryCache.setSender(this);
                        retryCache.add(messageWithTime);
                    }
                    return new MessageResult(false, "消息发送失败");
                }
                return new MessageResult(true, "消息发送成功");
            }
        };
    }

    /**
     * 构建消费者
     *
     * @param exchange       路由
     * @param queue          队列
     * @param exchangeType   类型
     * @param routingKey     key
     * @param messageProcess 消息处理者
     * @param <T>            消息对象
     * @return {<Message>MessageConsumer}
     */
    public <T> MessageConsumer buildMessageConsumer(final String exchange, final String queue, final ExchangeType exchangeType, final String toExchange, final String routingKey, boolean delayed, final MessageProcess<T> messageProcess) {
        log.info("\n创建消费实例");
        final Connection connection = connectionFactory.createConnection();
        String queueName;
        if (delayed) {
            queueName = queue + Constants.DEFAULT_QUEUE_DELAY_SUFFIX;
            // 绑定延时队列
            declareBindExchange(exchange, queueName, exchangeType, toExchange, routingKey + Constants.DEFAULT_QUEUE_DELAY_SUFFIX, true);
            // 绑定死信队列
            declareDeadLetterBindExchange(Constants.DEFAULT_DEAD_LETTER_EXCHANGE,
                    queue + Constants.DEFAULT_DEAD_LETTER_EXCHANGE_SUFFIX,
                    exchangeType.getName(), toExchange, Constants.DEFAULT_DEAD_LETTER_EXCHANGE + queue + Constants.DEFAULT_DEAD_LETTER_EXCHANGE_SUFFIX, true);
        } else {
            queueName = queue;
            //1 创建连接和channel
            declareBindExchange(exchange, queue, exchangeType, toExchange, routingKey, false);
        }
        //2 设置message序列化方法
        final MessagePropertiesConverter messagePropertiesConverter = new DefaultMessagePropertiesConverter();
        return new MessageConsumer() {
            final Channel channel;
            {
                channel = connection.createChannel(false);
            }
            @Override
            public MessageResult consume() {
                try {
                    // 通过basicGet获取原始数据
                    GetResponse response = channel.basicGet(queueName, false);
                    try {
                        while (response == null) {
                            response = channel.basicGet(queueName, false);
                            Thread.sleep(Constants.ONE_SECOND);
                        }
                    } catch (AmqpException e) {
                        log.error("AmqpException : {}", e.getMessage());
                        return null;
                    }
                    Message message = new Message(response.getBody(),
                            messagePropertiesConverter.toMessageProperties(response.getProps(), response.getEnvelope(), "UTF-8")
                    );
                    MessageResult messageResult;
                    try {
                        // 2 将原始数据转换为特定类型的包
                        @SuppressWarnings("unchecked")
                        T messageBean = (T) messageConverter.fromMessage(message);
                        // 3 处理数据返回结果
                        messageResult = messageProcess.process(messageBean);
                    } catch (Exception e) {
                        log.error("消息接收异常:\n", e);
                        messageResult = new MessageResult(false, "消息接收异常:\n " + e);
                    }
                    if (messageResult == null) {
                        messageResult = new MessageResult(false, "消息消费失败");
                    }
                    // 消息接收确认
                    if (messageResult.isSuccess()) {
                        channel.basicAck(response.getEnvelope()
                                .getDeliveryTag(), false);
                    } else {
                        Thread.sleep(Constants.ONE_SECOND);
                        log.info("消息处理失败: " + messageResult.getMessage());
                        channel.basicNack(response.getEnvelope()
                                .getDeliveryTag(), false, true);
                    }
                    return messageResult;
                } catch (Exception e) {
                    log.info("消费异常 Exception:\n ", e);
                    try {
                        channel.close();
                    } catch (IOException | TimeoutException ex) {
                        return new MessageResult(false, "关闭或取消异常\n" + e);
                    }
                    return new MessageResult(false, "exception:\n" + e);
                }
            }
        };
    }


    /**
     * 获取消息条数（测试）
     *
     * @param queue 队列
     * @return {int}
     * @throws IOException io
     */
    public int getMessageCount(final String queue) throws IOException {
        Connection connection = connectionFactory.createConnection();
        final Channel channel = connection.createChannel(false);
        final AMQP.Queue.DeclareOk declareOk = channel.queueDeclarePassive(queue);
        return declareOk.getMessageCount();
    }


    /**
     * 声明绑定队列交换机
     * synchronized 线程锁，防止多线程下多次创建的问题
     *
     * @param exchange   交换机名称
     * @param queue      队列名称
     * @param toExchange 交换机绑定
     * @param routingKey 路由标识
     */
    public synchronized void declareBindExchange(String exchange, String queue, ExchangeType exchangeType, String toExchange, String routingKey, boolean delayed) {
        // 添加缓存防止重复绑定浪费资源
        if (!BIND_CACHE.contains(exchange + queue + exchangeType)) {
            log.info("开始绑定队列交换机 QUEUE:{} EXCHANGE:{} TYPE:{}", queue, exchange, exchangeType);
            Map<String, Object> map = new HashMap<>(2, 1F);
            map.put("x-dead-letter-exchange", toExchange);
            map.put("x-dead-letter-routing-key", routingKey);
            //绑定队列
            bind(map, exchange, queue, exchangeType.getName(), routingKey, delayed);
            BIND_CACHE.add(exchange + queue + exchangeType);
        }
    }

    /**
     * 声明死信队列交换机
     *
     * @param deadExchange 死信路由名
     * @param queue        队列名称
     * @param toExchange   交换机绑定
     * @param routingKey   路由标识
     */
    public void declareDeadLetterBindExchange(String deadExchange, String queue, String exchangeType, String toExchange, String routingKey, boolean delayed) {
        // 添加缓存防止重复绑定浪费资源
        if (!BIND_CACHE.contains(deadExchange + queue + exchangeType + Constants.DEFAULT_DEAD_LETTER_EXCHANGE_SUFFIX)) {
            log.info("开始绑定死信队列交换机 QUEUE:{} EXCHANGE:{} TYPE:{}", queue, deadExchange, exchangeType);
            Map<String, Object> map = new HashMap<>(2, 1F);
            map.put("x-dead-letter-exchange", toExchange);
            map.put("x-dead-letter-routing-key", routingKey);
            //重试时间
            int retryTime = Constants.DEFAULT_WAIT_TO_RETRY_TIME;
            map.put("x-message-ttl", retryTime);
            //绑定队列
            bind(map, deadExchange, queue, exchangeType, routingKey, delayed);
            BIND_CACHE.add(deadExchange + queue + exchangeType + Constants.DEFAULT_DEAD_LETTER_EXCHANGE_SUFFIX);
        }
    }


    /**
     * 绑定路由队列
     *
     * @param map          队列信息
     * @param exchangeName 路由
     * @param queueName    队列
     * @param delayed      是否延时
     * @param type         路由类型
     */
    private void bind(Map<String, Object> map, String exchangeName, String queueName, String type, String routingKey, boolean delayed) {
        Queue queue = new Queue(queueName, true, false, false, map);
        //绑定 DIRECT 类型交换机
        if (BuiltinExchangeType.DIRECT.getType().equals(type)) {
            DirectExchange exchange = new DirectExchange(exchangeName, true, false);
            // 延时队列在 3.6 版本及以上支持 http://www.rabbitmq.com/community-plugins.html 下载 rabbitmq_delayed_message_exchange 插件
            if (delayed) {
                exchange.setDelayed(true);
            }
            declareExchange(exchange, delayed);
            rabbitAdmin.declareQueue(queue);
            rabbitAdmin.declareBinding(BindingBuilder.bind(queue).to(exchange).with(routingKey));
            log.info("BIND DIRECT SUCCESS");
        }
        //绑定 TOPIC  类型交换机
        if (BuiltinExchangeType.TOPIC.getType().equals(type)) {
            TopicExchange exchange = new TopicExchange(exchangeName, true, false);
            // 延时队列在 3.6 版本及以上支持 http://www.rabbitmq.com/community-plugins.html 下载 rabbitmq_delayed_message_exchange 插件
            if (delayed) {
                exchange.setDelayed(true);
            }
            declareExchange(exchange, delayed);
            rabbitAdmin.declareQueue(queue);
            rabbitAdmin.declareBinding(BindingBuilder.bind(queue).to(exchange).with(routingKey));
            log.info("BIND TOPIC SUCCESS");
        }
    }

    private void declareExchange(Exchange exchange, boolean isDelay) {
        try {
            rabbitAdmin.declareExchange(exchange);
        } catch (Exception e) {
            if (isDelay) {
                log.warn("使用了延时队列");
            }
            log.error("\n\n交换机 [{}] 类型 [{}] 创建失败 \n:{}\n", exchange.getName(), exchange.getType(), e);
        }
    }
}
