/*
 * Copyright (C) 2012, Apexes Network Technology. All rights reserved.
 * 
 *       http://www.apexes.net
 * 
 */
package net.apexes.fetion4j.core.client.transfer;

/**
 * Exception raised on transfer-level (network) errors.
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 1.0.0
 */
public class TransferException extends Exception {

    public TransferException(Throwable cause) {
        super(cause);
    }

    public TransferException(String message) {
        super(message);
    }
    
    public TransferException(String message, Throwable cause) {
        super(message, cause);
    }

    public TransferException() {
        super();
    }
}
