/*
 * Copyright (C) 2012, Apexes Network Technology. All rights reserved.
 * 
 *       http://www.apexes.net
 * 
 */
package net.apexes.fetion4j.core;

/**
 * Root exception for all errors raised by the Fetion4j client.
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 1.0.0
 */
public class FetionException extends Exception {
    private static final long serialVersionUID = 1L;
    
    public FetionException(String msg, Throwable e) {
        super(msg, e);
    }

    public FetionException(Throwable e) {
        super(e);
    }

    public FetionException(String msg) {
        super(msg);
    }

    protected FetionException() {
    }
}
