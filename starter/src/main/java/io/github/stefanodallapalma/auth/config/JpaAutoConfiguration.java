package io.github.stefanodallapalma.auth.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EntityScan(basePackages = "io.github.stefanodallapalma.auth.core.model")
@EnableJpaRepositories(basePackages = "io.github.stefanodallapalma.auth.core.repository")
public class JpaAutoConfiguration {
}
