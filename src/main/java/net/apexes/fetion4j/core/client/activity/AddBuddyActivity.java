/*
 * Copyright (C) 2013, Apexes Network Technology. All rights reserved.
 *
 *       http://www.apexes.net
 *
 */
package net.apexes.fetion4j.core.client.activity;

import net.apexes.fetion4j.core.FetionException;
import net.apexes.fetion4j.core.Result;
import net.apexes.fetion4j.core.client.Activity;
import net.apexes.fetion4j.core.client.Controller;
import net.apexes.fetion4j.core.client.FetionContext;
import net.apexes.fetion4j.core.sipc.RequestMessage;
import net.apexes.fetion4j.core.sipc.ResponseMessage;
import net.apexes.fetion4j.core.sipc.Sipc;
import net.apexes.fetion4j.core.user.Buddy;
import net.apexes.fetion4j.core.user.BuddyGroup;
import net.apexes.fetion4j.core.user.Contact;
import net.apexes.fetion4j.core.user.Relation;
import net.apexes.fetion4j.core.util.XmlElement;

/**
 * Activity handling the addition of a new Fetion buddy.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
public class AddBuddyActivity extends Activity {
    
    public AddBuddyActivity(FetionContext context, Controller controller, int callId) {
        super(context, controller, callId);
    }
    
    /**
     * 添加好友。
     * 
     * @param mobileNo
     *      手机号码
     * @param localName 
     *      要在本地显示的名称
     * @param buddyGroup
     *      要放到的好友分组，为 null 表示不放到到任何组中
     * @param desc
     *      对方看到的名称
     * @param phraseId 
     *      对方收到的添加好友请求的文字内容。对应 SystemConfig 中的
     *      “/parameters/hints/addbuddy-phrases/phrase”节点的 id 属性
     * @return 
     */
    public Result addBuddy(long mobileNo, String localName, 
            BuddyGroup buddyGroup, String desc, int phraseId) throws FetionException {
        String uri = String.format("tel:%d", mobileNo);
        return addBuddy(uri, localName, buddyGroup, desc, phraseId);
    }
    
    /**
     * 添加好友。
     * 
     * @param userId
     *      飞信号
     * @param localName 
     *      要在本地显示的名称
     * @param buddyGroup
     *      要放到的好友分组，为 null 表示不放到到任何组中
     * @param desc
     *      对方看到的名称
     * @param phraseId 
     *      对方收到的添加好友请求的文字内容。对应 SystemConfig 中的
     *      “/parameters/hints/addbuddy-phrases/phrase”节点的 id 属性
     * @return 
     */
    public Result addBuddy(int userId, String localName, 
            BuddyGroup buddyGroup, String desc, int phraseId) throws FetionException {
        String uri = String.format("sip:%d", userId);
        return addBuddy(uri, localName, buddyGroup, desc, phraseId);
    }
    
    /**
     * 
     * @param uri
     * @param localName
     * @param buddyGroup
     * @param desc
     * @param phraseId
     * @return
     * @throws FetionException 
     */
    private Result addBuddy(String uri, String localName, 
            BuddyGroup buddyGroup, String desc, int phraseId) throws FetionException {
        if (desc == null && desc.isEmpty()) {
            desc = getContext().getUserInfo().getPersonal().getName();
        }
        RequestMessage request = MessageHelper.createAddBuddyRequest(uri, 
                localName, buddyGroup, desc, phraseId);
        Result reply;
        ResponseMessage response = submit(request);
        int status = response.getStatus();
        if (status == Sipc.STATUS_ACTION_OK) {
            addedBuddy(response.getBody());
            reply = new Result(status, response.getStatusMessage(), Result.Type.SUCCESS, "添加好友成功！");
        } else {
            reply = new Result(status, response.getStatusMessage(), Result.Type.FAILURE, "添加好友失败！");
        }
        return reply;
    }
    
    private void addedBuddy(String body) {
        //<results><contacts version="407799076"><buddies>
        //<buddy user-id="123456789" status-code="200" error-reason="NO REASON"
        // uri="tel:13800138000" local-name="备注名称" buddy-lists=""
        // relation-status="0" online-notify="0"  basic-service-status="1" 
        // permission-values="identity=1;buddy=2;" desc="我是XX"
        // expose-mobile-no="1" expose-name="1" addbuddy-phrase-id="0" addbuddy-phrase="" />
        // </buddies></contacts></results>
        try {
            Contact contact = getContext().getUserInfo().getContact();
            XmlElement xml = new XmlElement();
            xml.parseString(body);
            xml = xml.getChild("contacts");
            String contactVersion = xml.getStringAttribute("version");
            xml = xml.getChild("buddies");
            for (XmlElement el : xml.getChildren("buddy")) {
                int userId = el.getIntAttribute("user-id");
                String uri = el.getStringAttribute("uri");
                String name = el.getStringAttribute("local-name");
                int r = el.getIntAttribute("relation-status");
                Relation relation = Relation.valueOf(r);
                Buddy buddy = new Buddy(userId, uri, name, relation);
                contact.addBuddy(buddy, contactVersion);
                getController().fireAddedBuddy(buddy, contactVersion);
            }
        } catch (Exception ex) {
            getContext().getLogHandler().error(AddBuddyActivity.class, body, ex);
        }
    }
}
