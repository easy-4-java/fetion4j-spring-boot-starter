/*
 * Copyright (C) 2013, Apexes Network Technology. All rights reserved.
 *
 *       http://www.apexes.net
 *
 */
package net.apexes.fetion4j.core.client;

import net.apexes.fetion4j.core.sipc.SipcMessage;

/**
 * Represents a Fetion conversation (chat) session.
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 1.0.0
 */
public abstract class Dialogue extends Activity {
    
    protected Dialogue(FetionContext context, Controller controller, int callId) {
        super(context, controller, callId);
    }
    
    /**
     * 
     * @param message 
     */
    public abstract void receive(SipcMessage message);
    
}
