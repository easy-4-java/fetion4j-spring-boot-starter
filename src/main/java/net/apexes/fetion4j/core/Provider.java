/*
 * Copyright (C) 2013, Apexes Network Technology. All rights reserved.
 *
 *       http://www.apexes.net
 *
 */
package net.apexes.fetion4j.core;

import net.apexes.fetion4j.core.util.XmlElement;

/**
 * Strategy for reading and persisting Fetion system configuration and user data.
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 1.0.0
 */
public interface Provider {
    
    /**
     * 
     * @return 
     */
    XmlElement readSystemConfig();
    
    /**
     * 
     * @return 
     */
    UserInfo readUserInfo();
    
}
