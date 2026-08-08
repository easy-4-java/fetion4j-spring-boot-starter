package net.apexes.fetion4j.core;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import net.apexes.fetion4j.core.user.Contact;
import net.apexes.fetion4j.core.user.Personal;

/**
 * Unit tests for {@link UserInfo}.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
@DisplayName("UserInfo Tests")
class UserInfoTest {

    @Test
    @DisplayName("Default constructor initialises quota and version defaults to \"0\"")
    void testDefaults() {
        UserInfo info = new UserInfo();
        assertThat(info.getQuota()).isNotNull();
        assertThat(info.getPersonalVersion()).isEqualTo("0");
        assertThat(info.getContactVersion()).isEqualTo("0");
    }

    @Test
    @DisplayName("personal/contact getters/setters and version resolution")
    void testSetters() {
        UserInfo info = new UserInfo();
        Personal p = new Personal(1, "u", "n");
        p.setVersion("v9");
        info.setPersonal(p);
        Contact c = new Contact("c3");
        info.setContact(c);
        assertThat(info.getPersonal()).isSameAs(p);
        assertThat(info.getContact()).isSameAs(c);
        assertThat(info.getPersonalVersion()).isEqualTo("v9");
        assertThat(info.getContactVersion()).isEqualTo("c3");

        Quota q = new Quota();
        info.setQuota(q);
        assertThat(info.getQuota()).isSameAs(q);
    }

    @Test
    @DisplayName("toString runs and references personal/contact")
    void testToString() {
        UserInfo info = new UserInfo();
        info.setPersonal(new Personal(1, "u", "n"));
        assertThat(info.toString()).isNotNull();
    }
}
