/*
 * Copyright (C) 2012, Apexes Network Technology. All rights reserved.
 * 
 *       http://www.apexes.net
 * 
 */
package net.apexes.fetion4j.core.user;

/**
 * Base type for Fetion user entities.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
public class User implements java.io.Serializable {
    
    /**
     * 飞信号
     */
    private int userId;
    /**
     * 用户的URI。
     * 格式为：sip:123456789@fetion.com.cn;p=1234 或 tel:13901234567
     */
    private String uri;
    /**
     * 名称
     */
    private String name;
    
    public User() {
    }
    
    public User(int userId) {
        this.userId = userId;
    }
    
    /**
     * 
     * @param userId
     * @param uri 
     * @param name
     */
    public User(int userId, String uri, String name) {
        this.uri = uri;
        this.userId = userId;
        this.name = name;
    }
    
    /** @return return the user id. */
    public int getUserId() {
        return userId;
    }
    
    /** @param userId set the user id. */
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    /** @return return the uri. */
    public String getUri() {
        return uri;
    }
    
    /** @param uri set the uri. */
    public void setUri(String uri) {
        this.uri = uri;
    }

    /** @return return the name. */
    public String getName() {
        return name;
    }

    /** @param name set the name. */
    public void setName(String name) {
        this.name = name;
    }
    
    @Override
    /**
     * <p>Hash code.</p>
     * @return the result
     */
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + this.userId;
        return hash;
    }

    @Override
    /**
     * <p>Equals.</p>
     * @param obj
     * @return the result
     */
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final User other = (User) obj;
        if (this.userId != other.userId) {
            return false;
        }
        return true;
    }

    @Override
    /**
     * <p>To string.</p>
     * @return the result
     */
    public String toString() {
        return "User{" + "userId=" + userId + ", uri=" + uri + ", name=" + name + '}';
    }
    
}
