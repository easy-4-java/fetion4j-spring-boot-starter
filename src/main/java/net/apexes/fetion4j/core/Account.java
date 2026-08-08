/*
 * Copyright (C) 2012, Apexes Network Technology. All rights reserved.
 * 
 *       http://www.apexes.net
 * 
 */
package net.apexes.fetion4j.core;

import net.apexes.fetion4j.core.client.ClientHelper;
import net.apexes.fetion4j.core.user.Presence;
import net.apexes.fetion4j.core.util.ConvertHelper;
import net.apexes.fetion4j.core.util.DigestHelper;

/**
 * Fetion account credentials and session state (URI, user id, mobile number, AES keys).
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 1.0.0
 */
public class Account {
   
    /**
     * 用户的URI。
     * 格式为：sip:123456789@fetion.com.cn;p=1234 或 tel:13901234567
     */
    private String uri;
    
    /**
     * 飞信号
     */
    private int userId;
    
    private String sid;
    
    /**
     * 手机号码
     */
    private long mobileNo;
    
    private String password;
    
    private Presence presence = Presence.ONLINE;
    
    /**
	 * 用于解密Credential的key
	 */
	private byte[] aesKey;
	
	/**
	 * 用于解密Credential的iv
	 */
	private byte[] aesIV;
    
    Account(long mobileNo, int userId, String uri) {
        this.mobileNo = mobileNo;
        this.userId = userId;
        this.uri = uri;
        sid = ClientHelper.getSidFromUri(uri);
        aesKey = DigestHelper.createAESKey();
		aesIV  = ConvertHelper.hexString2ByteNoSpace("00399F3D125DB5530AB5E000D6B0F45A");	//固定值
    }
    
    public int getUserId() {
        return userId;
    }
    
    public long getMobileNo() {
        return mobileNo;
    }

    public String getUri() {
        return uri;
    }

    public String getSid() {
        return sid;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Presence getPresence() {
        return presence;
    }

    public void setPresence(Presence presence) {
        this.presence = presence;
    }

    public byte[] getAesKey() {
        return aesKey;
    }

    public byte[] getAesIV() {
        return aesIV;
    }
    
    @Override
    public String toString() {
        return "Account{" + "uri=" + uri 
                + ", userId=" + userId 
                + ", sid=" + sid 
                + ", mobileNo=" + mobileNo 
                + ", password=" + password 
                + ", presence=" + presence + '}';
    }
}
