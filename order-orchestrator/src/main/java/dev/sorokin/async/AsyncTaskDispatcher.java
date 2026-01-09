package dev.sorokin.async;


import dev.sorokin.async.properties.TaskDispatcherProperties;
import dev.sorokin.repository.PaymentTaskJpaRepository;
import dev.sorokin.repository.entity.PaymentTaskEntity;
import dev.sorokin.repository.entity.TaskStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;


@Slf4j
@Component
@RequiredArgsConstructor
public class AsyncTaskDispatcher {

    private final TaskProcessor taskProcessor;
    private final ExecutorService taskDispatcherThreadPool;
    private final PaymentTaskJpaRepository taskRepository;
    private final TaskDispatcherProperties properties;

    public void dispatch(PaymentTaskEntity task) {
        CompletableFuture.supplyAsync(() -> taskProcessor.processTask(task), taskDispatcherThreadPool)
                .thenAccept(result -> handleTaskResult(task, result))
                .exceptionally(e -> handleException(e, task));
    }

    private void handleTaskResult(PaymentTaskEntity task, TaskStatus result) {
        log.info("Task {} finished with result: {}", task.getId(), result);
        switch (result) {
            case TaskStatus.SUCCEEDED -> succeedProcess(task, result);
            case TaskStatus.FAILED_NON_RETRYABLE -> finishProcess(task, result);
            case TaskStatus.FAILED_RETRYABLE -> makeRetry(task, result);
        }
    }

    private void succeedProcess(PaymentTaskEntity task, TaskStatus result) {
        log.info("Task {} was successfully executed", task.getId());
        task.setStatus(result);
        taskRepository.save(task);
    }

    private void finishProcess(PaymentTaskEntity task, TaskStatus result) {
        log.info("Task {} is failed. Process is finished", task.getId());
        task.setStatus(result);
        taskRepository.save(task);
    }

    private void makeRetry(PaymentTaskEntity task, TaskStatus result) {
        Integer attempts = task.getAttempts();
        if (attempts < properties.getMaxAttempts()) {
            log.info("Task {} will be retried", task.getId());
            OffsetDateTime nextAttemptAt = OffsetDateTime.now().plus(properties.getRetryDelay());
            task.setNextAttemptAt(nextAttemptAt);
            task.setStatus(result);
            taskRepository.save(task);
        } else {
            log.info("Max attempts reached for task {}", task.getId());
            task.setStatus(TaskStatus.FAILED_NON_RETRYABLE);
            taskRepository.save(task);
        }
    }

    private Void handleException(Throwable e, PaymentTaskEntity task) {
        log.info("Task {} failed with exception: {}", task.getId(), e.getMessage());
        makeRetry(task, TaskStatus.FAILED_RETRYABLE);
        return null;
    }
}
