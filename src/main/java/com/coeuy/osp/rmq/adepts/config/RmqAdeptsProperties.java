package com.coeuy.osp.rmq.adepts.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * <p>
 * Application 配置
 * </p>
 *
 * @author Yarnk .  yarnk@coeuy.com
 * @date 2020/6/17 11:17
 */
@Data
@Slf4j
@Configuration
@ConfigurationProperties(prefix = "rmq-adepts")
public class RmqAdeptsProperties {
    /**
     * 是否开启debug模式
     */
    private boolean debug;

    private int connectionLimit;

    private int connectionTimeout;

}
