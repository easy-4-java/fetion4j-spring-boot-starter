package net.apexes.fetion4j.core.client.activity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import net.apexes.fetion4j.core.Account;
import net.apexes.fetion4j.core.AuthSupportable;
import net.apexes.fetion4j.core.LogHandler;
import net.apexes.fetion4j.core.SystemConfig;
import net.apexes.fetion4j.core.UserInfo;
import net.apexes.fetion4j.core.client.CmccMobileValidator;
import net.apexes.fetion4j.core.client.FetionContext;
import net.apexes.fetion4j.core.sipc.RequestMessage;
import net.apexes.fetion4j.core.sipc.Sipc;
import net.apexes.fetion4j.core.user.Buddy;
import net.apexes.fetion4j.core.user.BuddyGroup;
import net.apexes.fetion4j.core.user.Relation;

/**
 * Unit tests for the package-private {@link MessageHelper}.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
@DisplayName("MessageHelper Tests")
class MessageHelperTest {

    // RSA public key (256 hex modulus + 010001 exponent) reused from AuthTest.
    private static final String PK_MODULUS =
            "B4621B7F3459D8345CD228A62F1BA50DD7C04F2AA0CEE6EAFC63077F4CC3C707"
          + "AF9E28AD3CE2ABC1614D363EEA88965DB92B0D5B4D7312E9729ED215F9783328C"
          + "C0A12FB1B1C874B970672C9963EAFD4BFE5D0F876EABE539AAA158D041CD0E4B8"
          + "535496CFE98B8E8102452E2768716613BC18249F4E4C5DEE1E62C3996BCFC7010";
    private static final String PK = PK_MODULUS + "010001";

    private FetionContext contextWithAccount() {
        Account account;
        try {
            java.lang.reflect.Constructor<Account> ctor =
                    Account.class.getDeclaredConstructor(long.class, int.class, String.class);
            ctor.setAccessible(true);
            account = ctor.newInstance(13900000000L, 12345, "sip:12345@fetion.com.cn;p=1");
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
        account.setPassword("123456");

        UserInfo userInfo = new UserInfo();

        FetionContext ctx = mock(FetionContext.class);
        when(ctx.getAccount()).thenReturn(account);
        when(ctx.getMachineCode()).thenReturn("MACHINE");
        when(ctx.getUserInfo()).thenReturn(userInfo);
        when(ctx.getLogHandler()).thenReturn(mock(LogHandler.class));
        when(ctx.getSystemConfig()).thenReturn(mock(SystemConfig.class));
        when(ctx.getCmccMobileValidator()).thenReturn(mock(CmccMobileValidator.class));
        when(ctx.getAuthSupportable()).thenReturn(mock(AuthSupportable.class));
        return ctx;
    }

    @Test
    @DisplayName("createLoginRequest sets CN and CL fields")
    void testCreateLoginRequest() {
        RequestMessage req = MessageHelper.createLoginRequest(contextWithAccount());
        assertThat(req.getMethod()).isEqualTo(Sipc.METHOD_R);
        assertThat(req.getFieldValue(Sipc.FIELD_CL)).isEqualTo("type=\"PCSmart\",version=\"1.0.0000\"");
        assertThat(req.getFieldValue(Sipc.FIELD_CN)).isNotBlank();
    }

    @Test
    @DisplayName("createAuthRequest builds an A-header and body using the context")
    void testCreateAuthRequest() {
        String wHeader = "Digest algorithm=\"SHA1-sess-v4\",nonce=\"1D3C\",key=\"" + PK + "\",signature=\"84E8\"";
        RequestMessage req = MessageHelper.createAuthRequest(contextWithAccount(), wHeader);
        assertThat(req.getMethod()).isEqualTo(Sipc.METHOD_R);
        assertThat(req.getFieldValue(Sipc.FIELD_A)).startsWith("Digest ");
        assertThat(req.getBody()).contains("machine-code=\"MACHINE\"")
                .contains("mobile-no=\"13900000000\"")
                .contains("user-id=\"12345\"");
    }

    @Test
    @DisplayName("createLogoutRequest sets X:0")
    void testCreateLogoutRequest() {
        RequestMessage req = MessageHelper.createLogoutRequest();
        assertThat(req.getFieldValue(Sipc.FIELD_X)).isEqualTo("0");
    }

    @Test
    @DisplayName("createKeepAliveRequest sets N:KeepAlive and a credentials body")
    void testCreateKeepAliveRequest() {
        RequestMessage req = MessageHelper.createKeepAliveRequest();
        assertThat(req.getFieldValue(Sipc.FIELD_N)).isEqualTo("KeepAlive");
        assertThat(req.getBody()).contains("credentials");
    }

    @Test
    @DisplayName("createFutileRequest sets N:SouthAfrica2010")
    void testCreateFutileRequest() {
        RequestMessage req = MessageHelper.createFutileRequest();
        assertThat(req.getFieldValue(Sipc.FIELD_N)).isEqualTo("SouthAfrica2010");
    }

    @Test
    @DisplayName("createMsgRequest sets N:CatMsg for chat and N:SendCatSMS for SMS")
    void testCreateMsgRequest() {
        Buddy buddy = new Buddy(1, "sip:1@fetion.com.cn;p=1", "n", Relation.BUDDY);
        RequestMessage chat = MessageHelper.createMsgRequest(buddy, "hi", false);
        assertThat(chat.getFieldValue(Sipc.FIELD_N)).isEqualTo("CatMsg");
        assertThat(chat.getFieldValue(Sipc.FIELD_T)).isEqualTo(buddy.getUri());
        assertThat(chat.getBody()).isEqualTo("hi");

        RequestMessage sms = MessageHelper.createMsgRequest(buddy, "hi", true);
        assertThat(sms.getFieldValue(Sipc.FIELD_N)).isEqualTo("SendCatSMS");
    }

    @Test
    @DisplayName("createSubPresenceRequest sets N:PresenceV4 and a body")
    void testCreateSubPresenceRequest() {
        RequestMessage req = MessageHelper.createSubPresenceRequest();
        assertThat(req.getFieldValue(Sipc.FIELD_N)).isEqualTo("PresenceV4");
        assertThat(req.getBody()).contains("subscription");
    }

    @Test
    @DisplayName("createAddBuddyRequest fills the add-buddy body template")
    void testCreateAddBuddyRequest() {
        BuddyGroup group = new BuddyGroup(7, "g");
        RequestMessage req = MessageHelper.createAddBuddyRequest("tel:13800138000", "name", group, "desc", 2);
        assertThat(req.getFieldValue(Sipc.FIELD_N)).isEqualTo("AddBuddyV4");
        assertThat(req.getBody()).contains("uri=\"tel:13800138000\"")
                .contains("local-name=\"name\"")
                .contains("buddy-lists=\"7\"")
                .contains("desc=\"desc\"")
                .contains("addbuddy-phrase-id=\"2\"");
    }

    @Test
    @DisplayName("createAddBuddyRequest handles null local-name and null group")
    void testCreateAddBuddyRequestNulls() {
        RequestMessage req = MessageHelper.createAddBuddyRequest("tel:1", null, null, "d", 0);
        assertThat(req.getBody()).contains("local-name=\"\"").contains("buddy-lists=\"\"");
    }

    @Test
    @DisplayName("createDeleteBuddyRequest sets delete-both flag")
    void testCreateDeleteBuddyRequest() {
        Buddy buddy = new Buddy(42, "sip:42@fetion.com.cn;p=1", "n", Relation.BUDDY);
        RequestMessage req = MessageHelper.createDeleteBuddyRequest(buddy, true);
        assertThat(req.getFieldValue(Sipc.FIELD_N)).isEqualTo("DeleteBuddyV4");
        assertThat(req.getBody()).contains("user-id=\"42\"").contains("delete-both=\"1\"");
        RequestMessage req2 = MessageHelper.createDeleteBuddyRequest(buddy, false);
        assertThat(req2.getBody()).contains("delete-both=\"0\"");
    }
}
