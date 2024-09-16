package io.stefadp.auth.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EntityScan(basePackages = "io.stefadp.auth.core.model")
@EnableJpaRepositories(basePackages = "io.stefadp.auth.core.repository")
public class JpaAutoConfiguration {
}
