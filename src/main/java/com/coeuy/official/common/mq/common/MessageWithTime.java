package com.coeuy.official.common.mq.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Yarnk
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageWithTime {
    private long id;
    private long time;
    private Object message;
}
