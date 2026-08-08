package net.apexes.fetion4j.core.sipc;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the SIPC message types ({@link RequestMessage},
 * {@link ResponseMessage}, {@link SipcMessageWriter}).
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
@DisplayName("SipcMessage Tests")
class SipcMessageTest {

    @Test
    @DisplayName("RequestMessage uses default acceptor fetion.com.cn")
    void testRequestDefaultAcceptor() {
        RequestMessage req = new RequestMessage(Sipc.METHOD_R);
        assertThat(req.getAcceptor()).isEqualTo("fetion.com.cn");
        assertThat(req.getMethod()).isEqualTo(Sipc.METHOD_R);
    }

    @Test
    @DisplayName("RequestMessage accepts a custom acceptor")
    void testRequestCustomAcceptor() {
        RequestMessage req = new RequestMessage(Sipc.METHOD_M, "example.com");
        assertThat(req.getAcceptor()).isEqualTo("example.com");
    }

    @Test
    @DisplayName("RequestMessage headline is METHOD acceptor SIPC_VERSION")
    void testRequestHeadline() {
        RequestMessage req = new RequestMessage(Sipc.METHOD_R, "fetion.com.cn");
        String text = req.getText();
        assertThat(text).startsWith("R fetion.com.cn " + Sipc.SIPC_VERSION);
    }

    @Test
    @DisplayName("addField/hasField/getFieldValue/setField/removeField behave correctly")
    void testFieldOperations() {
        RequestMessage req = new RequestMessage(Sipc.METHOD_R);
        req.setCallId(1);
        req.setSequence(1);

        req.addField(Sipc.FIELD_F, "847570000");
        assertThat(req.hasField(Sipc.FIELD_F)).isTrue();
        assertThat(req.getFieldValue(Sipc.FIELD_F)).isEqualTo("847570000");

        // setField replaces all existing fields of same name
        req.addField(Sipc.FIELD_F, "999");
        req.setField(Sipc.FIELD_F, "111");
        assertThat(req.getFieldValues(Sipc.FIELD_F)).containsExactly("111");

        // removeField removes the first matching field
        req.removeField(Sipc.FIELD_F);
        assertThat(req.hasField(Sipc.FIELD_F)).isFalse();

        // getFieldValue returns null when field absent
        assertThat(req.getFieldValue(Sipc.FIELD_F)).isNull();
    }

    @Test
    @DisplayName("getFieldValues returns all values for a repeated field name")
    void testGetFieldValuesMultiple() {
        RequestMessage req = new RequestMessage(Sipc.METHOD_R);
        req.addField(Sipc.FIELD_K, "1");
        req.addField(Sipc.FIELD_K, "2");
        List<String> values = req.getFieldValues(Sipc.FIELD_K);
        assertThat(values).containsExactly("1", "2");
    }

    @Test
    @DisplayName("removeAllField strips every field of the given name")
    void testRemoveAllField() {
        RequestMessage req = new RequestMessage(Sipc.METHOD_R);
        req.addField("X", "1");
        req.addField("X", "2");
        req.addField("X", "3");
        req.removeAllField("X");
        assertThat(req.hasField("X")).isFalse();
    }

    @Test
    @DisplayName("removeField is a no-op when the field is absent")
    void testRemoveFieldNoOp() {
        RequestMessage req = new RequestMessage(Sipc.METHOD_R);
        req.removeField("missing"); // should not throw
        assertThat(req.hasField("missing")).isFalse();
    }

    @Test
    @DisplayName("getBodyLength is 0 when body is null")
    void testBodyLengthNullBody() {
        assertThat(new RequestMessage(Sipc.METHOD_R).getBodyLength()).isZero();
    }

    @Test
    @DisplayName("getText includes L header and body when body is set")
    void testTextWithBody() {
        RequestMessage req = new RequestMessage(Sipc.METHOD_M);
        req.setCallId(7);
        req.setSequence(3);
        req.setBody("hello body");
        String text = req.getText();
        assertThat(text)
                .contains("L: 10")
                .contains(SipcMessage.SEPARATOR + SipcMessage.SEPARATOR + "hello body");
    }

    @Test
    @DisplayName("getText appends slice offset when set and body present")
    void testTextWithSliceOffset() {
        RequestMessage req = new RequestMessage(Sipc.METHOD_M);
        req.setCallId(1);
        req.setSequence(1);
        req.setBody("body");
        req.setSliceOffset(12);
        String text = req.getText();
        assertThat(text).contains("L: 4;p=12");
        assertThat(req.getSliceOffset()).isEqualTo(12);
    }

    @Test
    @DisplayName("getText ends with single separator when no body")
    void testTextNoBodyEnds() {
        RequestMessage req = new RequestMessage(Sipc.METHOD_R);
        req.setCallId(1);
        req.setSequence(1);
        String text = req.getText();
        assertThat(text).endsWith(SipcMessage.SEPARATOR);
        assertThat(text).doesNotContain("L: ");
    }

    @Test
    @DisplayName("toString returns getText()")
    void testToString() {
        RequestMessage req = new RequestMessage(Sipc.METHOD_R);
        req.setCallId(2);
        req.setSequence(2);
        assertThat(req.toString()).isEqualTo(req.getText());
    }

    @Test
    @DisplayName("ResponseMessage headline and accessors")
    void testResponseMessage() {
        ResponseMessage resp = new ResponseMessage(Sipc.STATUS_ACTION_OK, "OK");
        resp.setCallId(5);
        resp.setSequence(2);
        assertThat(resp.getStatus()).isEqualTo(Sipc.STATUS_ACTION_OK);
        assertThat(resp.getStatusMessage()).isEqualTo("OK");
        assertThat(resp.getHeadline()).isEqualTo(Sipc.SIPC_VERSION + " 200 OK");
        assertThat(resp.getText()).startsWith(Sipc.SIPC_VERSION + " 200 OK");

        RequestMessage req = new RequestMessage(Sipc.METHOD_R);
        resp.setRequestMessage(req);
        assertThat(resp.getRequestMessage()).isSameAs(req);
    }

    @Test
    @DisplayName("setMethod changes method on an existing message")
    void testSetMethod() {
        RequestMessage req = new RequestMessage(Sipc.METHOD_R);
        req.setMethod(Sipc.METHOD_M);
        assertThat(req.getMethod()).isEqualTo(Sipc.METHOD_M);
    }

    @Test
    @DisplayName("SipcMessageWriter writes message text bytes and closes the stream")
    void testWriter() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        SipcMessageWriter writer = new SipcMessageWriter(out);
        RequestMessage req = new RequestMessage(Sipc.METHOD_R);
        req.setCallId(1);
        req.setSequence(1);
        writer.write(req);
        assertThat(out.toByteArray()).isEqualTo(req.getText().getBytes());
        writer.close();
    }

    @Test
    @DisplayName("Sipc constants are non-null and well-formed")
    void testSipcConstants() {
        assertThat(Sipc.SIPC_VERSION).isEqualTo("SIP-C/4.0");
        assertThat(Sipc.METHOD_R).isEqualTo("R");
        assertThat(Sipc.STATUS_ACTION_OK).isEqualTo(200);
        assertThat(Sipc.FIELD_I).isEqualTo("I");
    }
}
