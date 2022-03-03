package com.coeuy.osp.rmq.adepts.common;

import com.coeuy.osp.rmq.adepts.builder.MessageSender;
import com.coeuy.osp.rmq.adepts.config.RabbitThreadFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 消息发送重试机制
 *
 * @author Yarnk
 */
@Slf4j
public class RetryCache {
    private boolean stop;
    private int retryTime = 3;
    private MessageSender sender;
    private ExecutorService executor;
    private final AtomicLong id = new AtomicLong();
    private final Map<Long, MessageWithTime> map = new ConcurrentSkipListMap<>();

    public void setRetryTime(int retryTime) {
        this.retryTime = retryTime;
    }


    public void setSender(MessageSender sender) {
        executor = new ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(15), RabbitThreadFactory.create("rmq-retry-executor", false), new ThreadPoolExecutor.AbortPolicy());
        this.sender = sender;
        this.stop = false;
        start();
    }

    public long generateId() {
        return id.incrementAndGet();
    }

    public void add(MessageWithTime messageWithTime) {
        map.putIfAbsent(messageWithTime.getId(), messageWithTime);
    }

    public void del(long id) {
        map.remove(id);
    }

    /**
     * 运行重试线程
     */
    private void start() {
        executor.execute(() -> {
            while (!this.stop) {
                log.info("消息发送重试启动···");
                try {
                    Thread.sleep(15 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                long now = System.currentTimeMillis();
                // 遍历失败消息集合
                for (Map.Entry<Long, MessageWithTime> entry : map.entrySet()) {
                    // 获取发送失败的消息
                    MessageWithTime messageWithTime = entry.getValue();
                    if (null != messageWithTime) {
                        // 判断发送时间有没有达到重发时间
                        if (messageWithTime.getTime() + Constants.VALID_TIME < now) {
                            log.info("重发消息:\n {}", messageWithTime.getMessage());
                            MessageResult messageResult = sender.send(messageWithTime);
                            if (!messageResult.isSuccess()) {
                                log.warn("消息重发失败 {} 错误信息 {}", messageWithTime, messageResult.getMessage());
                            } else {
                                log.info("消息重发成功 ：{}", messageWithTime.getMessage());
                                del(entry.getKey());
                            }
                        } else {
                            log.info("重试时间 {} 分钟已过 清除缓存:\n ", retryTime);
                            log.warn("失败消息 ：{}", messageWithTime.getMessage());
                            del(entry.getKey());
                        }
                    }
                }
            }
            // 当没有存在失败消息时，关闭当前线程
            Runtime.getRuntime()
                    .addShutdownHook(new Thread(this::stop));
        });
    }
    public void stop() {
        log.info("Retry close ···");
        this.stop = true;
    }
}
