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
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
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

    /** @return return the relation. */
    public Relation getRelation() {
        return relation;
    }

    /** @param relation set the relation. */
    public void setRelation(Relation relation) {
        this.relation = relation;
    }
    
    @Override
    /**
     * <p>To string.</p>
     * @return the result
     */
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
