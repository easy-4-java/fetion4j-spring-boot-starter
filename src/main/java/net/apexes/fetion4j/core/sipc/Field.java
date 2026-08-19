/*
 * Copyright (C) 2012, Apexes Network Technology. All rights reserved.
 * 
 *       http://www.apexes.net
 * 
 */

package net.apexes.fetion4j.core.sipc;

/**
 * Represents a header field of a Fetion SIPC message.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
class Field {
    
    /**
     * 头域名
     */
    private String name;
    /**
     * 头域值
     */
    private String value;
    
    public Field(String name, String value) {
        setName(name);
        setValue(value);
    }

    /** @return return the name. */
    public String getName() {
        return name;
    }

    /** @param name set the name. */
    public void setName(String name) {
        this.name = name;
    }

    /** @return return the value. */
    public String getValue() {
        return value;
    }

    /** @param value set the value. */
    public void setValue(String value) {
        this.value = value;
    }

    /** @return return the text. */
    public String getText() {
        return getName() + ": " + getValue();
    }

    @Override
    /**
     * <p>Hash code.</p>
     * @return the result
     */
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + (this.name != null ? this.name.hashCode() : 0);
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
        final Field other = (Field) obj;
        if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
            return false;
        }
        return true;
    }
    
}
