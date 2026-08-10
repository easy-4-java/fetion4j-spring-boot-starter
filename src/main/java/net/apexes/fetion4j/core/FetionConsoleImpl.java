/*
 * Copyright (C) 2012, Apexes Network Technology. All rights reserved.
 * 
 *       http://www.apexes.net
 * 
 */

package net.apexes.fetion4j.core;

import net.apexes.fetion4j.core.client.Controller;
import net.apexes.fetion4j.core.client.FetionContext;
import net.apexes.fetion4j.core.user.Buddy;

/**
 * Default {@link FetionConsole} implementation backed by a {@link net.apexes.fetion4j.core.client.Controller}.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
public class FetionConsoleImpl implements FetionConsole {
    
    private Controller controller;
    private boolean closed;
    
    FetionConsoleImpl(Controller controller) {
        this.controller = controller;
        closed = false;
    }
    
    @Override
    public UserInfo getUserInfo() {
        return controller.getContext().getUserInfo();
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    @Override
    public void close() throws FetionException {
        closed = true;
        controller.stop();
    }

    public Result addBuddy(int userId, String localName) throws FetionException {
        String displayName = controller.getContext().getUserInfo().getPersonal().getDisplayName();
        return controller.createAddBuddyActivity().addBuddy(userId, localName, null, displayName, 0);
    }

    public Result addBuddy(long mobileNo, String localName) throws FetionException {
        FetionContext context = controller.getContext();
        if (! context.getCmccMobileValidator().isCmccMobileNo(mobileNo)) {
            throw new FetionException("只能添加中国移动的手机号码。");
        }
        String displayName = context.getUserInfo().getPersonal().getDisplayName();
        return controller.createAddBuddyActivity().addBuddy(mobileNo, localName, null, displayName, 0);
    }
    
    @Override
    public Result removeBuddy(Buddy buddy) throws FetionException {
        return controller.createDeleteBuddyActivity().deleteBuddy(buddy, true);
    }

    @Override
    public Result sendMessage(Buddy buddy, String message) throws FetionException {
        return controller.createChatDialogue(buddy).sendMessage(message);
    }

    @Override
    public Result sendSMSMessage(Buddy buddy, String message) throws FetionException {
        return controller.createChatDialogue(buddy).sendSMSMessage(message);
    }
    
}
