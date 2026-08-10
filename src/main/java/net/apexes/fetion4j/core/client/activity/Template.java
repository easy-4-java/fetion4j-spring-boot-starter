/*
 * Copyright (C) 2012, Apexes Network Technology. All rights reserved.
 * 
 *       http://www.apexes.net
 * 
 */
package net.apexes.fetion4j.core.client.activity;

/**
 * SIPC protocol message templates used by the Fetion client.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
public interface Template {
    
    /**
     * 获取系统配置更新信息的模板
     */
    String TMPL_SYSTEM_CONFIG = "<config>"
            + "<user mobile-no=\"{mobile-no}\" />"
            + "<client type=\"PC\" version=\"4.0.0\" platform=\"W6.0\" />"
            + "<servers version=\"{servers-version}\" />"
            + "<service-no version=\"{service-no-version}\" />"
            + "<parameters version=\"{parameters-version}\" />"
            + "<hints version=\"{hints-version}\" />"
            + "<http-applications version=\"{http-applications-version}\" />"
            + "<client-config version=\"{client-config-version}\" />"
            + "<services version=\"{services-version}\" />"
            + "</config>";
    
    /**
     * 获取系统配置完全信息的模板
     */
    String TMPL_SYSTEM_CONFIG_INIT = "<config>"
            + "<user mobile-no=\"{mobile-no}\" />"
            + "<client type=\"PC\" version=\"4.0.0\" platform=\"W6.0\" />"
            + "<servers version=\"0\" />"
            + "<service-no version=\"0\" />"
            + "<parameters version=\"0\" />"
            + "<hints version=\"0\" />"
            + "<http-applications version=\"0\" />"
            + "<client-config version=\"0\" />"
            + "<services version=\"0\" />"
            + "</config>";
    
    String TMPT_USER_AUTH = "<args>"
            + "<device accept-language=\"zh-CN\" machine-code=\"{machine-code}\"/>"
            + "<caps value=\"5FF\" />"
            + "<events value=\"7F\" />"
            + "<user-info mobile-no=\"{mobile-no}\" user-id=\"{user-id}\">"
            + "<personal version=\"{personal-version}\" attributes=\"v4default\" />"
            + "<custom-config version=\"0\" />"
            + "<contact-list version=\"{contact-list-version}\" buddy-attributes=\"v4default\" />"
            + "</user-info>"
            //+ "<credentials domains=\"fetion.com.cn;m161.com.cn;www.ikuwa.cn;games.fetion.com.cn\" />"
            + "<presence><basic value=\"{presence-value}\" desc=\"\" /></presence>"
            + "</args>";
    
    /**
     * 添加好友
     */
    String TMPL_ADD_BUDDY = "<args><contacts><buddies>"
            + "<buddy uri=\"{uri}\" local-name=\"{local-name}\""
            + " buddy-lists=\"{buddy-lists}\" desc=\"{desc}\" expose-mobile-no=\"1\""
            + " expose-name=\"1\" addbuddy-phrase-id=\"{addbuddy-phrase-id}\" />"
            + "</buddies></contacts></args>";
    
    /**
     * 删除好友
     */
    String TMPL_DELETE_BUDDY = "<args><contacts><buddies>"
            + "<buddy user-id=\"{user-id}\" delete-both=\"{delete-both}\" />"
            + "</buddies></contacts></args>";
}
