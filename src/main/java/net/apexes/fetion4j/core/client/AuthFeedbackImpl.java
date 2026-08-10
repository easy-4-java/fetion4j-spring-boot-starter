/*
 * Copyright (C) 2013, Apexes Network Technology. All rights reserved.
 *
 *       http://www.apexes.net
 *
 */
package net.apexes.fetion4j.core.client;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import net.apexes.fetion4j.core.AuthFeedback;
import net.apexes.fetion4j.core.Captcha;
import net.apexes.fetion4j.core.sipc.ResponseMessage;
import net.apexes.fetion4j.core.util.XmlElement;

/**
 * Default {@link net.apexes.fetion4j.core.AuthFeedback} implementation.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
public class AuthFeedbackImpl implements AuthFeedback {
    
    private FetionContext context;
    private CaptchaImpl captcha;
    private XmlElement xml;
    private ResponseMessage response;
    private CountDownLatch latch;
    
    public AuthFeedbackImpl(FetionContext context, CaptchaImpl captcha, XmlElement xml) {
        this.context = context;
        this.captcha = captcha;
        this.xml = xml;
        latch = new CountDownLatch(1);
    }
    
    public AuthFeedbackImpl(FetionContext context, CaptchaImpl captcha, ResponseMessage response) {
        this.context = context;
        this.captcha = captcha;
        this.response = response;
        latch = new CountDownLatch(1);
    }
    
    private CaptchaImpl createCaptchaImpl() throws IOException {
        CaptchaImpl impl;
        if (xml != null) {
            XmlElement vEl = xml.getChild("verification");
            String algorithm = vEl.getStringAttribute("algorithm");
            String type = vEl.getStringAttribute("type");
            String text = vEl.getStringAttribute("text");
            String tips = vEl.getStringAttribute("tips");
            // 获取图形验证码的相关信息
            impl = ClientHelper.getCaptcha(context, algorithm, type, text, tips);
        } else {
            impl = ClientHelper.getCaptcha(context, response);
        }
        return impl;
    }

    @Override
    public void submit(String captchaCode) {
        captcha.setCode(captchaCode);
        latch.countDown();
    }

    @Override
    public void cancel() {
        captcha = null;
        latch.countDown();
    }

    @Override
    public Captcha tryAgain() throws IOException {
        captcha = createCaptchaImpl();
        return captcha;
    }
    
    public void authAndWait() throws IOException {
        if (captcha == null) {
            captcha = createCaptchaImpl();
        } else {
            captcha.setFailCount(captcha.getFailCount() + 1);
        }
        System.out.println("ImageId:" + captcha.getImageId());
        context.getAuthSupportable().needAuth(captcha, this);
        try {
            latch.await();
        } catch (InterruptedException ex) {
        }
    }
    
    /**
     * 
     * @return 返回 null 表示取消操作
     */
    public CaptchaImpl getCaptcha() {
        return captcha;
    }
    
}
