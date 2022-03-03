package com.coeuy.osp.rmq.adepts.consumer;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.GetResponse;
import com.rabbitmq.client.ShutdownSignalException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.support.DefaultMessagePropertiesConverter;
import org.springframework.amqp.rabbit.support.MessagePropertiesConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * <p> 主动接收消息 </p>
 *
 * @author Yarnk
 * @date 2020/5/30 11:52
 */
@Slf4j
@Component
public class ReceiveMessage {

    @Resource
    private ConnectionFactory connectionFactory;


    @Resource
    private MessageConverter messageConverter;


    /**
     * 主动获取消息
     *
     * @param queue   队列
     * @param timeOut 超时时间 秒
     * @param <T>     T
     * @return message
     */
    private <T> T receive(String queue, boolean autoAck, int timeOut, boolean waiting) {
        Connection connection = connectionFactory.createConnection();
        Channel channel = connection.createChannel(true);
        try {
            //1 通过basicGet获取原始数据
            GetResponse response;
            try {
                response = channel.basicGet(queue, autoAck);
            } catch (Exception e) {
                log.error("信道建立异常");
                channel.close();
                return null;
            }
            final int sleepTime = 1000;
            if (waiting) {
                while (response == null) {
                    response = channel.basicGet(queue, autoAck);
                    Thread.sleep(sleepTime);
                }
            } else {
                int count = 0;
                while (count < timeOut && response == null) {
                    count++;
                    response = channel.basicGet(queue, autoAck);
                    Thread.sleep(sleepTime);
                }
                if (response == null) {
                    log.info("消费等待超时，没有接收到消息");
                    return null;
                }
            }
            final MessagePropertiesConverter messagePropertiesConverter = new DefaultMessagePropertiesConverter();
            Message message = new Message(response.getBody(),
                    messagePropertiesConverter.toMessageProperties(response.getProps(), response.getEnvelope(), "UTF-8")
            );
            try {
                // 2 将原始数据转换为特定类型的包
                @SuppressWarnings("unchecked")
                T t = (T) messageConverter.fromMessage(message);
                log.info("手动获取消息 {}", t);
                // 确认消费
                channel.basicAck(response.getEnvelope()
                        .getDeliveryTag(), false);
                return t;

            } catch (Exception e) {
                log.error("消息处理异常:\n", e);
                channel.basicNack(response.getEnvelope()
                        .getDeliveryTag(), false, true);
            }

        } catch (InterruptedException e) {
            log.error("exception:\n", e);


        } catch (ShutdownSignalException | ConsumerCancelledException | IOException e) {
            log.error("exception:\n", e);

            try {
                // 关闭
                channel.close();
            } catch (IOException | TimeoutException ex) {
                log.error("exception:\n", ex);
            }
        } catch (Exception e) {
            log.info("exception:\n ", e);

            try {
                channel.close();
            } catch (IOException | TimeoutException ex) {
                ex.printStackTrace();
            }
        }

        return null;
    }


    /**
     * 主动拉取 （拉取一次）
     *
     * @param queue 队列
     * @param <T>   T
     * @return message
     */
    public <T> T receive(String queue, Boolean autoAck) {
        return receive(queue, autoAck, 1, false);
    }

    /**
     * 主动拉取 （拉取一次）
     *
     * @param queue 队列
     * @param <T>   T
     * @return message
     */
    public <T> T receive(String queue) {
        return receive(queue, false, 1, false);
    }

    /**
     * 等待接收（阻塞）
     *
     * @param queue 队列
     * @param <T>   T
     * @return message
     */
    public <T> T waitingToReceive(String queue) {
        return receive(queue, false, 0, true);
    }

    /**
     * 主动拉取（设置超时 秒）
     *
     * @param queue 队列名
     * @param <T>   T
     * @return message
     */
    public <T> T receive(String queue, int timeOut) {
        return receive(queue, false, timeOut, false);
    }

}
