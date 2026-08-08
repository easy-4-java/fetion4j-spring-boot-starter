package net.apexes.fetion4j.core.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import net.apexes.fetion4j.core.Account;
import net.apexes.fetion4j.core.AuthSupportable;
import net.apexes.fetion4j.core.FetionConsole;
import net.apexes.fetion4j.core.LogHandler;
import net.apexes.fetion4j.core.NotifyListener;
import net.apexes.fetion4j.core.SystemConfig;
import net.apexes.fetion4j.core.UserInfo;
import net.apexes.fetion4j.core.user.Buddy;
import net.apexes.fetion4j.core.user.Relation;
import net.apexes.fetion4j.core.user.User;
import net.apexes.fetion4j.core.util.XmlElement;

/**
 * Unit tests for {@link Controller} (listener registration and fire* event fan-out).
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
@DisplayName("Controller Tests")
class ControllerTest {

    private Controller controller;
    private RecordingListener listener1;
    private RecordingListener listener2;

    @BeforeEach
    void setUp() {
        controller = new Controller(stubContext());
        listener1 = new RecordingListener();
        listener2 = new RecordingListener();
        controller.addNotifyListener(listener1);
        controller.addNotifyListener(listener2);
    }

    @Test
    @DisplayName("isRunning defaults to false; getContext returns the provided context")
    void testInitialState() {
        assertThat(controller.isRunning()).isFalse();
        assertThat(controller.getContext()).isNotNull();
    }

    @Test
    @DisplayName("createSubAcitivity / createAddBuddyActivity / createDeleteBuddyActivity increment callId")
    void testCreateActivities() {
        assertThat(controller.createSubAcitivity().getCallId()).isEqualTo(1);
        assertThat(controller.createAddBuddyActivity().getCallId()).isEqualTo(2);
        assertThat(controller.createDeleteBuddyActivity().getCallId()).isEqualTo(3);
    }

    @Test
    @DisplayName("createChatDialogue returns a dialogue with incremented callId")
    void testCreateChatDialogue() {
        Buddy buddy = new Buddy(1, "sip:1@fetion.com.cn;p=1", "n", Relation.BUDDY);
        assertThat(controller.createChatDialogue(buddy).getCallId()).isPositive();
    }

    @Test
    @DisplayName("fireTransfeError notifies every registered listener")
    void testFireTransfeError() {
        Exception ex = new RuntimeException("x");
        controller.fireTransfeError("msg", ex);
        assertThat(listener1.transfeErrorCount).isEqualTo(1);
        assertThat(listener2.transfeErrorCount).isEqualTo(1);
    }

    @Test
    @DisplayName("fireChangedSystemConfig fans out the xml element")
    void testFireChangedSystemConfig() {
        XmlElement xml = new XmlElement();
        xml.setName("config");
        controller.fireChangedSystemConfig(xml);
        assertThat(listener1.systemConfig).isSameAs(xml);
        assertThat(listener2.systemConfig).isSameAs(xml);
    }

    @Test
    @DisplayName("fireCreatedAccount fans out the account")
    void testFireCreatedAccount() {
        Account acc = AccountTestSupport.newAccount();
        controller.fireCreatedAccount(acc);
        assertThat(listener1.account).isSameAs(acc);
    }

    @Test
    @DisplayName("fireLoginSuccessed / fireLogoutSuccessed fan out")
    void testFireLoginLogout() {
        UserInfo info = new UserInfo();
        controller.fireLoginSuccessed(mock(FetionConsole.class), info);
        assertThat(listener1.loginUserInfo).isSameAs(info);
        controller.fireLogoutSuccessed();
        assertThat(listener1.logoutCount).isEqualTo(1);
    }

    @Test
    @DisplayName("fireChangedUser / fireChangedBuddy / fireAddedBuddy / fireDeletedBuddy fan out")
    void testFireUserEvents() {
        User u = new User(1);
        Buddy b = new Buddy(2, "u", "n", Relation.BUDDY);
        controller.fireChangedUser(u);
        controller.fireChangedBuddy(b, "v1");
        controller.fireAddedBuddy(b, "v2");
        controller.fireDeletedBuddy(b, "v3");
        assertThat(listener1.changedUser).isSameAs(u);
        assertThat(listener1.changedBuddy).isSameAs(b);
        assertThat(listener1.addedBuddy).isSameAs(b);
        assertThat(listener1.deletedBuddy).isSameAs(b);
    }

    @Test
    @DisplayName("fireSmsCountChanged fans out the counts")
    void testFireSmsCount() {
        controller.fireSmsCountChanged(3, 5);
        assertThat(listener1.dayCount).isEqualTo(3);
        assertThat(listener1.monthCount).isEqualTo(5);
    }

    @Test
    @DisplayName("removeNotifyListener stops delivery to that listener")
    void testRemoveListener() {
        controller.removeNotifyListener(listener2);
        controller.fireLogoutSuccessed();
        assertThat(listener1.logoutCount).isEqualTo(1);
        assertThat(listener2.logoutCount).isZero();
    }

    private static FetionContext stubContext() {
        FetionContext ctx = mock(FetionContext.class);
        org.mockito.Mockito.when(ctx.getLogHandler()).thenReturn(mock(LogHandler.class));
        org.mockito.Mockito.when(ctx.getAccount()).thenReturn(AccountTestSupport.newAccount());
        org.mockito.Mockito.when(ctx.getUserInfo()).thenReturn(new UserInfo());
        org.mockito.Mockito.when(ctx.getSystemConfig()).thenReturn(mock(SystemConfig.class));
        org.mockito.Mockito.when(ctx.getCmccMobileValidator()).thenReturn(mock(CmccMobileValidator.class));
        org.mockito.Mockito.when(ctx.getAuthSupportable()).thenReturn(mock(AuthSupportable.class));
        return ctx;
    }

    /** Minimal NotifyListener that records each event for assertions. */
    static class RecordingListener implements NotifyListener {
        int transfeErrorCount;
        XmlElement systemConfig;
        Account account;
        UserInfo loginUserInfo;
        int logoutCount;
        User changedUser;
        Buddy changedBuddy;
        Buddy addedBuddy;
        Buddy deletedBuddy;
        int dayCount;
        int monthCount;

        @Override public void transfeError(String message, Exception exception) { transfeErrorCount++; }
        @Override public void changedSystemConfig(XmlElement systemConfig) { this.systemConfig = systemConfig; }
        @Override public void createdAccount(Account account) { this.account = account; }
        @Override public void loginSuccessed(FetionConsole console, UserInfo userInfo) { this.loginUserInfo = userInfo; }
        @Override public void logoutSuccessed() { logoutCount++; }
        @Override public void changedUser(User user) { this.changedUser = user; }
        @Override public void changedBuddy(Buddy buddy, String contactVersion) { this.changedBuddy = buddy; }
        @Override public void addedBuddy(Buddy buddy, String contactVersion) { this.addedBuddy = buddy; }
        @Override public void deletedBuddy(Buddy buddy, String contactVersion) { this.deletedBuddy = buddy; }
        @Override public void smsCountChanged(int dayCount, int monthCount) { this.dayCount = dayCount; this.monthCount = monthCount; }
    }
}
