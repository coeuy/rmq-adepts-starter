package com.coeuy.osp.rmq.adepts.builder;


import com.coeuy.osp.rmq.adepts.common.MessageResult;

/**
 * 消息消费者接口
 *
 * @author Yarnk
 */
public interface MessageConsumer {

    /**
     * 消费消息
     *
     * @return 消息消费结果
     */
    MessageResult consume();


}
