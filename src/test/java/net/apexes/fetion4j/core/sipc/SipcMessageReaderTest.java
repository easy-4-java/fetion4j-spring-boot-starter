package net.apexes.fetion4j.core.sipc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link SipcMessageReader}.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
@DisplayName("SipcMessageReader Tests")
class SipcMessageReaderTest {

    private static SipcMessageReader readerOf(String text) throws IOException {
        // DataInputStream.readLine uses \n as line terminator, so feed the text as-is.
        return new SipcMessageReader(new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    @DisplayName("read returns null at end of stream")
    void testReadReturnsNullAtEof() throws IOException, ParseException {
        SipcMessageReader reader = readerOf("");
        assertThat(reader.read()).isNull();
        reader.close();
    }

    @Test
    @DisplayName("read parses a request message headline and headers")
    void testReadRequest() throws IOException, ParseException {
        String text = "R fetion.com.cn " + Sipc.SIPC_VERSION + "\n"
                + "I: 9\n"
                + "Q: 2 R\n"
                + "F: 12345\n"
                + "\n";
        SipcMessageReader reader = readerOf(text);
        SipcMessage msg = reader.read();
        assertThat(msg).isInstanceOf(RequestMessage.class);
        assertThat(((RequestMessage) msg).getAcceptor()).isEqualTo("fetion.com.cn");
        assertThat(msg.getMethod()).isEqualTo("R");
        assertThat(msg.getCallId()).isEqualTo(9);
        assertThat(msg.getSequence()).isEqualTo(2);
        assertThat(msg.getFieldValue(Sipc.FIELD_F)).isEqualTo("12345");
    }

    @Test
    @DisplayName("read parses a response message with a plain body")
    void testReadResponseWithBody() throws IOException, ParseException {
        String body = "<results/>";
        String text = Sipc.SIPC_VERSION + " 200 OK\n"
                + "I: 1\n"
                + "Q: 1 R\n"
                + "L: " + body.length() + "\n"
                + "\n"
                + body;
        SipcMessageReader reader = readerOf(text);
        SipcMessage msg = reader.read();
        assertThat(msg).isInstanceOf(ResponseMessage.class);
        ResponseMessage resp = (ResponseMessage) msg;
        assertThat(resp.getStatus()).isEqualTo(200);
        assertThat(resp.getStatusMessage()).isEqualTo("OK");
        assertThat(msg.getBody()).isEqualTo(body);
    }

    @Test
    @DisplayName("read parses a response with a plain L header (no slice offset)")
    void testReadResponsePlainBody() throws IOException, ParseException {
        String body = "BODY";
        String text = Sipc.SIPC_VERSION + " 200 OK\n"
                + "I: 1\n"
                + "Q: 1 R\n"
                + "L: " + body.length() + "\n"
                + "\n"
                + body;
        SipcMessageReader reader = readerOf(text);
        SipcMessage msg = reader.read();
        assertThat(msg.getBody()).isEqualTo(body);
        assertThat(msg.getSliceOffset()).isEqualTo(-1);
    }

    @Test
    @DisplayName("read throws ParseException when response status is not an integer")
    void testReadResponseBadStatus() throws IOException {
        String text = Sipc.SIPC_VERSION + " NOTANUMBER OK\n\n";
        SipcMessageReader reader = readerOf(text);
        assertThatThrownBy(reader::read).isInstanceOf(ParseException.class);
    }

    @Test
    @DisplayName("read throws ParseException when Q sequence is not an integer")
    void testReadBadSequence() throws IOException {
        String text = "R fetion.com.cn " + Sipc.SIPC_VERSION + "\n"
                + "I: 1\n"
                + "Q: NOTANUMBER R\n"
                + "\n";
        SipcMessageReader reader = readerOf(text);
        assertThatThrownBy(reader::read).isInstanceOf(ParseException.class);
    }

    @Test
    @DisplayName("read returns null for an unrecognised headline")
    void testReadUnrecognisedHeadline() throws IOException, ParseException {
        SipcMessageReader reader = readerOf("GARBAGE LINE\n\n");
        assertThat(reader.read()).isNull();
    }

    @Test
    @DisplayName("close delegates to the underlying stream (no exception)")
    void testClose() throws IOException {
        SipcMessageReader reader = readerOf("");
        reader.close();
    }
}
