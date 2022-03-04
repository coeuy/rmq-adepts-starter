package com.coeuy.osp.rmq.adepts.common;

import org.springframework.amqp.core.ExchangeTypes;

/**
 * @author yarnk
 */
public enum ExchangeType {
    /***/
    DIRECT(ExchangeTypes.DIRECT),
    TOPIC(ExchangeTypes.TOPIC),
    FANOUT(ExchangeTypes.FANOUT),
    HEADERS(ExchangeTypes.HEADERS),
    SYSTEM(ExchangeTypes.SYSTEM);
    private final String name;
    ExchangeType(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }
}
