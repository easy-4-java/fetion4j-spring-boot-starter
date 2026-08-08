/*
 * Copyright (C) 2012, Apexes Network Technology. All rights reserved.
 * 
 *       http://www.apexes.net
 * 
 */
package net.apexes.fetion4j.core.user;

/**
 * Describes the relationship between the logged in user and a buddy.
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 1.0.0
 */
public enum Relation {

    /**
     * 发出了添加好友请求，但对方没有确认
     */
    UNCONFIRMED(0),
    /**
     * 已经是好友
     */
    BUDDY(1),
    /**
     * 对方拒绝了你添加好友的请求
     */
    DECLINED(2),
    /**
     * 陌生人
     */
    STRANGER(3),
    /**
     * 黑名单
     */
    BANNED(4);
    /**
     * 好友关系对应的值
     */
    private int value;

    /**
     * 以关系构造枚举
     *
     * @param value
     */
    Relation(int value) {
        this.value = value;
    }

    /**
     * 返回关系值
     *
     * @return
     */
    public int getValue() {
        return this.value;
    }

    /**
     * 从关系值中返回关系枚举
     *
     * @param v 	关系代表的值
     * @return	关系枚举
     */
    public static Relation valueOf(int v) {
        switch (v) {
            case 0:
                return UNCONFIRMED;
            case 1:
                return BUDDY;
            case 2:
                return DECLINED;
            case 3:
                return STRANGER;
            case 4:
                return BANNED;
            default:
                throw new IllegalArgumentException("Invalid relation value:" + v + ", expected 0,1,2,3,4.");
        }
    }
}
