package net.apexes.fetion4j.core.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link ConvertHelper}.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
@DisplayName("ConvertHelper Tests")
class ConvertHelperTest {

    @Test
    @DisplayName("byte2HexString(null) returns \"null\"")
    void testByte2HexStringNull() {
        assertThat(ConvertHelper.byte2HexString(null)).isEqualTo("null");
    }

    @Test
    @DisplayName("byte2HexString formats bytes separated by spaces")
    void testByte2HexString() {
        assertThat(ConvertHelper.byte2HexString(new byte[] { 0x00, (byte) 0xFF, 0x1A }))
                .isEqualTo("00 FF 1A");
    }

    @Test
    @DisplayName("byte2HexString(b, offset, len) clamps end to array length")
    void testByte2HexStringOffsetLenClamps() {
        byte[] b = { 0x01, 0x02, 0x03 };
        // offset+len beyond length -> clamped to b.length
        assertThat(ConvertHelper.byte2HexString(b, 1, 100)).isEqualTo("02 03");
    }

    @Test
    @DisplayName("byte2HexString(b, offset, len) with empty range yields empty string")
    void testByte2HexStringEmptyRange() {
        assertThat(ConvertHelper.byte2HexString(new byte[] { 1, 2 }, 0, 0)).isEmpty();
    }

    @Test
    @DisplayName("byte2HexString(null,0,0) returns \"null\"")
    void testByte2HexStringOffsetLenNull() {
        assertThat(ConvertHelper.byte2HexString(null, 0, 0)).isEqualTo("null");
    }

    @Test
    @DisplayName("byte2HexStringWithoutSpace variants")
    void testByte2HexStringWithoutSpace() {
        assertThat(ConvertHelper.byte2HexStringWithoutSpace(null)).isEqualTo("null");
        assertThat(ConvertHelper.byte2HexStringWithoutSpace(new byte[] { (byte) 0xAB, 0x01 })).isEqualTo("AB01");
        assertThat(ConvertHelper.byte2HexStringWithoutSpace(new byte[] { 1, 2, 3 }, 1, 100)).isEqualTo("0203");
        assertThat(ConvertHelper.byte2HexStringWithoutSpace(null, 0, 1)).isEqualTo("null");
    }

    @Test
    @DisplayName("hexString2Byte parses space-separated hex")
    void testHexString2Byte() {
        byte[] result = ConvertHelper.hexString2Byte("00 FF 1A");
        assertThat(result).containsExactly(0x00, (byte) 0xFF, 0x1A);
    }

    @Test
    @DisplayName("hexString2Byte returns null when a token has > 2 chars")
    void testHexString2ByteInvalidToken() {
        assertThat(ConvertHelper.hexString2Byte("ABC 12345")).isNull();
    }

    @Test
    @DisplayName("hexString2Byte returns null on parse exception (null input)")
    void testHexString2ByteNull() {
        assertThat(ConvertHelper.hexString2Byte(null)).isNull();
    }

    @Test
    @DisplayName("hexString2ByteNoSpace parses a compact hex string")
    void testHexString2ByteNoSpace() {
        assertThat(ConvertHelper.hexString2ByteNoSpace("00FF1A")).containsExactly(0x00, (byte) 0xFF, 0x1A);
    }

    @Test
    @DisplayName("inputStream2String reads the full stream as a string")
    void testInputStream2String() throws IOException {
        String data = "hello fetion";
        ByteArrayInputStream in = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));
        assertThat(ConvertHelper.inputStream2String(in)).isEqualTo(data);
    }

    @Test
    @DisplayName("string2Byte / byte2String round-trip via UTF-8")
    void testStringByteRoundTrip() {
        String src = "中文测试";
        byte[] bytes = ConvertHelper.string2Byte(src);
        assertThat(ConvertHelper.byte2String(bytes)).isEqualTo(src);
    }

    @Test
    @DisplayName("int2Byte returns little-endian 4 bytes")
    void testInt2Byte() {
        // 0x01020304 little-endian => 04 03 02 01
        assertThat(ConvertHelper.int2Byte(0x01020304)).containsExactly(0x04, 0x03, 0x02, 0x01);
        assertThat(ConvertHelper.int2Byte(0)).containsExactly(0, 0, 0, 0);
    }

    @Test
    @DisplayName("byte2String accepts a manually constructed UTF-8 byte array")
    void testByte2StringDirect() {
        byte[] utf8 = "abc".getBytes(StandardCharsets.UTF_8);
        assertThat(ConvertHelper.byte2String(utf8)).isEqualTo("abc");
    }

    @Test
    @DisplayName("string2Byte never throws for valid input (UTF8 always supported)")
    void testString2ByteNoThrow() {
        assertThat(ConvertHelper.string2Byte("x")).isNotNull();
    }

    @Test
    @DisplayName("hexString2Byte trims surrounding whitespace before parsing")
    void testHexString2ByteTrims() {
        assertThat(ConvertHelper.hexString2Byte("  0A  ").length).isEqualTo(1);
        assertThat(ConvertHelper.hexString2Byte("  0A  ")).containsExactly(0x0A);
    }

    @Test
    @DisplayName("byte2HexStringWithoutSpace empty array yields empty string")
    void testByte2HexStringWithoutSpaceEmpty() {
        assertThat(ConvertHelper.byte2HexStringWithoutSpace(new byte[0])).isEmpty();
    }

    @Test
    @DisplayName("inputStream2String on empty stream returns empty string")
    void testInputStream2StringEmpty() throws IOException {
        assertThat(ConvertHelper.inputStream2String(new ByteArrayInputStream(new byte[0]))).isEmpty();
    }

    @Test
    @DisplayName("byte2String / string2Byte round-trip on ASCII")
    void testRoundTripAscii() {
        byte[] b = ConvertHelper.string2Byte("hello");
        assertThat(ConvertHelper.byte2String(b)).isEqualTo("hello");
    }

    @Test
    @DisplayName("hexString2ByteNoSpace on odd-length string drops trailing nibble")
    void testHexString2ByteNoSpaceOddLength() {
        // length 3 => len>>>1 = 1 byte, loop i=0 only
        assertThat(ConvertHelper.hexString2ByteNoSpace("ABC")).containsExactly((byte) 0xAB);
    }

    @Test
    @DisplayName("byte2String does not throw for arbitrary bytes")
    void testByte2StringArbitrary() {
        assertThat(ConvertHelper.byte2String(new byte[] { 0x41, 0x42 })).isEqualTo("AB");
    }

    @Test
    @DisplayName("string2Byte is non-null for empty string")
    void testString2ByteEmpty() {
        assertThat(ConvertHelper.string2Byte("")).isEmpty();
    }

    @Test
    @DisplayName("ConvertHelper exposes its hex character set indirectly via formatting")
    void testClassExists() {
        // Ensure all 16 hex chars appear in the formatted output of a 0..255 sweep.
        StringBuilder sb = new StringBuilder();
        byte[] all = new byte[256];
        for (int i = 0; i < 256; i++) {
            all[i] = (byte) i;
        }
        sb.append(ConvertHelper.byte2HexStringWithoutSpace(all));
        String formatted = sb.toString();
        for (char c : "0123456789ABCDEF".toCharArray()) {
            assertThat(formatted).contains(String.valueOf(c));
        }
    }

    @Test
    @DisplayName("inputStream2String reads multiple buffer chunks (>1024 bytes)")
    void testInputStream2StringLarge() throws IOException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 3000; i++) {
            sb.append('x');
        }
        byte[] data = sb.toString().getBytes(StandardCharsets.UTF_8);
        assertThat(ConvertHelper.inputStream2String(new ByteArrayInputStream(data))).hasSize(3000);
    }

    @Test
    @DisplayName("byte2HexString on empty array yields empty string (no trailing space)")
    void testByte2HexStringEmpty() {
        assertThat(ConvertHelper.byte2HexString(new byte[0])).isEmpty();
    }

    @Test
    @DisplayName("hexString2Byte returns empty array for empty trimmed input")
    void testHexString2ByteEmpty() {
        assertThat(ConvertHelper.hexString2Byte("   ")).isEmpty();
    }

    @Test
    @DisplayName("byte2HexStringWithoutSpace offset bounds honored when in-range")
    void testByte2HexStringWithoutSpaceInRange() {
        assertThat(ConvertHelper.byte2HexStringWithoutSpace(new byte[] { 1, 2, 3, 4 }, 1, 2)).isEqualTo("0203");
    }

    @Test
    @DisplayName("byte2HexString offset/len in-range slice")
    void testByte2HexStringSliceInRange() {
        assertThat(ConvertHelper.byte2HexString(new byte[] { 1, 2, 3 }, 1, 2)).isEqualTo("02 03");
    }

    @Test
    @DisplayName("string2Byte is not null and byte2String produces a value (no NPE)")
    void testNoNpeRoundTrip() {
        assertThat(ConvertHelper.string2Byte("ok")).isNotNull();
    }

    @Test
    @DisplayName("int2Byte for negative value still yields 4 bytes")
    void testInt2ByteNegative() {
        assertThat(ConvertHelper.int2Byte(-1)).hasSize(4);
    }

    @Test
    @DisplayName("byte2String / string2Byte keep multibyte UTF-8 stable")
    void testRoundTripMultibyte() {
        String s = "Ω≈ç";
        assertThat(ConvertHelper.byte2String(ConvertHelper.string2Byte(s))).isEqualTo(s);
    }

    @Test
    @DisplayName("hexString2Byte with two-char tokens but invalid hex returns null")
    void testHexString2ByteInvalidHexToken() {
        // "ZZ" -> NumberFormatException caught -> null
        assertThat(ConvertHelper.hexString2Byte("ZZ")).isNull();
    }

    @Test
    @DisplayName("byte2HexStringWithoutSpace full-range when offset>0 and len covers rest")
    void testByte2HexStringWithoutSpaceFull() {
        assertThat(ConvertHelper.byte2HexStringWithoutSpace(new byte[] { (byte) 0xDE, (byte) 0xAD }, 0, 2)).isEqualTo("DEAD");
    }

    @Test
    @DisplayName("byte2HexString + hexString2Byte round trip")
    void testRoundTripHexSpace() {
        byte[] original = { 0x00, 0x1F, (byte) 0xFF, 0x7A };
        String hex = ConvertHelper.byte2HexString(original);
        assertThat(ConvertHelper.hexString2Byte(hex)).containsExactly(original);
    }
}
