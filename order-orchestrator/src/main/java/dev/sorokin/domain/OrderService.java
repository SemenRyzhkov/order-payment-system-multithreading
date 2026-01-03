package dev.sorokin.domain;

import dev.sorokin.api.OrderCreateRequestDto;
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

    public OrderEntity createOrder(
            OrderCreateRequestDto requestDto
    ) {
        var entity = OrderEntity.builder()
                .address(requestDto.address())
                .build();

        // todo асинхронная обработка заказа (создать таску)

        return orderRepository.save(entity);
    }

    public Optional<OrderEntity> findOrder(UUID id) {
        return orderRepository.findById(id);
    }
}
