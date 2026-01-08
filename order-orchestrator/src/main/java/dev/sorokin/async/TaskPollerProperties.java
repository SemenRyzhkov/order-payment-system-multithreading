package dev.sorokin.async;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "task-execution.poller")
public class TaskPollerProperties {

    private long pollIntervalMs;
    private int batchSize;
    private Duration retryDelay;
    private long lockInterval;

}
