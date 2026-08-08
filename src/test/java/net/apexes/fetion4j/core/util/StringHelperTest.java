package net.apexes.fetion4j.core.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link StringHelper}.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
@DisplayName("StringHelper Tests")
class StringHelperTest {

    @Test
    @DisplayName("qouteHtmlSpecialChars(null) returns null")
    void testQouteNull() {
        assertThat(StringHelper.qouteHtmlSpecialChars(null)).isNull();
    }

    @Test
    @DisplayName("qouteHtmlSpecialChars escapes &, \", ', <, >")
    void testQoute() {
        assertThat(StringHelper.qouteHtmlSpecialChars("a & b < c > d \" e ' f"))
                .isEqualTo("a &amp; b &lt; c &gt; d &quot; e &apos; f");
    }

    @Test
    @DisplayName("unqouteHtmlSpecialChars(null) returns null")
    void testUnqouteNull() {
        assertThat(StringHelper.unqouteHtmlSpecialChars(null)).isNull();
    }

    @Test
    @DisplayName("unqouteHtmlSpecialChars reverses entities and &nbsp;")
    void testUnqoute() {
        assertThat(StringHelper.unqouteHtmlSpecialChars("a&amp;b&lt;c&gt;d&quot;e&apos;f&nbsp;g"))
                .isEqualTo("a&b<c>d\"e'f g");
    }

    @Test
    @DisplayName("stripHtmlSpecialChars(null) returns null")
    void testStripNull() {
        assertThat(StringHelper.stripHtmlSpecialChars(null)).isNull();
    }

    @Test
    @DisplayName("stripHtmlSpecialChars removes tags and &nbsp; -> space")
    void testStrip() {
        // &nbsp; is replaced by a space character
        assertThat(StringHelper.stripHtmlSpecialChars("<b>hi</b>&nbsp;there")).isEqualTo("hi there");
        assertThat(StringHelper.stripHtmlSpecialChars("<br/>x")).isEqualTo("x");
    }

    @Test
    @DisplayName("format replaces {n} placeholders in order")
    void testFormat() {
        assertThat(StringHelper.format("{0} is {1}", "apple", "fruit")).isEqualTo("apple is fruit");
    }

    @Test
    @DisplayName("urlEncode percent-encodes special characters")
    void testUrlEncode() {
        assertThat(StringHelper.urlEncode("a b&c")).isEqualTo("a+b%26c");
    }

    @Test
    @DisplayName("base64Decode decodes a known base64 string back to bytes")
    void testBase64Decode() {
        // "hello" => "aGVsbG8="
        assertThat(StringHelper.base64Decode("aGVsbG8=")).containsExactly(
                "hello".getBytes(StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("firstToLowerCase lowercases only the first character")
    void testFirstToLowerCase() {
        assertThat(StringHelper.firstToLowerCase("Hello")).isEqualTo("hello");
        assertThat(StringHelper.firstToLowerCase("XYZ")).isEqualTo("xYZ");
    }

    @Test
    @DisplayName("firstToUpperCase uppercases only the first character")
    void testFirstToUpperCase() {
        assertThat(StringHelper.firstToUpperCase("hello")).isEqualTo("Hello");
        assertThat(StringHelper.firstToUpperCase("xyz")).isEqualTo("Xyz");
    }

    @Test
    @DisplayName("qoute then unqoute round-trips plain text")
    void testRoundTrip() {
        String s = "<tag attr=\"v\">&amp;</tag>";
        String quoted = StringHelper.qouteHtmlSpecialChars(s);
        assertThat(StringHelper.unqouteHtmlSpecialChars(quoted)).isEqualTo(s);
    }

    @Test
    @DisplayName("format with no args returns the pattern unchanged")
    void testFormatNoArgs() {
        assertThat(StringHelper.format("nothing")).isEqualTo("nothing");
    }

    @Test
    @DisplayName("urlEncode keeps unreserved chars untouched (JDK encodes ~ though)")
    void testUrlEncodeUnreserved() {
        assertThat(StringHelper.urlEncode("abc123-_.")).isEqualTo("abc123-_.");
    }
}
