package com.coeuy.osp.rmq.adepts.producer;

import com.coeuy.osp.rmq.adepts.builder.MessageQueueBuilder;
import com.coeuy.osp.rmq.adepts.builder.MessageSender;
import com.coeuy.osp.rmq.adepts.common.Constants;
import com.coeuy.osp.rmq.adepts.common.ExchangeType;
import com.coeuy.osp.rmq.adepts.common.MessageResult;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Yarnk
 */
@Slf4j
public class MessageProducer {

    private final MessageQueueBuilder messageAccessBuilder;

    public MessageProducer(MessageQueueBuilder messageAccessBuilder) {
        this.messageAccessBuilder = messageAccessBuilder;
    }

    public MessageResult sendMessage(String exchange, String queue, ExchangeType type, String deadExchange, String routingKey, Object message) {
        MessageSender messageSender = messageAccessBuilder.buildMessageSender(exchange, queue, type, deadExchange, routingKey,false,false);
        return messageSender.send(message);
    }

    public MessageResult sendDelayMessage(String exchange, String queue, ExchangeType type, String deadExchange, String routingKey, Object message, final int millisecond) {
        MessageSender messageSender = messageAccessBuilder.buildMessageSender(exchange, queue, type, deadExchange, routingKey,false,false);
        return messageSender.sendDelayMessage(message, millisecond);
    }


    public MessageResult sendTxMessage(String exchange, String queue, ExchangeType type, String deadExchange, String routingKey, Object message) {
        MessageSender messageSender = messageAccessBuilder.buildMessageSender(exchange, queue, type, deadExchange, routingKey,true,false);
        return messageSender.send(message);
    }

    public MessageResult sendTxDelayMessage(String exchange, String queue, ExchangeType type, String deadExchange, String routingKey, Object message, final int millisecond) {
        MessageSender messageSender = messageAccessBuilder.buildMessageSender(exchange, queue, type, deadExchange, routingKey,true,false);
        return messageSender.sendDelayMessage(message, millisecond);
    }


    public MessageResult sendRetryMessage(String exchange, String queue, ExchangeType type, String deadExchange, String routingKey, Object message) {
        MessageSender messageSender = messageAccessBuilder.buildMessageSender(exchange, queue, type, deadExchange, routingKey,false,true);
        return messageSender.send(message);
    }

    public MessageResult sendRetryDelayMessage(String exchange, String queue, ExchangeType type, String deadExchange, String routingKey, Object message, final int millisecond) {
        MessageSender messageSender = messageAccessBuilder.buildMessageSender(exchange, queue, type, deadExchange, routingKey,false,true);
        return messageSender.sendDelayMessage(message, millisecond);
    }


    public MessageResult sendRetryTxMessage(String exchange, String queue, ExchangeType type, String deadExchange, String routingKey, Object message) {
        MessageSender messageSender = messageAccessBuilder.buildMessageSender(exchange, queue, type, deadExchange, routingKey,true,true);
        return messageSender.send(message);
    }

    public MessageResult sendRetryTxDelayMessage(String exchange, String queue, ExchangeType type, String deadExchange, String routingKey, Object message, final int millisecond) {
        MessageSender messageSender = messageAccessBuilder.buildMessageSender(exchange, queue, type, deadExchange, routingKey,true,true);
        return messageSender.sendDelayMessage(message, millisecond);
    }

    /**
     * 指定队列发送消息
     *
     * @param queue   队列
     * @param message 消息
     * @return 发送执行结果
     */
    public MessageResult sendMessage(String queue, Object message) {
        // 使用默认路由
        String exchange = Constants.DEFAULT_EXCHANGE_NAME;
        // 默认死信路由
        String deadExchange = Constants.DEFAULT_DEAD_LETTER_EXCHANGE;
        // 默认标识组成
        // 使用默认路由类型
        return sendMessage(exchange, queue, ExchangeType.DIRECT, deadExchange, queue, message);
    }

    /**
     * 指定路由、队列发送消息
     *
     * @param exchange 路由
     * @param queue    队列
     * @param message  消息
     * @return 发送执行结果
     */
    public MessageResult sendMessage(String exchange, String queue, Object message) {
        // 使用默认路由类型
        // 默认死信路由
        String deadExchange = Constants.DEFAULT_DEAD_LETTER_EXCHANGE;
        // 默认标识组成
        String routingKey = exchange + queue;
        return sendMessage(exchange, queue, ExchangeType.DIRECT, deadExchange, routingKey, message);
    }

    /**
     * 指定路由、队列发送消息
     *
     * @param exchange 路由
     * @param queue    队列
     * @param message  消息
     * @return 发送执行结果
     */
    public MessageResult sendMessage(String exchange, String queue, String routingKey, Object message) {
        // 使用默认路由类型
        // 默认死信路由
        String deadExchange = Constants.DEFAULT_DEAD_LETTER_EXCHANGE;
        return sendMessage(exchange, queue, ExchangeType.DIRECT, deadExchange, routingKey, message);
    }

    /**
     * 指定路由类型、队列发送消息
     *
     * @param queue   队列
     * @param type    类型
     * @param message 消息
     * @return 发送执行结果
     */
    public MessageResult sendMessageWithExchangeType(String queue, ExchangeType type, Object message) {
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
    public MessageResult sendMessageWithExchangeType(String exchange, String queue, ExchangeType type, Object message) {
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
    public MessageResult sendDelayMessage(String exchange, String queue, Object message, final int millisecond) {
        // 使用默认路由类型
        // 默认死信路由
        String deadExchange = Constants.DEFAULT_DEAD_LETTER_EXCHANGE;
        // 默认标识组成
        String routingKey = exchange + queue;
        return sendDelayMessage(exchange, queue, ExchangeType.DIRECT, deadExchange, routingKey, message, millisecond);
    }

    /**
     * 指定路由名、队列名、发送延时消息
     *
     * @param queue       队列
     * @param message     消息
     * @param millisecond 延时 毫秒
     * @return 发送执行结果
     */
    public MessageResult sendDelayMessage(String queue, Object message, final int millisecond) {
        // 使用默认路由
        String exchange = Constants.DEFAULT_EXCHANGE_NAME;
        return sendDelayMessage(exchange, queue, message, millisecond);
    }

}
