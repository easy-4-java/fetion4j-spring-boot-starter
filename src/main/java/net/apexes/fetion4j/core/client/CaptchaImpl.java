/*
 * Copyright (C) 2013, Apexes Network Technology. All rights reserved.
 *
 *       http://www.apexes.net
 *
 */
package net.apexes.fetion4j.core.client;

import net.apexes.fetion4j.core.Captcha;

/**
 * Default {@link net.apexes.fetion4j.core.Captcha} implementation.
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 1.0.0
 */
public class CaptchaImpl implements Captcha {

    /**
     * 算法
     */
    private String verifyAlgorithm;
    private String verifyTye;
    /**
     * 验证原因
     */
    private String text;
    private String tips;
    /**
     * 验证图片编号
     */
    private String imageId;
    /**
     * 验证图片数据
     */
    private byte[] imageData;
    private String code;
    private int failCount;

    public CaptchaImpl(String algorithm, String verifyTye, String text, String tips,
            String imageId, byte[] imageData) {
        this.verifyAlgorithm = algorithm;
        this.verifyTye = verifyTye;
        this.text = text;
        this.tips = tips;
        this.imageId = imageId;
        this.imageData = imageData;
        failCount = 0;
    }

    @Override
    public byte[] getImageData() {
        return imageData;
    }

    @Override
    public String getImageId() {
        return imageId;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public String getTips() {
        return tips;
    }

    @Override
    public String getVerifyAlgorithm() {
        return verifyAlgorithm;
    }

    @Override
    public String getVerifyType() {
        return verifyTye;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public int getFailCount() {
        return failCount;
    }

    public void setFailCount(int failCount) {
        this.failCount = failCount;
    }

    @Override
    public String toString() {
        return "Captcha{" + "verifyAlgorithm=" + verifyAlgorithm
                + ", verifyTye=" + verifyTye
                + ", text=" + text
                + ", tips=" + tips
                + ", imageId=" + imageId
                + ", code=" + code
                + ", failCount=" + failCount
                + '}';
    }
}
