package com.coeuy.official.common.mq.common;

import com.coeuy.official.common.mq.builder.MessageConsumer;
import com.coeuy.official.common.mq.builder.MessageQueueBuilder;
import com.coeuy.official.common.mq.config.RabbitThreadFactory;
import com.coeuy.official.common.mq.consumer.MessageProcess;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Yarnk
 */
@Slf4j
public class ThreadPoolConsumer<T> {
    private final ExecutorService executor;
    private final ThreadPoolConsumerBuilder<T> infoHolder;
    private boolean stop = false;

    private ThreadPoolConsumer(ThreadPoolConsumerBuilder<T> threadPoolConsumerBuilder) {
        this.infoHolder = threadPoolConsumerBuilder;
        executor = new ThreadPoolExecutor(threadPoolConsumerBuilder.threadCount,
                threadPoolConsumerBuilder.threadCount, 0,
                TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(15),
                RabbitThreadFactory.create("rmq-consumer-executor", false),
                new ThreadPoolExecutor.AbortPolicy()
        );
    }

    /**
     * 运行监听
     */
    public void start() {
        log.info("开始执行监听 线程数[{}] 队列[{}] ", infoHolder.threadCount, infoHolder.queue);
        for (int i = 0;
             i < infoHolder.threadCount;
             i++) {
            // 构造messageConsumer
            final MessageConsumer messageConsumer = infoHolder.messageBrokerBuilder.buildMessageConsumer(
                    infoHolder.exchange,
                    infoHolder.queue,
                    infoHolder.type,
                    Constants.DEFAULT_DEAD_LETTER_EXCHANGE,
                    infoHolder.routingKey,
                    infoHolder.delayed,
                    infoHolder.messageProcess
            );
            executor.execute(() -> {
                while (!stop) {
                    try {
                        // 执行consume 消费
                        MessageResult messageResult = messageConsumer.consume();
                        if (infoHolder.intervalMils > 0) {
                            try {
                                Thread.sleep(infoHolder.intervalMils);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                                log.error("interrupt ", e);
                            }
                        }
                        if (messageResult == null) {

                            break;
                        } else if (!messageResult.isSuccess()) {
                            log.warn("消费失败： Msg[{}] ", messageResult.getMessage());
                        } else {
                            log.info("消费成功：Msg [{}]", messageResult.getMessage());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        log.error("消费异常： ", e);
                    }
                }

            });
            shutdown();
        }
        Runtime.getRuntime()
                .addShutdownHook(new Thread(this::stop));
    }

    public void stop() {
        log.info("MessageConsumer to Shutdown ···");
        this.stop = !stop;
        try {
            Thread.sleep(Constants.ONE_SECOND);
            shutdown();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void shutdown() {
        if (!executor.isShutdown()) {
            executor.shutdown();
        }

    }

    /**
     * 构造器
     *
     * @param <T>
     */
    public static class ThreadPoolConsumerBuilder<T> {
        int threadCount;
        long intervalMils;
        boolean delayed;
        MessageQueueBuilder messageBrokerBuilder;
        String exchange;
        String routingKey;
        String queue;
        String type;
        MessageProcess<T> messageProcess;

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
    }
}
