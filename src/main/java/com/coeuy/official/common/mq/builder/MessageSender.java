package com.coeuy.official.common.mq.builder;


import com.coeuy.official.common.mq.common.MessageResult;

/**
 * 消息发送者接口
 *
 * @author Yarnk
 */
public interface MessageSender {

    /**
     * 发送普通消息
     *
     * @param message obj
     * @return 消息发送结果
     */
    MessageResult send(Object message);

    /**
     * 发送约定消息
     *
     * @param message message
     * @param time    time
     * @return 消息发送结果
     */
    MessageResult send(long time, Object message);

    /**
     * 发送延时普通消息
     *
     * @param message     obj
     * @param millisecond 毫秒
     * @return 消息发送结果
     */
    MessageResult sendDelayMessage(Object message, final int millisecond);


    /**
     * 发送延时普通消息
     *
     * @param message     obj
     * @param millisecond 毫秒
     * @param time        time
     * @return 消息发送结果
     */
    MessageResult sendDelayMessage(long time, Object message, final int millisecond);

}
