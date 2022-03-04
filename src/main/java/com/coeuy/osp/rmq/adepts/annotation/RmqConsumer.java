package com.coeuy.osp.rmq.adepts.annotation;

import com.coeuy.osp.rmq.adepts.common.Constants;
import com.coeuy.osp.rmq.adepts.common.ExchangeType;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Indexed;

import java.lang.annotation.*;


/**
 * 声明消费队列
 * @author yarnk
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
@Indexed
@Component
public @interface RmqConsumer {
    /**
     * 队列名称
     * @return s
     */
    String queue();

    /**
     * 交换机名称
     * @return exchange
     */
    String exchange() default Constants.DEFAULT_EXCHANGE_NAME;

    /**
     * 交换机主题类型
     * @return
     */
    ExchangeType type() default ExchangeType.DIRECT;

    /**
     * 核心线程数
     * @return
     */
    int core() default 1;

    /**
     * 最大线程数
     * @return
     */
    int max() default 1;

    /**
     * 消息处理时隔 毫秒
     * @return
     */
    int interval() default Constants.DEFAULT_INTERVAL_MILS;

    /**
     * 是否延时队列
     * @return
     */
    boolean delayed() default false;

    /**
     * routingKey
     * @return routingKey
     */
    String rk() default "";
}
