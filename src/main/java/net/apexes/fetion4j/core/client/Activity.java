/*
 * Copyright (C) 2012, Apexes Network Technology. All rights reserved.
 * 
 *       http://www.apexes.net
 * 
 */
package net.apexes.fetion4j.core.client;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import net.apexes.fetion4j.core.FetionException;
import net.apexes.fetion4j.core.sipc.RequestMessage;
import net.apexes.fetion4j.core.sipc.ResponseMessage;
import net.apexes.fetion4j.core.sipc.Sipc;

/**
 * Represents an ongoing Fetion client activity (e.g. adding a buddy, chatting).
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
public abstract class Activity {

    private FetionContext context;
    private Controller controller;
    /**
     * 对话的CallId，即 I 头域的值
     */
    private int callId;
    /**
     * 用于记录下每种请求的当前 Sequence，即 Q 头域的内容
     */
    private HashMap<String, Integer> methodSequenceMap;

    /**
     *
     * @param context
     * @param dispatcher
     * @param callId
     */
    protected Activity(FetionContext context, Controller controller, int callId) {
        this.context = context;
        this.controller = controller;
        this.callId = callId;
        methodSequenceMap = new HashMap<String, Integer>();
    }

    protected FetionContext getContext() {
        return context;
    }
    
    protected Controller getController() {
        return controller;
    }

    public int getCallId() {
        return callId;
    }
    
    /**
     * 提交SIPC请求
     *
     * @param request SIPC请求
     * @throws FetionException
     * @return
     */
    protected ResponseMessage submit(RequestMessage request) throws FetionException {
        return submit(request, 5000);
    }
    
    /**
     * 提交SIPC请求
     *
     * @param request SIPC请求
     * @param keepSequence Q头域的值保持不变
     * @throws FetionException
     * @return
     */
    protected ResponseMessage submit(RequestMessage request, boolean keepSequence) 
            throws FetionException {
        return submit(request, 5000, keepSequence);
    }
    
    /**
     *
     * @param request
     * @param timeout
     * @return
     * @throws FetionException
     */
    protected ResponseMessage submit(RequestMessage request, long timeout) throws FetionException {
        return submit(request, timeout, false);
    }
    
    /**
     *
     * @param request
     * @param timeout
     * @param keepSequence
     * @return
     * @throws FetionException
     */
    protected ResponseMessage submit(RequestMessage request, long timeout, boolean keepSequence)
            throws FetionException {
        request = completeRequest(request, keepSequence);
        ResponseMessage response = controller.submit(request, timeout);
        if (response == null) {
            throw new FetionException("服务器无回应，提交失败！");
        }
        if (response.getStatus() == Sipc.STATUS_EXTENSION_REQUIRED) {
            response = verify(response, timeout);
        }
        return response;
    }
    
    /**
     * 向SIPC请求添加 F、I 头域。
     *
     * @param request
     * @param keepSequence
     * @return
     */
    private RequestMessage completeRequest(RequestMessage request, boolean keepSequence) {
        request.setField(Sipc.FIELD_F, context.getAccount().getSid());
        request.setCallId(callId);
        // 如果没有要求保先Q头域的值请将Q头域的值增1，在进行图形验证码验证时，Q头域的值需保持不变。
        if (! keepSequence) {
            synchronized (this) {
                Integer sequence = methodSequenceMap.get(request.getMethod());
                if (sequence == null) {
                    sequence = 0;
                }
                sequence++;
                methodSequenceMap.put(request.getMethod(), sequence);
                request.setSequence(sequence);
            }
        }
        return request;
    }
    
    private ResponseMessage verify(ResponseMessage response, long timeout) throws FetionException {
        CaptchaImpl captcha = null;
        int status;
        do {    
            try {
                AuthFeedbackImpl feedback = new AuthFeedbackImpl(context, captcha, response);
                feedback.authAndWait();
                captcha = feedback.getCaptcha();
                // 取消登录
                if (captcha == null) {
                    return null;
                }
            } catch (IOException ex) {
                throw new FetionException("获取验证码失败！", ex);
            }
            RequestMessage request = response.getRequestMessage();
            // 含验证码的请求比正常的只多了A头域，A头域的格式为：
            // A: Verify algorithm="picc-ChangeMachine",response="uykj",chid="8e4",type="GeneralPic"
            // 注意：如果原消息中有 A: Digest 头域，则该请求中将有两个A头域
            String aVerify = String.format(
                    "Verify algorithm=\"%s\",response=\"%s\",chid=\"%s\",type=\"%s\"",
                    captcha.getVerifyAlgorithm(),
                    captcha.getCode(),
                    captcha.getImageId(),
                    captcha.getVerifyType());
            // 由于允许反复验证，所以先要将原来的记录图形验证信息的A头域删除
            List<String> valueList = request.getFieldValues(Sipc.FIELD_A);
            for (int i = valueList.size() - 1; i >= 0; i--) {
                String value = valueList.get(i);
                if (value.startsWith("Verify ")) {
                    valueList.remove(i);
                }
            }
            request.removeAllField(Sipc.FIELD_A);
            for (String value : valueList) {
                request.addField(Sipc.FIELD_A, value);
            }
            request.addField(Sipc.FIELD_A, aVerify);
            response = submit(request, timeout, true);
            status = response.getStatus();
        } while (status == Sipc.STATUS_BAD_EXTENSION);
        return response;
    }
    
}
