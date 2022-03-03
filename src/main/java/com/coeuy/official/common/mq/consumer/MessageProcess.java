package com.coeuy.official.common.mq.consumer;


import com.coeuy.official.common.mq.common.MessageResult;

/**
 * <p> 消息处理接口 </p>
 *
 * @author Yarnk
 */
public interface MessageProcess<T> {


    /**
     * 消息接收处理
     *
     * @param message 消息
     * @return {@link MessageResult}
     */
    MessageResult process(T message);
}
