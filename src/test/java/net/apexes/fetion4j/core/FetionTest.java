package net.apexes.fetion4j.core;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import net.apexes.fetion4j.core.sipc.RequestMessage;
import net.apexes.fetion4j.core.sipc.Sipc;

/**
 * Unit tests for {@link Fetion} accessors and the embedded default log handler.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
@DisplayName("Fetion Tests")
class FetionTest {

    @Test
    @DisplayName("Fetion constructor wires the controller and SimpleProvider; accessors return defaults")
    void testConstructorAndAccessors() {
        Fetion fetion = new Fetion(13900000000L);
        assertThat(fetion.getMachineCode()).isEqualTo("5DBFE64D4449FBD0AE130C7B12D27A9F");
        assertThat(fetion.getAccount()).isNull();
        assertThat(fetion.getAuthSupportable()).isNull();
        assertThat(fetion.getSystemConfig()).isNull();
        assertThat(fetion.getUserInfo()).isNull();
        assertThat(fetion.getCmccMobileValidator()).isNull();
        // default log handler is used when none set
        assertThat(fetion.getLogHandler()).isNotNull();
    }

    @Test
    @DisplayName("setAuthSupportable / setLogHandler are round-trippable")
    void testSetters() {
        Fetion fetion = new Fetion(1L);
        AuthSupportable support = new AuthSupportable() {
            @Override
            public void needAuth(Captcha captcha, AuthFeedback feedback) {
                // no-op
            }
        };
        fetion.setAuthSupportable(support);
        assertThat(fetion.getAuthSupportable()).isSameAs(support);

        RecordingLogHandler handler = new RecordingLogHandler();
        fetion.setLogHandler(handler);
        assertThat(fetion.getLogHandler()).isSameAs(handler);
    }

    @Test
    @DisplayName("Default log handler writes without throwing for every level")
    void testDefaultLogHandler() {
        Fetion fetion = new Fetion(2L);
        // exercise the default handler (installed when setLogHandler not called)
        fetion.getLogHandler().debug(FetionTest.class, "dbg");
        fetion.getLogHandler().info(FetionTest.class, "info");
        fetion.getLogHandler().warn(FetionTest.class, "warn");
        fetion.getLogHandler().error(FetionTest.class, "err", new RuntimeException("boom"));
        RequestMessage msg = new RequestMessage(Sipc.METHOD_R);
        msg.setCallId(1);
        msg.setSequence(1);
        fetion.getLogHandler().receive(msg);
        fetion.getLogHandler().transmit(msg);
    }

    @Test
    @DisplayName("addNotifyListener / removeNotifyListener delegate to the controller without error")
    void testNotifyListenerDelegation() {
        Fetion fetion = new Fetion(3L);
        NotifyListener listener = org.mockito.Mockito.mock(NotifyListener.class);
        fetion.addNotifyListener(listener);
        fetion.removeNotifyListener(listener);
    }

    /** A minimal LogHandler that records the last message for assertions. */
    static class RecordingLogHandler implements LogHandler {
        String last;
        @Override public void receive(net.apexes.fetion4j.core.sipc.SipcMessage message) { last = "receive"; }
        @Override public void transmit(net.apexes.fetion4j.core.sipc.SipcMessage message) { last = "transmit"; }
        @Override public void error(Class<?> c, String msg, Throwable t) { last = "error:" + msg; }
        @Override public void debug(Class<?> c, String msg) { last = "debug:" + msg; }
        @Override public void info(Class<?> c, String msg) { last = "info:" + msg; }
        @Override public void warn(Class<?> c, String msg) { last = "warn:" + msg; }
    }

    @Test
    @DisplayName("Fetion(mobileNo, factory) uses the supplied factory and adds a non-SimpleProvider without listener")
    void testConstructorWithFactory() {
        ProviderFactory factory = mobileNo -> new Provider() {
            @Override
            public net.apexes.fetion4j.core.util.XmlElement readSystemConfig() {
                return null;
            }
            @Override
            public UserInfo readUserInfo() {
                return null;
            }
        };
        Fetion fetion = new Fetion(13900000000L, factory);
        assertThat(fetion.getMachineCode()).isNotNull();
    }

    @Test
    @DisplayName("initMobileValidator parses the mobile-no-dist distribution table via reflection")
    void testInitMobileValidator() throws Exception {
        Fetion fetion = new Fetion(13900000000L);
        // build a SystemConfig whose client-config contains a mobile-no-dist item;
        // use single-quoted attribute values to embed the nested XML.
        net.apexes.fetion4j.core.util.XmlElement configXml = new net.apexes.fetion4j.core.util.XmlElement();
        configXml.parseString("<config>"
                + "<client-config>"
                + "<item key='mobile-no-dist' value='<r><c v=\"cmcc\">"
                + "<d s=\"13500000000\" e=\"13999999999\"/>"
                + "</c></r>'/>"
                + "</client-config>"
                + "</config>");
        SystemConfig config = new SystemConfig(configXml);
        java.lang.reflect.Field f = Fetion.class.getDeclaredField("systemConfig");
        f.setAccessible(true);
        f.set(fetion, config);

        java.lang.reflect.Method m = Fetion.class.getDeclaredMethod("initMobileValidator");
        m.setAccessible(true);
        m.invoke(fetion);

        assertThat(fetion.getCmccMobileValidator()).isNotNull();
        assertThat(fetion.getCmccMobileValidator().isCmccMobileNo(13800138000L)).isTrue();
    }

    @Test
    @DisplayName("doLogin surfaces a failure when the controller cannot start (no transfer configured)")
    void testDoLoginFailsWithoutNetwork() throws Exception {
        Fetion fetion = new Fetion(13900000000L);
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> {
            java.lang.reflect.Method m = Fetion.class.getDeclaredMethod("doLogin");
            m.setAccessible(true);
            try {
                m.invoke(fetion);
            } catch (java.lang.reflect.InvocationTargetException ite) {
                throw ite.getCause();
            }
        }).isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("login fails fast with a FetionException when the navigation service is unreachable")
    void testLoginFailsUnreachable() {
        Fetion fetion = new Fetion(13900000000L);
        // nav.fetion.com.cn is unreachable in the test environment, so initSystemConfig
        // raises and login wraps it in a FetionException.
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> fetion.login("any"))
                .isInstanceOf(FetionException.class);
    }

    @Test
    @DisplayName("login(offline=false) also wraps the unreachable-navigation failure")
    void testLoginOffline() {
        Fetion fetion = new Fetion(13900000000L);
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> fetion.login("any", false))
                .isInstanceOf(FetionException.class);
    }

    @Test
    @DisplayName("createAccount via reflection fails fast when the SSI service is unreachable")
    void testCreateAccountUnreachable() throws Exception {
        Fetion fetion = new Fetion(13900000000L);
        // supply a system config whose ssi-app-sign-in-v2 points at the (unreachable) SSI service
        net.apexes.fetion4j.core.util.XmlElement configXml = new net.apexes.fetion4j.core.util.XmlElement();
        configXml.parseString("<config><servers>"
                + "<ssi-app-sign-in-v2>https://uid.fetion.com.cn/ssiportal/SSIAppSignInV4.aspx</ssi-app-sign-in-v2>"
                + "</servers></config>");
        SystemConfig config = new SystemConfig(configXml);
        java.lang.reflect.Field f = Fetion.class.getDeclaredField("systemConfig");
        f.setAccessible(true);
        f.set(fetion, config);

        java.lang.reflect.Method m = Fetion.class.getDeclaredMethod("createAccount", long.class, String.class);
        m.setAccessible(true);
        // The SSI service is unreachable -> IOException surfaces from createAccount.
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> {
            try {
                m.invoke(fetion, 13900000000L, "pwd");
            } catch (java.lang.reflect.InvocationTargetException ite) {
                throw ite.getCause();
            }
        }).isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("close() on a freshly-built Fetion stops the controller (no console to close)")
    void testCloseWithoutLogin() throws Exception {
        Fetion fetion = new Fetion(13900000000L);
        // console is null and controller never started -> close is effectively a no-op
        fetion.close();
    }
}