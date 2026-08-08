package net.apexes.fetion4j.core.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link XmlParseException}.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
@DisplayName("XmlParseException Tests")
class XmlParseExceptionTest {

    @Test
    @DisplayName("Constructor with (name, message) sets lineNr to NO_LINE")
    void testConstructorNameMessage() {
        XmlParseException ex = new XmlParseException("element", "boom");
        assertThat(ex.getLineNr()).isEqualTo(XmlParseException.NO_LINE);
        assertThat(ex.getMessage()).contains("a element element").contains("boom");
    }

    @Test
    @DisplayName("Constructor with null name refers to \"the XML definition\"")
    void testConstructorNullName() {
        XmlParseException ex = new XmlParseException(null, "boom");
        assertThat(ex.getMessage()).contains("the XML definition");
    }

    @Test
    @DisplayName("Constructor with (name, lineNr, message) stores the line number")
    void testConstructorWithLineNr() {
        XmlParseException ex = new XmlParseException("el", 42, "oops");
        assertThat(ex.getLineNr()).isEqualTo(42);
        assertThat(ex.getMessage()).contains("at line 42");
    }

    @Test
    @DisplayName("NO_LINE constant is -1")
    void testNoLineConstant() {
        assertThat(XmlParseException.NO_LINE).isEqualTo(-1);
    }
}
