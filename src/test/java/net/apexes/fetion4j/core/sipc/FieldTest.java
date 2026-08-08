package net.apexes.fetion4j.core.sipc;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the package-private {@link Field} value type.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
@DisplayName("Field Tests")
class FieldTest {

    @Test
    @DisplayName("Constructor sets name/value; getters return them")
    void testConstructorAndGetters() {
        Field f = new Field("N", "V");
        assertThat(f.getName()).isEqualTo("N");
        assertThat(f.getValue()).isEqualTo("V");
    }

    @Test
    @DisplayName("Setters mutate name and value")
    void testSetters() {
        Field f = new Field("a", "b");
        f.setName("x");
        f.setValue("y");
        assertThat(f.getName()).isEqualTo("x");
        assertThat(f.getValue()).isEqualTo("y");
    }

    @Test
    @DisplayName("getText renders \"name: value\"")
    void testGetText() {
        assertThat(new Field("I", "1").getText()).isEqualTo("I: 1");
    }

    @Test
    @DisplayName("equals/hashCode are based only on name")
    void testEqualsHashCode() {
        Field f1 = new Field("N", "1");
        Field f2 = new Field("N", "2"); // same name, different value
        Field f3 = new Field("M", "1");
        assertThat(f1).isEqualTo(f2);
        assertThat(f1.hashCode()).isEqualTo(f2.hashCode());
        assertThat(f1).isNotEqualTo(f3);
        assertThat(f1).isNotEqualTo(null);
        assertThat(f1).isNotEqualTo("not a field");
    }

    @Test
    @DisplayName("hashCode is stable for a null name")
    void testHashCodeNullName() {
        Field f = new Field(null, "v");
        int h1 = f.hashCode();
        f.setName(null);
        assertThat(f.hashCode()).isEqualTo(h1);
    }

    @Test
    @DisplayName("equals returns false for null and different class")
    void testEqualsEdgeCases() {
        Field f = new Field("N", "V");
        assertThat(f.equals(null)).isFalse();
        assertThat(f.equals("string")).isFalse();
    }
}
