/*
 * Copyright (C) 2012, Apexes Network Technology. All rights reserved.
 * 
 *       http://www.apexes.net
 * 
 */
package net.apexes.fetion4j.core.sipc;

/**
 * Fetion SIPC protocol constants and status codes.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
public interface Sipc {
    
    /**
     * SIPC版本字符串
     */
    String SIPC_VERSION = "SIP-C/4.0";
    
    /**
     * Authorization
     */
    String FIELD_A = "A";
    /**
     * ContentType
     */
    String FIELD_C = "C";
    /**
     * Client
     */
    String FIELD_CL = "CL";
    /**
     * Cnoce
     */
    String FIELD_CN = "CN";
    /**
     * Date
     */
    String FIELD_D = "D";
    /**
     * ContentEncoding
     */
    String FIELD_E = "E";
    /**
     * From
     */
    String FIELD_F = "F";
    /**
     * CallID
     */
    String FIELD_I = "I";
    /**
     * Supported
     */
    String FIELD_K = "K";
    /**
     * ContentLength
     */
    String FIELD_L = "L";
    /**
     * Contact
     */
    String FIELD_M = "M";
    /**
     * Event
     */
    String FIELD_N = "N";
    /**
     * CSeq
     */
    String FIELD_Q = "Q";
    /**
     * QMethod
     */
    String FIELD_QMETHOD = "QMETHOD";
    /**
     * ReferredBy
     */
    String FIELD_RB = "RB";
    /**
     * Require
     */
    String FIELD_RQ = "RQ";
    /**
     * ReferTo
     */
    String FIELD_RT = "RT";
    /**
     * 
     */
    String FIELD_S = "S";
    /**
     * Source
     */
    String FIELD_SO = "SO";
    /**
     * To
     */
    String FIELD_T = "T";
    /**
     * 
     */
    String FIELD_TEMP = "TEMP_A";
    /**
     * Unsupported
     */
    String FIELD_UK = "UK";
    /**
     * 
     */
    String FIELD_V = "V";
    /**
     * WWWAuthenticate
     */
    String FIELD_W = "W";
    /**
     * Expires
     */
    String FIELD_X = "X";
    /**
     * MessageID
     */
    String FIELD_XI = "XI";
    /**
     * Ack消息，会话(Session)发起方用以向SIP Proxy确认会话的开始。
     */
    String METHOD_A = "A";
    /**
     * Bye消息，用以结束会话。
     */
    String METHOD_B = "B";
    /**
     * BENotify消息，不需要回复的通知。
     */
    String METHOD_BN = "BN";
    /**
     * Cancel消息，用以取消正在进行中的INVITE请求。
     */
    String METHOD_C = "C";
    /**
     * 用以建立会话过程的SIP方法。
     */
    String METHOD_I = "I";
    /**
     * Info消息，用以在SIP协议中支持应用相关的控制信息，飞信传文件时，用到了这个方法。
     */
    String METHOD_IN = "IN";
    /**
     * Message消息，用以支持即时消息的SIP方法。
     */
    String METHOD_M = "M";
    /**
     * Notify消息，需要回复的通知。
     */
    String METHOD_N = "N";
    /**
     * Options消息，用来查询对端或服务器的能力。 比如了解对方支持什么编码类型，飞信传文件时会使用。
     */
    String METHOD_O = "O";
    /**
     * Refer消息，其功能是要求接受方通过使用在请求中提供的联系地址信息联系第三方。
     */
    String METHOD_REF = "REF";
    /**
     * Register消息，这是SIP的标准方法，用来向服务器登记。
     */
    String METHOD_R = "R";
    /**
     * Service消息，用来向SIP服务器请求额外的服务。
     */
    String METHOD_S = "S";
    /**
     * Subscribf消息，这个方法被用来向服务器订阅事件异步通知。 服务器就会用
     * NOTIFY或BENOTIFY(微软扩展的）方法，将事件通知给飞信客户端。
     * 如飞信订阅用户的presence事件，比如上线啊，下线啊什么的
     */
    String METHOD_SUB = "SUB";
    /**
     * 操作已经成功接收，正在操作中
     */
    int STATUS_TRYING = 100;
    /**
     * 部分结果
     */
    int STATUS_PARTIAL = 188;
    /**
     * 操作成功
     */
    int STATUS_ACTION_OK = 200;
    /**
     * 发送手机短信成功
     */
    int STATUS_SEND_SMS_OK = 280;
    /**
     * 请求包损坏
     */
    int STATUS_BAD = 400;
    /**
     * 未授权的
     */
    int STATUS_UNAUTHORIZED = 401;
    /**
     * 操作被阻止
     */
    int STATUS_FORBIDDEN = 403;
    /**
     * 未找到
     */
    int STATUS_NOT_FOUND = 404;
    /**
     * 需要验证
     */
    int STATUS_EXTENSION_REQUIRED = 421;
    /**
     * 验证失败
     */
    int STATUS_BAD_EXTENSION = 420;
    /**
     *
     */
    int STATUS_BUSY_HERE = 486;
    /**
     *
     */
    int STATUS_REQUEST_FAILUREV4 = 444;
    /**
     * 请求失败
     */
    int STATUS_REQUEST_FAILURE = 494;
    /**
     * 
     */
    int STATUS_SERVER_INTERNAL_ERROR = 500;
    /**
     * 服务暂时不可用
     */
    int STATUS_SERVER_UNAVAILABLE = 503;
    /**
     * 对方已经存在
     */
    int STATUS_TA_EXIST = 521;
    /**
     * 没有定义
     */
    int STATUS_NO_SUBSCRIPTION = 522;
    /**
     * 超时
     */
    int STATUS_TIME_OUT = 504;
}
