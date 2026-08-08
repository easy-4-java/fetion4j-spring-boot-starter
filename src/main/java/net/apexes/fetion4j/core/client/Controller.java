/*
 * Copyright (C) 2012, Apexes Network Technology. All rights reserved.
 * 
 *       http://www.apexes.net
 * 
 */
package net.apexes.fetion4j.core.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.apexes.fetion4j.core.Account;
import net.apexes.fetion4j.core.FetionConsole;
import net.apexes.fetion4j.core.FetionException;
import net.apexes.fetion4j.core.NotifyListener;
import net.apexes.fetion4j.core.Result;
import net.apexes.fetion4j.core.UserInfo;
import net.apexes.fetion4j.core.client.activity.AddBuddyActivity;
import net.apexes.fetion4j.core.client.activity.ChatDialogue;
import net.apexes.fetion4j.core.client.activity.DeleteBuddyActivity;
import net.apexes.fetion4j.core.client.activity.MainDialogue;
import net.apexes.fetion4j.core.client.activity.SubActivity;
import net.apexes.fetion4j.core.client.transfer.Transfer;
import net.apexes.fetion4j.core.client.transfer.TransferException;
import net.apexes.fetion4j.core.client.transfer.TransferProxy;
import net.apexes.fetion4j.core.sipc.RequestMessage;
import net.apexes.fetion4j.core.sipc.ResponseMessage;
import net.apexes.fetion4j.core.sipc.SipcMessage;
import net.apexes.fetion4j.core.user.Buddy;
import net.apexes.fetion4j.core.user.User;
import net.apexes.fetion4j.core.util.XmlElement;

/**
 * Drives the Fetion client lifecycle and dispatches notifications to listeners.
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 1.0.0
 */
public final class Controller {

    /**
     *
     */
    private FetionContext context;
    //
    private List<NotifyListener> listeners;
    private Dispatcher dispatcher;
    /**
     *
     */
    private volatile boolean running;
    /**
     *
     */
    private ExecutorService executor;
    /**
     *
     */
    private Transfer transfer;
    /**
     *
     */
    private ConcurrentHashMap<Integer, Dialogue> dialogueMap;
    /**
     * 对话的CallId，即 I 头域的值
     */
    private int currentCallId = 0;
    private MainDialogue mainDialogue;
    
    /**
     *
     * @param context
     * @param executor
     */
    public Controller(FetionContext context) {
        this.context = context;
        listeners = Collections.synchronizedList(new ArrayList<NotifyListener>());
        dialogueMap = new ConcurrentHashMap<Integer, Dialogue>();
        running = false;
    }

    /**
     * 如果正在运行则返回 true，否则返回 false
     *
     * @return
     */
    public boolean isRunning() {
        return running;
    }

    public FetionContext getContext() {
        return context;
    }

    ExecutorService getExecutorService() {
        return executor;
    }

    Transfer getTransfer() {
        return transfer;
    }

    public void start() throws FetionException {
        if (!running) {
            running = true;
            transfer = new TransferProxy(context);
            try {
                transfer.startTransfer();
            } catch (TransferException ex) {
                throw new FetionException(ex.getMessage(), ex);
            }
            executor = Executors.newCachedThreadPool();

            dispatcher = new Dispatcher(this);

            currentCallId = 1;
            mainDialogue = new MainDialogue(context, this, currentCallId);
            registerDialogue(mainDialogue);
            Result result = mainDialogue.login();
            if (result == null) {
                throw new FetionException("登录失败！");
            }
            if (result.getType() == Result.Type.FAILURE) {
                throw new FetionException(result.getDescribe());
            }
        }
    }

    public void stop() throws FetionException {
        if (running) {
            if (mainDialogue != null) {
                mainDialogue.logout();
            }
            
            running = false;

            executor.shutdownNow();
            try {
                transfer.stopTransfer();
            } catch (TransferException ex) {
                throw new FetionException(ex.getMessage(), ex);
            }
            dialogueMap.clear();
            mainDialogue = null;
            fireLogoutSuccessed();
        }
    }

    void receive(SipcMessage message) {
        int callId = message.getCallId();
        Dialogue dialogue = dialogueMap.get(callId);
        if (dialogue != null) {
            dialogue.receive(message);
        }
        /**
         * @TODO 如果在注册的activity中找不到 CallId 相符的，判断是不是好友发来的聊天请求，如果是则创建一个新的聊天会话。
         */
    }

    /**
     *
     * @param request
     * @param timeout 超时时间，单位为毫秒(ms)
     * @return
     * @throws FetionException
     */
    ResponseMessage submit(RequestMessage request, long timeout) throws FetionException {
        return dispatcher.submit(request, timeout);
    }

    private void registerDialogue(Dialogue dialogue) {
        dialogueMap.put(dialogue.getCallId(), dialogue);
    }

    /**
     * 创建一个聊天会话。
     *
     * @param buddy
     * @return
     */
    public ChatDialogue createChatDialogue(Buddy buddy) {
        currentCallId++;
        ChatDialogue dialogue = new ChatDialogue(context, this, currentCallId, buddy);
        registerDialogue(new ChatDialogue(context, this, currentCallId, buddy));
        return dialogue;
    }

    /**
     *
     * @return
     */
    public SubActivity createSubAcitivity() {
        currentCallId++;
        return new SubActivity(context, this, currentCallId);
    }
    
    /**
     *
     * @return
     */
    public AddBuddyActivity createAddBuddyActivity() {
        currentCallId++;
        return new AddBuddyActivity(context, this, currentCallId);
    }
    
    /**
     *
     * @return
     */
    public DeleteBuddyActivity createDeleteBuddyActivity() {
        currentCallId++;
        return new DeleteBuddyActivity(context, this, currentCallId);
    }
    
    public synchronized void addNotifyListener(NotifyListener l) {
        listeners.add(l);
    }
    
    public synchronized void removeNotifyListener(NotifyListener l) {
        listeners.remove(l);
    }
    
    /**
     * 
     * @param message
     * @param exception 
     */
    public synchronized void fireTransfeError(String message, Exception exception) {
        for (NotifyListener l : listeners) {
            l.transfeError(message, exception);
        }
    }
    
    /**
     * 完成初始化时如果SystemConfig有更改。
     * 
     * @param systemConfig 从服务端获取到的 SystemConfig 对象。
     */
    public synchronized void fireChangedSystemConfig(XmlElement xml) {
        for (NotifyListener l : listeners) {
            l.changedSystemConfig(xml);
        }
    }
    
    /**
     * 获取账号信息成功。(由SSI登录得到)
     * 
     * @param account 获得的账号。
     */
    public synchronized void fireCreatedAccount(Account account) {
        for (NotifyListener l : listeners) {
            l.createdAccount(account);
        }
    }
    
    /**
     * 登录成功。
     * 
     * @param console 登录成功后返回的控制板。
     * @param userInfo 登录成功后的 UserInfo 对象。
     */
    public synchronized void fireLoginSuccessed(FetionConsole console, UserInfo userInfo) {
        for (NotifyListener l : listeners) {
            l.loginSuccessed(console, userInfo);
        }
    }
    
    /**
     * 通知注销登录成功。
     */
    public synchronized void fireLogoutSuccessed() {
        for (NotifyListener l : listeners) {
            l.logoutSuccessed();
        }
    }
    
    /**
     * 通知联系人资料有更新。
     * 
     * @param user 资料有更新的联系人。
     */
    public synchronized void fireChangedUser(User user) {
        for (NotifyListener l : listeners) {
            l.changedUser(user);
        }
    }
    
    /**
     * 通知好友资料有更新，主要是指 relation 属性。
     * 
     * @param buddy 资料有更新的好友。
     * @param contactVersion 
     */
    public synchronized void fireChangedBuddy(Buddy buddy, String contactVersion) {
        for (NotifyListener l : listeners) {
            l.changedBuddy(buddy, contactVersion);
        }
    }
    
    /**
     * 通知添加好友成功。
     * 
     * @param buddy 新添加的好友。
     * @param contactVersion 添加好友成功后 Contact 的版本号。
     */
    public synchronized void fireAddedBuddy(Buddy buddy, String contactVersion) {
        for (NotifyListener l : listeners) {
            l.addedBuddy(buddy, contactVersion);
        }
    }
    
    /**
     * 通知删除好友成功。
     * 
     * @param buddy 已经成功删除的好友。
     * @param contactVersion 删除好友后的 Contact 版本号。
     */
    public synchronized void fireDeletedBuddy(Buddy buddy, String contactVersion) {
        for (NotifyListener l : listeners) {
            l.deletedBuddy(buddy, contactVersion);
        }
    }
    
    /**
     * 通知发送手机短信的数量更改。
     * 
     * @param dayCount 当天发送的短信数。
     * @param monthCount 当月发送的短信数。
     */
    public synchronized void fireSmsCountChanged(int dayCount, int monthCount) {
        for (NotifyListener l : listeners) {
            l.smsCountChanged(dayCount, monthCount);
        }
    }
    
}
