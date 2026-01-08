package dev.sorokin.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class AsyncConfiguration {

    @Bean
    public ExecutorService taskExecutorThreadPool() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    @Bean
    public ExecutorService taskProcessorThreadPool() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}
