/*
 * Copyright (C) 2013, Apexes Network Technology. All rights reserved.
 *
 *       http://www.apexes.net
 *
 */
package net.apexes.fetion4j.core;

import net.apexes.fetion4j.core.user.Buddy;
import net.apexes.fetion4j.core.user.User;
import net.apexes.fetion4j.core.util.XmlElement;

/**
 * Listener for asynchronous Fetion server notifications.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
public interface NotifyListener {
    
    /**
     * 传输出错。
     * 
     * @param message 错误信息。
     * @param exception 出错引发的异常。
     */
    void transfeError(String message, Exception exception);
    
    /**
     * 完成初始化时如果SystemConfig有更改。
     * 
     * @param systemConfig 从服务端获取到的 SystemConfig 对象。
     */
    void changedSystemConfig(XmlElement systemConfig);
    
    /**
     * 获取账号信息成功。(由SSI登录得到)
     * 
     * @param account 获得的账号。
     */
    void createdAccount(Account account);
    
    /**
     * 登录成功。
     * 
     * @param console 登录成功后返回的控制板。
     * @param userInfo 登录成功后的 UserInfo 对象。
     */
    void loginSuccessed(FetionConsole console, UserInfo userInfo);
    
    /**
     * 注销登录成功。
     */
    void logoutSuccessed();
    
    /**
     * 联系人资料有更新。
     * 
     * @param user 资料有更新的联系人。
     */
    void changedUser(User user);
    
    /**
     * 好友资料有更新，主要是指 relation 属性。
     * 
     * @param buddy 资料有更新的好友。
     * @param contactVersion 
     */
    void changedBuddy(Buddy buddy, String contactVersion);
    
    /**
     * 添加好友成功。
     * 
     * @param buddy 新添加的好友。
     * @param contactVersion 添加好友成功后 Contact 的版本号。
     */
    void addedBuddy(Buddy buddy, String contactVersion);
    
    /**
     * 删除好友成功。
     * 
     * @param buddy 已经成功删除的好友。
     * @param contactVersion 删除好友后的 Contact 版本号。
     */
    void deletedBuddy(Buddy buddy, String contactVersion);
    
    /**
     * 发送手机短信的数量更改。
     * 
     * @param dayCount 当天发送的短信数。
     * @param monthCount 当月发送的短信数。
     */
    void smsCountChanged(int dayCount, int monthCount);
    
}
