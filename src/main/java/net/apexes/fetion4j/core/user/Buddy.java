/*
 * Copyright (C) 2012, Apexes Network Technology. All rights reserved.
 * 
 *       http://www.apexes.net
 * 
 */
package net.apexes.fetion4j.core.user;

/**
 * Represents a Fetion buddy entry.
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 1.0.0
 */
public class Buddy extends Personal {
    
    /**
     * 关系
     */
    private Relation relation;
    
    public Buddy() {
    }
    
    public Buddy(int userId, String uri, String name, Relation relation) {
        super(userId, uri, name);
        this.relation = relation;
    }

    public Relation getRelation() {
        return relation;
    }

    public void setRelation(Relation relation) {
        this.relation = relation;
    }
    
    @Override
    public String toString() {
        return "Buddy{" 
                + "version=" + getVersion()
                + ", uri=" + getUri() 
                + ", userId=" + getUserId() 
                + ", name=" + getName()
                + ", sid=" + getSid()
                + ", mobileNo=" + getMobileNo()
                + ", nickname=" + getNickname()
                + ", impresa=" + getImpresa()
                + ", carrier=" + getCarrier()
                + ", carrierStatus=" + getCarrierStatus()
                + ", smsOnlineStatus=" + getSmsOnlineStatus()
                + ", presence=" + getPresence()
                + ", relation=" + relation
                + '}';
    }
    
}
