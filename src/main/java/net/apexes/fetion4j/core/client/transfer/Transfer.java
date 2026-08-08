/*
 * Copyright (C) 2012, Apexes Network Technology. All rights reserved.
 * 
 *       http://www.apexes.net
 * 
 */
package net.apexes.fetion4j.core.client.transfer;

import java.io.IOException;
import java.text.ParseException;

import net.apexes.fetion4j.core.sipc.SipcMessage;

/**
 * Contract for sending and receiving Fetion SIPC messages over the wire.
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 1.0.0
 */
public interface Transfer {
    
    /**
     * 启动传输
     * @throws TransferException
     */
	public void startTransfer() throws TransferException;
	
    /**
     * 停止传输
     * 
     * @throws TransferException 
     */
	public void stopTransfer() throws TransferException;
    
    /**
     * 读取一个SIPC消息
     * 
     * @return 
     * @throws TransferException
     */
    public SipcMessage read() throws IOException, ParseException;
    
    /**
     * 发送一个SIPC消息
     * 
     * @param message 
     * @throws TransferException
     */
    public void write(SipcMessage message) throws IOException;
    
    /**
     * 如果传输对象已经关闭则返回 true
     * @return 
     */
    public boolean isClosed();
    
	/**
	 * 返回这个传输对象的名字
     * 
	 * @return
	 */
	public String getTransferName();
    
}
