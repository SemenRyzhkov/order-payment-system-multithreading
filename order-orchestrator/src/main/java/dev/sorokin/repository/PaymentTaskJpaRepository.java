package dev.sorokin.repository;

import dev.sorokin.repository.entity.PaymentTaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PaymentTaskJpaRepository extends JpaRepository<PaymentTaskEntity, UUID> {
}
