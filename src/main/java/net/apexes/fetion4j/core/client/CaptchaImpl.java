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
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
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
    /** @return return the image data. */
    public byte[] getImageData() {
        return imageData;
    }

    @Override
    /** @return return the image id. */
    public String getImageId() {
        return imageId;
    }

    @Override
    /** @return return the text. */
    public String getText() {
        return text;
    }

    @Override
    /** @return return the tips. */
    public String getTips() {
        return tips;
    }

    @Override
    /** @return return the verify algorithm. */
    public String getVerifyAlgorithm() {
        return verifyAlgorithm;
    }

    @Override
    /** @return return the verify type. */
    public String getVerifyType() {
        return verifyTye;
    }

    /** @return return the code. */
    public String getCode() {
        return code;
    }

    /** @param code set the code. */
    public void setCode(String code) {
        this.code = code;
    }

    @Override
    /** @return return the fail count. */
    public int getFailCount() {
        return failCount;
    }

    /** @param failCount set the fail count. */
    public void setFailCount(int failCount) {
        this.failCount = failCount;
    }

    @Override
    /**
     * <p>To string.</p>
     * @return the result
     */
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
