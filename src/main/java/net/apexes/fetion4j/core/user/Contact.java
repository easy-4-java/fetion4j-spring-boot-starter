/*
 * Copyright (C) 2012, Apexes Network Technology. All rights reserved.
 * 
 *       http://www.apexes.net
 * 
 */

package net.apexes.fetion4j.core.user;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Represents a Fetion contact.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
public final class Contact implements java.io.Serializable {
    
    private String version;
    /**
     * 好友分组列表
     */
    private List<BuddyGroup> buddyGroupList;
    /**
     * 好友列表
     */
    private List<Buddy> buddyList;
    /**
     * 黑名单
     */
    private List<User> blacklist;
    
    /**
     * 
     * @param version 
     */
    public Contact(String version) {
        this.version = version;
        buddyGroupList = new ArrayList<BuddyGroup>();
        buddyList = Collections.synchronizedList(new ArrayList<Buddy>());
        blacklist = new ArrayList<User>();
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public void addBuddyGroup(BuddyGroup buddyGroup) {
        buddyGroupList.add(buddyGroup);
    }
    
    public void addBuddy(Buddy buddy) {
        buddyList.add(buddy);
    }
    
    public void addBuddy(Buddy buddy, String version) {
        addBuddy(buddy);
        this.version = version;
    }
    
    public void removeBuddy(Buddy buddy, String version) {
        buddyList.remove(buddy);
        this.version = version;
    }
    
    public void addBlacklist(User user) {
        blacklist.add(user);
    }
    
    public Buddy findBuddy(int userId) {
        for (Buddy buddy : buddyList) {
            if (buddy.getUserId() == userId) {
                return buddy;
            }
        }
        return null;
    }
    
    public Buddy findBuddy(String uri) {
        for (Buddy buddy : buddyList) {
            if (buddy.getUri().equals(uri)) {
                return buddy;
            }
        }
        return null;
    }
    
    public Collection<BuddyGroup> getBuddyGroups() {
        return buddyGroupList;
    }
    
    public Collection<Buddy> getBuddys() {
        return buddyList;
    }
    
    public Collection<User> getBlacklist() {
        return blacklist;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder("通讯录 version=");
        buf.append(version);
        buf.append("\r\n");
        buf.append("[好友列表]");
        buf.append("\r\n");
        for (Buddy buddy : buddyList) {
            buf.append(buddy);
            buf.append("\r\n");
        }
        buf.append("[黑名单]");
        buf.append("\r\n");
        for (User user : blacklist) {
            buf.append(user);
            buf.append("\r\n");
        }
        return buf.toString();
    }

}
