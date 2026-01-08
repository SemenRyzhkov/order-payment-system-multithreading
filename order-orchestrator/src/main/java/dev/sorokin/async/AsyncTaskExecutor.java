package dev.sorokin.async;


import dev.sorokin.repository.entity.PaymentTaskEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;


@Slf4j
@Component
@RequiredArgsConstructor
public class AsyncTaskExecutor {

    private final TaskProcessor taskProcessor;
    private final ExecutorService taskExecutorThreadPool;

    public void execute(PaymentTaskEntity taskEntity) {
        CompletableFuture.supplyAsync(() -> taskProcessor.processTask(taskEntity), taskExecutorThreadPool)
                .thenAccept(result -> {

                })
                .exceptionally(e -> {

                    return null;
                });

    }
}
