package dev.sorokin.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class AsyncConfiguration {

    @Bean(destroyMethod = "shutdown")
    public ExecutorService taskDispatcherThreadPool(
            @Value("${task-execution.dispatcher.thread-pool-size}") int threadPoolSize
    ) {
//        return Executors.newFixedThreadPool(properties.getThreadPoolSize());
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    @Bean(destroyMethod = "shutdown")
    public ExecutorService taskProcessorThreadPool(
            @Value("${task-execution.external-http.thread-pool-size}") int threadPoolSize
    ) {
//        return Executors.newFixedThreadPool(threadPoolSize);
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}
