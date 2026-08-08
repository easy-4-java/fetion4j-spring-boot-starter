package net.apexes.fetion4j.spring.boot;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the Fetion4j starter, bound to the
 * {@code spring.dozer.*} namespace.
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 1.0.0
 */
@ConfigurationProperties(Fetion4jProperties.PREFIX)
public class Fetion4jProperties {

	/** Configuration prefix used by Spring Boot to bind properties. */
	public static final String PREFIX = "spring.dozer";

	/** Whether the Fetion integration should be enabled (default {@code false}). */
	private boolean enabled = false;
	/** Spring resource definition for mapping files, e.g. {@code classpath*:/*.dozer.xml}. */
	private String mappingFiles;

	/** @return {@code true} when the integration is enabled. */
	public boolean isEnabled() {
		return enabled;
	}
	/** @param enabled {@code true} to enable the integration. */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	/** @return the configured mapping-files resource pattern. */
	public String getMappingFiles() {
		return mappingFiles;
	}
	/** @param mappingFiles the mapping-files resource pattern. */
	public void setMappingFiles(String mappingFiles) {
		this.mappingFiles = mappingFiles;
	}


}