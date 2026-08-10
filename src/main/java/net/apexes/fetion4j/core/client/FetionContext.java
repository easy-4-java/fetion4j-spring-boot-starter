/*
 * Copyright (C) 2012, Apexes Network Technology. All rights reserved.
 * 
 *       http://www.apexes.net
 * 
 */

package net.apexes.fetion4j.core.client;

import net.apexes.fetion4j.core.Account;
import net.apexes.fetion4j.core.AuthSupportable;
import net.apexes.fetion4j.core.LogHandler;
import net.apexes.fetion4j.core.SystemConfig;
import net.apexes.fetion4j.core.UserInfo;

/**
 * Context object exposing shared state and collaborators to the Fetion client.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
public interface FetionContext {
    
    /**
     * 返回客户端的机器码。
     * @return 
     */
    String getMachineCode();
    
    /**
     * 
     * @return 
     */
    Account getAccount();
    
    /**
     * 
     * @return 
     */
    AuthSupportable getAuthSupportable();
    
    /**
     * 返回系统配置。
     * @return 
     */
    SystemConfig getSystemConfig();
    
    /**
     * 
     * @return 
     */
    UserInfo getUserInfo();
    
    /**
     * 
     * @return 
     */
    CmccMobileValidator getCmccMobileValidator();
    
    /**
     * 
     * @return 
     */
    LogHandler getLogHandler();

}
