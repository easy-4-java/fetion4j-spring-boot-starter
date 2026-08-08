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
import net.apexes.fetion4j.core.user.Contact;
import net.apexes.fetion4j.core.util.XmlElement;

/**
 * Activity handling the removal of an existing Fetion buddy.
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 1.0.0
 */
public class DeleteBuddyActivity extends Activity {
    
    public DeleteBuddyActivity(FetionContext context, Controller controller, int callId) {
        super(context, controller, callId);
    }
    
    public Result deleteBuddy(Buddy buddy) throws FetionException {
        return deleteBuddy(buddy, true);
    }
    
    public Result deleteBuddy(Buddy buddy, boolean deleteBoth) throws FetionException {
        Result reply;
        RequestMessage request = MessageHelper.createDeleteBuddyRequest(buddy, deleteBoth);
        ResponseMessage response = submit(request);
        int status = response.getStatus();
        if (status == Sipc.STATUS_ACTION_OK) {
            deletedBuddy(response.getBody());
            reply = new Result(status, response.getStatusMessage(), Result.Type.SUCCESS, "删除好友成功！");
        } else {
            reply = new Result(status, response.getStatusMessage(), Result.Type.FAILURE, "删除好友失败！");
        }
        return reply;
    }
    
    private void deletedBuddy(String body) {
        //<results><contacts version="407799031"><buddies>
        //<buddy user-id="463991346" delete-both="1" />
        //</buddies></contacts></results>
        try {
            Contact contact = getContext().getUserInfo().getContact();
            XmlElement xml = new XmlElement();
            xml.parseString(body);
            xml = xml.getChild("contacts");
            String contactVersion = xml.getStringAttribute("version");
            xml = xml.getChild("buddies");
            for (XmlElement el : xml.getChildren("buddy")) {
                int userId = el.getIntAttribute("user-id");
                Buddy buddy = contact.findBuddy(userId);
                if (buddy != null) {
                    contact.removeBuddy(buddy, contactVersion);
                    getController().fireDeletedBuddy(buddy, contactVersion);
                }
            }
        } catch (Exception ex) {
            getContext().getLogHandler().error(AddBuddyActivity.class, body, ex);
        }
    }
}
