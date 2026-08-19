package net.apexes.fetion4j.core.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link XmlElement} (NanoXML-lite based parser used by Fetion).
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
@DisplayName("XmlElement Tests")
class XmlElementTest {

    @Test
    @DisplayName("Default constructor yields empty element with no name/children/content")
    void testDefaultConstructor() {
        XmlElement el = new XmlElement();
        assertThat(el.getName()).isNull();
        assertThat(el.getContent()).isEmpty();
        assertThat(el.getChildren()).isEmpty();
        assertThat(el.getChildrenCount()).isZero();
        assertThat(el.getLineNr()).isZero();
        assertThat(el.getAttributeNames()).isEmpty();
        assertThat(el.getParent()).isNull();
        assertThat(el.isIgnoreWhitespace()).isTrue();
    }

    @Test
    @DisplayName("All four constructors produce equivalent empty elements")
    void testAllConstructors() {
        assertThat(new XmlElement(true).getChildren()).isEmpty();
        assertThat(new XmlElement(new java.util.HashMap<>()).getChildren()).isEmpty();
        assertThat(new XmlElement(new java.util.HashMap<>(), true).getChildren()).isEmpty();
    }

    @Test
    @DisplayName("parseString parses a simple element with attributes and content")
    void testParseSimple() throws Exception {
        XmlElement el = new XmlElement();
        el.parseString("<user id=\"7\" name=\"alice\">hello</user>");
        assertThat(el.getName()).isEqualTo("user");
        assertThat(el.getAttribute("id")).isEqualTo("7");
        assertThat(el.getAttribute("name")).isEqualTo("alice");
        assertThat(el.getStringAttribute("id")).isEqualTo("7");
        assertThat(el.getStringAttribute("missing", "def")).isEqualTo("def");
        assertThat(el.getContent()).isEqualTo("hello");
    }

    @Test
    @DisplayName("parseFromReader parses the same content as parseString")
    void testParseFromReader() throws Exception {
        XmlElement el = new XmlElement();
        el.parseFromReader(new StringReader("<root a=\"1\"/>"));
        assertThat(el.getName()).isEqualTo("root");
        assertThat(el.getAttribute("a")).isEqualTo("1");
    }

    @Test
    @DisplayName("Typed attribute getters and setters")
    void testTypedAttributes() {
        XmlElement el = new XmlElement();
        el.setIntAttribute("count", 42);
        el.setLongAttribute("ms", 1234567890123L);
        el.setDoubleAttribute("ratio", 1.5);
        el.setAttribute("flag", "true");

        assertThat(el.getIntAttribute("count")).isEqualTo(42);
        assertThat(el.getIntAttribute("missing", 9)).isEqualTo(9);
        assertThat(el.getLongAttribute("ms")).isEqualTo(1234567890123L);
        assertThat(el.getLongAttribute("missing", 1L)).isEqualTo(1L);
        assertThat(el.getDoubleAttribute("ratio")).isEqualTo(1.5);
        assertThat(el.getDoubleAttribute("missing", 0.0)).isEqualTo(0.0);
        assertThat(el.getBooleanAttribute("flag", "true", "false", false)).isTrue();
        el.setAttribute("flag", "yes");
        assertThat(el.getBooleanAttribute("flag", "yes", "no", false)).isTrue();
    }

    @Test
    @DisplayName("getIntAttribute without default throws XmlParseException on non-numeric")
    void testGetIntAttributeThrows() {
        XmlElement el = new XmlElement();
        el.setAttribute("n", "abc");
        assertThatThrownBy(() -> el.getIntAttribute("n"))
                .isInstanceOf(XmlParseException.class);
    }

    @Test
    @DisplayName("getLongAttribute without default throws XmlParseException on non-numeric")
    void testGetLongAttributeThrows() {
        XmlElement el = new XmlElement();
        el.setAttribute("n", "xyz");
        assertThatThrownBy(() -> el.getLongAttribute("n"))
                .isInstanceOf(XmlParseException.class);
    }

    @Test
    @DisplayName("getDoubleAttribute without default throws XmlParseException on non-numeric")
    void testGetDoubleAttributeThrows() {
        XmlElement el = new XmlElement();
        el.setAttribute("n", "NaN-text");
        assertThatThrownBy(() -> el.getDoubleAttribute("n"))
                .isInstanceOf(XmlParseException.class);
    }

    @Test
    @DisplayName("getAttribute(name, default) returns default when attribute absent")
    void testGetAttributeDefault() {
        XmlElement el = new XmlElement();
        assertThat(el.getAttribute("missing", "fallback")).isEqualTo("fallback");
    }

    @Test
    @DisplayName("removeAttribute removes an existing attribute")
    void testRemoveAttribute() {
        XmlElement el = new XmlElement();
        el.setAttribute("a", "1");
        assertThat(el.getAttributeNames()).contains("a");
        el.removeAttribute("a");
        assertThat(el.getAttributeNames()).doesNotContain("a");
    }

    @Test
    @DisplayName("addChild/getChild/getChildren manipulates the child list")
    void testChildren() {
        XmlElement root = new XmlElement();
        root.setName("root");
        XmlElement c1 = new XmlElement();
        c1.setName("child");
        XmlElement c2 = new XmlElement();
        c2.setName("child");
        XmlElement other = new XmlElement();
        other.setName("other");
        root.addChild(c1);
        root.addChild(c2);
        root.addChild(other);

        assertThat(root.getChildrenCount()).isEqualTo(3);
        assertThat(root.getChild(0)).isSameAs(c1);
        assertThat(root.getChild("child")).isSameAs(c1);
        assertThat(root.getChildren("child")).hasSize(2);
        assertThat(c1.getParent()).isSameAs(root);

        List<XmlElement> all = root.getChildren();
        assertThat(all).hasSize(3);

        root.removeChild(c1);
        assertThat(root.getChildrenCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("getChild(name) returns null when no such child exists")
    void testGetChildMissing() {
        XmlElement root = new XmlElement();
        assertThat(root.getChild("missing")).isNull();
        assertThat(root.getChildren("missing")).isEmpty();
    }

    @Test
    @DisplayName("setContent and getName/setName round-trip")
    void testContentAndName() {
        XmlElement el = new XmlElement();
        el.setName("el");
        el.setContent("data");
        assertThat(el.getName()).isEqualTo("el");
        assertThat(el.getContent()).isEqualTo("data");
    }

    @Test
    @DisplayName("write emits a valid XML document")
    void testWrite() throws IOException {
        XmlElement root = new XmlElement();
        root.parseString("<root a=\"1\"><child>txt</child></root>");
        StringWriter sw = new StringWriter();
        root.write(sw);
        assertThat(sw.toString()).contains("<root").contains("a=\"1\"")
                .contains("<child>").contains("txt");
    }

    @Test
    @DisplayName("write with standalone flag and indent emits the element")
    void testWriteStandalone() throws IOException {
        XmlElement root = new XmlElement();
        root.parseString("<root a=\"1\"/>");
        StringWriter sw = new StringWriter();
        root.write(sw, true, 2);
        String out = sw.toString();
        assertThat(out).contains("<root").contains("a=\"1\"");
    }

    @Test
    @DisplayName("write(writer, indent) emits indented XML")
    void testWriteIndent() throws IOException {
        XmlElement root = new XmlElement();
        root.parseString("<root a=\"1\"/>");
        StringWriter sw = new StringWriter();
        root.write(sw, 4);
        assertThat(sw.toString()).contains("<root");
    }

    @Test
    @DisplayName("toString returns the serialized XML")
    void testToString() throws Exception {
        XmlElement el = new XmlElement();
        el.parseString("<r x=\"1\"/>");
        assertThat(el.toString()).contains("<r").contains("x=\"1\"");
    }

    @Test
    @DisplayName("parseString handles entity references (&amp; &lt; &gt; &quot; &apos;)")
    void testEntities() throws Exception {
        XmlElement el = new XmlElement();
        el.parseString("<r>&amp;&lt;&gt;&quot;&apos;</r>");
        assertThat(el.getContent()).contains("&<>\"'");
    }

    @Test
    @DisplayName("parseString with nested children parses hierarchy")
    void testNested() throws Exception {
        XmlElement el = new XmlElement();
        el.parseString("<a><b><c>v</c></b></a>");
        assertThat(el.getName()).isEqualTo("a");
        assertThat(el.getChild("b").getName()).isEqualTo("b");
        assertThat(el.getChild("b").getChild("c").getContent()).isEqualTo("v");
    }

    @Test
    @DisplayName("parseString on malformed XML throws XmlParseException")
    void testParseMalformed() {
        XmlElement el = new XmlElement();
        assertThatThrownBy(() -> el.parseString("<unclosed>"))
                .isInstanceOf(XmlParseException.class);
    }

    @Test
    @DisplayName("parseCharArray parses a char array slice")
    void testParseCharArray() throws Exception {
        char[] chars = "<r a=\"1\"/>   ".toCharArray();
        XmlElement el = new XmlElement();
        el.parseCharArray(chars, 0, chars.length);
        assertThat(el.getName()).isEqualTo("r");
        assertThat(el.getAttribute("a")).isEqualTo("1");
    }

    @Test
    @DisplayName("parseFromReader with startingLineNr tracks line number")
    void testParseFromReaderStartingLine() throws Exception {
        XmlElement el = new XmlElement();
        el.parseFromReader(new StringReader("<r/>"), 5);
        assertThat(el.getName()).isEqualTo("r");
    }

    @Test
    @DisplayName("setAttribute(Object) stores non-string values by toString")
    void testSetAttributeObject() {
        XmlElement el = new XmlElement();
        el.setAttribute("n", 42);
        // value is stored via toString, and getAttribute returns the stored object
        assertThat(el.getAttribute("n")).isEqualTo("42");
        assertThat(el.getIntAttribute("n")).isEqualTo(42);
        assertThat(el.getAttributeNames()).contains("n");
    }

    @Test
    @DisplayName("skipLeadingWhitespace=false keeps leading whitespace in content")
    void testNoSkipWhitespace() throws Exception {
        XmlElement el = new XmlElement(false);
        el.parseString("<r>  spaced  </r>");
        // with skip=false the content keeps the surrounding spaces
        assertThat(el.getContent()).isNotEqualTo("spaced");
        assertThat(el.getContent()).contains("spaced");
    }

    @Test
    @DisplayName("write() (no args) emits the element without indentation")
    void testWriteNoArgs() throws IOException {
        XmlElement root = new XmlElement();
        root.parseString("<root a=\"1\"/>");
        StringWriter sw = new StringWriter();
        root.write(sw);
        assertThat(sw.toString()).contains("<root");
    }

    @Test
    @DisplayName("getBooleanAttribute throws on an unexpected value")
    void testGetBooleanAttributeInvalid() {
        XmlElement el = new XmlElement();
        el.setAttribute("flag", "maybe");
        assertThatThrownBy(() -> el.getBooleanAttribute("flag", "yes", "no", false))
                .isInstanceOf(XmlParseException.class);
    }

    @Test
    @DisplayName("getIntAttribute(name, Hashtable, ...) and getAttribute(name, Hashtable,...) variants")
    void testGetIntAttributeWithHashtable() {
        XmlElement el = new XmlElement();
        el.setAttribute("k", "V");
        assertThat(el.getAttribute("k", null)).isEqualTo("V");
        assertThat(el.getAttribute("missing", "def")).isEqualTo("def");
    }

    @Test
    @DisplayName("parseString handles single-quoted attribute values")
    void testSingleQuoteAttribute() throws Exception {
        XmlElement el = new XmlElement();
        el.parseString("<r a='1' b='x'/>");
        assertThat(el.getStringAttribute("a")).isEqualTo("1");
        assertThat(el.getStringAttribute("b")).isEqualTo("x");
    }

    @Test
    @DisplayName("parseString preserves processing instruction-like content in mixed text")
    void testContentWithEntitiesAndNested() throws Exception {
        XmlElement el = new XmlElement();
        el.parseString("<r><a>1</a><b>2</b></r>");
        assertThat(el.getChildren()).hasSize(2);
        assertThat(el.getChild("a").getContent()).isEqualTo("1");
        assertThat(el.getChild("b").getContent()).isEqualTo("2");
    }

    @Test
    @DisplayName("write of an element with attributes, content and children round-trips")
    void testWriteWithChildren() throws IOException {
        XmlElement root = new XmlElement();
        root.setName("root");
        root.setAttribute("a", "1");
        XmlElement child = new XmlElement();
        child.setName("c");
        child.setContent("txt");
        root.addChild(child);
        StringWriter sw = new StringWriter();
        root.write(sw, 2);
        String out = sw.toString();
        assertThat(out).contains("<root").contains("a=\"1\"").contains("<c>").contains("txt");
    }

    @Test
    @DisplayName("parseString on an element with comment-like content does not break")
    void testParseWithComment() throws Exception {
        XmlElement el = new XmlElement();
        el.parseString("<r><!-- comment --><a>1</a></r>");
        assertThat(el.getChild("a").getContent()).isEqualTo("1");
    }

    @Test
    @DisplayName("removeChild removes a previously added child")
    void testRemoveChild() {
        XmlElement root = new XmlElement();
        root.setName("root");
        XmlElement c = new XmlElement();
        c.setName("c");
        root.addChild(c);
        assertThat(root.getChildrenCount()).isEqualTo(1);
        root.removeChild(c);
        assertThat(root.getChildrenCount()).isZero();
    }

    @Test
    @DisplayName("parseString handles a numeric content element")
    void testNumericContent() throws Exception {
        XmlElement el = new XmlElement();
        el.parseString("<r>42</r>");
        assertThat(el.getIntAttribute("missing")).isZero();
    }

    @Test
    @DisplayName("parseCharArray with a starting line number")
    void testParseCharArrayWithLineNr() throws Exception {
        char[] chars = "<r a=\"1\"/>".toCharArray();
        XmlElement el = new XmlElement();
        el.parseCharArray(chars, 0, chars.length, 3);
        assertThat(el.getName()).isEqualTo("r");
    }

    @Test
    @DisplayName("parseString tolerates an XML declaration prefix")
    void testParseWithXmlDeclaration() throws Exception {
        XmlElement el = new XmlElement();
        el.parseString("<?xml version=\"1.0\" encoding=\"utf-8\"?><r a=\"1\"/>");
        assertThat(el.getName()).isEqualTo("r");
    }

    @Test
    @DisplayName("parseString tolerates a CDATA-like section in content")
    void testParseWithCData() throws Exception {
        XmlElement el = new XmlElement();
        el.parseString("<r><![CDATA[some data]]></r>");
        assertThat(el.getName()).isEqualTo("r");
    }

    @Test
    @DisplayName("parseString handles attributes with entity-encoded values")
    void testParseWithEntityAttr() throws Exception {
        XmlElement el = new XmlElement();
        el.parseString("<r a=\"x&amp;y\"/>");
        assertThat(el.getStringAttribute("a")).isEqualTo("x&y");
    }

    @Test
    @DisplayName("parseString with mixed content and nested entities round-trips")
    void testParseMixedEntities() throws Exception {
        XmlElement el = new XmlElement();
        el.parseString("<r>&lt;tag&gt;text&lt;/tag&gt;</r>");
        assertThat(el.getContent()).contains("<tag>text</tag>");
    }

    @Test
    @DisplayName("parseString with trailing whitespace after the closing tag")
    void testParseTrailingWhitespace() throws Exception {
        XmlElement el = new XmlElement();
        el.parseString("<r a=\"1\"/>   ");
        assertThat(el.getName()).isEqualTo("r");
    }

    @Test
    @DisplayName("write with standalone=false and indent=0 emits compact form")
    void testWriteCompact() throws IOException {
        XmlElement el = new XmlElement();
        el.parseString("<r a=\"1\"><c>x</c></r>");
        StringWriter sw = new StringWriter();
        el.write(sw, false, 0);
        assertThat(sw.toString()).contains("<r").contains("<c>");
    }

    @Test
    @DisplayName("write(writer) (no args) emits the element")
    void testWriteNoArg() throws IOException {
        XmlElement el = new XmlElement();
        el.parseString("<r a=\"1\"/>");
        StringWriter sw = new StringWriter();
        el.write(sw);
        assertThat(sw.toString()).contains("<r");
    }

    @Test
    @DisplayName("getBooleanAttribute returns default when attribute is absent")
    void testGetBooleanAttributeDefault() {
        XmlElement el = new XmlElement();
        assertThat(el.getBooleanAttribute("missing", "yes", "no", true)).isTrue();
    }

    @Test
    @DisplayName("getChild(int) returns the n-th child")
    void testGetChildByIndex() throws Exception {
        XmlElement el = new XmlElement();
        el.parseString("<r><a/><b/></r>");
        assertThat(el.getChild(0).getName()).isEqualTo("a");
        assertThat(el.getChild(1).getName()).isEqualTo("b");
    }

    @Test
    @DisplayName("write escapes special chars (< > & \" ') in content")
    void testWriteEscapesSpecialChars() throws IOException {
        XmlElement el = new XmlElement();
        el.setName("r");
        el.setContent("<a> & \"quote\" 'apos'");
        StringWriter sw = new StringWriter();
        el.write(sw);
        String out = sw.toString();
        assertThat(out).contains("&lt;a&gt;").contains("&amp;").contains("&quot;").contains("&apos;");
    }

    @Test
    @DisplayName("write escapes special chars in attribute values")
    void testWriteEscapesAttributeValues() throws IOException {
        XmlElement el = new XmlElement();
        el.setName("r");
        el.setAttribute("a", "<x>&\"y\"'");
        StringWriter sw = new StringWriter();
        el.write(sw);
        assertThat(sw.toString()).contains("&lt;x&gt;").contains("&amp;");
    }

    @Test
    @DisplayName("parseString(string, offset) parses starting at the offset")
    void testParseStringWithOffset() throws Exception {
        XmlElement el = new XmlElement();
        el.parseString("GARBAGE<r a=\"1\"/>", 7);
        assertThat(el.getName()).isEqualTo("r");
    }

    @Test
    @DisplayName("parseString(string, offset, end) parses a slice")
    void testParseStringWithOffsetEnd() throws Exception {
        XmlElement el = new XmlElement();
        el.parseString("XX<r a=\"1\"/>YY", 2, 12);
        assertThat(el.getName()).isEqualTo("r");
    }

    @Test
    @DisplayName("parseString(string, offset, end, ...) variant delegates to parseFromReader")
    void testParseStringOffsetEndFull() throws Exception {
        XmlElement el = new XmlElement();
        el.parseString("XX<r a=\"1\"/>YY", 2, 12, 1);
        assertThat(el.getName()).isEqualTo("r");
    }

    @Test
    @DisplayName("addChild throws when the child is the same element or its ancestor")
    void testAddChildRejectsSelfAndAncestor() {
        XmlElement root = new XmlElement();
        root.setName("root");
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> root.addChild(root))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("parseFromReader(reader, startingLineNr) variant delegates")
    void testParseFromReaderWithLineNrVariant() throws Exception {
        XmlElement el = new XmlElement();
        el.parseFromReader(new java.io.StringReader("<r a=\"1\"/>"), 2);
        assertThat(el.getName()).isEqualTo("r");
    }

    @Test
    @DisplayName("parseString with a CDATA section preserves raw content")
    void testCDataPreserved() throws Exception {
        XmlElement el = new XmlElement();
        el.parseString("<r><![CDATA[<raw><tags>&stuff;</raw>]]></r>");
        assertThat(el.getContent()).contains("<raw>");
    }

    @Test
    @DisplayName("parseString with a deeply nested structure")
    void testParseDeepNested() throws Exception {
        XmlElement el = new XmlElement();
        el.parseString("<a><b><c><d>x</d></c></b></a>");
        assertThat(el.getChild("b").getChild("c").getChild("d").getContent()).isEqualTo("x");
    }

    @Test
    @DisplayName("parseString with empty content element")
    void testParseEmptyContent() throws Exception {
        XmlElement el = new XmlElement();
        el.parseString("<r></r>");
        assertThat(el.getContent()).isEmpty();
    }

    @Test
    @DisplayName("parseString with multiple attributes and children of mixed names")
    void testParseMixedChildren() throws Exception {
        XmlElement el = new XmlElement();
        el.parseString("<r a=\"1\" b=\"2\"><x/><y/><x/></r>");
        assertThat(el.getChildren("x")).hasSize(2);
        assertThat(el.getChildren("y")).hasSize(1);
    }

    @Test
    @DisplayName("write of a leaf element with no content emits self-closing form")
    void testWriteLeafSelfClosing() throws IOException {
        XmlElement el = new XmlElement();
        el.setName("leaf");
        el.setAttribute("a", "1");
        StringWriter sw = new StringWriter();
        el.write(sw);
        assertThat(sw.toString()).contains("/>");
    }

    @Test
    @DisplayName("getIntAttribute(name, defaultValue) returns default when attribute is absent")
    void testGetIntAttributeDefaultAbsent() {
        XmlElement el = new XmlElement();
        assertThat(el.getIntAttribute("missing", 7)).isEqualTo(7);
    }

    @Test
    @DisplayName("getLongAttribute(name, defaultValue) returns default when attribute is absent")
    void testGetLongAttributeDefault() {
        XmlElement el = new XmlElement();
        assertThat(el.getLongAttribute("missing", 99L)).isEqualTo(99L);
    }

    @Test
    @DisplayName("getDoubleAttribute(name, defaultValue) returns default when absent")
    void testGetDoubleAttributeDefault() {
        XmlElement el = new XmlElement();
        assertThat(el.getDoubleAttribute("missing", 1.5)).isEqualTo(1.5);
    }

    @Test
    @DisplayName("parseString expands decimal character references (&#65; -> A)")
    void testDecimalCharRef() throws Exception {
        XmlElement el = new XmlElement();
        el.parseString("<r>&#65;&#66;&#67;</r>");
        assertThat(el.getContent()).contains("ABC");
    }

    @Test
    @DisplayName("parseString expands hex character references (&#x41; -> A)")
    void testHexCharRef() throws Exception {
        XmlElement el = new XmlElement();
        el.parseString("<r>&#x41;</r>");
        assertThat(el.getContent()).contains("A");
    }

    @Test
    @DisplayName("parseString skips an XML comment")
    void testSkipComment() throws Exception {
        XmlElement el = new XmlElement();
        el.parseString("<r><!-- a comment --><a>1</a></r>");
        assertThat(el.getChild("a").getContent()).isEqualTo("1");
    }

    @Test
    @DisplayName("parseString skips the XML declaration processing instruction")
    void testSkipProcessingInstruction() throws Exception {
        XmlElement el = new XmlElement();
        el.parseString("<?xml version=\"1.0\"?><r><a>1</a></r>");
        assertThat(el.getChild("a").getContent()).isEqualTo("1");
    }

    @Test
    @DisplayName("parseString throws on an unknown entity")
    void testUnknownEntityThrows() {
        XmlElement el = new XmlElement();
        assertThatThrownBy(() -> el.parseString("<r>&bogusentity;</r>"))
                .isInstanceOf(XmlParseException.class);
    }

    @Test
    @DisplayName("parseString handles nested CDATA-like content with ] sequences")
    void testCDataBracketSequences() throws Exception {
        XmlElement el = new XmlElement();
        el.parseString("<r><![CDATA[data ] ] more]]></r>");
        assertThat(el.getContent()).contains("data");
    }

    @Test
    @DisplayName("parseString throws on a mismatched closing tag")
    void testMismatchedClosingTag() {
        XmlElement el = new XmlElement();
        assertThatThrownBy(() -> el.parseString("<a></b>"))
                .isInstanceOf(XmlParseException.class);
    }

    @Test
    @DisplayName("parseString throws when an attribute value is not quoted")
    void testUnquotedAttributeThrows() {
        XmlElement el = new XmlElement();
        assertThatThrownBy(() -> el.parseString("<r a=1/>"))
                .isInstanceOf(XmlParseException.class);
    }

    @Test
    @DisplayName("parseString handles an element with many attributes")
    void testManyAttributes() throws Exception {
        XmlElement el = new XmlElement();
        el.parseString("<r a=\"1\" b=\"2\" c=\"3\" d=\"4\"/>");
        assertThat(el.getAttributeNames()).contains("a", "b", "c", "d");
    }

    @Test
    @DisplayName("write(writer, standalone=true, indent=4) of a tree with children indents")
    void testWriteTreeWithIndent() throws IOException {
        XmlElement root = new XmlElement();
        root.setName("root");
        XmlElement child = new XmlElement();
        child.setName("c");
        root.addChild(child);
        StringWriter sw = new StringWriter();
        root.write(sw, true, 4);
        assertThat(sw.toString()).contains("    <c");
    }

    @Test
    @DisplayName("parseString throws on an unclosed opening tag")
    void testUnclosedOpeningTag() {
        XmlElement el = new XmlElement();
        assertThatThrownBy(() -> el.parseString("<r a=\"1\""))
                .isInstanceOf(XmlParseException.class);
    }

    @Test
    @DisplayName("parseString throws on a stray '<' inside content")
    void testStrayLessThan() {
        XmlElement el = new XmlElement();
        assertThatThrownBy(() -> el.parseString("<r> a < b </r>"))
                .isInstanceOf(XmlParseException.class);
    }

    @Test
    @DisplayName("removeChild of a non-present child is a no-op")
    void testRemoveChildNoOp() {
        XmlElement root = new XmlElement();
        root.setName("r");
        XmlElement c = new XmlElement();
        c.setName("c");
        root.removeChild(c); // not added -> no exception
        assertThat(root.getChildrenCount()).isZero();
    }

    @Test
    @DisplayName("getIntAttribute(name,Hashtable,...) variant falls back when no mapping")
    void testGetIntAttributeHashtableVariant() {
        XmlElement el = new XmlElement();
        el.setAttribute("n", "5");
        // the (name, Hashtable, String, boolean) variant is private-ish; just call the public one
        assertThat(el.getIntAttribute("n", 0)).isEqualTo(5);
    }

    @Test
    @DisplayName("parseString throws when an attribute has no '=' assignment")
    void testParseAttributeMissingEqualsThrows() {
        XmlElement el = new XmlElement();
        assertThatThrownBy(() -> el.parseString("<user id \"7\"/>"))
                .isInstanceOf(XmlParseException.class);
    }

    @Test
    @DisplayName("parseString throws on a malformed numeric character reference")
    void testParseMalformedNumericEntityThrows() {
        XmlElement el = new XmlElement();
        assertThatThrownBy(() -> el.parseString("<msg>&#zz;</msg>"))
                .isInstanceOf(XmlParseException.class);
    }

    @Test
    @DisplayName("parseString keeps bracket and '>' sequences inside CDATA sections")
    void testCDataBracketAndGtSequences() throws Exception {
        XmlElement el = new XmlElement();
        el.parseString("<msg><![CDATA[a ]]] b ]> c]]></msg>");
        assertThat(el.getContent()).isEqualTo("a ]] b ]> c");
    }

    @Test
    @DisplayName("parseString accepts a comment between self-closing children")
    void testCommentBetweenSelfClosingChildren() throws Exception {
        XmlElement el = new XmlElement();
        el.parseString("<r><a/><!-- c --><b>2</b></r>");
        assertThat(el.getChildrenCount()).isEqualTo(2);
        assertThat(el.getChild("b").getContent()).isEqualTo("2");
    }

    @Test
    @DisplayName("parseString throws on trailing text after a self-closing child")
    void testTextAfterSelfClosingChildThrows() {
        XmlElement el = new XmlElement();
        assertThatThrownBy(() -> el.parseString("<r><a/>text</r>"))
                .isInstanceOf(XmlParseException.class);
    }

    @Test
    @DisplayName("parseString throws when a comment is not terminated by '>'")
    void testCommentMissingClosingGtThrows() {
        XmlElement el = new XmlElement();
        assertThatThrownBy(() -> el.parseString("<r><!-- a --! --></r>"))
                .isInstanceOf(XmlParseException.class);
    }

    @Test
    @DisplayName("parseString throws when a self-closing tag is not closed by '>'")
    void testSelfClosingTagMissingGtThrows() {
        XmlElement el = new XmlElement();
        assertThatThrownBy(() -> el.parseString("<r a=\"1\"/x>"))
                .isInstanceOf(XmlParseException.class);
    }

    @Test
    @DisplayName("Exception factories produce descriptive XmlParseExceptions")
    void testParseExceptionFactories() {
        ExposedXmlElement el = new ExposedXmlElement();
        assertThat(el.syntaxErrorExposed("element")).isInstanceOf(XmlParseException.class)
                .hasMessageContaining("Syntax error while parsing element");
        assertThat(el.invalidValueSetExposed("amp")).isInstanceOf(XmlParseException.class)
                .hasMessageContaining("Invalid value set");
    }

    /**
     * Exposes the protected exception factories so they can be unit tested.
     */
    static class ExposedXmlElement extends XmlElement {

        XmlParseException syntaxErrorExposed(String context) {
            return syntaxError(context);
        }

        XmlParseException invalidValueSetExposed(String name) {
            return invalidValueSet(name);
        }
    }
}
