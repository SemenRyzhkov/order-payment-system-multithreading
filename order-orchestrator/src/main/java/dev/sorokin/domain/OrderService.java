package dev.sorokin.domain;

import dev.sorokin.api.OrderCreateRequestDto;
import dev.sorokin.repository.OrderJpaRepository;
import dev.sorokin.repository.PaymentTaskJpaRepository;
import dev.sorokin.repository.entity.OrderEntity;
import dev.sorokin.repository.entity.PaymentStatus;
import dev.sorokin.repository.entity.PaymentTaskEntity;
import dev.sorokin.repository.entity.TaskStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderJpaRepository orderRepository;
    private final PaymentTaskJpaRepository paymentTaskRepository;

    public OrderEntity createOrder(
            OrderCreateRequestDto requestDto
    ) {
        var entity = OrderEntity.builder()
                .address(requestDto.address())
                .paymentStatus(PaymentStatus.NEW)
                .clientEstimate(requestDto.clientEstimate())
                .build();

        var saved = orderRepository.save(entity);

        paymentTaskRepository.save(PaymentTaskEntity.builder()
                .orderId(saved.getId())
                .status(TaskStatus.NEW)
                .build());

        log.info("Payment task for order {} created", saved.getId());

        return saved;
    }

    public Optional<OrderEntity> findOrder(UUID id) {
        return orderRepository.findById(id);
    }
}
