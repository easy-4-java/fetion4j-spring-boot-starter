package net.apexes.fetion4j.core.client;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import net.apexes.fetion4j.core.sipc.RequestMessage;
import net.apexes.fetion4j.core.sipc.Sipc;

/**
 * Unit tests for the pure static helpers in {@link ClientHelper}.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
@DisplayName("ClientHelper Tests")
class ClientHelperTest {

    @Test
    @DisplayName("isGroupUri detects sip:PG... uris")
    void testIsGroupUri() {
        assertThat(ClientHelper.isGroupUri("sip:PG123@fetion.com.cn")).isTrue();
        assertThat(ClientHelper.isGroupUri("sip:123@fetion.com.cn;p=1")).isFalse();
        assertThat(ClientHelper.isGroupUri(null)).isFalse();
    }

    @Test
    @DisplayName("isMobileUri detects tel:... uris")
    void testIsMobileUri() {
        assertThat(ClientHelper.isMobileUri("tel:13900000000")).isTrue();
        assertThat(ClientHelper.isMobileUri("sip:1@fetion.com.cn;p=1")).isFalse();
        assertThat(ClientHelper.isMobileUri(null)).isFalse();
    }

    @Test
    @DisplayName("isSipUri validates the sip:...@fetion.com.cn;p=<digits> shape")
    void testIsSipUri() {
        assertThat(ClientHelper.isSipUri("sip:12345@fetion.com.cn;p=1")).isTrue();
        assertThat(ClientHelper.isSipUri("sip:abc@fetion.com.cn;p=42")).isTrue();
        assertThat(ClientHelper.isSipUri("tel:13900000000")).isFalse();
        assertThat(ClientHelper.isSipUri("sip:12345@fetion.com.cn;p=0")).isFalse();
        assertThat(ClientHelper.isSipUri("")).isFalse();
        assertThat(ClientHelper.isSipUri(null)).isFalse();
    }

    @Test
    @DisplayName("getSidFromUri extracts the sid from a valid sip uri")
    void testGetSidFromUriValid() {
        assertThat(ClientHelper.getSidFromUri("sip:98765@fetion.com.cn;p=3")).isEqualTo("98765");
    }

    @Test
    @DisplayName("getSidFromUri returns null when uri is not a sip uri")
    void testGetSidFromUriInvalid() {
        assertThat(ClientHelper.getSidFromUri("tel:13900000000")).isNull();
        assertThat(ClientHelper.getSidFromUri(null)).isNull();
    }

    @Test
    @DisplayName("createSipcMessageKey concatenates callId_sequence_method")
    void testCreateSipcMessageKey() {
        RequestMessage req = new RequestMessage(Sipc.METHOD_R);
        req.setCallId(7);
        req.setSequence(3);
        assertThat(ClientHelper.createSipcMessageKey(req)).isEqualTo("7_3_R");
    }
}
