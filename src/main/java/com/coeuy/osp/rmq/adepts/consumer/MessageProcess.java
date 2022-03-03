package com.coeuy.osp.rmq.adepts.consumer;


import com.coeuy.osp.rmq.adepts.common.MessageResult;

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
