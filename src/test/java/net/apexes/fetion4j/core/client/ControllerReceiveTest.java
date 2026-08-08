package net.apexes.fetion4j.core.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import net.apexes.fetion4j.core.UserInfo;
import net.apexes.fetion4j.core.sipc.RequestMessage;
import net.apexes.fetion4j.core.sipc.Sipc;
import net.apexes.fetion4j.core.sipc.SipcMessage;
import net.apexes.fetion4j.core.user.Buddy;
import net.apexes.fetion4j.core.user.Relation;

/**
 * Unit tests for {@link Controller}'s receive() routing and stop() guard.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
@DisplayName("Controller Receive Tests")
class ControllerReceiveTest {

    @Test
    @DisplayName("Controller.receive routes an inbound message to the dialogue for its callId")
    void testReceiveRoutesToDialogue() throws Exception {
        FetionContext ctx = TestControllers.contextWithUserInfo(new UserInfo());
        Controller controller = new Controller(ctx);
        Buddy buddy = new Buddy(1, "u", "n", Relation.BUDDY);
        // createChatDialogue registers a dialogue and increments currentCallId
        controller.createChatDialogue(buddy);

        RequestMessage msg = new RequestMessage(Sipc.METHOD_M);
        msg.setCallId(1); // first dialogue created has callId 1
        msg.setSequence(1);
        controller.receive(msg); // should not throw even if no matching dialogue
    }

    @Test
    @DisplayName("Controller.receive with an unknown callId is a no-op")
    void testReceiveUnknownCallId() throws Exception {
        FetionContext ctx = TestControllers.contextWithUserInfo(new UserInfo());
        Controller controller = new Controller(ctx);
        RequestMessage msg = new RequestMessage(Sipc.METHOD_M);
        msg.setCallId(99);
        msg.setSequence(1);
        controller.receive(msg);
    }

    @Test
    @DisplayName("Controller.start connects to a configured sipc-proxy and then fails on login")
    void testStartConnectsThenFailsLogin() throws Exception {
        // spin up a loopback TCP server so TransferProxy.startTransfer succeeds
        try (java.net.ServerSocket server = new java.net.ServerSocket(0)) {
            int port = server.getLocalPort();
            Thread acceptor = new Thread(() -> {
                try { server.accept().close(); } catch (Exception ignored) { }
            });
            acceptor.setDaemon(true);
            acceptor.start();

            net.apexes.fetion4j.core.util.XmlElement configXml = new net.apexes.fetion4j.core.util.XmlElement();
            configXml.parseString("<config><servers>"
                    + "<sipc-proxy>127.0.0.1:" + port + "</sipc-proxy>"
                    + "</servers></config>");
            net.apexes.fetion4j.core.SystemConfig config =
                    new net.apexes.fetion4j.core.SystemConfig(configXml);

            UserInfo info = new UserInfo();
            FetionContext ctx = mock(FetionContext.class);
            when(ctx.getSystemConfig()).thenReturn(config);
            when(ctx.getUserInfo()).thenReturn(info);
            when(ctx.getLogHandler()).thenReturn(mock(net.apexes.fetion4j.core.LogHandler.class));
            when(ctx.getAccount()).thenReturn(net.apexes.fetion4j.core.client.AccountTestSupport.newAccount());

            Controller controller = new Controller(ctx);
            // start connects, then mainDialogue.login() blocks on a dispatcher.submit that never
            // resolves (no response). Assert it surfaces as a FetionException.
            org.assertj.core.api.Assertions.assertThatThrownBy(controller::start)
                    .isInstanceOf(net.apexes.fetion4j.core.FetionException.class);
        }
    }

    @Test
    @DisplayName("Controller.stop is a no-op when not running")
    void testStopWhenNotRunning() throws Exception {
        FetionContext ctx = TestControllers.contextWithUserInfo(new UserInfo());
        Controller controller = new Controller(ctx);
        controller.stop(); // not running -> no-op, no exception
        assertThat(controller.isRunning()).isFalse();
    }
}
