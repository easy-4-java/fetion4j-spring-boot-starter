package net.apexes.fetion4j.core;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import net.apexes.fetion4j.core.user.Presence;

/**
 * Unit tests for {@link Account}.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
@DisplayName("Account Tests")
class AccountTest {

    @Test
    @DisplayName("Constructor parses sid, sets mobile/userId/uri and generates AES key/IV")
    void testConstructor() {
        Account acc = new Account(13900000000L, 12345, "sip:12345@fetion.com.cn;p=3");
        assertThat(acc.getMobileNo()).isEqualTo(13900000000L);
        assertThat(acc.getUserId()).isEqualTo(12345);
        assertThat(acc.getUri()).isEqualTo("sip:12345@fetion.com.cn;p=3");
        assertThat(acc.getSid()).isEqualTo("12345");
        assertThat(acc.getAesKey()).hasSize(32);
        assertThat(acc.getAesIV()).hasSize(16);
    }

    @Test
    @DisplayName("Password is round-trippable")
    void testPassword() {
        Account acc = new Account(1L, 1, "sip:1@fetion.com.cn;p=1");
        acc.setPassword("secret");
        assertThat(acc.getPassword()).isEqualTo("secret");
    }

    @Test
    @DisplayName("Presence defaults to ONLINE and is settable")
    void testPresence() {
        Account acc = new Account(1L, 1, "sip:1@fetion.com.cn;p=1");
        assertThat(acc.getPresence()).isEqualTo(Presence.ONLINE);
        acc.setPresence(Presence.AWAY);
        assertThat(acc.getPresence()).isEqualTo(Presence.AWAY);
    }

    @Test
    @DisplayName("toString includes uri/userId/mobileNo/presence")
    void testToString() {
        Account acc = new Account(139L, 7, "sip:7@fetion.com.cn;p=1");
        acc.setPassword("p");
        assertThat(acc.toString())
                .contains("uri=sip:7@fetion.com.cn;p=1")
                .contains("userId=7")
                .contains("mobileNo=139")
                .contains("presence=ONLINE");
    }
}
