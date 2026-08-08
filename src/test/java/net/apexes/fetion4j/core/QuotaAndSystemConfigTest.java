package net.apexes.fetion4j.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import net.apexes.fetion4j.core.client.Controller;
import net.apexes.fetion4j.core.util.XmlElement;

/**
 * Unit tests for {@link Quota} and {@link SystemConfig}.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
@DisplayName("Quota & SystemConfig Tests")
class QuotaAndSystemConfigTest {

    @Test
    @DisplayName("Quota.update parses limit and frequency and fires sms-count-changed")
    void testQuotaUpdate() throws Exception {
        XmlElement xml = new XmlElement();
        xml.parseString("<quotas>"
                + "<quota-limit>"
                + "<limit name=\"max-buddies\" value=\"500\"/>"
                + "</quota-limit>"
                + "<quota-frequency>"
                + "<frequency name=\"send-sms\" day-limit=\"1000\" day-count=\"6\" month-limit=\"15000\" month-count=\"6\"/>"
                + "</quota-frequency>"
                + "</quotas>");
        Controller controller = mock(Controller.class);
        Quota quota = new Quota();
        quota.update(controller, xml);

        assertThat(quota.getMaxBuddies()).isEqualTo(500);
        assertThat(quota.getSendSmsDayLimit()).isEqualTo(1000);
        assertThat(quota.getSendSmsDayCount()).isEqualTo(6);
        assertThat(quota.getSendSmsMonthLimit()).isEqualTo(15000);
        assertThat(quota.getSendSmsMonthCount()).isEqualTo(6);
        verify(controller).fireSmsCountChanged(6, 6);
    }

    @Test
    @DisplayName("Quota.update is resilient to missing quota-limit / quota-frequency")
    void testQuotaUpdateEmpty() throws Exception {
        XmlElement xml = new XmlElement();
        xml.parseString("<quotas/>");
        Controller controller = mock(Controller.class);
        Quota quota = new Quota();
        quota.update(controller, xml);
        assertThat(quota.getMaxBuddies()).isZero();
        verifyNoInteractions(controller);
    }

    @Test
    @DisplayName("Quota.toString summarizes counts")
    void testQuotaToString() {
        Quota q = new Quota();
        assertThat(q.toString()).contains("Quota{").contains("maxBuddies=0");
    }

    @Test
    @DisplayName("SystemConfig parses version map and exposes getSummary / getValue / update")
    void testSystemConfig() throws Exception {
        XmlElement xml = new XmlElement();
        xml.parseString("<config>"
                + "<servers version=\"1\">"
                + "<get-pic-code>http://pic</get-pic-code>"
                + "</servers>"
                + "<service-no version=\"2\"/>"
                + "<parameters version=\"3\"/>"
                + "<hints version=\"4\"/>"
                + "<http-applications version=\"5\"/>"
                + "<client-config version=\"6\"/>"
                + "<services version=\"7\"/>"
                + "</config>");
        SystemConfig cfg = new SystemConfig(xml);
        assertThat(cfg.getValue("/config/servers/get-pic-code")).isEqualTo("http://pic");
        // getSummary replaces {servers-version} with the parsed version "1"
        assertThat(cfg.getSummary()).contains("servers version=\"1\"").isNotNull();
        assertThat(cfg.getXml()).isSameAs(xml);
        assertThat(cfg.toString()).contains("<config");

        // update with a new servers node returns isChanged=true
        XmlElement updated = new XmlElement();
        updated.parseString("<config><servers version=\"99\"/></config>");
        assertThat(cfg.update(updated)).isTrue();
    }

    @Test
    @DisplayName("SystemConfig.update returns false when nothing changed")
    void testSystemConfigUpdateNoChange() throws Exception {
        XmlElement xml = new XmlElement();
        xml.parseString("<config><servers version=\"1\"/></config>");
        SystemConfig cfg = new SystemConfig(xml);
        XmlElement empty = new XmlElement();
        empty.parseString("<config/>");
        assertThat(cfg.update(empty)).isFalse();
    }

    @Test
    @DisplayName("SystemConfig.getSummary replaces placeholders with 0 when versions are null")
    void testSystemConfigSummaryNullVersions() throws Exception {
        XmlElement xml = new XmlElement();
        xml.parseString("<config/>");
        SystemConfig cfg = new SystemConfig(xml);
        // all version placeholders replaced (with "0")
        String summary = cfg.getSummary();
        assertThat(summary).doesNotContain("{servers-version}");
    }
}
