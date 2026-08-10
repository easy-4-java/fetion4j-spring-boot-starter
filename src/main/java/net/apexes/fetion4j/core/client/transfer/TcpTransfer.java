/*
 * Copyright (C) 2012, Apexes Network Technology. All rights reserved.
 * 
 *       http://www.apexes.net
 * 
 */
package net.apexes.fetion4j.core.client.transfer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.ParseException;

import net.apexes.fetion4j.core.sipc.SipcMessage;
import net.apexes.fetion4j.core.sipc.SipcMessageReader;
import net.apexes.fetion4j.core.sipc.SipcMessageWriter;

/**
 * TCP based implementation of a Fetion message transfer.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
public class TcpTransfer implements Transfer {

    private Socket socket;
    private String hostname;
    private int port;
    private SipcMessageReader reader;
    private SipcMessageWriter writer;

    public TcpTransfer(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    @Override
    public void startTransfer() throws TransferException {
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(hostname, port));
            reader = new SipcMessageReader(socket.getInputStream());
            writer = new SipcMessageWriter(socket.getOutputStream());
        } catch (UnknownHostException ex) {
            throw new TransferException("服务器地址无效!", ex);
        } catch (IOException ex) {
            throw new TransferException("连接服务器失败！", ex);
        }
    }

    @Override
    public void stopTransfer() throws TransferException {
        try {
            socket.close();
        } catch (IOException ex) {
            throw new TransferException("关闭连接失败！", ex);
        }
    }
    
    @Override
    public boolean isClosed() {
        return socket.isClosed();
    }

    @Override
    public String getTransferName() {
        return "TcpTransfer-" + socket;
    }

    @Override
    public SipcMessage read() throws IOException, ParseException {
        return reader.read();
    }

    @Override
    public void write(SipcMessage message) throws IOException {
        writer.write(message);
    }
}
