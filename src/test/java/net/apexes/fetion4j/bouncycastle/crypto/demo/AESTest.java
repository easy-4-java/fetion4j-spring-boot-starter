package net.apexes.fetion4j.bouncycastle.crypto.demo;

/*
 * Copyright (C) 2011, Apexes Network Technology. All rights reserved.
 * 
 *       http://www.apexes.net
 * 
 */
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import net.apexes.fetion4j.bouncycastle.crypto.BufferedBlockCipher;
import net.apexes.fetion4j.bouncycastle.crypto.engines.AESLightEngine;
import net.apexes.fetion4j.bouncycastle.crypto.modes.CBCBlockCipher;
import net.apexes.fetion4j.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import net.apexes.fetion4j.bouncycastle.crypto.params.KeyParameter;
import net.apexes.fetion4j.bouncycastle.crypto.params.ParametersWithIV;
//import org.bouncycastle.util.encoders.Hex;
// NOTE: sun.misc.BASE64Decoder was removed in JDK 9+; replaced by java.util.Base64.

/**
 *
 * @author HeDYn<hedyn@foxmail.com>
 */
public class AESTest {

    public static void main(String[] args) throws Exception {
        //jec();
        System.out.println("==========================");
        
        java.security.SecureRandom random = new java.security.SecureRandom();
        byte[] keys = random.generateSeed(32);
        key = byte2HexStringWithoutSpace(keys);
        System.out.println(key);
        bouncycastle();

        /*
        byte[] encryptedContent = new BASE64Decoder().decodeBuffer(target);
        byte[] keyBytes = hexString2ByteNoSpace(key);
        byte[] ivBytes = hexString2ByteNoSpace(IV);
        byte[] decryptedContent = bouncycastleDecrypt(encryptedContent, keyBytes, ivBytes);
        System.out.println(new String(decryptedContent));
        
        decryptedContent = AESDecrypt(encryptedContent, keyBytes, ivBytes);
        System.out.println(new String(decryptedContent));
        //*/
    }
    
    private static String target = "LToJhkEFXAJvhOvNGEBvfzizrunz9YEOXbcP0aPyOcbbKBG0jmuW6k+n5Xkpt3mnUuyMeqew/TbEt7zRocS5smGqn0uYE+6YKf9G8GZAJTKEBgI6JrYgNJcTEDy4bwXvC9ucx6zTZV62Kb144UCjdG223JU+GiKB4gYcEmRWlX7i3P8hALzrcTg8IDUWOF1U";
    private static String key = "396C37DF0CED1DF2AA2D27D3CF9DA871A0E9EEC65DA7CD718EB7E09B8C000050";
    private static String IV = "00399F3D125DB5530AB5E000D6B0F45A";
    private static String result = "CBIOAAA1FTUVwCZCzg0EIwhm8Y6WMLKJbb3t/AM+HP1xwsvBFxHs5aS3DI9gB1jA2HyucZsqdAbM//DFErTFsLjK4M26BamRx1FrJQ57rsF3A4jSCmag/YvBUuHNk73maSTbrH4AAA==";

    private static void jec() throws Exception {
        //*
        byte[] targetBytes = java.util.Base64.getDecoder().decode(target);
        byte[] keyBytes = hexString2ByteNoSpace(key);
        byte[] ivBytes = hexString2ByteNoSpace(IV);

        System.out.println("decrypted bytes:" + byte2HexStringWithoutSpace(targetBytes));
        // Get the KeyGenerator
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        kgen.init(0x100); // 192 and 256 bits may not be available
        // Generate the secret key specs.
        SecretKey skey = kgen.generateKey();
        byte[] raw = skey.getEncoded();

        SecretKeySpec skeySpec = new SecretKeySpec(keyBytes, "AES");
        IvParameterSpec iv = new IvParameterSpec(ivBytes);//

        System.out.println("Key:" + byte2HexStringWithoutSpace(raw));

        // Instantiate the cipher

        Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

        byte[] encrypted = cipher.doFinal(targetBytes);
        System.out.println("decrypted bytes:" + byte2HexStringWithoutSpace(encrypted));
        System.out.println("decrypted string: " + new String(encrypted, "utf8"));
        // */
    }

    private static void bouncycastle() {
        byte[] keyBytes = hexString2ByteNoSpace(key);
        byte[] ivBytes = hexString2ByteNoSpace(IV);
        String content = result;
        System.out.println("Original content:");
        System.out.println(content);
        try {
            BufferedBlockCipher engine = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESLightEngine()));
            engine.init(true, new ParametersWithIV(new KeyParameter(keyBytes), ivBytes));
            byte[] enc = new byte[engine.getOutputSize(content.getBytes().length)];
            int size1 = engine.processBytes(content.getBytes(), 0, content.getBytes().length, enc, 0);
            int size2 = engine.doFinal(enc, size1);
            System.out.println("size2 =" + size2);
            byte[] encryptedContent = new byte[size1 + size2];
            System.arraycopy(enc, 0, encryptedContent, 0, encryptedContent.length);
            System.out.print("Encrypted Content:");
            System.out.println(byte2HexStringWithoutSpace(encryptedContent));

            //encryptedContent = new BASE64Decoder().decodeBuffer(target);

            engine.init(false, new ParametersWithIV(new KeyParameter(keyBytes), ivBytes));
            byte[] dec = new byte[engine.getOutputSize(encryptedContent.length)];
            size1 = engine.processBytes(encryptedContent, 0, encryptedContent.length, dec, 0);
            size2 = engine.doFinal(dec, size1);
            System.out.println("size2 =" + size2);
            byte[] decryptedContent = new byte[size1 + size2];
            System.arraycopy(dec, 0, decryptedContent, 0, decryptedContent.length);
            System.out.println("Decrypted Content:");
            System.out.println(new String(decryptedContent));

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static byte[] bouncycastleDecrypt(byte[] encrypted, byte[] key, byte[] iv) throws Exception {
        BufferedBlockCipher engine = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESLightEngine()));
        engine.init(false, new ParametersWithIV(new KeyParameter(key), iv));
        byte[] dec = new byte[engine.getOutputSize(encrypted.length)];
        int size1 = engine.processBytes(encrypted, 0, encrypted.length, dec, 0);
        int size2 = engine.doFinal(dec, size1);
        byte[] decryptedContent = new byte[size1 + size2];
        System.arraycopy(dec, 0, decryptedContent, 0, decryptedContent.length);
        return decryptedContent;
    }

    public static byte[] AESDecrypt(byte[] encrypted, byte[] key, byte[] iv) {
        try {
            SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, ivSpec);
            return cipher.doFinal(encrypted);
        } catch (Exception e) {
            throw new IllegalArgumentException("AESDecrypt failed.", e);
        }
    }

    /**
     * 把一个16进制字符串转换为字节数组，字符串没有空格，所以每两个字符
     * 一个字节
     * 
     * @param s
     * @return
     */
    public static byte[] hexString2ByteNoSpace(String s) {
        int len = s.length();
        byte[] ret = new byte[len >>> 1];
        for (int i = 0; i <= len - 2; i += 2) {
            ret[i >>> 1] = (byte) (Integer.parseInt(s.substring(i, i + 2).trim(), 16) & 0xFF);
        }
        return ret;
    }

    /**
     * 把字节数组转换成16进制字符串
     * 
     * @param b
     * 			字节数组
     * @return
     * 			16进制字符串，每个字节没有空格分隔
     */
    public static String byte2HexStringWithoutSpace(byte[] b) {
        if (b == null) {
            return "null";
        }

        return byte2HexStringWithoutSpace(b, 0, b.length);
    }
    // 16进制字符数组
    private static char[] hex = new char[]{
        '0', '1', '2', '3', '4', '5', '6', '7',
        '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
    };

    /**
     * 把字节数组转换成16进制字符串
     * 
     * @param b
     * 			字节数组
     * @param offset
     * 			从哪里开始转换
     * @param len
     * 			转换的长度
     * @return 16进制字符串，每个字节没有空格分隔
     */
    public static String byte2HexStringWithoutSpace(byte[] b, int offset, int len) {
        if (b == null) {
            return "null";
        }

        // 检查索引范围
        int end = offset + len;
        if (end > b.length) {
            end = b.length;
        }

        StringBuffer sb = new StringBuffer();

        for (int i = offset; i < end; i++) {
            sb.append(hex[(b[i] & 0xF0) >>> 4]).append(hex[b[i] & 0xF]);
        }
        return sb.toString();
    }
}
