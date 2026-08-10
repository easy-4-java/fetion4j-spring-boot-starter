/*
 * Copyright (C) 2011, Apexes Network Technology. All rights reserved.
 * 
 *       http://www.apexes.net
 * 
 */
package net.apexes.fetion4j.core.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

/**
 * Digest (hash) helpers used for Fetion authentication.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
public final class DigestHelper {

    /**
     * 计算MD5值
     * @param bytes		须计算的字节数组
     * @return			计算结果
     */
    public static byte[] MD5(byte[] bytes) {
        return digest("MD5", bytes);
    }

    /**
     * 计算SHA1值
     * @param bytes		须计算的字节数组
     * @return			计算结果
     */
    public static byte[] SHA1(byte[] bytes) {
        return digest("SHA-1", bytes);
    }

    /**
     * 计算HASH值
     * @param type		Hash类型
     * @param bytes		需计算的字节
     * @return			HASH结果
     */
    private static byte[] digest(String type, byte[] bytes) {
        try {
            MessageDigest dist = MessageDigest.getInstance(type);
            return dist.digest(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("Cannot find digest:" + type, e);
        }
    }

    /**
     * 生成256位AES对称加密算法的密钥
     * @return
     */
    public static byte[] createAESKey() {
        //java.security.SecureRandom random = new java.security.SecureRandom();
        //return random.generateSeed(32);
        try {
            KeyGenerator kgen = KeyGenerator.getInstance("AES");
            kgen.init(0x100);
            SecretKey skey = kgen.generateKey();
            return skey.getEncoded();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("Cannot find key generator: AES", e);
        }
    }
}
