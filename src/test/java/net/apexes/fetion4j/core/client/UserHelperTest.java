package net.apexes.fetion4j.core.client;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import net.apexes.fetion4j.core.user.Buddy;
import net.apexes.fetion4j.core.user.Personal;
import net.apexes.fetion4j.core.user.Presence;
import net.apexes.fetion4j.core.user.Relation;
import net.apexes.fetion4j.core.user.User;
import net.apexes.fetion4j.core.util.XmlElement;

/**
 * Unit tests for {@link UserHelper}.
 *
 * <p>Note: {@code UserHelper} derives attribute names directly from JavaBean
 * property names (camelCase), so XML attributes use {@code userId} etc.,
 * not {@code user-id}.</p>
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
@DisplayName("UserHelper Tests")
class UserHelperTest {

    @Test
    @DisplayName("toUser parses a Personal XML element")
    void testToUserPersonal() {
        XmlElement xml = new XmlElement();
        xml.parseString("<personal userId=\"7\" uri=\"sip:7@fetion.com.cn;p=1\" name=\"alice\""
                + " mobileNo=\"13900000000\" nickname=\"al\" impresa=\"sig\""
                + " carrier=\"CMCC\" carrierStatus=\"0\" smsOnlineStatus=\"0.0:0:0\""
                + " presence=\"400\" />");
        Object result = UserHelper.toUser(xml);
        assertThat(result).isInstanceOf(Personal.class);
        Personal p = (Personal) result;
        assertThat(p.getUserId()).isEqualTo(7);
        assertThat(p.getUri()).isEqualTo("sip:7@fetion.com.cn;p=1");
        assertThat(p.getNickname()).isEqualTo("al");
        assertThat(p.getCarrier()).isEqualTo("CMCC");
        assertThat(p.getPresence()).isEqualTo(Presence.ONLINE);
    }

    @Test
    @DisplayName("toUser parses a Buddy XML element including relation enum")
    void testToUserBuddy() {
        XmlElement xml = new XmlElement();
        xml.parseString("<buddy userId=\"9\" uri=\"sip:9@fetion.com.cn;p=1\" name=\"bob\""
                + " relation=\"1\" />");
        Object result = UserHelper.toUser(xml);
        assertThat(result).isInstanceOf(Buddy.class);
        Buddy b = (Buddy) result;
        assertThat(b.getUserId()).isEqualTo(9);
        assertThat(b.getRelation()).isEqualTo(Relation.BUDDY);
    }

    @Test
    @DisplayName("toUser parses a plain User XML element")
    void testToUserUser() {
        XmlElement xml = new XmlElement();
        xml.parseString("<user userId=\"1\" uri=\"sip:1@fetion.com.cn;p=1\" name=\"x\" />");
        Object result = UserHelper.toUser(xml);
        assertThat(result).isInstanceOf(User.class);
        assertThat(((User) result).getUserId()).isEqualTo(1);
    }

    @Test
    @DisplayName("toUser returns null for an unknown element name")
    void testToUserUnknown() {
        XmlElement xml = new XmlElement();
        xml.parseString("<nonexistent userId=\"1\"/>");
        assertThat(UserHelper.toUser(xml)).isNull();
    }

    @Test
    @DisplayName("toXml serializes a User and its fields")
    void testToXmlUser() {
        User u = new User(5, "sip:5@fetion.com.cn;p=1", "n");
        XmlElement el = UserHelper.toXml(u);
        assertThat(el.getName()).isEqualTo("user");
        assertThat(el.getIntAttribute("userId")).isEqualTo(5);
        assertThat(el.getStringAttribute("uri")).isEqualTo("sip:5@fetion.com.cn;p=1");
    }

    @Test
    @DisplayName("toXml serializes a Buddy including the relation enum value")
    void testToXmlBuddy() {
        Buddy b = new Buddy(2, "sip:2@fetion.com.cn;p=1", "n", Relation.BUDDY);
        XmlElement el = UserHelper.toXml(b);
        assertThat(el.getName()).isEqualTo("buddy");
        assertThat(el.getIntAttribute("relation")).isEqualTo(Relation.BUDDY.getValue());
    }

    @Test
    @DisplayName("toXml/toUser round-trip for a plain user")
    void testRoundTrip() {
        User u = new User(8, "sip:8@fetion.com.cn;p=1", "name8");
        XmlElement el = UserHelper.toXml(u);
        Object parsed = UserHelper.toUser(el);
        assertThat(parsed).isInstanceOf(User.class);
        assertThat(((User) parsed).getUserId()).isEqualTo(8);
        assertThat(((User) parsed).getName()).isEqualTo("name8");
    }
}
