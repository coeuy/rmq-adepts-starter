package com.coeuy.osp.rmq.adepts.common;


/**
 * <p> 通道常量 </p>
 *
 * @author Yarnk
 * @date 2020/4/10 11:22
 */
public interface Constants {

    /**
     * ---------------------------------MQ系统常量start----------------------------------------/
     * /**默认队列名
     */
    String DEFAULT_QUEUE_NAME = "default-queue";

    String NULL_VALUE = "";
    /**
     * 默认路由名
     */
    String DEFAULT_EXCHANGE_NAME = "default-exchange";
    /**
     * 默认路由类型
     */
    String DEFAULT_EXCHANGE_TYPE = "direct";
    /**
     * 默认延时路由名
     */
    String DEFAULT_DELAY_EXCHANGE_NAME = "default-delay-exchange";
    /**
     * 默认死信路由名
     */
    String DEFAULT_DEAD_LETTER_EXCHANGE = "default-dead-letter-exchange";
    /**
     * 默认死信后缀
     */
    String DEFAULT_DEAD_LETTER_EXCHANGE_SUFFIX = "_dead";
    /**
     * 默认重试后缀
     */
    String DEFAULT_QUEUE_RETRY_SUFFIX = "_retry";
    /**
     * 默认延时后缀
     */
    String DEFAULT_QUEUE_DELAY_SUFFIX = "_delay";
    /**
     * 默认重试时间
     */
    Integer DEFAULT_WAIT_TO_RETRY_TIME = 10000;
    /**
     * 默认线程数
     */
    int DEFAULT_THREAD_COUNT = 1;
    /**
     * 处理间隔时间 单位 mils
     */
    int DEFAULT_INTERVAL_MILS = 0;
    /**
     * consumer失败后等待时间 mils
     */
    int ONE_SECOND = 1000;
    /**
     * 异常sleep时间 mils
     */
    int ONE_MINUTE = 5 * 1000;
    /**
     * MQ消息retry时间 mils
     */
    int RETRY_TIME_INTERVAL = 60 * 1000;
    /**
     * MQ消息有效时间
     */
    int VALID_TIME = ONE_MINUTE;


    /**
     * -------------------------------------消息转发中心队列约定---------------------------------------/
     */
    String FORWARD_SMS = "smart.home.forward.send.sms";


    String FORWARD_EMAIL = "smart.home.forward.send.email";


    String FORWARD_DING_TALK = "smart.home.forward.send.ding";


    /**
     * ---------------------------------USER模块常用队列约定---------------------------------------/
     * <p>
     * /**通过ID通知对应管理员下线
     */
    String ADMIN_LOGOUT = "smart.home.admin.logout";

    /**
     * 通过ID刷新管理员登录信息（不需要退出登录 修改敏感信息时按场景需求） @see EmailInfo.class
     */
    String REFRESH_ADMIN_LOGIN_INFO = "smart.home.auth.info.admin.refresh";

    /**
     * 清除管理员单位（租户）信息
     */
    String CLEAR_ADMIN_UNIT = "smart.home.unit.clear";

    /**
     * 修改密码通知用户下线重新登录，并且邮件或者短信通知用户（管理员）
     */
    String ADMIN_CHANGE_PASSWORD_MESSAGE = "smart.home.password.change.admin";

    /**
     * 修改密码通知用户下线重新登录，并且邮件或者短信通知用户（管理员）
     */
    String CUSTOMER_LOGIN_EDGE_OFF = "smart.home.auth.login.customer.edge.off";

    /**
     * 修改密码通知用户下线重新登录，并且邮件或者短信通知用户（管理员）
     */
    String ADMIN_CHANGE_ROLE = "smart.home.role.change";


    /**
     * ---------------------------------INFO模块常用队列约定---------------------------------------/
     * <p>
     * /**默认通知
     */
    String MESSAGE = "message";
}
