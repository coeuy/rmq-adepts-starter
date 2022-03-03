package com.coeuy.osp.rmq.adepts.producer;

import com.coeuy.osp.rmq.adepts.common.Constants;
import com.coeuy.osp.rmq.adepts.common.MessageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author Yarnk
 * @date 2020/6/23 11:10
 */
@Slf4j
@Component
public class SimpleSender {

    private static MessageProducer messageProducer;

    @Resource
    public void setMessageProducer(MessageProducer messageProducer) {
        SimpleSender.messageProducer = messageProducer;
    }

    public static MessageResult sendMessage(String exchange, String queue, String type, String deadExchange, String routingKey, Object message) {
        return messageProducer.sendMessage(exchange, queue, type, deadExchange, routingKey, message);
    }

    public static MessageResult sendDelayMessage(String exchange, String queue, String type, String deadExchange, String routingKey, Object message, final int millisecond) {
        return messageProducer.sendDelayMessage(exchange, queue, type, deadExchange, routingKey, message, millisecond);
    }

    /**
     * 指定队列发送消息
     *
     * @param queue   队列
     * @param message 消息
     * @return 发送执行结果
     */
    public static MessageResult sendMessage(String queue, Object message) {
        // 使用默认路由
        String exchange = Constants.DEFAULT_EXCHANGE_NAME;
        // 使用默认路由类型
        String type = Constants.DEFAULT_EXCHANGE_TYPE;
        // 默认死信路由
        String deadExchange = Constants.DEFAULT_DEAD_LETTER_EXCHANGE;
        // 默认标识组成

        return sendMessage(exchange, queue, type, deadExchange, exchange + queue, message);
    }

    /**
     * 指定路由、队列发送消息
     *
     * @param exchange 路由
     * @param queue    队列
     * @param message  消息
     * @return 发送执行结果
     */
    public static MessageResult sendMessage(String exchange, String queue, Object message) {
        // 使用默认路由类型
        String type = Constants.DEFAULT_EXCHANGE_TYPE;
        // 默认死信路由
        String deadExchange = Constants.DEFAULT_DEAD_LETTER_EXCHANGE;
        // 默认标识组成
        String routingKey = exchange + queue;
        return sendMessage(exchange, queue, type, deadExchange, routingKey, message);
    }

    /**
     * 指定路由、队列发送消息
     *
     * @param exchange 路由
     * @param queue    队列
     * @param message  消息
     * @return 发送执行结果
     */
    public static MessageResult sendMessage(String exchange, String queue, String routingKey, Object message) {
        // 使用默认路由类型
        String type = Constants.DEFAULT_EXCHANGE_TYPE;
        // 默认死信路由
        String deadExchange = Constants.DEFAULT_DEAD_LETTER_EXCHANGE;
        return sendMessage(exchange, queue, type, deadExchange, routingKey, message);
    }

    /**
     * 指定路由类型、队列发送消息
     *
     * @param queue   队列
     * @param type    类型
     * @param message 消息
     * @return 发送执行结果
     */
    public static MessageResult sendMessageWithExchangeType(String queue, String type, Object message) {
        // 使用默认路由
        String exchange = Constants.DEFAULT_EXCHANGE_NAME;
        // 默认死信路由
        String deadExchange = Constants.DEFAULT_DEAD_LETTER_EXCHANGE;
        return sendMessage(exchange, queue, type, deadExchange, queue, message);
    }

    /**
     * 指定路由名、队列名、路由类型发送消息
     *
     * @param exchange 路由
     * @param queue    队列
     * @param type     类型
     * @param message  消息
     * @return 发送执行结果
     */
    public static MessageResult sendMessageWithExchangeType(String exchange, String queue, String type, Object message) {
        // 默认死信路由
        String deadExchange = Constants.DEFAULT_DEAD_LETTER_EXCHANGE;
        // 默认标识组成
        String routingKey = exchange + queue;
        return sendMessage(exchange, queue, type, deadExchange, routingKey, message);
    }


    /**
     * 指定路由名、队列名、发送延时消息
     *
     * @param exchange 路由
     * @param queue    队列
     * @param message  消息
     * @return 发送执行结果
     */
    public static MessageResult sendDelayMessage(String exchange, String queue, Object message, final int millisecond) {
        // 使用默认路由类型
        String type = Constants.DEFAULT_EXCHANGE_TYPE;
        // 默认死信路由
        String deadExchange = Constants.DEFAULT_DEAD_LETTER_EXCHANGE;
        // 默认标识组成
        String routingKey = exchange + queue;
        return sendDelayMessage(exchange, queue, type, deadExchange, routingKey, message, millisecond);
    }

    /**
     * 指定路由名、队列名、发送延时消息
     *
     * @param queue       队列
     * @param message     消息
     * @param millisecond 延时 毫秒
     * @return 发送执行结果
     */
    public static MessageResult sendDelayMessage(String queue, Object message, final int millisecond) {
        // 使用默认路由
        String exchange = Constants.DEFAULT_DELAY_EXCHANGE_NAME;
        return sendDelayMessage(exchange, queue, message, millisecond);
    }

}
