package dev.sorokin.repository.entity;

import dev.sorokin.repository.converter.StepConverter;
import dev.sorokin.repository.converter.TaskStatusConverter;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "payment_tasks")
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class PaymentTaskEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID id;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "status", nullable = false, length = 32)
    @Convert(converter = TaskStatusConverter.class)
    private TaskStatus status;

    @Column(name = "step", nullable = false, length = 32)
    @Convert(converter = StepConverter.class)
    private Step step;

    @Column(name = "attempts", nullable = false)
    private Integer attempts;

    @Column(name = "next_attempt_at")
    private OffsetDateTime nextAttemptAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "locked_until")
    private OffsetDateTime lockedUntil;

    @PrePersist
    protected void onCreate() {
        final OffsetDateTime now = OffsetDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.attempts == null) {
            this.attempts = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }
}