/*
 * Copyright (C) 2013, Apexes Network Technology. All rights reserved.
 *
 *       http://www.apexes.net
 *
 */
package net.apexes.fetion4j.core.client;

import net.apexes.fetion4j.core.Provider;
import net.apexes.fetion4j.core.ProviderFactory;

/**
 * Factory producing {@link SimpleProvider} instances.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
public class SimpleProviderFactory implements ProviderFactory {
    
    private FetionContext context;
    
    public SimpleProviderFactory(FetionContext context) {
        this.context = context;
    }
    
    @Override
    public Provider create(long mobileNo) {
        return new SimpleProvider(context, mobileNo);
    }
}
