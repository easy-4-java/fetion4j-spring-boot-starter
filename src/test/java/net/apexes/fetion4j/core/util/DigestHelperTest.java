package net.apexes.fetion4j.core.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link DigestHelper}.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
@DisplayName("DigestHelper Tests")
class DigestHelperTest {

    @Test
    @DisplayName("MD5 of empty input equals the well-known MD5 of empty string")
    void testMd5Empty() {
        byte[] md5 = DigestHelper.MD5(new byte[0]);
        assertThat(ConvertHelper.byte2HexStringWithoutSpace(md5)).isEqualTo("D41D8CD98F00B204E9800998ECF8427E");
    }

    @Test
    @DisplayName("MD5 of \"abc\" matches the known MD5 digest")
    void testMd5Abc() {
        byte[] md5 = DigestHelper.MD5("abc".getBytes(StandardCharsets.ISO_8859_1));
        assertThat(ConvertHelper.byte2HexStringWithoutSpace(md5)).isEqualTo("900150983CD24FB0D6963F7D28E17F72");
    }

    @Test
    @DisplayName("SHA1 of \"abc\" matches the known SHA-1 digest")
    void testSha1Abc() {
        byte[] sha = DigestHelper.SHA1("abc".getBytes(StandardCharsets.ISO_8859_1));
        assertThat(ConvertHelper.byte2HexStringWithoutSpace(sha))
                .isEqualTo("A9993E364706816ABA3E25717850C26C9CD0D89D");
    }

    @Test
    @DisplayName("SHA1 of empty input is the well-known SHA-1 of empty string")
    void testSha1Empty() {
        byte[] sha = DigestHelper.SHA1(new byte[0]);
        assertThat(ConvertHelper.byte2HexStringWithoutSpace(sha))
                .isEqualTo("DA39A3EE5E6B4B0D3255BFEF95601890AFD80709");
    }

    @Test
    @DisplayName("createAESKey returns a 32-byte (256-bit) key")
    void testCreateAesKeyLength() {
        byte[] key = DigestHelper.createAESKey();
        assertThat(key).hasSize(32);
    }

    @Test
    @DisplayName("createAESKey returns distinct keys across calls (random)")
    void testCreateAesKeyRandom() {
        byte[] a = DigestHelper.createAESKey();
        byte[] b = DigestHelper.createAESKey();
        assertThat(a).isNotEqualTo(b);
    }

    @Test
    @DisplayName("MD5 is stable for repeated calls")
    void testMd5Stable() {
        byte[] data = "fetion".getBytes(StandardCharsets.ISO_8859_1);
        byte[] d1 = DigestHelper.MD5(data);
        byte[] d2 = DigestHelper.MD5(data);
        assertThat(d1).isEqualTo(d2);
    }
}
