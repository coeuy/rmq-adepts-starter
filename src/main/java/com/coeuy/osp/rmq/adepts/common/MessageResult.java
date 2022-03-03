package com.coeuy.osp.rmq.adepts.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p> 消息状态返回 <p/>
 *
 * @author Yarnk
 * @date 2020/05/27 18:20
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageResult {
    /**
     * 是否成功
     */
    private boolean isSuccess;

    /**
     * 提示信息
     */
    private String message;

    public static MessageResult success() {
        return new MessageResult(true, "成功");
    }

    public static MessageResult success(String message) {
        return new MessageResult(true, message);
    }

    public static MessageResult error() {
        return new MessageResult(false, "错误");
    }

    public static MessageResult error(String message) {
        return new MessageResult(false, message);
    }

}
