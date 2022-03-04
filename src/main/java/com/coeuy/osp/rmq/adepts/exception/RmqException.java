package com.coeuy.osp.rmq.adepts.exception;

import java.io.Serializable;

/**
 * Rmq异常类
 */
public class RmqException extends RuntimeException implements Serializable {

    private static final long serialVersionUID = -262435628725446696L;

    public RmqException(String message) {
        super(message);
    }
}
