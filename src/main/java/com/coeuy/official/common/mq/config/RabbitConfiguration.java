package com.coeuy.official.common.mq.config;

import com.coeuy.official.common.mq.builder.MessageQueueBuilder;
import com.coeuy.official.common.mq.producer.MessageProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.annotation.Resource;

/**
 * <p> MQ核心配置 </p>
 *
 * @author Yarnk
 * @date 2020/4/10 11:21
 */
@Slf4j
@Configuration
@ComponentScan(basePackages = {"com.coeuy.official"})
public class RabbitConfiguration {


    public RabbitConfiguration() {
        log.info("                                          __                            __                       \n" +
                " /|/|                                    /  |                         |/  |      /    |          \n" +
                "( / | ___  ___  ___  ___  ___  ___      (   |      ___       ___      |___| ___    ___| ___  ___ \n" +
                "|   )|___)|___ |___ |   )|   )|___)     |  \\)|   )|   )|   )|___)     |   )|   )| |   )|   )|___)\n" +
                "|  / |__   __/  __/ |__/||__/ |__       |__/\\|__/ |__/||__/ |__       |__/ |    | |__/ |__/ |__  \n" +
                "                         __/                                                           __/       ");
    }

    @Resource
    private RabbitProperties rabbitProperties;

    @Bean
    public MessageQueueBuilder getMessageBrokerBuilder() {
        return new MessageQueueBuilder(rabbitTemplate(), getConnectionFactory(), rabbitAdmin(), jackson2JsonMessageConverter());
    }

    @Bean
    public MessageProducer getMessageProducer() {
        return new MessageProducer(getMessageBrokerBuilder());
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    @Primary
    protected RabbitTemplate rabbitTemplate() {
        RabbitTemplate rabbitTemplate = new RabbitTemplate();
        // 连接工厂
        try {
            rabbitTemplate.setConnectionFactory(getConnectionFactory());
        } catch (Exception e) {
            log.error("创建连接异常", e);
        }
        // 序列化器
        rabbitTemplate.setMessageConverter(jackson2JsonMessageConverter());
        // 走完整个流程 防止消息丢失
        rabbitTemplate.setMandatory(true);
        // 设置回调消息
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            assert correlationData != null;
            if (ack) {
                log.info("\n回调ID [{}] 消息发送到交换机 成功 :\n{}", correlationData.getId(), cause);
            } else {
                assert cause != null;
                final String shutdown = "channel shutdown";
                final String closed = "Channel closed";
                if (cause.contains(shutdown) || cause.contains(closed)) {
                    if (log.isDebugEnabled()) {
                        log.debug("资源关闭···");
                    }
                } else {
                    log.warn("\n回调ID [{}] 消息发送到交换机 失败 :\n{}", correlationData.getId(), cause);

                }
            }
        });
        // 失败回调
        rabbitTemplate.setReturnCallback((message, replyCode, replyText, tmpExchange, tmpRoutingKey) -> {
            log.warn("\n消息发送失败: [{}] [{}] [{}] [{}]", replyCode, replyText, tmpExchange, tmpRoutingKey);
            log.warn("\n消息从交换机路由到队列失败: \n路由: [{}], \n路由关系: [{}],\n 回调代码: [{}], \n回调信息: [{}], \n消息内容: [{}]", tmpExchange, tmpRoutingKey, replyCode, replyText, message);
        });
        return rabbitTemplate;
    }


    @Bean
    @Primary
    protected RabbitAdmin rabbitAdmin() {
        return new RabbitAdmin(getConnectionFactory());
    }


    @Bean
    @Primary
    protected ConnectionFactory getConnectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        log.info("create connect {}", rabbitProperties.getAddresses());
        connectionFactory.setAddresses(rabbitProperties.getAddresses());
        connectionFactory.setUsername(rabbitProperties.getUsername());
        connectionFactory.setPassword(rabbitProperties.getPassword());
        connectionFactory.setVirtualHost(rabbitProperties.getVirtualHost());
        connectionFactory.setPublisherReturns(true);
        connectionFactory.setCacheMode(CachingConnectionFactory.CacheMode.CHANNEL);
        connectionFactory.setChannelCacheSize(100);
        return connectionFactory;
    }
}
