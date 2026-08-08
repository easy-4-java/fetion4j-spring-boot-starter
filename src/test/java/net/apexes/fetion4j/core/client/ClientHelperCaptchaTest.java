package net.apexes.fetion4j.core.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import net.apexes.fetion4j.core.SystemConfig;
import net.apexes.fetion4j.core.util.XmlElement;

/**
 * Unit tests for {@link ClientHelper#getCaptcha} using a loopback HTTP server.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
@DisplayName("ClientHelper Captcha Tests")
class ClientHelperCaptchaTest {

    private HttpServer server;
    private int port;

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        port = server.getAddress().getPort();
        server.start();
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    private void enqueue(String response) {
        server.createContext("/", exchange -> respond(exchange, 200, response));
    }

    private static void respond(HttpExchange exchange, int status, String body) throws IOException {
        byte[] data = body.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, data.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(data);
        }
    }

    private FetionContext contextWithGetPicCode() throws Exception {
        XmlElement configXml = new XmlElement();
        configXml.parseString("<config><servers>"
                + "<get-pic-code>http://127.0.0.1:" + port + "/getpic</get-pic-code>"
                + "</servers></config>");
        SystemConfig config = new SystemConfig(configXml);
        FetionContext ctx = mock(FetionContext.class);
        when(ctx.getSystemConfig()).thenReturn(config);
        return ctx;
    }

    @Test
    @DisplayName("getCaptcha returns a CaptchaImpl when the server responds with a pic-certificate")
    void testGetCaptchaSuccess() throws Exception {
        // pic data must be valid base64 to satisfy Base64.decodeBase64
        enqueue("<results><pic-certificate id=\"pid\" pic=\"aGk=\" /></results>");
        FetionContext ctx = contextWithGetPicCode();
        CaptchaImpl captcha = ClientHelper.getCaptcha(ctx, "algo", "GeneralPic", "text", "tips");
        assertThat(captcha).isNotNull();
        assertThat(captcha.getImageId()).isEqualTo("pid");
        assertThat(captcha.getVerifyAlgorithm()).isEqualTo("algo");
    }

    @Test
    @DisplayName("getCaptcha throws IOException when the server returns a non-OK status")
    void testGetCaptchaNonOk() throws Exception {
        server.createContext("/", exchange -> respond(exchange, 500, "err"));
        FetionContext ctx = contextWithGetPicCode();
        assertThatThrownBy(() -> ClientHelper.getCaptcha(ctx, "a", "t", "x", "tips"))
                .isInstanceOf(IOException.class);
    }

    @Test
    @DisplayName("getCaptcha(context, response) parses the W header and body reason")
    void testGetCaptchaFromResponse() throws Exception {
        enqueue("<results><pic-certificate id=\"pid2\" pic=\"aGk=\" /></results>");
        net.apexes.fetion4j.core.sipc.ResponseMessage response =
                new net.apexes.fetion4j.core.sipc.ResponseMessage(200, "OK");
        response.setField(net.apexes.fetion4j.core.sipc.Sipc.FIELD_W,
                "Verify algorithm=\"algo\",type=\"GeneralPic\"");
        response.setBody("<results><reason text=\"t\" tips=\"x\"/></results>");
        FetionContext ctx = contextWithGetPicCode();
        CaptchaImpl captcha = ClientHelper.getCaptcha(ctx, response);
        assertThat(captcha.getImageId()).isEqualTo("pid2");
    }
}
