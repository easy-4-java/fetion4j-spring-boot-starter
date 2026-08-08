/*
 * Copyright (C) 2012, Apexes Network Technology. All rights reserved.
 * 
 *       http://www.apexes.net
 * 
 */

package net.apexes.fetion4j.core.user;

/**
 * Personal profile information of the logged in Fetion user.
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 1.0.0
 */
public class Personal extends User {
    
    private String version;
    /**
     * 
     */
    private String sid;
    /**
     * 手机号码
     */
    private long mobileNo;
    /**
     * 昵称
     */
    private String nickname;
    /**
     * 个性签名
     */
    private String impresa;
    /**
     * 网关，c="CMCC"为中国移动，c=""表示不是手机注册的，这时cs的值会为1
     */
    private String carrier;
    /**
     * cs，0-正常，1-停机，2-关闭飞信服务
     */
    private String carrierStatus;
    /**
     * sms，从现在算起，多少时间内不用手机短信接收飞信信息。
     * 如"365.0:0:0" 表示手机不接收飞信消息，等下次客户端登录时接收
     */
    private String smsOnlineStatus;
    /**
     * 在线状态
     */
    private Presence presence;
    
    public Personal() {
    }
    
    public Personal(int userId, String uri, String name) {
        super(userId, uri, name);
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
    
    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public long getMobileNo() {
        return mobileNo;
    }

    public void setMobileNo(long mobileNo) {
        this.mobileNo = mobileNo;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getImpresa() {
        return impresa;
    }

    public void setImpresa(String impresa) {
        this.impresa = impresa;
    }

    public String getCarrier() {
        return carrier;
    }

    public void setCarrier(String carrier) {
        this.carrier = carrier;
    }

    public String getCarrierStatus() {
        return carrierStatus;
    }

    public void setCarrierStatus(String carrierStatus) {
        this.carrierStatus = carrierStatus;
    }

    public String getSmsOnlineStatus() {
        return smsOnlineStatus;
    }

    public void setSmsOnlineStatus(String smsOnlineStatus) {
        this.smsOnlineStatus = smsOnlineStatus;
    }

    public Presence getPresence() {
        return presence;
    }

    public void setPresence(Presence presence) {
        this.presence = presence;
    }
    
    /**
     * 是否能向该好友发送SMS信息。
     * 
     * @return 
     */
    public boolean supportSMS() {
        return "CMCC".equals(carrier) && "0".equals(carrierStatus) && "0.0:0:0".equals(smsOnlineStatus);
    }
    
    /**
     * 显示的名称。
     * 
     * @return 
     */
    public String getDisplayName() {
        if (super.getName() != null && !super.getName().isEmpty()) {
            return super.getName();
        }
        if (nickname != null) {
            return nickname;
        }
        return String.valueOf(getUserId());
    }

    @Override
    public String toString() {
        return "Personal{" 
                + "version=" + version 
                + ", uri=" + getUri() 
                + ", userId=" + getUserId() 
                + ", name=" + getName()
                + ", sid=" + sid 
                + ", mobileNo=" + mobileNo 
                + ", nickname=" + nickname 
                + ", impresa=" + impresa 
                + ", carrier=" + carrier 
                + ", carrierStatus=" + carrierStatus 
                + ", smsOnlineStatus=" + smsOnlineStatus 
                + ", presence=" + presence 
                + '}';
    }
    
}
