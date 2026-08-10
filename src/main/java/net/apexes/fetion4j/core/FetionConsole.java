/*
 * Copyright (C) 2012, Apexes Network Technology. All rights reserved.
 * 
 *       http://www.apexes.net
 * 
 */
package net.apexes.fetion4j.core;

import net.apexes.fetion4j.core.user.Buddy;

/**
 * Interactive console contract for sending and receiving Fetion messages after login.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
public interface FetionConsole {
    
    /**
     * 返回用户信息。
     * 
     * @return 
     */
    UserInfo getUserInfo();
    
    /**
     * 如果控制器已经关闭则返回 true，否则返回 false。
     * 
     * @return 
     */
    boolean isClosed();
    
    /**
     * 关闭控制器。此操作将注销登录。
     * 
     * @throws FetionException 
     */
    void close() throws FetionException;

    /**
     * 添加好友。
     * 
     * @param userId 飞信号码
     * @param localName
     * @return 
     */
    Result addBuddy(int userId, String localName) throws FetionException;
    
    /**
     * 添加好友。
     * 
     * @param mobileNo 手机号码
     * @param localName
     * @return 
     */
    Result addBuddy(long mobileNo, String localName) throws FetionException;

    /**
     * 删除好友。
     * 
     * @param buddy
     * @return 
     */
    Result removeBuddy(Buddy buddy) throws FetionException;

    /**
     * 向好友发送信息。
     * 
     * @param buddy
     * @param message
     * @return 
     */
    Result sendMessage(Buddy buddy, String message) throws FetionException;
    
    /**
     * 向好友发送手机短信。
     * 
     * @param buddy
     * @param message
     * @return 
     */
    Result sendSMSMessage(Buddy buddy, String message) throws FetionException;
}
