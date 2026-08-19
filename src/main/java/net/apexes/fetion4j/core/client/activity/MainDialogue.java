/*
 * Copyright (C) 2012, Apexes Network Technology. All rights reserved.
 * 
 *       http://www.apexes.net
 * 
 */
package net.apexes.fetion4j.core.client.activity;

import java.text.ParseException;
import java.util.Timer;
import java.util.TimerTask;

import net.apexes.fetion4j.core.FetionException;
import net.apexes.fetion4j.core.Result;
import net.apexes.fetion4j.core.Result.Type;
import net.apexes.fetion4j.core.client.Controller;
import net.apexes.fetion4j.core.client.Dialogue;
import net.apexes.fetion4j.core.client.FetionContext;
import net.apexes.fetion4j.core.sipc.RequestMessage;
import net.apexes.fetion4j.core.sipc.ResponseMessage;
import net.apexes.fetion4j.core.sipc.Sipc;
import net.apexes.fetion4j.core.sipc.SipcMessage;
import net.apexes.fetion4j.core.util.XmlElement;

/**
 * Dialogue representing the main Fetion session.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
public class MainDialogue extends Dialogue {

    /**
     *
     */
    private Timer timer;
    /**
     * 心跳包的发送间隔，单位为微秒
     */
    private int keepAliveTime = 500000;

    /**
     *
     * @param context
     * @param dispatcher
     */
    public MainDialogue(FetionContext context, Controller controller, int callId) {
        super(context, controller, callId);
    }

    @Override
    /**
     * <p>Receive.</p>
     * @param message
     */
    public void receive(SipcMessage message) {
        if (Sipc.METHOD_BN.equals(message.getMethod())) {
            parseBN(message);
        }
    }

    /**
     * <p>Close.</p>
     */
    public void close() {
        terminateKeepAlive();
    }

    /**
     * 登录
     *
     * @return
     */
    public Result login() throws FetionException {
        Result result = null;
        // 发送发起登录的请求
        RequestMessage request = MessageHelper.createLoginRequest(getContext());
        ResponseMessage response = submit(request);
        if (response != null) {
            // 开始登录身份验证，这里将发送加密后的用户密码
            String authDigest = response.getFieldValue(Sipc.FIELD_W);
            request = MessageHelper.createAuthRequest(getContext(), authDigest);
            response = submit(request);
            if (response != null) {
                int status = response.getStatus();
                if (status == Sipc.STATUS_ACTION_OK) {
                    // 更新用户配置
                    XmlElement xml = new XmlElement();
                    xml.parseString(response.getBody());
                    XmlElement uXml = xml.getChild("user-info");
                    try {
                        getContext().getUserInfo().updateOnLogined(getController(), uXml);
                    } catch (ParseException ex) {
                        getContext().getLogHandler().error(MainDialogue.class, "用户信息解析出错！", ex);
                    } catch (Exception ex) {
                        getContext().getLogHandler().error(MainDialogue.class, "保存用户信息出错！", ex);
                    }

                    // 设置心跳包的发送间隔，这里取最大发送间隔的五分之四，间隔时间单位为毫秒(ms)
                    String x = response.getFieldValue(Sipc.FIELD_X);
                    keepAliveTime = (Integer.valueOf(x) / 5 * 4) * 1000;

                    // 启动发送心跳包线程
                    launchKeepAlive();

                    getController().createSubAcitivity().submitSubPresence();

                    result = new Result(status, response.getStatusMessage(), Type.SUCCESS, "登录成功！");
                } else {
                    result = new Result(status, response.getStatusMessage(), Result.Type.FAILURE, "登录失败!");
                }
            }
        }
        return result;
    }

    /**
     * 注销登录
     *
     * @return
     */
    public void logout() throws FetionException {
        // 发送注销登录请求
        submit(MessageHelper.createLogoutRequest());
        // 终止心跳包发送线程
        terminateKeepAlive();
    }

    /**
     * 启动心跳包发送线程
     */
    private void launchKeepAlive() {
        TimerTask task = new TimerTask() {
            /**
             * <p>Run.</p>
             */
            public void run() {
                try {
                    submit(MessageHelper.createKeepAliveRequest());
                } catch (FetionException ex) {
                    getContext().getLogHandler().error(MainDialogue.class, "发送心跳包出错！", ex);
                }
            }
        };
        timer = new Timer();
        timer.schedule(task, keepAliveTime, keepAliveTime);
    }

    /**
     * 终止发送心跳包线程
     */
    private void terminateKeepAlive() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    /**
     * <p>Parse b n.</p>
     * @param message
     */
    private void parseBN(SipcMessage message) {
        String nValue = message.getFieldValue(Sipc.FIELD_N);
        if ("PresenceV4".equals(nValue)) {
            XmlElement xml = new XmlElement();
            xml.parseString(message.getBody());
            try {
                getContext().getUserInfo().updateOnPresenceChanged(getController(), xml);
            } catch (Exception ex) {
                getContext().getLogHandler().error(MainDialogue.class, "更新用户信息出错！", ex);
            }
        } else if ("SyncUserInfoV4".equals(nValue)) {
            XmlElement xml = new XmlElement();
            xml.parseString(message.getBody());
            try {
                getContext().getUserInfo().updateOnSyncUserInfoChanged(getController(), xml);
            } catch (Exception ex) {
                getContext().getLogHandler().error(MainDialogue.class, "更新用户信息出错！", ex);
            }
        }
    }
}
