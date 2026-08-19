/*
 * Copyright (C) 2013, Apexes Network Technology. All rights reserved.
 *
 *       http://www.apexes.net
 *
 */
package net.apexes.fetion4j.core.client;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import net.apexes.fetion4j.core.Account;
import net.apexes.fetion4j.core.FetionConsole;
import net.apexes.fetion4j.core.NotifyListener;
import net.apexes.fetion4j.core.Provider;
import net.apexes.fetion4j.core.UserInfo;
import net.apexes.fetion4j.core.user.Buddy;
import net.apexes.fetion4j.core.user.BuddyGroup;
import net.apexes.fetion4j.core.user.Contact;
import net.apexes.fetion4j.core.user.Personal;
import net.apexes.fetion4j.core.user.User;
import net.apexes.fetion4j.core.util.XmlElement;
import net.apexes.fetion4j.core.util.XmlElementHelper;

/**
 * Simple in-memory {@link Provider} implementation.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
public class SimpleProvider implements Provider, NotifyListener {
    
    private FetionContext context;
    private File systemConfigFile;
    private File userInfoFile;

    SimpleProvider(FetionContext context, long mobileNo) {
        this.context = context;
        File dir = new File(".users", Long.toString(mobileNo));
        if (!dir.exists()) {
            dir.mkdirs();
        }
        systemConfigFile = new File(dir, "systemconfig.xml");
        userInfoFile = new File(dir, "userInfo.xml");
    }

    @Override
    /**
     * <p>Read system config.</p>
     * @return the result
     */
    public XmlElement readSystemConfig() {
        XmlElement xml = null;
        try {
            xml = XmlElementHelper.open(systemConfigFile, "UTF-8");
        } catch (Exception ex) {
        }
        return xml;
    }

    @Override
    /**
     * <p>Read user info.</p>
     * @return the result
     */
    public UserInfo readUserInfo() {
        UserInfo userInfo = null;
        try {
            XmlElement xml = XmlElementHelper.open(userInfoFile, "UTF-8");
            userInfo = new UserInfo();
            // 个人信息
            XmlElement personalEl = xml.getChild("personal");
            Personal personal = (Personal) UserHelper.toUser(personalEl);
            userInfo.setPersonal(personal);
            // 通讯录
            XmlElement contactEl = xml.getChild("contact-list");
            String version = contactEl.getStringAttribute("version");
            Contact contact = new Contact(version);
            userInfo.setContact(contact);
            // 好友分组
            XmlElement buddyGroupsEl = contactEl.getChild("buddy-groups");
            if (buddyGroupsEl != null) {
                for (XmlElement el : buddyGroupsEl.getChildren()) {
                    int id = el.getIntAttribute("id");
                    String name = el.getStringAttribute("name");
                    BuddyGroup buddyGroup = new BuddyGroup(id, name);
                    contact.addBuddyGroup(buddyGroup);
                }
            }
            // 好友列表
            XmlElement buddysEl = contactEl.getChild("buddys");
            if (buddysEl != null) {
                for (XmlElement el : buddysEl.getChildren()) {
                    Buddy buddy = (Buddy) UserHelper.toUser(el);
                    contact.addBuddy(buddy);
                }
            }
            // 黑名单
            XmlElement blacklistEl = contactEl.getChild("blacklist");
            if (blacklistEl != null) {
                for (XmlElement el : blacklistEl.getChildren()) {
                    User user = (User) UserHelper.toUser(el);
                    contact.addBlacklist(user);
                }
            }
        } catch (Exception ex) {
        }
        return userInfo;
    }
    
    /**
     * <p>Write system config.</p>
     * @param xml
     */
    public void writeSystemConfig(XmlElement xml) throws IOException {
        XmlElementHelper.write(xml, systemConfigFile, "UTF-8");
    }
    
    /**
     * <p>Write user info.</p>
     * @param userInfo
     */
    public void writeUserInfo(UserInfo userInfo) throws IOException {
        XmlElement xml = new XmlElement();
        xml.setName("user-info");
        // 个人信息
        Personal personal = userInfo.getPersonal();
        if (personal != null) {
            XmlElement personalEl = UserHelper.toXml(personal);
            xml.addChild(personalEl);
        }
        // 通讯录
        Contact contact = userInfo.getContact();
        if (contact != null) {
            XmlElement contactEl = new XmlElement();
            contactEl.setName("contact-list");
            contactEl.setAttribute("version", contact.getVersion());
            xml.addChild(contactEl);
            // 好友分组
            Collection<BuddyGroup> buddyGroups = contact.getBuddyGroups();
            if (buddyGroups != null) {
                XmlElement buddyGroupsEl = new XmlElement();
                buddyGroupsEl.setName("buddy-groups");
                contactEl.addChild(buddyGroupsEl);
                for (BuddyGroup buddyGroup : buddyGroups) {
                    XmlElement el = new XmlElement();
                    el.setName("buddy-group");
                    el.setIntAttribute("id", buddyGroup.getId());
                    el.setAttribute("name", buddyGroup.getName());
                    buddyGroupsEl.addChild(el);
                }
            }
            // 好友列表
            Collection<Buddy> buddys = contact.getBuddys();
            if (buddyGroups != null) {
                XmlElement buddysEl = new XmlElement();
                buddysEl.setName("buddys");
                contactEl.addChild(buddysEl);
                for (Buddy buddy : buddys) {
                    XmlElement el = UserHelper.toXml(buddy);
                    buddysEl.addChild(el);
                }
            }
            // 黑名单
            Collection<User> blacklist = contact.getBlacklist();
            if (blacklist != null) {
                XmlElement blacklistEl = new XmlElement();
                blacklistEl.setName("blacklist");
                contactEl.addChild(blacklistEl);
                for (User user : blacklist) {
                    XmlElement el = UserHelper.toXml(user);
                    blacklistEl.addChild(el);
                }
            }
        }
        XmlElementHelper.write(xml, userInfoFile, "UTF-8");
    }

    @Override
    /**
     * <p>Transfe error.</p>
     * @param message
     * @param exception
     */
    public void transfeError(String message, Exception exception) {
        context.getLogHandler().error(SimpleProvider.class, message, exception);
    }
    
    @Override
    /**
     * <p>Changed system config.</p>
     * @param systemConfig
     */
    public void changedSystemConfig(XmlElement systemConfig) {
        try {
            writeSystemConfig(systemConfig);
        } catch (Exception ex) {
            context.getLogHandler().error(SimpleProvider.class, "保存SystemConfig出错！", ex);
        }
    }

    @Override
    /**
     * <p>Created account.</p>
     * @param account
     */
    public void createdAccount(Account account) {
    }

    @Override
    /**
     * <p>Login successed.</p>
     * @param console
     * @param userInfo
     */
    public void loginSuccessed(FetionConsole console, UserInfo userInfo) {
        try {
            writeUserInfo(userInfo);
        } catch (Exception ex) {
            context.getLogHandler().error(SimpleProvider.class, "保存UserInfo出错！", ex);
        }
    }

    @Override
    /**
     * <p>Logout successed.</p>
     */
    public void logoutSuccessed() {
        try {
            writeUserInfo(context.getUserInfo());
        } catch (Exception ex) {
            context.getLogHandler().error(SimpleProvider.class, "保存UserInfo出错！", ex);
        }
    }

    @Override
    /**
     * <p>Changed user.</p>
     * @param user
     */
    public void changedUser(User user) {
        context.getLogHandler().debug(SimpleProvider.class, "更新联系人：" + user);
    }

    @Override
    /**
     * <p>Changed buddy.</p>
     * @param buddy
     * @param contactVersion
     */
    public void changedBuddy(Buddy buddy, String contactVersion) {
        context.getLogHandler().debug(SimpleProvider.class, "[" + contactVersion + "]更新好友：" + buddy);
    }

    @Override
    /**
     * <p>Added buddy.</p>
     * @param buddy
     * @param contactVersion
     */
    public void addedBuddy(Buddy buddy, String contactVersion) {
        context.getLogHandler().debug(SimpleProvider.class, "[" + contactVersion + "]添加好友：" + buddy);
    }

    @Override
    /**
     * <p>Deleted buddy.</p>
     * @param buddy
     * @param contactVersion
     */
    public void deletedBuddy(Buddy buddy, String contactVersion) {
        context.getLogHandler().debug(SimpleProvider.class, "[" + contactVersion + "]删除好友：" + buddy);
    }

    @Override
    /**
     * <p>Sms count changed.</p>
     * @param dayCount
     * @param monthCount
     */
    public void smsCountChanged(int dayCount, int monthCount) {
        context.getLogHandler().debug(SimpleProvider.class, "dayCount=" + dayCount + ", monthCount=" + monthCount);
    }
}
