/*
 * Copyright (C) 2013, Apexes Network Technology. All rights reserved.
 *
 *       http://www.apexes.net
 *
 */
package net.apexes.fetion4j.core;

import java.text.ParseException;
import java.util.LinkedHashMap;

import net.apexes.fetion4j.core.client.Controller;
import net.apexes.fetion4j.core.user.Buddy;
import net.apexes.fetion4j.core.user.BuddyGroup;
import net.apexes.fetion4j.core.user.Contact;
import net.apexes.fetion4j.core.user.Personal;
import net.apexes.fetion4j.core.user.Presence;
import net.apexes.fetion4j.core.user.Relation;
import net.apexes.fetion4j.core.user.User;
import net.apexes.fetion4j.core.util.XmlElement;
import net.apexes.fetion4j.core.util.XmlElementHelper;

/**
 * Cached Fetion user information (personal profile, contacts, etc.).
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
public class UserInfo {

    /**
     * 个人信息
     */
    private Personal personal;
    /**
     * 通讯录
     */
    private Contact contact;
    /**
     * 配额情况
     */
    private Quota quota;
    /**
     *
     */
    private LinkedHashMap<String, XmlElement> xmlMap;

    public UserInfo() {
        quota = new Quota();
        xmlMap = new LinkedHashMap<String, XmlElement>();
    }

    public Personal getPersonal() {
        return personal;
    }

    public void setPersonal(Personal personal) {
        this.personal = personal;
    }

    public Contact getContact() {
        return contact;
    }

    public void setContact(Contact contact) {
        this.contact = contact;
    }

    public Quota getQuota() {
        return quota;
    }

    public void setQuota(Quota quota) {
        this.quota = quota;
    }

    public String getPersonalVersion() {
        return personal == null ? "0" : personal.getVersion();
    }

    public String getContactVersion() {
        return contact == null ? "0" : contact.getVersion();
    }

    /**
     * 
     * @param controller
     * @param xml
     * @throws ParseException
     * @throws Exception 
     */
    public void updateOnLogined(Controller controller, XmlElement xml) 
            throws ParseException, Exception {
        for (XmlElement el : xml.getChildren()) {
            String elName = el.getName();
            if ("personal".equals(elName)) {
                String version = el.getStringAttribute("version");
                if (!getPersonalVersion().equals(version)) {
                    personal = toPersonal(el);
                }
            } else if ("contact-list".equals(elName)) {
                String version = el.getStringAttribute("version");
                if (!getContactVersion().equals(version)) {
                    contact = toContact(el);
                }
            } else if ("quotas".equals(elName)) {
                quota.update(controller, el);
            } else {
                xmlMap.put(elName, el);
            }
        }
    }

    /**
     * 
     * @param controller
     * @param xml
     * @throws Exception 
     */
    public void updateOnPresenceChanged(Controller controller, XmlElement xml) throws Exception {
        // <events><event type="PresenceChanged"><contacts>
        // <c id="123456789"><p v="0" sid="987654321" 
        // su="sip:987654321@fetion.com.cn;p=6087" 
        // m="13800138000" c="CMCC" cs="0" s="1" l="0" svc="99" 
        // n="13800138000" i="" p="0" sms="0.0:0:0" sp="0" sh="0"/>
        // <pr di="" b="0" d="" dt="" dc="0"></pr>
        // </c></contacts></event></events>
        xml = XmlElementHelper.getNode(xml, "/events/event/contacts");
        for (XmlElement el : xml.getChildren("c")) {
            int userId = el.getIntAttribute("id");
            Personal user = contact.findBuddy(userId);
            if (user == null) {
                if (userId == personal.getUserId()) {
                    user = personal;
                } else {
                    break;
                }
            }
            XmlElement pEl = el.getChild("p");
            if (pEl != null) {
                if (user instanceof Buddy) {
                    user.setVersion(pEl.getStringAttribute("v", user.getVersion()));
                }
                user.setSid(pEl.getStringAttribute("sid", user.getSid()));
                user.setUri(pEl.getStringAttribute("su", user.getUri()));
                // 不直接取Long型属性，而是先转成字符串，防止 m="" 这种情况引起异常
                String m = pEl.getStringAttribute("m");
                if (m != null && !m.isEmpty()) {
                    user.setMobileNo(Long.valueOf(m));
                }
                user.setCarrier(pEl.getStringAttribute("c", user.getCarrier()));
                user.setCarrierStatus(pEl.getStringAttribute("cs", user.getCarrierStatus()));
                user.setNickname(pEl.getStringAttribute("n", user.getNickname()));
                user.setImpresa(pEl.getStringAttribute("i", user.getImpresa()));
                user.setSmsOnlineStatus(pEl.getStringAttribute("sms", user.getSmsOnlineStatus()));
            }
            XmlElement prEl = el.getChild("pr");
            if (prEl != null) {
                user.setPresence(Presence.valueOf(prEl.getIntAttribute("b")));
            }
            controller.fireChangedUser(user);
        }
    }
    
    public void updateOnSyncUserInfoChanged(Controller controller, XmlElement xml) throws Exception {
        // 删除好友:
        //<events><event type="SyncUserInfo"><user-info>
        //<contact-list version="407799031">
        //<buddies><buddy action="remove" user-id="123456789" /></buddies>
        //</contact-list></user-info></event></events>
        // 添加好友：
        //<events><event type="SyncUserInfo"><user-info>
        //<contact-list version="407799087"><buddies>
        //<buddy action="remove" user-id="123456789"/>
        //<buddy action="add" user-id="123456789" uri="sip:987654321@fetion.com.cn;p=277"
        // relation-status="1" local-name="备注名称" buddy-lists="" online-notify="0" 
        // permission-values="identity=1;" />
        //<buddy action="update" user-id="123456789" relation-status="1"/>
        //</buddies></contact-list></user-info></event></events>
        xml = XmlElementHelper.getNode(xml, "/events/event/user-info/contact-list");
        if (xml != null) {
            String contactVersion = xml.getStringAttribute("version");
            xml = xml.getChild("buddies");
            for (XmlElement el : xml.getChildren("buddy")) {
                String action = el.getStringAttribute("action");
                if ("update".equals(action)) {
                    int userId = el.getIntAttribute("user-id");
                    Buddy buddy = contact.findBuddy(userId);
                    int r = el.getIntAttribute("relation-status");
                    Relation relation = Relation.valueOf(r);
                    buddy.setRelation(relation);
                    controller.fireChangedBuddy(buddy, contactVersion);
                }
            }
        }
    }
    
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("UserInfo\r\n");
        buf.append("个人信息 version=");
        buf.append(getPersonalVersion());
        buf.append("\r\n");
        buf.append(personal);
        buf.append(contact);
        buf.append("\r\n");
        buf.append(quota);
        return buf.toString();
    }
    
    /**
     * 
     * @param xml
     * @return
     * @throws ParseException 
     */
    private static Personal toPersonal(XmlElement xml) throws ParseException {
        Personal personal = new Personal();
        String version = xml.getStringAttribute("version");
        int userId = xml.getIntAttribute("user-id");
        String uri = xml.getStringAttribute("uri");
        String name = xml.getStringAttribute("name");
        if (version == null || userId == 0 || uri == null) {
            throw new ParseException(xml.toString(), 0);
        }
        personal.setUserId(userId);
        personal.setUri(uri);
        personal.setName(name);
        personal.setVersion(version);
        personal.setSid(xml.getStringAttribute("sid", personal.getSid()));
        String m = xml.getStringAttribute("mobile-no");
        if (m != null && !m.isEmpty()) {
            personal.setMobileNo(Long.valueOf(m));
        }
        personal.setNickname(xml.getStringAttribute("nickname", personal.getNickname()));
        personal.setImpresa(xml.getStringAttribute("impresa", personal.getImpresa()));
        return personal;
    }

    /**
     * 
     * @param xml
     * @return
     * @throws ParseException 
     */
    private static Contact toContact(XmlElement xml) throws ParseException {
        String version = xml.getStringAttribute("version");
        if (version == null) {
            throw new ParseException(xml.toString(), 0);
        }
        Contact contact = new Contact(version);
        for (XmlElement el : xml.getChildren()) {
            String elName = el.getName();
            if ("buddy-lists".equals(elName)) {
                // <buddy-list id="1" name="我的好友"/>
                for (XmlElement el0 : el.getChildren("buddy-list")) {
                    int id = el0.getIntAttribute("id");
                    String name = el0.getStringAttribute("name");
                    contact.addBuddyGroup(new BuddyGroup(id, name));
                }
            } else if ("buddies".equals(elName)) {
                // <b i="123456789" u="sip:987654321@fetion.com.cn;p=1335" n="名称" l="2" f="0" r="1" o="0" p="identity=1;"/>
                for (XmlElement el0 : el.getChildren("b")) {
                    int userId = el0.getIntAttribute("i");
                    String uri = el0.getStringAttribute("u");
                    String name = el0.getStringAttribute("n");
                    int r = el0.getIntAttribute("r");
                    Relation relation = Relation.valueOf(r);
                    Buddy buddy = new Buddy(userId, uri, name, relation);
                    contact.addBuddy(buddy);
                }
            } else if ("blacklist".equals(elName)) {
                // <k i="123456789" u="sip:987654321@fetion.com.cn;p=3423" n=""/>
                for (XmlElement el0 : el.getChildren("k")) {
                    int userId = el0.getIntAttribute("i");
                    String uri = el0.getStringAttribute("u");
                    String name = el0.getStringAttribute("n");
                    contact.addBlacklist(new User(userId, uri, name));
                }
            }
        }
        return contact;
    }
}
