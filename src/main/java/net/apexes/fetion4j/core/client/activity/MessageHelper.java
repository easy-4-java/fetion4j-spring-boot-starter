/*
 * Copyright (C) 2012, Apexes Network Technology. All rights reserved.
 * 
 *       http://www.apexes.net
 * 
 */

package net.apexes.fetion4j.core.client.activity;

import java.util.Random;

import net.apexes.fetion4j.core.Account;
import net.apexes.fetion4j.core.client.FetionContext;
import net.apexes.fetion4j.core.client.auth.AuthDigest;
import net.apexes.fetion4j.core.sipc.RequestMessage;
import net.apexes.fetion4j.core.sipc.Sipc;
import net.apexes.fetion4j.core.user.Buddy;
import net.apexes.fetion4j.core.user.BuddyGroup;

/**
 * Helper for building and parsing Fetion chat messages used by the activity layer.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
class MessageHelper {
    
    /**
     * 创建登录请求。
     *
     * 登录请求格式：
     * <pre>
     * R fetion.com.cn SIP-C/4.0
     * F: 123456789
     * I: 1
     * Q: 1 R
     * CN: D1052260
     * CL: type="PCSmart",version="1.0.0000"
     * </pre>
     * 
     * @param clientType
     * @param clientVersion
     * @return 
     */
    public static RequestMessage createLoginRequest(FetionContext context) {
        RequestMessage request = new RequestMessage(Sipc.METHOD_R);
        String cn = Integer.toHexString(new Random().nextInt()).toUpperCase();
        request.setField(Sipc.FIELD_CN, cn);
        request.setField(Sipc.FIELD_CL, "type=\"PCSmart\",version=\"1.0.0000\"");
        return request;
    }

    /**
     * 创建用户验证的SIPC请求。
     *
     * Unauthoried 消息格式如下：
     * <pre>
     * SIP-C/4.0 401 Unauthoried
     * F: 123456789
     * I: 1
     * Q: 1 R
     * W: Digest algorithm="SHA1-sess-v4",nonce="1D3C",key="C3C7",signature="84E8"
     * D: Sun, 26 Feb 2012 11:59:04 GMT
     * </pre>
     *
     * 验证请求消息格式如下：
     * <pre>
     * R fetion.com.cn SIP-C/4.0
     * F: 123456789
     * I: 1
     * Q: 2 R
     * A: Digest algorithm="SHA1-sess-v4",response="4C9D2"
     * L: 471
     * </pre>
     *
     * @param context
     * @param authDigest response 的 W 头域的值。
     * @return 
     */
    public static RequestMessage createAuthRequest(FetionContext context, String authDigest) {
        String machineCode = context.getMachineCode();
        Account account = context.getAccount();
        String personalVersion = context.getUserInfo().getPersonalVersion();
        String contactVersion = context.getUserInfo().getContactVersion();
        AuthDigest digest = AuthDigest.parse(authDigest);
        digest.generateResponse(account);

        RequestMessage request = new RequestMessage(Sipc.METHOD_R);
        request.setField(Sipc.FIELD_A, digest.toString());

        String body = Template.TMPT_USER_AUTH;
        body = body.replace("{machine-code}", machineCode);
        body = body.replace("{mobile-no}", String.valueOf(account.getMobileNo()));
        body = body.replace("{user-id}", String.valueOf(account.getUserId()));
        body = body.replace("{presence-value}", String.valueOf(account.getPresence().getValue()));
        body = body.replace("{personal-version}", personalVersion);
        body = body.replace("{contact-list-version}", contactVersion);
        request.setBody(body);

        return request;
    }

    /**
     * 创建注销登录的SIPC请求。
     *
     * 注销登录请求格式：
     * <pre>
     * R fetion.com.cn SIP-C/4.0
     * F: 123456789
     * I: 1
     * Q: 3 R
     * X: 0
     * </pre>
     */
    public static RequestMessage createLogoutRequest() {
        RequestMessage request = new RequestMessage(Sipc.METHOD_R);
        request.setField(Sipc.FIELD_X, "0");
        return request;
    }

    /**
     * 创建一个心跳包。
     *
     * 心跳包格式：
     * <pre>
     * R fetion.com.cn SIP-C/4.0
     * F: 123456789
     * I: 1
     * Q: 3 R
     * N: KeepAlive
     * L: 77
     * 
     * &lt;args&gt;&lt;credentials domains="fetion.com.cn;m161.com.cn;www.ikuwa.cn" /&gt;&lt;/args&gt;
     * </pre>
     *
     * @param context
     * @return
     */
    public static RequestMessage createKeepAliveRequest() {
        RequestMessage request = new RequestMessage(Sipc.METHOD_R);
        request.setField(Sipc.FIELD_N, "KeepAlive");
        request.setBody("<args><credentials domains=\"fetion.com.cn;m161.com.cn;www.ikuwa.cn\" /></args>");
        return request;
    }
    
    /**
     * 创建请求。
     * 在向好友发送信息时，如果是以手机短信形式送达，需要再发送一个任意请求，
     * 服务器才会将发送信息请求的回复传出，所以就用向服务器发送这个无用的请求。
     * 参见“迷你飞信”的作法(PCSmart)
     *
     * SIPC请求格式：
     * <pre>
     * O fetion.com.cn SIP-C/4.0
     * F: 123456789
     * I: 5
     * Q: 2 O
     * N: SouthAfrica2010
     *  
     * </pre>
     */
    public static RequestMessage createFutileRequest() {
        RequestMessage request = new RequestMessage(Sipc.METHOD_O);
        request.setField(Sipc.FIELD_N, "SouthAfrica2010");
        return request;
    }
    
    /**
     * 创建发送信息请求。
     *
     * SIPC请求格式：
     * <pre>
     * M fetion.com.cn SIP-C/4.0
     * F: 123456789
     * I: 4
     * Q: 1 M
     * T: sip:987654321@fetion.com.cn;p=277
     * C: text/plain
     * K: SaveHistory
     * N: CatMsg
     * L: 12
     * 
     * test11111111
     * </pre>\
     * 
     * @param buddy 发给的好友
     * @param msg 信息内容
     * @param isSms 为 true 是以手机短信的形式发送。
     * @return 
     */
    public static RequestMessage createMsgRequest(Buddy buddy, String msg, boolean isSms) {
        RequestMessage request = new RequestMessage(Sipc.METHOD_M);
        request.setField(Sipc.FIELD_T, buddy.getUri());
        request.setField(Sipc.FIELD_C, "text/plain");
        request.setField(Sipc.FIELD_K, "SaveHistory");
        request.setField(Sipc.FIELD_N, isSms ? "SendCatSMS" : "CatMsg");
        request.setBody(msg);
        return request;
    }
    
    
    /**
     * 创建 SUB PresenceV4 请求。
     * 
     * SUB PresenceV4 请求格式：
     * <pre>
     * SUB fetion.com.cn SIP-C/4.0
     * F: 123456789
     * I: 2
     * Q: 1 SUB
     * N: PresenceV4
     * L: 108
     * 
     * &lt;args&gt;&lt;subscription self="v4default;mail-count;impresa" 
     * buddy="v4default;multi-client;" version="" /&gt;&lt;/args&gt;
     * </pre>
     * 
     * @return 
     */
    public static RequestMessage createSubPresenceRequest() {
        RequestMessage request = new RequestMessage(Sipc.METHOD_SUB);
        request.setField(Sipc.FIELD_N, "PresenceV4");
        String body = "<args><subscription self=\"v4default\" buddy=\"v4default\" version=\"0\" /></args>";
        request.setBody(body);
        return request;
    }
    
    /**
     * 创建添加好友请求。
     * 
     * 添加好友请求格式：
     * <pre>
     * S fetion.com.cn SIP-C/4.0
     * F: 123456789
     * I: 22 
     * Q: 1 S
     * N: AddBuddyV4
     * L: 193
     * 
     * &lt;args&gt;&lt;contacts&gt;&lt;buddies&gt;&lt;buddy uri="tel:13800138000" 
     * local-name="显示名称" buddy-lists="" desc="淡茗" expose-mobile-no="1"  
     * expose-name="1" addbuddy-phrase-id="0" /&gt;&lt;/buddies&gt;&lt;/contacts&gt;&lt;/args&gt;
     * </pre>
     * 
     * @param uri
     *      通常为“tel:手机号码”或“sip:飞信号”
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
    public static RequestMessage createAddBuddyRequest(String uri, String localName, 
            BuddyGroup buddyGroup, String desc, int phraseId) {
        RequestMessage request = new RequestMessage(Sipc.METHOD_S);
        request.setField(Sipc.FIELD_N, "AddBuddyV4");
        String body = Template.TMPL_ADD_BUDDY;
        body = body.replace("{uri}", uri);
        body = body.replace("{local-name}", localName == null ? "" : localName);
        body = body.replace("{buddy-lists}", buddyGroup == null ? "" : String.valueOf(buddyGroup.getId()));
        body = body.replace("{desc}", desc);
        body = body.replace("{addbuddy-phrase-id}", String.valueOf(phraseId));
        request.setBody(body);
        return request; 
    }
    
    /**
     * 创建删除好友的SIPC请求
     * 
     * 删除好友请求格式：
     * <pre>
     * S fetion.com.cn SIP-C/4.0
     * F: 123456789
     * I: 15 
     * Q: 1 S
     * N: DeleteBuddyV4
     * L: 98
     * 
     * <args><contacts><buddies><buddy user-id="487654321" 
     * delete-both="1" /></buddies></contacts></args>
     * </pre>
     * 
     * @param buddy 要删除的好友。
     * @param deleteBoth 是否将自己从对方的好友列表中删除，为 true 时表示删除。 
     * @return 
     */
    public static RequestMessage createDeleteBuddyRequest(Buddy buddy, boolean deleteBoth) {
        RequestMessage request = new RequestMessage(Sipc.METHOD_S);
        request.setField(Sipc.FIELD_N, "DeleteBuddyV4");
        String body = Template.TMPL_DELETE_BUDDY;
        body = body.replace("{user-id}", String.valueOf(buddy.getUserId()));
        body = body.replace("{delete-both}", deleteBoth ? "1" : "0");
        request.setBody(body);
        return request;
    }

}
