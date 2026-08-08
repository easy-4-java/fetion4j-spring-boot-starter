package net.apexes.fetion4j.core.client;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import net.apexes.fetion4j.core.util.XmlElement;

/**
 * Unit tests for {@link CmccMobileValidator}.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
@DisplayName("CmccMobileValidator Tests")
class CmccMobileValidatorTest {

    private XmlElement distTable() throws Exception {
        XmlElement xml = new XmlElement();
        xml.parseString("<r><c v=\"cmcc\">"
                + "<d s=\"13500000000\" e=\"13999999999\"/>"
                + "<d s=\"15900000000\" e=\"15999999999\"/>"
                + "</c></r>");
        return xml;
    }

    @Test
    @DisplayName("isCmccMobileNo returns true within a registered range")
    void testWithinRange() throws Exception {
        CmccMobileValidator v = new CmccMobileValidator(distTable());
        assertThat(v.isCmccMobileNo(13800138000L)).isTrue();
        assertThat(v.isCmccMobileNo(13500000000L)).isTrue();
        assertThat(v.isCmccMobileNo(13999999999L)).isTrue();
        assertThat(v.isCmccMobileNo(15912345678L)).isTrue();
    }

    @Test
    @DisplayName("isCmccMobileNo returns false outside all ranges")
    void testOutsideRange() throws Exception {
        CmccMobileValidator v = new CmccMobileValidator(distTable());
        assertThat(v.isCmccMobileNo(13400000000L)).isFalse();
        assertThat(v.isCmccMobileNo(15800000000L)).isFalse();
    }

    @Test
    @DisplayName("Entries with zero start/end are skipped")
    void testSkipsZeroRanges() throws Exception {
        XmlElement xml = new XmlElement();
        xml.parseString("<r><c v=\"cmcc\">"
                + "<d s=\"0\" e=\"0\"/>"
                + "<d s=\"13000000000\" e=\"13199999999\"/>"
                + "</c></r>");
        CmccMobileValidator v = new CmccMobileValidator(xml);
        assertThat(v.isCmccMobileNo(13012345678L)).isTrue();
    }
}
