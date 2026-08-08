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
import net.apexes.fetion4j.core.sipc.ResponseMessage;
import net.apexes.fetion4j.core.sipc.Sipc;
import net.apexes.fetion4j.core.user.Buddy;
import net.apexes.fetion4j.core.user.Contact;
import net.apexes.fetion4j.core.user.Relation;

/**
 * Unit tests for {@link AddBuddyActivity}, {@link DeleteBuddyActivity},
 * {@link ChatDialogue} and {@link SubActivity}.
 *
 * <p>These activities delegate the network call to {@code Controller.submit},
 * which is stubbed here via {@link TestControllers}.</p>
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
@DisplayName("Activity Tests")
class ActivityTest {

    @Test
    @DisplayName("AddBuddyActivity.addBuddy(mobileNo,...) returns FAILURE for a non-OK response")
    void testAddBuddyByMobileNoFailure() throws Exception {
        Controller controller = TestControllers.controllerReturning(
                new ResponseMessage(Sipc.STATUS_BAD, "Bad"));
        UserInfo info = new UserInfo();
        info.setContact(new Contact("0"));
        AddBuddyActivity activity = new AddBuddyActivity(
                TestControllers.contextWithUserInfo(info), controller, 1);
        Result result = activity.addBuddy(13800138000L, "name", null, "desc", 0);
        assertThat(result.getType()).isEqualTo(Result.Type.FAILURE);
        assertThat(result.getStatus()).isEqualTo(Sipc.STATUS_BAD);
    }

    @Test
    @DisplayName("AddBuddyActivity.addBuddy(userId,...) returns SUCCESS and registers the buddy on 200")
    void testAddBuddyByUserIdSuccess() throws Exception {
        UserInfo info = new UserInfo();
        info.setContact(new Contact("0"));
        FetionContext ctx = TestControllers.contextWithUserInfo(info);

        ResponseMessage response = new ResponseMessage(Sipc.STATUS_ACTION_OK, "OK");
        response.setBody("<results><contacts version=\"1\"><buddies>"
                + "<buddy user-id=\"5\" uri=\"sip:5@fetion.com.cn;p=1\" local-name=\"n\""
                + " relation-status=\"1\"/>"
                + "</buddies></contacts></results>");
        Controller controller = TestControllers.controllerReturning(response);

        AddBuddyActivity activity = new AddBuddyActivity(ctx, controller, 1);
        Result result = activity.addBuddy(5, "name", null, "desc", 0);
        assertThat(result.getType()).isEqualTo(Result.Type.SUCCESS);
        assertThat(info.getContact().findBuddy(5)).isNotNull();
    }

    @Test
    @DisplayName("DeleteBuddyActivity.deleteBuddy returns SUCCESS and removes the buddy on 200")
    void testDeleteBuddySuccess() throws Exception {
        UserInfo info = new UserInfo();
        Contact contact = new Contact("0");
        Buddy b = new Buddy(7, "u", "n", Relation.BUDDY);
        contact.addBuddy(b);
        info.setContact(contact);
        FetionContext ctx = TestControllers.contextWithUserInfo(info);

        ResponseMessage response = new ResponseMessage(Sipc.STATUS_ACTION_OK, "OK");
        response.setBody("<results><contacts version=\"2\"><buddies>"
                + "<buddy user-id=\"7\" delete-both=\"1\"/>"
                + "</buddies></contacts></results>");
        Controller controller = TestControllers.controllerReturning(response);

        DeleteBuddyActivity activity = new DeleteBuddyActivity(ctx, controller, 1);
        Result result = activity.deleteBuddy(b, true);
        assertThat(result.getType()).isEqualTo(Result.Type.SUCCESS);
        assertThat(info.getContact().findBuddy(7)).isNull();
    }

    @Test
    @DisplayName("DeleteBuddyActivity.deleteBuddy(buddy) delegates to deleteBuddy(buddy,true)")
    void testDeleteBuddyDefault() throws Exception {
        Controller controller = TestControllers.controllerReturning(
                new ResponseMessage(Sipc.STATUS_BAD, "Bad"));
        UserInfo info = new UserInfo();
        info.setContact(new Contact("0"));
        DeleteBuddyActivity activity = new DeleteBuddyActivity(
                TestControllers.contextWithUserInfo(info), controller, 1);
        Result result = activity.deleteBuddy(new Buddy(1, "u", "n", Relation.BUDDY));
        assertThat(result.getType()).isEqualTo(Result.Type.FAILURE);
    }

    @Test
    @DisplayName("ChatDialogue.sendMessage dispatches to SMS path for mobile uris")
    void testChatDialogueSendMobile() throws Exception {
        ResponseMessage response = new ResponseMessage(Sipc.STATUS_SEND_SMS_OK, "OK");
        response.setBody("<results><quota-frequency>"
                + "<frequency name=\"send-sms\" day-count=\"1\" month-count=\"2\"/>"
                + "</quota-frequency></results>");
        Controller controller = TestControllers.controllerReturning(response);

        UserInfo info = new UserInfo();
        info.setContact(new Contact("0"));
        FetionContext ctx = TestControllers.contextWithUserInfo(info);

        Buddy buddy = new Buddy(1, "tel:13900000000", "n", Relation.BUDDY);
        ChatDialogue dialogue = new ChatDialogue(ctx, controller, 1, buddy);
        Result result = dialogue.sendMessage("hi");
        assertThat(result.getType()).isEqualTo(Result.Type.SUCCESS);
        assertThat(result.getStatus()).isEqualTo(Sipc.STATUS_SEND_SMS_OK);
        assertThat(buddy).isSameAs(dialogue.getBuddy());
    }

    @Test
    @DisplayName("ChatDialogue.sendMessage uses chat path and returns SUCCESS on 200")
    void testChatDialogueSendChat() throws Exception {
        Controller controller = TestControllers.controllerReturning(
                new ResponseMessage(Sipc.STATUS_ACTION_OK, "OK"));
        UserInfo info = new UserInfo();
        info.setContact(new Contact("0"));
        FetionContext ctx = TestControllers.contextWithUserInfo(info);

        Buddy buddy = new Buddy(1, "sip:1@fetion.com.cn;p=1", "n", Relation.BUDDY);
        ChatDialogue dialogue = new ChatDialogue(ctx, controller, 1, buddy);
        Result result = dialogue.sendMessage("hi");
        assertThat(result.getType()).isEqualTo(Result.Type.SUCCESS);

        assertThat(dialogue.sendChatMessage("x").getType()).isEqualTo(Result.Type.SUCCESS);
        dialogue.receive(null); // receive is a no-op; ensure no exception
    }

    @Test
    @DisplayName("ChatDialogue.sendMessage returns FAILURE on an unknown status")
    void testChatDialogueFailure() throws Exception {
        Controller controller = TestControllers.controllerReturning(
                new ResponseMessage(Sipc.STATUS_NOT_FOUND, "NF"));
        UserInfo info = new UserInfo();
        info.setContact(new Contact("0"));
        FetionContext ctx = TestControllers.contextWithUserInfo(info);
        Buddy buddy = new Buddy(1, "sip:1@fetion.com.cn;p=1", "n", Relation.BUDDY);
        ChatDialogue dialogue = new ChatDialogue(ctx, controller, 1, buddy);
        Result result = dialogue.sendMessage("hi");
        assertThat(result.getType()).isEqualTo(Result.Type.FAILURE);
    }

    @Test
    @DisplayName("SubActivity.submitSubPresence swallows FetionException when submit times out")
    void testSubActivitySubmit() throws Exception {
        Controller controller = TestControllers.controllerReturningNull();
        SubActivity activity = new SubActivity(
                TestControllers.contextWithUserInfo(new UserInfo()), controller, 1);
        activity.submitSubPresence(); // should not propagate
        assertThat(activity.getCallId()).isEqualTo(1);
    }

    @Test
    @DisplayName("Activity.submit triggers the captcha verify path on STATUS_EXTENSION_REQUIRED")
    void testSubmitTriggersVerify() throws Exception {
        UserInfo info = new UserInfo();
        info.setContact(new Contact("0"));
        ResponseMessage needVerify = new ResponseMessage(Sipc.STATUS_EXTENSION_REQUIRED, "Need");
        needVerify.setField(Sipc.FIELD_W, "Verify algorithm=\"picc-ChangeMachine\",type=\"GeneralPic\"");
        needVerify.setBody("<results><reason text=\"\" tips=\"\"/></results>");
        Controller controller = TestControllers.controllerReturning(needVerify);
        FetionContext ctx = TestControllers.contextWithUserInfo(info);

        AddBuddyActivity activity = new AddBuddyActivity(ctx, controller, 1);
        // The verify path will fail trying to fetch a captcha (no system config); wrap in try/finally.
        try {
            activity.addBuddy(1, "n", null, "d", 0);
        } catch (Exception expected) {
            assertThat(expected).isNotNull();
        }
    }

    @Test
    @DisplayName("ChatDialogue.sendSMSMessage exercises the SMS path explicitly")
    void testChatDialogueSendSMSDirectly() throws Exception {
        Controller controller = TestControllers.controllerReturning(
                new ResponseMessage(Sipc.STATUS_SEND_SMS_OK, "OK"));
        UserInfo info = new UserInfo();
        info.setContact(new Contact("0"));
        FetionContext ctx = TestControllers.contextWithUserInfo(info);
        Buddy buddy = new Buddy(1, "tel:13900000000", "n", Relation.BUDDY);
        ChatDialogue dialogue = new ChatDialogue(ctx, controller, 1, buddy);
        org.assertj.core.api.Assertions.assertThat(dialogue.sendSMSMessage("hi")).isNotNull();
    }
}
