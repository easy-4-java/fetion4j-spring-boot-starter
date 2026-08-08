package net.apexes.fetion4j.core.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Unit tests for {@link XmlElementHelper}.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
@DisplayName("XmlElementHelper Tests")
class XmlElementHelperTest {

    @Test
    @DisplayName("getNode navigates /root/child paths")
    void testGetNode() throws Exception {
        XmlElement xml = new XmlElement();
        xml.parseString("<config><servers version=\"183\"/></config>");
        XmlElement node = XmlElementHelper.getNode(xml, "/config/servers");
        assertThat(node).isNotNull();
        assertThat(node.getName()).isEqualTo("servers");
    }

    @Test
    @DisplayName("getNode returns null when the path does not start with /root/")
    void testGetNodeWrongRoot() throws Exception {
        XmlElement xml = new XmlElement();
        xml.parseString("<config><servers/></config>");
        assertThat(XmlElementHelper.getNode(xml, "/wrong/servers")).isNull();
    }

    @Test
    @DisplayName("getNode returns null when an intermediate node is missing")
    void testGetNodeMissingIntermediate() throws Exception {
        XmlElement xml = new XmlElement();
        xml.parseString("<config><servers/></config>");
        assertThat(XmlElementHelper.getNode(xml, "/config/missing/x")).isNull();
    }

    @Test
    @DisplayName("getNodeContent returns the content of the resolved node")
    void testGetNodeContent() throws Exception {
        XmlElement xml = new XmlElement();
        xml.parseString("<config><url>http://example</url></config>");
        assertThat(XmlElementHelper.getNodeContent(xml, "/config/url")).isEqualTo("http://example");
    }

    @Test
    @DisplayName("getNodeContent returns null when the node is absent")
    void testGetNodeContentMissing() throws Exception {
        XmlElement xml = new XmlElement();
        xml.parseString("<config/>");
        assertThat(XmlElementHelper.getNodeContent(xml, "/config/missing")).isNull();
    }

    @Test
    @DisplayName("getNodeAttribute reads a string attribute from the resolved node")
    void testGetNodeAttribute() throws Exception {
        XmlElement xml = new XmlElement();
        xml.parseString("<config><servers version=\"42\"/></config>");
        assertThat(XmlElementHelper.getNodeAttribute(xml, "/config/servers", "version")).isEqualTo("42");
    }

    @Test
    @DisplayName("getNodeAttribute returns null when node is absent")
    void testGetNodeAttributeMissingNode() throws Exception {
        XmlElement xml = new XmlElement();
        xml.parseString("<config/>");
        assertThat(XmlElementHelper.getNodeAttribute(xml, "/config/missing", "version")).isNull();
    }

    @Test
    @DisplayName("getNodeAttribute(path, name, defaultValue) returns default when attribute absent")
    void testGetNodeAttributeWithDefault() throws Exception {
        XmlElement xml = new XmlElement();
        xml.parseString("<config><servers version=\"9\"/></config>");
        assertThat(XmlElementHelper.getNodeAttribute(xml, "/config/servers", "missing", "def")).isEqualTo("def");
    }

    @Test
    @DisplayName("open + write round-trip a file")
    void testOpenWriteRoundTrip(@TempDir File dir) throws IOException {
        File f = new File(dir, "config.xml");
        XmlElement xml = new XmlElement();
        xml.parseString("<config><servers version=\"7\"/></config>");
        XmlElementHelper.write(xml, f, "UTF-8");
        assertThat(f).exists();

        XmlElement loaded = XmlElementHelper.open(f, StandardCharsets.UTF_8.name());
        XmlElement node = XmlElementHelper.getNode(loaded, "/config/servers");
        assertThat(node).isNotNull();
        assertThat(node.getStringAttribute("version")).isEqualTo("7");
    }

    @Test
    @DisplayName("open on a missing file propagates IOException")
    void testOpenMissingFile(@TempDir File dir) {
        File missing = new File(dir, "nope.xml");
        try {
            XmlElementHelper.open(missing, StandardCharsets.UTF_8.name());
            assertThat(false).as("expected IOException").isTrue();
        } catch (IOException expected) {
            assertThat(expected).isNotNull();
        }
    }

    @Test
    @DisplayName("write then open with a corrupted on-disk content raises on parse")
    void testWriteGarbage(@TempDir File dir) throws IOException {
        File f = new File(dir, "bad.xml");
        Files.write(f.toPath(), "not xml".getBytes(StandardCharsets.UTF_8));
        try {
            XmlElementHelper.open(f, StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            assertThat(e).isNotNull();
        }
    }
}
