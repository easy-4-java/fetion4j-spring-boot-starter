/*
 * Copyright (C) 2012, Apexes Network Technology. All rights reserved.
 * 
 *       http://www.apexes.net
 * 
 */
package net.apexes.fetion4j.core.client;

import java.text.ParseException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import net.apexes.fetion4j.core.FetionException;
import net.apexes.fetion4j.core.sipc.RequestMessage;
import net.apexes.fetion4j.core.sipc.ResponseMessage;
import net.apexes.fetion4j.core.sipc.SipcMessage;

/**
 * Dispatches incoming Fetion SIPC messages to the appropriate handler.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
public class Dispatcher {

    private Controller controller;
    private LinkedBlockingQueue<SipcMessage> sendQueue;
    /**
     * 用于保存本地请求，以便在得到回复时与 RequestMessage 对象配对
     */
    private ConcurrentHashMap<String, RequestMessage> requestMap;
    private ConcurrentHashMap<String, ResponseMessage> responseMap;

    Dispatcher(Controller controller) {
        this.controller = controller;
        sendQueue = new LinkedBlockingQueue<SipcMessage>();
        requestMap = new ConcurrentHashMap<String, RequestMessage>();
        responseMap = new ConcurrentHashMap<String, ResponseMessage>();
        controller.getExecutorService().submit(new ReceiverTask());
        controller.getExecutorService().submit(new SenderTask());
    }

    /**
     * 提交SIPC请求并阻塞线程等待返回的回复。
     *
     * @param request
     * @param timeout 超时时间，单位为毫秒(ms)
     * @return
     * @throws FetionException
     */
    ResponseMessage submit(RequestMessage request, long timeout) throws FetionException {
        if (!controller.isRunning()) {
            throw new FetionException("提交请求失败，与服务器的连接已断开！");
        }
        String key = ClientHelper.createSipcMessageKey(request);
        requestMap.put(key, request);
        try {
            sendQueue.put(request);
            synchronized (request) {
                request.wait(timeout);
            }
            requestMap.remove(key);
            return responseMap.remove(key);
        } catch (Exception ex) {
            throw new FetionException("提交请求失败！", ex);
        }
    }

    private void receive(SipcMessage message) {
        if (message instanceof ResponseMessage) {
            ResponseMessage response = (ResponseMessage) message;
            String key = ClientHelper.createSipcMessageKey(response);
            RequestMessage request = requestMap.get(key);
            response.setRequestMessage(request);
            responseMap.put(key, response);
            if (request != null) {
                synchronized (request) {
                    request.notifyAll();
                }
                return;
            }
        }
        // 找不到对应请求的回复或者来自服务器的通知交由 Controller 去处理。
        controller.receive(message);
    }

    /**
     * 用于接收从服务端传来的SIPC消息，并将接收到的消息交给派发器处理。
     */
    private class ReceiverTask implements Runnable {

        public void run() {
            while (!Thread.interrupted()) {
                if (!controller.isRunning()) {
                    break;
                }
                try {
                    SipcMessage message = controller.getTransfer().read();
                    if (message != null) {
                        controller.getContext().getLogHandler().receive(message);
                        receive(message);
                    }
                } catch (ParseException ex) {
                    controller.getContext().getLogHandler().error(Controller.class, "解析SIPC信令出错！", ex);
                } catch (Exception ex) {
                    if (controller.isRunning()) {
                        controller.fireTransfeError("传输出错！", ex);
                    }
                }
            }
        }
    }

    /**
     *
     */
    private class SenderTask implements Runnable {

        public void run() {
            while (!Thread.interrupted()) {
                if (!controller.isRunning()) {
                    break;
                }
                try {
                    SipcMessage message = sendQueue.take();
                    controller.getTransfer().write(message);
                    controller.getContext().getLogHandler().transmit(message);
                } catch (Exception ex) {
                    if (controller.isRunning()) {
                        controller.fireTransfeError("传输出错！", ex);
                    }
                }
            }
        }
    }
}
