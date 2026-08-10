/*
 * Copyright (C) 2012, Apexes Network Technology. All rights reserved.
 * 
 *       http://www.apexes.net
 * 
 */
package net.apexes.fetion4j.core.client.activity;

import net.apexes.fetion4j.core.FetionException;
import net.apexes.fetion4j.core.Result;
import net.apexes.fetion4j.core.client.ClientHelper;
import net.apexes.fetion4j.core.client.Controller;
import net.apexes.fetion4j.core.client.Dialogue;
import net.apexes.fetion4j.core.client.FetionContext;
import net.apexes.fetion4j.core.sipc.RequestMessage;
import net.apexes.fetion4j.core.sipc.ResponseMessage;
import net.apexes.fetion4j.core.sipc.Sipc;
import net.apexes.fetion4j.core.sipc.SipcMessage;
import net.apexes.fetion4j.core.user.Buddy;
import net.apexes.fetion4j.core.util.XmlElement;

/**
 * Dialogue representing a one-to-one chat session.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
public class ChatDialogue extends Dialogue {

    private Buddy buddy;

    public ChatDialogue(FetionContext context, Controller controller, int callId, Buddy buddy) {
        super(context, controller, callId);
        this.buddy = buddy;
    }

    @Override
    public void receive(SipcMessage message) {
    }

    public Buddy getBuddy() {
        return buddy;
    }

    public Result sendMessage(String msg) throws FetionException {
        if (ClientHelper.isMobileUri(buddy.getUri())) {
            return sendSMSMessage(msg);
        }
        return sendChatMessage(msg);
    }

    public Result sendSMSMessage(String msg) throws FetionException {
        return submitRequest(MessageHelper.createMsgRequest(buddy, msg, true));
    }

    public Result sendChatMessage(String msg) throws FetionException {
        return submitRequest(MessageHelper.createMsgRequest(buddy, msg, false));
    }

    /**
     * 
     *
     * @param request
     * @return
     * @throws FetionException
     */
    private Result submitRequest(RequestMessage request) throws FetionException {
        ResponseMessage response = submit(request);
        int status = response.getStatus();
        switch (status) {
            case Sipc.STATUS_ACTION_OK:
                return new Result(status,
                        response.getStatusMessage(),
                        Result.Type.SUCCESS, "发送成功。");
                
            case Sipc.STATUS_SEND_SMS_OK:
                updateQuotaFrequency(response.getBody());
                return new Result(status,
                        response.getStatusMessage(),
                        Result.Type.SUCCESS, "已发送到对方手机。");
            default:
                return new Result(status,
                        response.getStatusMessage(),
                        Result.Type.FAILURE, "发送失败。");
        }
    }
    
    private void updateQuotaFrequency(String body) {
        //<results><quota-frequency>
        //<frequency name="send-sms" day-count="5" month-count="17"/>
        //</quota-frequency></results>
        try {
            XmlElement xml = new XmlElement();
            xml.parseString(body);
            getContext().getUserInfo().getQuota().update(getController(), xml);
        } catch (Exception ex) {
            getContext().getLogHandler().error(ChatDialogue.class, body, ex);
        }
    }
}
