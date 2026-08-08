package net.apexes.fetion4j.core.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link Base64}.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
@DisplayName("Base64 Tests")
class Base64Test {

    @Test
    @DisplayName("encodeBase64 + decodeBase64 round-trip arbitrary bytes")
    void testEncodeDecodeRoundTrip() {
        byte[] data = "Hello, Fetion! 中文".getBytes(StandardCharsets.UTF_8);
        byte[] encoded = Base64.encodeBase64(data);
        assertThat(Base64.decodeBase64(encoded)).containsExactly(data);
    }

    @Test
    @DisplayName("encodeBase64 of empty input returns empty array")
    void testEncodeEmpty() {
        assertThat(Base64.encodeBase64(new byte[0])).isEmpty();
    }

    @Test
    @DisplayName("encodeBase64(data, isChunked=true) appends a line break separator")
    void testEncodeChunked() {
        byte[] data = "1234567890123456789012345678901234567890".getBytes(StandardCharsets.UTF_8);
        byte[] chunked = Base64.encodeBase64Chunked(data);
        // chunked output ends with a CRLF chunk separator
        assertThat(new String(chunked, StandardCharsets.UTF_8)).endsWith("\r\n");
    }

    @Test
    @DisplayName("encodeBase64(data, true) equals encodeBase64Chunked")
    void testEncodeIsChunkedFlag() {
        byte[] data = "abcdefghij".getBytes(StandardCharsets.UTF_8);
        assertThat(Base64.encodeBase64(data, true)).isEqualTo(Base64.encodeBase64Chunked(data));
    }

    @Test
    @DisplayName("encodeBase64(1 byte) produces 4-char padding \"XX==\"")
    void testEncodeOneByte() {
        byte[] enc = Base64.encodeBase64(new byte[] { 'M' });
        assertThat(new String(enc, StandardCharsets.UTF_8)).isEqualTo("TQ==");
    }

    @Test
    @DisplayName("encodeBase64(2 bytes) produces 4-char \"XXX=\"")
    void testEncodeTwoBytes() {
        byte[] enc = Base64.encodeBase64(new byte[] { 'M', 'a' });
        assertThat(new String(enc, StandardCharsets.UTF_8)).isEqualTo("TWE=");
    }

    @Test
    @DisplayName("decodeBase64 of valid base64 whitespace bytes returns empty data")
    void testDecodeNonBase64() {
        // whitespace bytes are discarded by discardNonBase64, yielding an empty decode
        byte[] result = Base64.decodeBase64(new byte[] { ' ', '\n', '\t' });
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("isArrayByteBase64 detects valid base64 bytes")
    void testIsArrayByteBase64() {
        assertThat(Base64.isArrayByteBase64("TWE=".getBytes(StandardCharsets.UTF_8))).isTrue();
    }

    @Test
    @DisplayName("decode(Object) returns a byte[] for byte[] input")
    void testDecodeObject() {
        byte[] enc = Base64.encodeBase64("hi".getBytes(StandardCharsets.UTF_8));
        Object out = new Base64().decode((Object) enc);
        assertThat(out).isInstanceOf(byte[].class);
        assertThat((byte[]) out).containsExactly("hi".getBytes(StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("decode(Object) throws InvalidParameterException for a non byte-array object")
    void testDecodeObjectInvalid() {
        assertThatThrownBy(() -> new Base64().decode("not bytes"))
                .isInstanceOf(java.security.InvalidParameterException.class);
    }

    @Test
    @DisplayName("decode(byte[]) returns decoded bytes")
    void testDecodeByteArray() {
        byte[] enc = Base64.encodeBase64("xyz".getBytes(StandardCharsets.UTF_8));
        assertThat(new Base64().decode(enc)).containsExactly("xyz".getBytes(StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("encode(Object) returns base64 bytes for byte[] input")
    void testEncodeObject() {
        Object out = new Base64().encode((Object) "hi".getBytes(StandardCharsets.UTF_8));
        assertThat(out).isInstanceOf(byte[].class);
        assertThat(new String((byte[]) out, StandardCharsets.UTF_8)).isEqualTo("aGk=");
    }

    @Test
    @DisplayName("encode(byte[]) returns base64 bytes")
    void testEncodeByteArray() {
        byte[] out = new Base64().encode("hi".getBytes(StandardCharsets.UTF_8));
        assertThat(new String(out, StandardCharsets.UTF_8)).isEqualTo("aGk=");
    }

    @Test
    @DisplayName("encode(Object) throws InvalidParameterException for a non byte-array object")
    void testEncodeObjectInvalid() {
        assertThatThrownBy(() -> new Base64().encode("not bytes"))
                .isInstanceOf(java.security.InvalidParameterException.class);
    }

    @Test
    @DisplayName("decodeBase64 of a valid padded block returns the decoded bytes")
    void testDecodeMalformedChunk() {
        byte[] r = Base64.decodeBase64("TQ==".getBytes(StandardCharsets.UTF_8));
        assertThat(r).containsExactly((byte) 'M');
    }

    @Test
    @DisplayName("encodeBase64 + decodeBase64 round-trip large random data")
    void testRoundTripLarge() {
        byte[] data = new byte[1024];
        new java.util.Random(42L).nextBytes(data);
        assertThat(Base64.decodeBase64(Base64.encodeBase64(data))).containsExactly(data);
    }
}
