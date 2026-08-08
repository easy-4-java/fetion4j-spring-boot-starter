/*
 * Copyright (C) 2013, Apexes Network Technology. All rights reserved.
 *
 *       http://www.apexes.net
 *
 */
package net.apexes.fetion4j.core;

import java.io.IOException;

/**
 * Feedback callback used during Fetion authentication (e.g. captcha challenges).
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 1.0.0
 */
public interface AuthFeedback {
    
    /**
     * 提交验证码。
     * 
     * @param captchaCode 
     */
    void submit(String captchaCode);
    
    /**
     * 取消操作。
     */
    void cancel();
    
    /**
     * 换一个图形验证码。
     * @return 
     * @throws IOException 
     */
    Captcha tryAgain() throws IOException;
    
}
