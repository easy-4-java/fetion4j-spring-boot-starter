/*
 * Copyright (C) 2012, Apexes Network Technology. All rights reserved.
 * 
 *       http://www.apexes.net
 * 
 */

package net.apexes.fetion4j.core.sipc;

/**
 * A Fetion SIPC request message.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
public class RequestMessage extends SipcMessage {
    
    private String acceptor;
    
    public RequestMessage(String method) {
        this(method, "fetion.com.cn");
    }
    
    public RequestMessage(String method, String acceptor) {
        this.acceptor = acceptor; 
        setMethod(method);
    }
    
    /** @return return the acceptor. */
    public String getAcceptor() {
        return acceptor;
    }
    
    @Override
    /** @return return the headline. */
    protected String getHeadline() {
        return getMethod() + " " + getAcceptor() + " " + Sipc.SIPC_VERSION;
    }
}
