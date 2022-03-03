package com.coeuy.osp.rmq.adepts.common;

import com.coeuy.osp.rmq.adepts.builder.MessageQueueBuilder;
import com.coeuy.osp.rmq.adepts.consumer.MessageProcess;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * 构造器
 *
 * @param <T>
 */
@Slf4j
@Getter
public class ThreadPoolConsumerBuilder<T> {

    private int threadCount;
    private long intervalMils;
    private boolean delayed;
    private MessageQueueBuilder messageBrokerBuilder;
    private String exchange;
    private String routingKey;
    private String queue;
    private String type;
    private MessageProcess<T> messageProcess;

    public ThreadPoolConsumerBuilder<T> setThreadCount(int threadCount) {
        this.threadCount = threadCount;
        return this;
    }

    public ThreadPoolConsumerBuilder<T> setIntervalMils(long intervalMils) {
        this.intervalMils = intervalMils;
        return this;
    }

    public ThreadPoolConsumerBuilder<T> setDelayed(boolean delayed) {
        this.delayed = delayed;
        return this;
    }

    public ThreadPoolConsumerBuilder<T> setMessageBrokerBuilder(MessageQueueBuilder messageBrokerBuilder) {
        this.messageBrokerBuilder = messageBrokerBuilder;
        return this;
    }

    public ThreadPoolConsumerBuilder<T> setExchange(String exchange) {
        this.exchange = exchange;
        return this;
    }

    public ThreadPoolConsumerBuilder<T> setRoutingKey(String routingKey) {
        this.routingKey = routingKey;
        return this;
    }

    public ThreadPoolConsumerBuilder<T> setQueue(String queue) {
        this.queue = queue;
        return this;
    }

    public ThreadPoolConsumerBuilder<T> setType(String type) {
        this.type = type;
        return this;
    }

    public ThreadPoolConsumerBuilder<T> setMessageProcess(MessageProcess<T> messageProcess) {
        this.messageProcess = messageProcess;
        return this;
    }

    public ThreadPoolConsumer<T> build() {
        return new ThreadPoolConsumer<>(this);
    }

    @Override
    public String toString() {
        return "ThreadPoolConsumerBuilder{" +
                "threadCount=" + threadCount +
                ", intervalMils=" + intervalMils +
                ", delayed=" + delayed +
                ", messageBrokerBuilder=" + messageBrokerBuilder +
                ", exchange='" + exchange + '\'' +
                ", routingKey='" + routingKey + '\'' +
                ", queue='" + queue + '\'' +
                ", type='" + type + '\'' +
                ", messageProcess=" + messageProcess +
                '}';
    }
}