/*
 * Copyright (C) 2012, Apexes Network Technology. All rights reserved.
 * 
 *       http://www.apexes.net
 * 
 */
package net.apexes.fetion4j.core;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import net.apexes.fetion4j.core.client.AuthFeedbackImpl;
import net.apexes.fetion4j.core.client.CaptchaImpl;
import net.apexes.fetion4j.core.client.CmccMobileValidator;
import net.apexes.fetion4j.core.client.Controller;
import net.apexes.fetion4j.core.client.FetionContext;
import net.apexes.fetion4j.core.client.SimpleProvider;
import net.apexes.fetion4j.core.client.SimpleProviderFactory;
import net.apexes.fetion4j.core.client.activity.Template;
import net.apexes.fetion4j.core.client.auth.PasswordEncrypterV4;
import net.apexes.fetion4j.core.sipc.Sipc;
import net.apexes.fetion4j.core.sipc.SipcMessage;
import net.apexes.fetion4j.core.user.Personal;
import net.apexes.fetion4j.core.user.Presence;
import net.apexes.fetion4j.core.util.XmlElement;

/**
 * Entry point of the Fetion4j client; logs in to Fetion and exposes a {@link FetionConsole}.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
public class Fetion implements FetionContext {

    private final static String URL_GET_SYSTEM_CONFIG = "http://nav.fetion.com.cn/nav/getsystemconfig.aspx";
    private static final LogHandler DEFAULT_LOG_HANDLER = new DefaultLogHandler();
    //
    private Provider provider;
    private CmccMobileValidator mobileValidator;
    private Controller controller;
    //
    private SystemConfig systemConfig;
    private UserInfo userInfo;
    private long mobileNo;
    private Account account;
    private LogHandler logHandler;
    private AuthSupportable support;
    //
    private FetionConsole console;

    public Fetion(long mobileNo) {
        this(mobileNo, null);
    }

    public Fetion(long mobileNo, ProviderFactory factory) {
        this.mobileNo = mobileNo;
        controller = new Controller(this);
        if (factory == null) {
            factory = new SimpleProviderFactory(this);
        }
        provider = factory.create(mobileNo);
        if (provider instanceof SimpleProvider) {
            addNotifyListener((SimpleProvider) provider);
        }
    }

    /**
     * <p>Login.</p>
     * @param password
     * @return the result
     */
    public FetionConsole login(String password) throws FetionException {
        return login(password, true);
    }

    /**
     * <p>Login.</p>
     * @param password
     * @param online
     * @return the result
     */
    public FetionConsole login(String password, boolean online) throws FetionException {
        Presence presence = online ? Presence.ONLINE : Presence.OFFLINE;
        try {
            initSystemConfig();
            initMobileValidator();
            initUserInfo();

            Personal personal = userInfo.getPersonal();
            if (personal == null) {
                account = createAccount(mobileNo, password);
                controller.fireCreatedAccount(account);
            } else {
                String uri = personal.getUri();
                int userId = personal.getUserId();
                account = new Account(mobileNo, userId, uri);
            }
            if (account == null) {
                return null;
            }
            account.setPassword(password);
            account.setPresence(presence);
        } catch (Exception ex) {
            throw new FetionException("获取系统配置失败！", ex);
        }
        return doLogin();
    }

    /**
     * <p>Do login.</p>
     * @return the result
     */
    private FetionConsole doLogin() throws FetionException {
        controller.start();
        console = new FetionConsoleImpl(controller);
        controller.fireLoginSuccessed(console, userInfo);
        return console;
    }

    /**
     * <p>Close.</p>
     */
    public void close() throws FetionException {
        if (console != null && !console.isClosed()) {
            console.close();
        }
        controller.stop();
    }

    /**
     * <p>Init system config.</p>
     */
    private void initSystemConfig() throws Exception {
        XmlElement xml = provider.readSystemConfig();
        if (xml != null) {
            systemConfig = new SystemConfig(xml);
        }

        URL url = new URL(URL_GET_SYSTEM_CONFIG);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.addRequestProperty("User-Agent", "IIC2.0/PC 4.0.0");
        String content;
        if (systemConfig == null) {
            content = Template.TMPL_SYSTEM_CONFIG_INIT;
        } else {
            content = systemConfig.getSummary();
        }
        content = content.replace("{mobile-no}", Long.toString(mobileNo));
        getLogHandler().debug(Fetion.class, content);
        OutputStream out = conn.getOutputStream();
        out.write(content.getBytes());
        out.flush();
        xml = new XmlElement();
        xml.parseFromReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
        boolean isChanged = true;
        if (systemConfig == null) {
            systemConfig = new SystemConfig(xml);
        } else {
            isChanged = systemConfig.update(xml);
        }
        if (isChanged) {
            controller.fireChangedSystemConfig(systemConfig.getXml());
        }
    }
    
    /**
     * <p>Init mobile validator.</p>
     */
    private void initMobileValidator() {
        XmlElement xml = systemConfig.getXml().getChild("client-config");
        for (XmlElement el : xml.getChildren("item")) {
            if ("mobile-no-dist".equals(el.getStringAttribute("key"))) {
                XmlElement vEl = new XmlElement();
                vEl.parseString(el.getStringAttribute("value"));
                mobileValidator = new CmccMobileValidator(vEl);
            }
        }
    }

    /**
     * <p>Init user info.</p>
     */
    private void initUserInfo() throws IOException {
        userInfo = provider.readUserInfo();
        if (userInfo == null) {
            userInfo = new UserInfo();
        }
    }

    @Override
    /** @return return the machine code. */
    public String getMachineCode() {
        return "5DBFE64D4449FBD0AE130C7B12D27A9F";
    }
    
    @Override
    /** @return return the account. */
    public Account getAccount() {
        return account;
    }
    
    @Override
    /** @return return the auth supportable. */
    public AuthSupportable getAuthSupportable() {
        return support;
    }
    
    /** @param support set the auth supportable. */
    public void setAuthSupportable(AuthSupportable support) {
        this.support = support;
    }
    
    @Override
    /** @return return the log handler. */
    public LogHandler getLogHandler() {
        return logHandler == null ? DEFAULT_LOG_HANDLER : logHandler;
    }

    /** @param logHandler set the log handler. */
    public void setLogHandler(LogHandler logHandler) {
        this.logHandler = logHandler;
    }

    @Override
    /** @return return the system config. */
    public SystemConfig getSystemConfig() {
        return systemConfig;
    }

    @Override
    /** @return return the user info. */
    public UserInfo getUserInfo() {
        return userInfo;
    }

    @Override
    /** @return return the cmcc mobile validator. */
    public CmccMobileValidator getCmccMobileValidator() {
        return mobileValidator;
    }
    
    /**
     * <p>Create account.</p>
     * @param mobileNo
     * @param password
     * @return the result
     */
    private Account createAccount(long mobileNo, String password) throws IOException {
        StringBuilder buf = new StringBuilder();
        buf.append(getSystemConfig().getValue("/config/servers/ssi-app-sign-in-v2"));
        buf.append("?mobileno=");
        buf.append(mobileNo);
        buf.append("&domains=fetion.com.cn%3bm161.com.cn%3bwww.ikuwa.cn");
        buf.append("&v4digest-type=1");
        buf.append("&v4digest=");
        buf.append(PasswordEncrypterV4.encryptV4(password));
        String basicUrl = buf.toString();

        getLogHandler().debug(Fetion.class, basicUrl);

        XmlElement xml;
        URL url = new URL(basicUrl.toString());
        CaptchaImpl captcha = null;
        do {
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            int status = conn.getResponseCode();
            switch (status) {
                case Sipc.STATUS_ACTION_OK:
                    xml = new XmlElement();
                    xml.parseFromReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                    getLogHandler().debug(Fetion.class, xml.toString());
                    XmlElement uEl = xml.getChild("user");
                    int userId = uEl.getIntAttribute("user-id");
                    String uri = uEl.getStringAttribute("uri");
                    return new Account(mobileNo, userId, uri);

                case Sipc.STATUS_EXTENSION_REQUIRED:
                case Sipc.STATUS_BAD_EXTENSION:
                    // 接收到的数据格式：
                    // <?xml version="1.0" encoding="utf-8" ?>
                    // <results status-code="421" desc="password error max">
                    // <verification algorithm="picc-PasswordErrorMax" type="GeneralPic" 
                    // text="为保..." tips="如果您..."></verification></results>
                    xml = new XmlElement();
                    xml.parseFromReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                    AuthFeedbackImpl feedback = new AuthFeedbackImpl(this, captcha, xml);
                    feedback.authAndWait();
                    captcha = feedback.getCaptcha();
                    // 在输入图形验证码时取消了操作
                    if (captcha == null) {
                        return null;
                    }
                    buf = new StringBuilder(basicUrl);
                    buf.append("&pid=");
                    buf.append(captcha.getImageId());
                    buf.append("&pic=");
                    buf.append(captcha.getCode());
                    buf.append("&algorithm=");
                    buf.append(captcha.getVerifyAlgorithm());
                    url = new URL(buf.toString());
                    break;

                default:
                    throw new IOException("SSI登录失败！错误代码：" + status);
            }
        } while (url != null);
        return null;
    }
    
    /**
     * <p>Add notify listener.</p>
     * @param l
     */
    public void addNotifyListener(NotifyListener l) {
        controller.addNotifyListener(l);
    }
    
    /**
     * <p>Remove notify listener.</p>
     * @param l
     */
    public void removeNotifyListener(NotifyListener l) {
        controller.removeNotifyListener(l);
    }

    /**
     *
     */
    private static class DefaultLogHandler implements LogHandler {

        @Override
        /**
         * <p>Receive.</p>
         * @param message
         */
        public void receive(SipcMessage message) {
            System.out.println("<<< ---------------------");
            System.out.println(message);
            System.out.println("-------------------------");
        }

        @Override
        /**
         * <p>Transmit.</p>
         * @param message
         */
        public void transmit(SipcMessage message) {
            System.out.println(">>> ---------------------");
            System.out.println(message);
            System.out.println("-------------------------");
        }

        @Override
        /**
         * <p>Error.</p>
         * @param c
         * @param msg
         * @param t
         */
        public void error(Class<?> c, String msg, Throwable t) {
            System.out.println("[error][" + c + "]" + msg + "\n" + t.getMessage() + "\n");
            t.printStackTrace();
        }

        @Override
        /**
         * <p>Debug.</p>
         * @param c
         * @param msg
         */
        public void debug(Class<?> c, String msg) {
            System.out.println("[debug][" + c + "]" + msg + "\n");
        }

        @Override
        /**
         * <p>Info.</p>
         * @param c
         * @param msg
         */
        public void info(Class<?> c, String msg) {
            System.out.println("[info][" + c + "]" + msg + "\n");
        }

        @Override
        /**
         * <p>Warn.</p>
         * @param c
         * @param msg
         */
        public void warn(Class<?> c, String msg) {
            System.out.println("[warn][" + c + "]" + msg + "\n");
        }
    }
}
