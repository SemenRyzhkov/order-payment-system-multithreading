package dev.sorokin.api;

import dev.sorokin.domain.OrderEntity;
import dev.sorokin.domain.OrderService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/order")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderDto> createOrder(
            @RequestBody OrderCreateRequestDto orderCreateRequestDto
    ) {
        log.info("Received request to create order: request={}", orderCreateRequestDto);
        var created = orderService.createOrder(orderCreateRequestDto);
        log.info("Created order: created={}", created);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(mapEntityToDto(created));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDto> getOrder(
            @PathVariable UUID id
    ) {
        log.info("Getting order with id {}", id);
        var foundOrder = orderService.findOrder(id);
        return foundOrder
                .map(this::mapEntityToDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private OrderDto mapEntityToDto(OrderEntity order) {
        return OrderDto.builder()
                .id(order.getId())
                .address(order.getAddress())
                .build();
    }
}
