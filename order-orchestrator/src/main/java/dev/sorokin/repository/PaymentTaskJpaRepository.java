package dev.sorokin.repository;

import dev.sorokin.repository.entity.PaymentTaskEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface PaymentTaskJpaRepository
        extends JpaRepository<PaymentTaskEntity, UUID> {

    @Modifying
    @Query(value = """
            UPDATE payment_tasks
            SET status := statusReserved,
                attempts = attempts + 1,
                updated_at = NOW()
            WHERE id IN (
                SELECT id FROM (
                    SELECT id FROM payment_tasks
                    WHERE status IN (:statusNew, :statusRetryable)
                      AND (next_attempt_at IS NULL OR next_attempt_at <= NOW())
                    ORDER BY created_at
                    LIMIT :limit
                    FOR UPDATE SKIP LOCKED
                ) AS unlocked
            )
            RETURNING id, order_id, status, step, attempts, next_attempt_at, created_at, updated_at
            """, nativeQuery = true)
    @Transactional
    List<PaymentTaskEntity> findAndReserveTasks(
            @Param("limit") int limit,
            @Param("statusReserved") int statusReserved,
            @Param("statusNew") int statusNew,
            @Param("statusRetryable") int statusRetryable
    );
}