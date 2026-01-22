package dev.sorokin.client;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Data
@Configuration
@ConfigurationProperties(prefix = "clients.stub")
public class StubClientProperties {
    private String baseUrl;
    private Duration connectTimeout;
    private Duration readTimeout;
}
