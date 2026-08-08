/*
 * Copyright (C) 2012, Apexes Network Technology. All rights reserved.
 * 
 *       http://www.apexes.net
 * 
 */
package net.apexes.fetion4j.core.user;

/**
 * Enumeration of Fetion presence states.
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 1.0.0
 */
public enum Presence {

    /**
     * 在线状态
     */
    ONLINE(400),
    /**
     * 离线状态，和隐身状态是一个值
     */
    OFFLINE(0),
    /**
     * 忙碌状态
     */
    BUSY(600),
    /**
     * 离开状态
     */
    AWAY(100),
    /**
     * 机器人状态
     */
    ROBOT(499),
    /**
     * 未知
     */
    UNKNOWN(-1);
    /**
     * 状态对应的值
     */
    private int value;

    /**
     * 构造枚举
     *
     * @param value
     */
    Presence(int value) {
        this.value = value;
    }

    /**
     * 返回值
     *
     * @return
     */
    public int getValue() {
        return this.value;
    }

    /**
     * 返回指定值的枚举
     *
     * @param v	值
     * @return	枚举
     */
    public static Presence valueOf(int v) {
        switch (v) {
            case 0:
                return OFFLINE;
            case 100:
                return AWAY;
            case 400:
                return ONLINE;
            case 499:
                return ROBOT;
            case 600:
                return BUSY;
            default:
                return UNKNOWN;
        }
    }
}
