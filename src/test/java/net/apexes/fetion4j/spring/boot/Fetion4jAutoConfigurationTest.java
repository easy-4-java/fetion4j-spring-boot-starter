package net.apexes.fetion4j.spring.boot;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

/**
 * Unit tests for {@link Fetion4jAutoConfiguration} and {@link Fetion4jProperties}.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
@DisplayName("Fetion4j AutoConfiguration Tests")
class Fetion4jAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner();

    @Test
    @DisplayName("Fetion4jProperties prefix constant is spring.dozer")
    void testPrefix() {
        assertThat(Fetion4jProperties.PREFIX).isEqualTo("spring.dozer");
    }

    @Test
    @DisplayName("Fetion4jProperties enabled defaults to false and is round-trippable")
    void testEnabled() {
        Fetion4jProperties props = new Fetion4jProperties();
        assertThat(props.isEnabled()).isFalse();
        props.setEnabled(true);
        assertThat(props.isEnabled()).isTrue();
    }

    @Test
    @DisplayName("Fetion4jProperties mappingFiles is round-trippable")
    void testMappingFiles() {
        Fetion4jProperties props = new Fetion4jProperties();
        assertThat(props.getMappingFiles()).isNull();
        props.setMappingFiles("classpath*:/*.dozer.xml");
        assertThat(props.getMappingFiles()).isEqualTo("classpath*:/*.dozer.xml");
    }

    @Test
    @DisplayName("Auto-configuration registers when spring.dozer.enabled=true")
    void testLoadsWhenEnabled() {
        runner.withUserConfiguration(Fetion4jAutoConfiguration.class)
                .withPropertyValues("spring.dozer.enabled=true")
                .run(context -> assertThat(context).hasSingleBean(Fetion4jAutoConfiguration.class));
    }

    @Test
    @DisplayName("Auto-configuration is absent when property is missing")
    void testAbsentWhenPropertyMissing() {
        runner.withUserConfiguration(Fetion4jAutoConfiguration.class)
                .run(context -> assertThat(context).doesNotHaveBean(Fetion4jAutoConfiguration.class));
    }

    @Test
    @DisplayName("Fetion4jAutoConfiguration exposes a static resolver field")
    void testResolverExists() {
        assertThat(Fetion4jAutoConfiguration.resolver).isNotNull();
    }
}
