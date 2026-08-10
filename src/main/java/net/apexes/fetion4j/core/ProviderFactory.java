/*
 * Copyright (C) 2013, Apexes Network Technology. All rights reserved.
 *
 *       http://www.apexes.net
 *
 */
package net.apexes.fetion4j.core;

/**
 * Factory contract for creating {@link Provider} instances.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
public interface ProviderFactory {
    
    /**
     * 
     * @param mobileNo
     * @return 
     */
    Provider create(long mobileNo);
    
}
