package com.coeuy.osp.rmq.adepts.common;

import com.coeuy.osp.rmq.adepts.builder.MessageConsumer;
import com.coeuy.osp.rmq.adepts.config.RabbitThreadFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;

/**
 * @author Yarnk
 */
@Slf4j
public class ThreadPoolConsumer<T> {


    private final ExecutorService executor;

    private final ThreadPoolConsumerBuilder<T> infoHolder;

    private boolean stop = false;

    public ThreadPoolConsumer(ThreadPoolConsumerBuilder<T> threadPoolConsumerBuilder) {
        ThreadFactory threadFactory = RabbitThreadFactory.create("rmq-adepts-consumer-executor", true);
        this.infoHolder = threadPoolConsumerBuilder;
        this.executor = new ThreadPoolExecutor(
                threadPoolConsumerBuilder.getThreadCount(),
                threadPoolConsumerBuilder.getThreadCount() + 10,
                1000,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(10),
                threadFactory,
                new ThreadPoolExecutor.AbortPolicy());
    }


    /**
     * 运行监听
     */
    public void start() {
        // 构造messageConsumer
        log.info("开始执行监听 线程数[{}] 队列[{}] ", infoHolder.getThreadCount(), infoHolder.getQueue());
        for (int i = 0; i < infoHolder.getThreadCount(); i++) {
            log.info("开始执行第{}条线程", i + 1);
            try{
                this.executor.execute(() -> {
                    MessageConsumer messageConsumer = infoHolder.getMessageBrokerBuilder().buildMessageConsumer(
                            infoHolder.getExchange(),
                            infoHolder.getQueue(),
                            infoHolder.getType(),
                            Constants.DEFAULT_DEAD_LETTER_EXCHANGE,
                            infoHolder.getRoutingKey(),
                            infoHolder.isDelayed(),
                            infoHolder.getMessageProcess()
                    );
                    while (!stop) {
                        try {
                            // 执行consume 消费
                            final MessageResult messageResult = messageConsumer.consume();
                            if (infoHolder.getIntervalMils() > 0) {
                                try {
                                    Thread.sleep(infoHolder.getIntervalMils());
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                    log.error("interrupt ", e);
                                }
                            }
                            if (messageResult == null) {
                                log.warn("消息处理确认为空");
                                break;
                            }
                            if (!messageResult.isSuccess()) {
                                log.warn("消费失败: 回执消息[{}] ", messageResult.getMessage());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            log.error("消费异常： ", e);
                        }
                    }
                });
            }catch (Exception e){
                log.info("调度任务执行失败",e);
                shutdown();
            }

        }
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
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
            log.info("消费任务线程关闭");
            executor.shutdown();
        }

    }

    @Override
    public String toString() {
        return "ThreadPoolConsumer{" +
                "executor=" + executor +
                ", infoHolder=" + infoHolder +
                ", stop=" + stop +
                '}';
    }

}
