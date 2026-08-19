/*
 * Copyright (C) 2012, Apexes Network Technology. All rights reserved.
 * 
 *       http://www.apexes.net
 * 
 */
package net.apexes.fetion4j.core.client.auth;

import java.util.HashMap;

import net.apexes.fetion4j.core.Account;
import net.apexes.fetion4j.core.util.ConvertHelper;

/**
 * Holds the Fetion v4 authentication digest values.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
public class AuthDigest {

    private String algorithm;
    private String nonce;
    private String key;
    private String signature;
    private String response;

    private AuthDigest(String algorithm, String nonce, String key, String signature) {
        this.algorithm = algorithm;
        this.nonce = nonce;
        this.key = key;
        this.signature = signature;
    }

    /** @return return the algorithm. */
    public String getAlgorithm() {
        return algorithm;
    }

    /** @return return the key. */
    public String getKey() {
        return key;
    }

    /** @return return the nonce. */
    public String getNonce() {
        return nonce;
    }

    /** @return return the signature. */
    public String getSignature() {
        return signature;
    }

    /** @return return the response. */
    public String getResponse() {
        return response;
    }

    /**
     * <p>Generate response.</p>
     * @param account
     */
    public void generateResponse(Account account) {
        AuthGeneratorV4 auth = new AuthGeneratorV4();
        String passHex = PasswordEncrypterV4.encryptV4(account.getUserId(), account.getPassword());
        String aeskey = ConvertHelper.byte2HexStringWithoutSpace(account.getAesKey());
        this.response = auth.generate(key, passHex, nonce, aeskey);
    }

    @Override
    /**
     * <p>To string.</p>
     * @return the result
     */
    public String toString() {
        if (response == null) {
            return "Digest "
                    + String.format("algorithm=\"%s\",", algorithm)
                    + String.format("nonce=\"%s\",", nonce)
                    + String.format("key=\"%s\",", key)
                    + String.format("signature=\"%s\"", signature);
        }
        return "Digest "
                + String.format("algorithm=\"%s\",", algorithm)
                + String.format("response=\"%s\"", response);
    }

    /**
     * 将字符串解析为一个 AuthDigest 对象。
     *
     * 字符串的格式应当如下：
     * <pre>
     * Digest algorithm="SHA1-sess-v4",nonce="1D3C",key="C3C7",signature="84E8"
     * </pre>
     *
     * @param digest
     * @return
     */
    public static AuthDigest parse(String digest) {
        HashMap<String, String> map = new HashMap<String, String>();
        String[] strArr = digest.replace("Digest ", "").split("=|,");
        for (int i = 0; i < strArr.length; i += 2) {
            map.put(strArr[i], strArr[i + 1].replace("\"", ""));
        }
        String algorithm = map.get("algorithm");
        String nonce = map.get("nonce");
        String key = map.get("key");
        String signature = map.get("signature");
        return new AuthDigest(algorithm, nonce, key, signature);
    }
    
}
