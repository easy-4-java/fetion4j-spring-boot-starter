/*
 * Copyright (C) 2012, Apexes Network Technology. All rights reserved.
 * 
 *       http://www.apexes.net
 * 
 */
package net.apexes.fetion4j.core.client.transfer;

import java.io.IOException;
import java.text.ParseException;

import net.apexes.fetion4j.core.client.FetionContext;
import net.apexes.fetion4j.core.sipc.SipcMessage;

/**
 * Optional proxy configuration applied to Fetion transfers.
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 1.0.0
 */
public class TransferProxy implements Transfer {

    private FetionContext context;
    private Transfer impl;

    public TransferProxy(FetionContext context) {
        this.context = context;
    }

    public void startTransfer() throws TransferException {
        TransferException exception = null;
        Transfer transfer = null;
        String key = "/config/servers/sipc-proxy";
        String host = context.getSystemConfig().getValue(key);
        if (host != null) {
            try {
                context.getLogHandler().debug(TransferProxy.class, key + "=" + host);
                String[] addrs = host.split(":");
                transfer = new TcpTransfer(addrs[0], Integer.valueOf(addrs[1]));
                transfer.startTransfer();
            } catch (TransferException ex) {
                exception = ex;
            }
        }
        if (transfer == null) {
            key = "/config/servers/sipc-proxy-backup";
            host = context.getSystemConfig().getValue(key);
            if (host == null) {
                if (exception == null) {
                    exception = new TransferException("配置文件有误！");
                }
                throw exception;
            }
            context.getLogHandler().debug(TransferProxy.class, key + "=" + host);
            String[] addrs = host.split(":");
            transfer = new TcpTransfer(addrs[0], Integer.valueOf(addrs[1]));
            transfer.startTransfer();
        }
        context.getLogHandler().debug(TransferProxy.class, "连接服务器成功：" + host);
        impl = transfer;
    }

    public void stopTransfer() throws TransferException {
        impl.stopTransfer();
    }

    public SipcMessage read() throws IOException, ParseException {
        return impl.read();
    }

    public void write(SipcMessage message) throws IOException {
        impl.write(message);
    }

    public boolean isClosed() {
        return impl.isClosed();
    }

    public String getTransferName() {
        return impl.getTransferName();
    }
}
