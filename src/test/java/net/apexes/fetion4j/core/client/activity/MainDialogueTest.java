package net.apexes.fetion4j.core.client.activity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import net.apexes.fetion4j.core.Result;
import net.apexes.fetion4j.core.UserInfo;
import net.apexes.fetion4j.core.client.Controller;
import net.apexes.fetion4j.core.client.FetionContext;
import net.apexes.fetion4j.core.client.TestControllers;
import net.apexes.fetion4j.core.sipc.RequestMessage;
import net.apexes.fetion4j.core.sipc.ResponseMessage;
import net.apexes.fetion4j.core.sipc.Sipc;
import net.apexes.fetion4j.core.user.Buddy;
import net.apexes.fetion4j.core.user.Contact;
import net.apexes.fetion4j.core.user.Personal;
import net.apexes.fetion4j.core.user.Relation;

/**
 * Unit tests for {@link MainDialogue}.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
@DisplayName("MainDialogue Tests")
class MainDialogueTest {

    private FetionContext contextWith(UserInfo info) {
        return TestControllers.contextWithUserInfo(info);
    }

    @Test
    @DisplayName("login throws FetionException when the login response is null (timeout)")
    void testLoginTimeout() throws Exception {
        Controller controller = TestControllers.controllerReturningNull();
        UserInfo info = new UserInfo();
        MainDialogue dialogue = new MainDialogue(contextWith(info), controller, 1);
        org.assertj.core.api.Assertions.assertThatThrownBy(dialogue::login)
                .isInstanceOf(net.apexes.fetion4j.core.FetionException.class);
    }

    @Test
    @DisplayName("login returns FAILURE on a non-OK auth response")
    void testLoginFailure() throws Exception {
        ResponseMessage r1 = new ResponseMessage(Sipc.STATUS_UNAUTHORIZED, "Unauthorized");
        r1.setField(Sipc.FIELD_W, "Digest algorithm=\"SHA1-sess-v4\",nonce=\"1D3C\",key=\""
                + PK + "\",signature=\"84E8\"");
        ResponseMessage r2 = new ResponseMessage(Sipc.STATUS_FORBIDDEN, "Forbidden");

        UserInfo info = new UserInfo();
        Controller controller = TestControllers.controllerWithResponses(r1, r2);
        MainDialogue dialogue = new MainDialogue(contextWith(info), controller, 1);
        Result result = dialogue.login();
        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualTo(Result.Type.FAILURE);
    }

    @Test
    @DisplayName("login returns SUCCESS when the auth response is 200")
    void testLoginSuccess() throws Exception {
        ResponseMessage r1 = new ResponseMessage(Sipc.STATUS_UNAUTHORIZED, "Unauthorized");
        r1.setField(Sipc.FIELD_W, "Digest algorithm=\"SHA1-sess-v4\",nonce=\"1D3C\",key=\""
                + PK + "\",signature=\"84E8\"");
        ResponseMessage r2 = new ResponseMessage(Sipc.STATUS_ACTION_OK, "OK");
        r2.setField(Sipc.FIELD_X, "500");
        r2.setBody("<results><user-info>"
                + "<personal version=\"1\" user-id=\"1\" uri=\"sip:1@fetion.com.cn;p=1\" name=\"n\"/>"
                + "<contact-list version=\"1\"/>"
                + "</user-info></results>");

        UserInfo info = new UserInfo();
        FetionContext ctx = contextWith(info);
        Controller controller = TestControllers.controllerWithResponses(r1, r2);
        TestControllers.withSubActivity(controller, ctx);

        MainDialogue dialogue = new MainDialogue(ctx, controller, 1);
        Result result = dialogue.login();
        assertThat(result.getType()).isEqualTo(Result.Type.SUCCESS);

        // close() terminates the keep-alive timer
        dialogue.close();
    }

    @Test
    @DisplayName("receive ignores messages that are not BN; parses PresenceV4 BN")
    void testReceive() {
        UserInfo info = new UserInfo();
        Personal self = new Personal(1, "sip:1@fetion.com.cn;p=1", "me");
        info.setPersonal(self);
        Contact contact = new Contact("1");
        Buddy b = new Buddy(2, "sip:2@fetion.com.cn;p=1", "b", Relation.BUDDY);
        contact.addBuddy(b);
        info.setContact(contact);

        Controller controller = mock(Controller.class);
        MainDialogue dialogue = new MainDialogue(contextWith(info), controller, 1);

        // a non-BN message is ignored
        RequestMessage other = new RequestMessage(Sipc.METHOD_R);
        other.setCallId(1);
        other.setSequence(1);
        dialogue.receive(other);

        // a BN PresenceV4 message updates the buddy
        RequestMessage bn = new RequestMessage(Sipc.METHOD_BN);
        bn.setCallId(1);
        bn.setSequence(1);
        bn.setField(Sipc.FIELD_N, "PresenceV4");
        bn.setBody("<events><event type=\"PresenceChanged\"><contacts>"
                + "<c id=\"2\"><p v=\"9\" sid=\"22\" su=\"sip:2@fetion.com.cn;p=2\"/>"
                + "<pr b=\"600\"/></c></contacts></event></events>");
        dialogue.receive(bn);
        assertThat(b.getSid()).isEqualTo("22");
    }

    @Test
    @DisplayName("receive parses a SyncUserInfoV4 BN message")
    void testReceiveSyncUserInfo() {
        UserInfo info = new UserInfo();
        info.setPersonal(new Personal(1, "u", "n"));
        Contact contact = new Contact("1");
        Buddy b = new Buddy(5, "u", "n", Relation.UNCONFIRMED);
        contact.addBuddy(b);
        info.setContact(contact);

        Controller controller = mock(Controller.class);
        MainDialogue dialogue = new MainDialogue(contextWith(info), controller, 1);

        RequestMessage bn = new RequestMessage(Sipc.METHOD_BN);
        bn.setCallId(1);
        bn.setSequence(1);
        bn.setField(Sipc.FIELD_N, "SyncUserInfoV4");
        bn.setBody("<events><event type=\"SyncUserInfo\"><user-info>"
                + "<contact-list version=\"42\"><buddies>"
                + "<buddy action=\"update\" user-id=\"5\" relation-status=\"1\"/>"
                + "</buddies></contact-list></user-info></event></events>");
        dialogue.receive(bn);
        assertThat(b.getRelation()).isEqualTo(Relation.BUDDY);
    }

    @Test
    @DisplayName("logout submits the logout request and terminates keep-alive")
    void testLogout() throws Exception {
        Controller controller = TestControllers.controllerReturning(
                new ResponseMessage(Sipc.STATUS_ACTION_OK, "OK"));
        MainDialogue dialogue = new MainDialogue(
                contextWith(new UserInfo()), controller, 1);
        dialogue.logout(); // submit returns OK; terminateKeepAlive is a no-op when timer is null
    }

    @Test
    @DisplayName("close() is safe to call when keep-alive timer was never launched")
    void testCloseWithoutLogin() {
        Controller controller = mock(Controller.class);
        MainDialogue dialogue = new MainDialogue(
                contextWith(new UserInfo()), controller, 1);
        dialogue.close();
    }

    // RSA public key reused from AuthTest (modulus + exponent 0x010001)
    private static final String PK_MODULUS =
            "B4621B7F3459D8345CD228A62F1BA50DD7C04F2AA0CEE6EAFC63077F4CC3C707"
          + "AF9E28AD3CE2ABC1614D363EEA88965DB92B0D5B4D7312E9729ED215F9783328C"
          + "C0A12FB1B1C874B970672C9963EAFD4BFE5D0F876EABE539AAA158D041CD0E4B8"
          + "535496CFE98B8E8102452E2768716613BC18249F4E4C5DEE1E62C3996BCFC7010";
    private static final String PK = PK_MODULUS + "010001";
}
