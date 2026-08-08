package net.apexes.fetion4j.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import net.apexes.fetion4j.core.client.CmccMobileValidator;
import net.apexes.fetion4j.core.client.Controller;
import net.apexes.fetion4j.core.client.FetionContext;
import net.apexes.fetion4j.core.client.activity.AddBuddyActivity;
import net.apexes.fetion4j.core.client.Controller;
import net.apexes.fetion4j.core.client.activity.AddBuddyActivity;
import net.apexes.fetion4j.core.client.activity.ChatDialogue;
import net.apexes.fetion4j.core.client.activity.DeleteBuddyActivity;
import net.apexes.fetion4j.core.user.Buddy;
import net.apexes.fetion4j.core.user.Personal;
import net.apexes.fetion4j.core.user.Relation;

/**
 * Unit tests for {@link FetionConsoleImpl}.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
@DisplayName("FetionConsoleImpl Tests")
class FetionConsoleImplTest {

    private FetionContext contextWith(Personal personal) {
        UserInfo info = new UserInfo();
        info.setPersonal(personal);
        info.setContact(new net.apexes.fetion4j.core.user.Contact("0"));
        FetionContext ctx = mock(FetionContext.class);
        when(ctx.getUserInfo()).thenReturn(info);
        when(ctx.getCmccMobileValidator()).thenReturn(new AlwaysCmccValidator());
        return ctx;
    }

    private Controller controllerFor(FetionContext ctx,
                                     AddBuddyActivity add,
                                     DeleteBuddyActivity del,
                                     ChatDialogue chat) throws Exception {
        Controller controller = mock(Controller.class);
        when(controller.getContext()).thenReturn(ctx);
        when(controller.createAddBuddyActivity()).thenReturn(add);
        when(controller.createDeleteBuddyActivity()).thenReturn(del);
        when(controller.createChatDialogue(org.mockito.ArgumentMatchers.any())).thenReturn(chat);
        return controller;
    }

    @Test
    @DisplayName("getUserInfo delegates to the controller context; isClosed defaults to false")
    void testGetUserInfoAndIsClosed() throws Exception {
        Personal personal = new Personal(1, "u", "n");
        FetionContext ctx = contextWith(personal);
        Controller controller = controllerFor(ctx, null, null, null);
        FetionConsoleImpl console = new FetionConsoleImpl(controller);
        assertThat(console.getUserInfo().getPersonal()).isSameAs(personal);
        assertThat(console.isClosed()).isFalse();
    }

    @Test
    @DisplayName("close sets closed=true and stops the controller")
    void testClose() throws Exception {
        FetionContext ctx = contextWith(new Personal(1, "u", "n"));
        Controller controller = controllerFor(ctx, null, null, null);
        FetionConsoleImpl console = new FetionConsoleImpl(controller);
        console.close(); // stop() is a no-op on a mock
        assertThat(console.isClosed()).isTrue();
    }

    @Test
    @DisplayName("addBuddy(userId,...) delegates to the add-buddy activity")
    void testAddBuddyByUserId() throws Exception {
        Personal personal = new Personal(1, "u", "n");
        FetionContext ctx = contextWith(personal);
        AddBuddyActivity add = mock(AddBuddyActivity.class);
        when(add.addBuddy(anyInt(), org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.isNull(), org.mockito.ArgumentMatchers.anyString(), anyInt()))
                .thenReturn(new Result(200, "OK", Result.Type.SUCCESS, "ok"));
        Controller controller = controllerFor(ctx, add, null, null);
        FetionConsoleImpl console = new FetionConsoleImpl(controller);
        Result result = console.addBuddy(42, "name");
        assertThat(result.getType()).isEqualTo(Result.Type.SUCCESS);
    }

    @Test
    @DisplayName("addBuddy(mobileNo,...) throws when the number is not a CMCC mobile")
    void testAddBuddyByMobileNoNotCmcc() throws Exception {
        Personal personal = new Personal(1, "u", "n");
        FetionContext ctx = mock(FetionContext.class);
        UserInfo info = new UserInfo();
        info.setPersonal(personal);
        when(ctx.getUserInfo()).thenReturn(info);
        when(ctx.getCmccMobileValidator()).thenReturn(new NeverCmccValidator());
        Controller controller = controllerFor(ctx, null, null, null);
        FetionConsoleImpl console = new FetionConsoleImpl(controller);
        assertThatThrownBy(() -> console.addBuddy(111L, "name"))
                .isInstanceOf(FetionException.class);
    }

    @Test
    @DisplayName("removeBuddy delegates to the delete-buddy activity")
    void testRemoveBuddy() throws Exception {
        FetionContext ctx = contextWith(new Personal(1, "u", "n"));
        DeleteBuddyActivity del = mock(DeleteBuddyActivity.class);
        when(del.deleteBuddy(org.mockito.ArgumentMatchers.any(Buddy.class), anyBoolean()))
                .thenReturn(new Result(200, "OK", Result.Type.SUCCESS, "ok"));
        Controller controller = controllerFor(ctx, null, del, null);
        FetionConsoleImpl console = new FetionConsoleImpl(controller);
        Result result = console.removeBuddy(new Buddy(2, "u", "n", Relation.BUDDY));
        assertThat(result.getType()).isEqualTo(Result.Type.SUCCESS);
    }

    @Test
    @DisplayName("sendMessage / sendSMSMessage delegate to the chat dialogue")
    void testSendMessage() throws Exception {
        FetionContext ctx = contextWith(new Personal(1, "u", "n"));
        ChatDialogue chat = mock(ChatDialogue.class);
        when(chat.sendMessage(org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(new Result(200, "OK", Result.Type.SUCCESS, "ok"));
        when(chat.sendSMSMessage(org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(new Result(280, "OK", Result.Type.SUCCESS, "sms"));
        Controller controller = controllerFor(ctx, null, null, chat);
        FetionConsoleImpl console = new FetionConsoleImpl(controller);
        Buddy buddy = new Buddy(3, "u", "n", Relation.BUDDY);
        assertThat(console.sendMessage(buddy, "hi").getType()).isEqualTo(Result.Type.SUCCESS);
        assertThat(console.sendSMSMessage(buddy, "hi").getStatus()).isEqualTo(280);
    }

    static class AlwaysCmccValidator extends CmccMobileValidator {
        AlwaysCmccValidator() {
            super(parseTable());
        }
        private static net.apexes.fetion4j.core.util.XmlElement parseTable() {
            try {
                net.apexes.fetion4j.core.util.XmlElement el = new net.apexes.fetion4j.core.util.XmlElement();
                el.parseString("<r><c v=\"cmcc\"><d s=\"1\" e=\"99999999999\"/></c></r>");
                return el;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        @Override
        public boolean isCmccMobileNo(long mobileNo) {
            return true;
        }
    }

    static class NeverCmccValidator extends AlwaysCmccValidator {
        @Override
        public boolean isCmccMobileNo(long mobileNo) {
            return false;
        }
    }
}
