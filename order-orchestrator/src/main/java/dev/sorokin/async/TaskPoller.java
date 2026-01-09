package dev.sorokin.async;


import dev.sorokin.async.properties.TaskPollerProperties;
import dev.sorokin.repository.PaymentTaskJpaRepository;
import dev.sorokin.repository.entity.PaymentTaskEntity;
import dev.sorokin.repository.entity.Step;
import dev.sorokin.repository.entity.TaskStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;


@Slf4j
@Component
@RequiredArgsConstructor
public class TaskPoller {

    private final PaymentTaskJpaRepository taskJpaRepository;
    private final AsyncTaskDispatcher dispatcher;
    private final TaskPollerProperties properties;

    @Scheduled(
            fixedRateString = "${task-execution.poller.poll-interval-ms}"
    )
    public void poll() {
        log.info("Polling for tasks");
        List<PaymentTaskEntity> taskList = getTaskList();
        if (taskList.isEmpty()) {
            return;
        }
        taskList.forEach(dispatcher::dispatch);
    }

    public List<PaymentTaskEntity> getTaskList() {
        List<PaymentTaskEntity> taskList = taskJpaRepository
                .findAndReserveTasks(
                        properties.getBatchSize(),
                        TaskStatus.IN_PROGRESS.getCode(),
                        TaskStatus.NEW.getCode(),
                        TaskStatus.FAILED_RETRYABLE.getCode()
                );

        log.info("Task List size: {}. Task List IDs: {}",
                taskList.size(),
                taskList.stream()
                        .map(PaymentTaskEntity::getId)
                        .toList()
        );

        return taskList;
    }
}
