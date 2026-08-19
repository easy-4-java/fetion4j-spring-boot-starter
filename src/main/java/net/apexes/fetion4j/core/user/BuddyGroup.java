/*
 * Copyright (C) 2012, Apexes Network Technology. All rights reserved.
 * 
 *       http://www.apexes.net
 * 
 */
package net.apexes.fetion4j.core.user;

/**
 * Represents a Fetion buddy group (contact list folder).
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
public class BuddyGroup implements java.io.Serializable {
    
    private int id;
    
    private String name;
    
    public BuddyGroup(int id, String name) {
        this.id = id;
        this.name = name;
    }

    /** @return return the id. */
    public int getId() {
        return id;
    }

    /** @return return the name. */
    public String getName() {
        return name;
    }

    @Override
    /**
     * <p>To string.</p>
     * @return the result
     */
    public String toString() {
        return name;
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
        final BuddyGroup other = (BuddyGroup) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }

    @Override
    /**
     * <p>Hash code.</p>
     * @return the result
     */
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + this.id;
        return hash;
    }
    
}
