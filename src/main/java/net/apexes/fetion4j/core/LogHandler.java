package net.apexes.fetion4j.core;

import net.apexes.fetion4j.core.sipc.SipcMessage;

/**
 * Handler for logging Fetion protocol messages and client events.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
public interface LogHandler {
    
    void transmit(SipcMessage message);
    
    void receive(SipcMessage message);
    
    void error(Class<?> c, String msg, Throwable t);
    
    void debug(Class<?> c, String msg);
    
    void info(Class<?> c, String msg);
    
    void warn(Class<?> c, String msg);
    
}
