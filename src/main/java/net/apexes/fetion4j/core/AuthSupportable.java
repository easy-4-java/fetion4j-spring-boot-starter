/*
 * Copyright (C) 2012, Apexes Network Technology. All rights reserved.
 * 
 *       http://www.apexes.net
 * 
 */
package net.apexes.fetion4j.core;

/**
 * Contract for objects able to provide Fetion authentication support.
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 1.0.0
 */
public interface AuthSupportable {
    
    /**
     * 需要输入图形验证码。
     * 
     * @param captcha 包含验证图片等信息的对象。
     * @param feedback 验证回执。
     */
    void needAuth(Captcha captcha, AuthFeedback feedback);
    
}
