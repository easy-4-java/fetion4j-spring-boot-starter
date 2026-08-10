/*
 * Copyright (C) 2012, Apexes Network Technology. All rights reserved.
 * 
 *       http://www.apexes.net
 * 
 */
package net.apexes.fetion4j.core.client;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import net.apexes.fetion4j.core.FetionException;
import net.apexes.fetion4j.core.sipc.ResponseMessage;
import net.apexes.fetion4j.core.sipc.Sipc;
import net.apexes.fetion4j.core.sipc.SipcMessage;
import net.apexes.fetion4j.core.util.Base64;
import net.apexes.fetion4j.core.util.XmlElement;

/**
 * Static helper methods used across the Fetion client implementation.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
public final class ClientHelper {
    
    /**
     * 生成SIPC消息的key，生成规则为：callId_sequence_mothod
     *
     * @param message
     * @return
     */
    public synchronized static String createSipcMessageKey(SipcMessage message) {
        return message.getCallId() + "_" + message.getSequence() + "_" + message.getMethod();
    }

    /**
     * 判断是否是群 uri
     *
     * @return
     */
    public static boolean isGroupUri(String uri) {
        return uri != null && uri.startsWith("sip:PG");
    }

    /**
     * 判断是否是手机号组成的 uri
     *
     * @param uri
     * @return
     */
    public static boolean isMobileUri(String uri) {
        return uri != null && uri.startsWith("tel:");
    }
    
    /**
     * 
     * @param account
     * @return 
     */
    public static boolean isSipUri(String account) {
        if (account == null || account.isEmpty()) {
            return false;
        }
        return account.matches("sip:\\w+@fetion.com.cn;p=[1-9][0-9]*");
    }
    
    /**
     * 
     * @param uri
     * @return 
     */
    public static String getSidFromUri(String uri) {
        if (isSipUri(uri)) {
            int i1 = uri.indexOf(':') + 1;
            int i2 = uri.indexOf('@');
            return uri.substring(i1, i2);
        }
        return null;
    }

    /**
     *
     * @param response
     * @return
     */
    public static CaptchaImpl getCaptcha(FetionContext config, ResponseMessage response)
            throws IOException {
        // W: Verify algorithm="picc-ChangeMachine",type="GeneralPic"
        String w = response.getFieldValue(Sipc.FIELD_W);
        HashMap<String, String> map = new HashMap<String, String>();
        String[] strArr = w.replace("Verify ", "").split("=|,");
        for (int i = 0; i < strArr.length; i += 2) {
            map.put(strArr[i], strArr[i + 1].replace("\"", ""));
        }
        String algorithm = map.get("algorithm");
        String type = map.get("type");

        // <results><reason text="" tips="" text2=""/></results>
        String body = response.getBody();
        XmlElement bodyEl = new XmlElement();
        bodyEl.parseString(body);
        XmlElement reasonEl = bodyEl.getChild("reason");
        String text = reasonEl.getStringAttribute("text");
        String tips = reasonEl.getStringAttribute("tips");
        return getCaptcha(config, algorithm, type, text, tips);
    }

    /**
     *
     * @param config
     * @param algorithm
     * @param type
     * @param text
     * @param tips
     * @return
     * @throws FetionException
     */
    public static CaptchaImpl getCaptcha(
            FetionContext context,
            String algorithm,
            String type,
            String text,
            String tips) throws IOException {
        String picurl = context.getSystemConfig().getValue("/config/servers/get-pic-code");
        picurl += "?algorithm=" + algorithm;
        URL url = new URL(picurl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.addRequestProperty("User-Agent", "IIC2.0/PC 4.0.0");
        if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
            // <results><pic-certificate id="7a6" pic="/9j/4AAQSkZJRgABAQEAYABgAAD" /></results>
            XmlElement xmlEl = new XmlElement();
            InputStreamReader reader = new InputStreamReader(conn.getInputStream());
            xmlEl.parseFromReader(reader);
            XmlElement pic = xmlEl.getChild("pic-certificate");
            String pid = pic.getStringAttribute("id");
            String code = pic.getStringAttribute("pic");
            byte[] base64Data = code.getBytes();
            byte[] bytes = Base64.decodeBase64(base64Data);
            return new CaptchaImpl(algorithm, type, text, tips, pid, bytes);
        } else {
            throw new IOException("Http response is not OK. code=" + conn.getResponseCode());
        }
    }
    
}
