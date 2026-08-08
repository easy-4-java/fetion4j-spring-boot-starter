package net.apexes.fetion4j.core.client.activity;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpServer;

import net.apexes.fetion4j.core.AuthSupportable;
import net.apexes.fetion4j.core.Captcha;
import net.apexes.fetion4j.core.SystemConfig;
import net.apexes.fetion4j.core.UserInfo;
import net.apexes.fetion4j.core.client.Controller;
import net.apexes.fetion4j.core.sipc.ResponseMessage;
import net.apexes.fetion4j.core.sipc.Sipc;
import net.apexes.fetion4j.core.user.Contact;
import net.apexes.fetion4j.core.util.XmlElement;

/**
 * Unit tests covering the {@link net.apexes.fetion4j.core.client.Activity#verify} captcha loop.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
@DisplayName("Activity Verify Tests")
class ActivityVerifyTest {

    private HttpServer server;
    private int port;

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        port = server.getAddress().getPort();
        server.createContext("/", exchange -> {
            byte[] data = "<results><pic-certificate id=\"pid\" pic=\"aGk=\" /></results>"
                    .getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, data.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(data);
            }
        });
        server.start();
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    @Test
    @DisplayName("submit re-submits after a captcha challenge and returns the final response")
    void testSubmitWithCaptcha() throws Exception {
        XmlElement configXml = new XmlElement();
        configXml.parseString("<config><servers>"
                + "<get-pic-code>http://127.0.0.1:" + port + "/getpic</get-pic-code>"
                + "</servers></config>");
        SystemConfig config = new SystemConfig(configXml);
        UserInfo info = new UserInfo();
        info.setContact(new Contact("0"));
        net.apexes.fetion4j.core.client.FetionContext ctx =
                net.apexes.fetion4j.core.client.TestControllers.contextWithUserInfo(info);
        org.mockito.Mockito.when(ctx.getSystemConfig()).thenReturn(config);
        org.mockito.Mockito.when(ctx.getAuthSupportable()).thenReturn(new AuthSupportable() {
            @Override
            public void needAuth(Captcha captcha, net.apexes.fetion4j.core.AuthFeedback feedback) {
                feedback.submit("1234");
            }
        });

        ResponseMessage needVerify = new ResponseMessage(Sipc.STATUS_EXTENSION_REQUIRED, "Need");
        needVerify.setField(Sipc.FIELD_W, "Verify algorithm=\"algo\",type=\"GeneralPic\"");
        needVerify.setBody("<results><reason text=\"\" tips=\"\"/></results>");
        ResponseMessage ok = new ResponseMessage(Sipc.STATUS_ACTION_OK, "OK");
        Controller controller = net.apexes.fetion4j.core.client.TestControllers
                .controllerWithVerifyFlow(needVerify, ok);

        AddBuddyActivity activity = new AddBuddyActivity(ctx, controller, 1);
        net.apexes.fetion4j.core.Result result = activity.addBuddy(1, "n", null, "d", 0);
        assertThat(result.getType()).isEqualTo(net.apexes.fetion4j.core.Result.Type.SUCCESS);
    }
}
