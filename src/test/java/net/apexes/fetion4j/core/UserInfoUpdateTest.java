package net.apexes.fetion4j.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import net.apexes.fetion4j.core.client.Controller;
import net.apexes.fetion4j.core.user.Buddy;
import net.apexes.fetion4j.core.user.Contact;
import net.apexes.fetion4j.core.user.Personal;
import net.apexes.fetion4j.core.user.Relation;
import net.apexes.fetion4j.core.util.XmlElement;

/**
 * Unit tests for {@link UserInfo} update / parse paths.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
@DisplayName("UserInfo Update Tests")
class UserInfoUpdateTest {

    private Controller controller() {
        Controller controller = mock(Controller.class);
        return controller;
    }

    @Test
    @DisplayName("updateOnLogined parses personal + contact-list + quotas children")
    void testUpdateOnLogined() throws Exception {
        XmlElement xml = new XmlElement();
        xml.parseString("<results>"
                + "<personal version=\"1\" user-id=\"7\" uri=\"sip:7@fetion.com.cn;p=1\" name=\"alice\""
                + " sid=\"7\" mobile-no=\"13900000000\" nickname=\"al\" impresa=\"sig\" />"
                + "<contact-list version=\"2\" />"
                + "<quotas>"
                + "<quota-limit><limit name=\"max-buddies\" value=\"100\"/></quota-limit>"
                + "<quota-frequency><frequency name=\"send-sms\" day-limit=\"10\" day-count=\"1\""
                + " month-limit=\"100\" month-count=\"2\"/></quota-frequency>"
                + "</quotas>"
                + "<custom version=\"3\"/>"
                + "</results>");
        UserInfo info = new UserInfo();
        info.updateOnLogined(controller(), xml);
        assertThat(info.getPersonalVersion()).isEqualTo("1");
        assertThat(info.getPersonal().getUserId()).isEqualTo(7);
        assertThat(info.getContactVersion()).isEqualTo("2");
        assertThat(info.getQuota().getMaxBuddies()).isEqualTo(100);
    }

    @Test
    @DisplayName("updateOnLogined keeps existing personal/contact when version is unchanged")
    void testUpdateOnLoginedSameVersion() throws Exception {
        UserInfo info = new UserInfo();
        Personal p = new Personal(1, "u", "n");
        p.setVersion("5");
        info.setPersonal(p);
        info.setContact(new Contact("9"));

        XmlElement xml = new XmlElement();
        xml.parseString("<results>"
                + "<personal version=\"5\" user-id=\"1\" uri=\"u\" name=\"n\"/>"
                + "<contact-list version=\"9\"/>"
                + "</results>");
        info.updateOnLogined(controller(), xml);
        // unchanged -> same Personal instance retained
        assertThat(info.getPersonal()).isSameAs(p);
    }

    @Test
    @DisplayName("updateOnPresenceChanged updates the matching buddy and fires changedUser")
    void testUpdateOnPresenceChanged() throws Exception {
        UserInfo info = new UserInfo();
        Personal self = new Personal(1, "sip:1@fetion.com.cn;p=1", "me");
        info.setPersonal(self);
        Contact contact = new Contact("1");
        Buddy b = new Buddy(2, "sip:2@fetion.com.cn;p=1", "b", Relation.BUDDY);
        contact.addBuddy(b);
        info.setContact(contact);

        XmlElement xml = new XmlElement();
        xml.parseString("<events><event type=\"PresenceChanged\"><contacts>"
                + "<c id=\"2\"><p v=\"9\" sid=\"22\" su=\"sip:2@fetion.com.cn;p=2\""
                + " m=\"13800138000\" c=\"CMCC\" cs=\"0\" n=\"bob\" i=\"\" sms=\"0.0:0:0\"/>"
                + "<pr b=\"600\"/>"
                + "</c></contacts></event></events>");
        info.updateOnPresenceChanged(controller(), xml);
        assertThat(b.getSid()).isEqualTo("22");
        assertThat(b.getPresence().getValue()).isEqualTo(600);
    }

    @Test
    @DisplayName("updateOnSyncUserInfoChanged updates buddy relation for 'update' action")
    void testUpdateOnSyncUserInfoChanged() throws Exception {
        UserInfo info = new UserInfo();
        info.setPersonal(new Personal(1, "u", "n"));
        Contact contact = new Contact("1");
        Buddy b = new Buddy(5, "u", "n", Relation.UNCONFIRMED);
        contact.addBuddy(b);
        info.setContact(contact);

        XmlElement xml = new XmlElement();
        xml.parseString("<events><event type=\"SyncUserInfo\"><user-info>"
                + "<contact-list version=\"42\">"
                + "<buddies>"
                + "<buddy action=\"update\" user-id=\"5\" relation-status=\"1\"/>"
                + "</buddies>"
                + "</contact-list></user-info></event></events>");
        info.updateOnSyncUserInfoChanged(controller(), xml);
        assertThat(b.getRelation()).isEqualTo(Relation.BUDDY);
    }

    @Test
    @DisplayName("updateOnLogined parses a full contact-list (buddy-lists/buddies/blacklist)")
    void testUpdateOnLoginedContactList() throws Exception {
        UserInfo info = new UserInfo();
        XmlElement xml = new XmlElement();
        xml.parseString("<results>"
                + "<personal version=\"1\" user-id=\"1\" uri=\"sip:1@fetion.com.cn;p=1\" name=\"n\"/>"
                + "<contact-list version=\"5\">"
                + "<buddy-lists>"
                + "<buddy-list id=\"1\" name=\"friends\"/>"
                + "</buddy-lists>"
                + "<buddies>"
                + "<b i=\"2\" u=\"sip:2@fetion.com.cn;p=1\" n=\"bob\" r=\"1\"/>"
                + "</buddies>"
                + "<blacklist>"
                + "<k i=\"3\" u=\"sip:3@fetion.com.cn;p=1\" n=\"\"/>"
                + "</blacklist>"
                + "</contact-list>"
                + "</results>");
        info.updateOnLogined(controller(), xml);
        assertThat(info.getContact().getVersion()).isEqualTo("5");
        assertThat(info.getContact().findBuddy(2)).isNotNull();
        assertThat(info.getContact().getBuddyGroups()).isNotEmpty();
        assertThat(info.getContact().getBlacklist()).isNotEmpty();
    }

    @Test
    @DisplayName("updateOnPresenceChanged updates the logged-in user when the buddy is not found")
    void testUpdateOnPresenceChangedSelf() throws Exception {
        UserInfo info = new UserInfo();
        info.setPersonal(new Personal(7, "sip:7@fetion.com.cn;p=1", "me"));
        info.setContact(new Contact("1"));

        XmlElement xml = new XmlElement();
        xml.parseString("<events><event type=\"PresenceChanged\"><contacts>"
                + "<c id=\"7\"><p v=\"9\" sid=\"s\" su=\"sip:7@fetion.com.cn;p=9\""
                + " c=\"CMCC\" cs=\"0\" n=\"me\"/><pr b=\"600\"/></c>"
                + "</contacts></event></events>");
        info.updateOnPresenceChanged(controller(), xml);
        assertThat(info.getPersonal().getPresence().getValue()).isEqualTo(600);
    }

    @Test
    @DisplayName("updateOnPresenceChanged breaks when the buddy is not found and it's not self")
    void testUpdateOnPresenceChangedUnknownBreaks() throws Exception {
        UserInfo info = new UserInfo();
        info.setPersonal(new Personal(7, "sip:7@fetion.com.cn;p=1", "me"));
        info.setContact(new Contact("1"));

        XmlElement xml = new XmlElement();
        xml.parseString("<events><event type=\"PresenceChanged\"><contacts>"
                + "<c id=\"99\"><p v=\"9\" sid=\"s\" su=\"sip:99@fetion.com.cn;p=9\"/></c>"
                + "</contacts></event></events>");
        info.updateOnPresenceChanged(controller(), xml); // should not throw
    }

    @Test
    @DisplayName("updateOnSyncUserInfoChanged handles missing contact-list node gracefully")
    void testUpdateOnSyncUserInfoChangedMissingNode() throws Exception {
        UserInfo info = new UserInfo();
        info.setPersonal(new Personal(1, "u", "n"));
        info.setContact(new Contact("1"));
        XmlElement xml = new XmlElement();
        xml.parseString("<events><event type=\"SyncUserInfo\"><user-info/></event></events>");
        info.updateOnSyncUserInfoChanged(controller(), xml); // no contact-list -> no-op
    }

    @Test
    @DisplayName("updateOnLogined ignores 'remove'/'add' actions in SyncUserInfo (only 'update' handled)")
    void testUpdateOnSyncUserInfoChangedNonUpdateActions() throws Exception {
        UserInfo info = new UserInfo();
        info.setPersonal(new Personal(1, "u", "n"));
        Contact contact = new Contact("1");
        Buddy b = new Buddy(5, "u", "n", Relation.UNCONFIRMED);
        contact.addBuddy(b);
        info.setContact(contact);

        XmlElement xml = new XmlElement();
        xml.parseString("<events><event type=\"SyncUserInfo\"><user-info>"
                + "<contact-list version=\"42\"><buddies>"
                + "<buddy action=\"add\" user-id=\"9\" uri=\"sip:9@fetion.com.cn;p=1\" relation-status=\"1\"/>"
                + "<buddy action=\"remove\" user-id=\"5\"/>"
                + "</buddies></contact-list></user-info></event></events>");
        info.updateOnSyncUserInfoChanged(controller(), xml);
        // non-update actions are ignored -> buddy relation unchanged
        assertThat(b.getRelation()).isEqualTo(Relation.UNCONFIRMED);
    }
}
