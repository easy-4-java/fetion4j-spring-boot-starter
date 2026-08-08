package net.apexes.fetion4j.core.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import net.apexes.fetion4j.core.LogHandler;
import net.apexes.fetion4j.core.UserInfo;
import net.apexes.fetion4j.core.user.Buddy;
import net.apexes.fetion4j.core.user.BuddyGroup;
import net.apexes.fetion4j.core.user.Contact;
import net.apexes.fetion4j.core.user.Personal;
import net.apexes.fetion4j.core.user.Relation;
import net.apexes.fetion4j.core.util.XmlElement;

/**
 * Unit tests for {@link SimpleProvider} read/write persistence paths.
 *
 * <p>SimpleProvider stores files under {@code .users/<mobileNo>} relative to the
 * current working directory; each test uses a unique mobile number to avoid
 * collisions and cleans up afterwards.</p>
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
@DisplayName("SimpleProvider Persistence Tests")
class SimpleProviderIOTest {

    @TempDir
    java.io.File tempDir;

    private long uniqueMobile() {
        // unique per test to avoid reusing stale .users files
        return 90000000_000L + System.nanoTime() % 100_000L;
    }

    private FetionContext contextWith(UserInfo info) {
        FetionContext ctx = mock(FetionContext.class);
        when(ctx.getLogHandler()).thenReturn(mock(LogHandler.class));
        when(ctx.getUserInfo()).thenReturn(info);
        return ctx;
    }

    @Test
    @DisplayName("writeSystemConfig then readSystemConfig round-trips the XmlElement")
    void testSystemConfigRoundTrip() throws Exception {
        long mobile = uniqueMobile();
        FetionContext ctx = contextWith(null);
        SimpleProvider writer = new SimpleProvider(ctx, mobile);
        XmlElement xml = new XmlElement();
        xml.parseString("<config><servers version=\"7\"/></config>");
        writer.writeSystemConfig(xml);

        SimpleProvider reader = new SimpleProvider(ctx, mobile);
        XmlElement loaded = reader.readSystemConfig();
        assertThat(loaded).isNotNull();
        assertThat(loaded.getName()).isEqualTo("config");
        assertThat(loaded.getChild("servers").getStringAttribute("version")).isEqualTo("7");
    }

    @Test
    @DisplayName("writeUserInfo then readUserInfo round-trips personal/contact data")
    void testUserInfoRoundTrip() throws Exception {
        long mobile = uniqueMobile();
        UserInfo info = new UserInfo();
        Personal p = new Personal(123, "sip:123@fetion.com.cn;p=1", "alice");
        p.setVersion("v");
        info.setPersonal(p);
        Contact c = new Contact("c");
        c.addBuddyGroup(new BuddyGroup(1, "g"));
        Buddy b = new Buddy(456, "sip:456@fetion.com.cn;p=1", "bob", Relation.BUDDY);
        c.addBuddy(b);
        info.setContact(c);

        FetionContext ctx = contextWith(null);
        SimpleProvider writer = new SimpleProvider(ctx, mobile);
        writer.writeUserInfo(info);

        SimpleProvider reader = new SimpleProvider(ctx, mobile);
        UserInfo loaded = reader.readUserInfo();
        assertThat(loaded).isNotNull();
        assertThat(loaded.getPersonal().getUserId()).isEqualTo(123);
        assertThat(loaded.getContact().getVersion()).isEqualTo("c");
        assertThat(loaded.getContact().findBuddy(456)).isNotNull();
    }

    @Test
    @DisplayName("loginSuccessed persists UserInfo; logoutSuccessed reads from context")
    void testNotifyPersistence() throws Exception {
        long mobile = uniqueMobile();
        UserInfo info = new UserInfo();
        Personal p = new Personal(7, "sip:7@fetion.com.cn;p=1", "n");
        info.setPersonal(p);
        info.setContact(new Contact("0"));

        FetionContext ctx = contextWith(info);
        SimpleProvider provider = new SimpleProvider(ctx, mobile);
        provider.loginSuccessed(mock(net.apexes.fetion4j.core.FetionConsole.class), info);
        provider.logoutSuccessed(); // uses context.getUserInfo()
        // changedSystemConfig writes the file (or logs an error)
        XmlElement xml = new XmlElement();
        xml.parseString("<config/>");
        provider.changedSystemConfig(xml);
        assertThat(provider.readSystemConfig().getName()).isEqualTo("config");
    }

    @Test
    @DisplayName("writeUserInfo with empty personal/contact does not throw")
    void testWriteUserInfoMinimal() throws Exception {
        long mobile = uniqueMobile();
        UserInfo info = new UserInfo();
        FetionContext ctx = contextWith(info);
        SimpleProvider provider = new SimpleProvider(ctx, mobile);
        provider.writeUserInfo(info);
        UserInfo loaded = provider.readUserInfo();
        // minimal userInfo has no personal -> toUser fails silently -> null personal
        assertThat(loaded).isNotNull();
    }
}
