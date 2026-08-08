package net.apexes.fetion4j.spring.boot;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

/**
 * Spring Boot auto-configuration for the Fetion (China Mobile IM) integration.
 * <p>
 * Activates only when {@code spring.dozer.enabled} is set to {@code true}, and
 * binds {@link Fetion4jProperties} to the {@code spring.dozer.*} namespace.
 * Exposes a shared {@link ResourcePatternResolver} for resolving classpath
 * resources used by the integration.
 * </p>
 *
 * <h3>Configuration</h3>
 * <ul>
 *   <li>{@code spring.dozer.enabled} — opt-in switch (default {@code false})</li>
 *   <li>{@code spring.dozer.mapping-files} — resource pattern for mapping files, e.g. {@code classpath*:/*.dozer.xml}</li>
 * </ul>
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 1.0.0
 */
@Configuration
@ConditionalOnProperty(prefix = Fetion4jProperties.PREFIX, value = "enabled", havingValue = "true")
@EnableConfigurationProperties({ Fetion4jProperties.class })
public class Fetion4jAutoConfiguration {

	// Spring resource pattern resolver.
	// "classpath"   : loads a single resource from the classpath (including jars); returns only the first match even if multiple exist, so use "classpath*:" when multiple matches are required.
	// "classpath*"  : loads ALL matching resources from the classpath (including jars). Wildcard classpath uses the ClassLoader's Enumeration<URL> getResources(String name)
	//                 method to find resources before the wildcard prefix, then applies pattern matching to resolve the matching resources.
	protected static ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
	/*
	@Bean
	@ConditionalOnMissingBean

*/
}
