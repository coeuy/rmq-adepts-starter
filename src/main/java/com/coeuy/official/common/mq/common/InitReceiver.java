package com.coeuy.official.common.mq.common;

import com.coeuy.official.common.mq.builder.MessageConsumer;
import com.coeuy.official.common.mq.builder.MessageQueueBuilder;
import com.coeuy.official.common.mq.consumer.MessageProcess;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * <p> 初始化接收者（主动） </p>
 *
 * @author Yarnk
 * @date 2020/5/30 12:01
 */
@Slf4j
@Service
public class InitReceiver {

    @Resource
    private MessageQueueBuilder messageBrokerBuilder;

    public <T> MessageResult initAndConsume(final String exchange, final String queue, final String exchangeType, final String deadExchange, final String routingKey, boolean delayed, final MessageProcess<T> messageProcess) {
        MessageConsumer messageConsumer = messageBrokerBuilder.buildMessageConsumer(exchange, queue, exchangeType, deadExchange, routingKey, delayed, messageProcess);
        return messageConsumer.consume();
    }
}

