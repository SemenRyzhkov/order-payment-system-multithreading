package dev.sorokin.async;


import dev.sorokin.api.payment.*;
import dev.sorokin.api.warehouse.CalculatePricingRequestDto;
import dev.sorokin.api.warehouse.CalculatePricingResponseDto;
import dev.sorokin.client.StubHttpClient;
import dev.sorokin.repository.OrderJpaRepository;
import dev.sorokin.repository.entity.OrderEntity;
import dev.sorokin.repository.entity.PaymentStatus;
import dev.sorokin.repository.entity.PaymentTaskEntity;
import dev.sorokin.repository.entity.TaskStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@Slf4j
@Component
@RequiredArgsConstructor
public class TaskProcessor {

    private final OrderJpaRepository orderJpaRepository;
    private final StubHttpClient httpClient;
    private final ExecutorService taskProcessorThreadPool;

    public TaskStatus processTask(PaymentTaskEntity task) {
        Optional<OrderEntity> optionalOrderEntity = orderJpaRepository.findById(task.getOrderId());

        if (optionalOrderEntity.isEmpty()) {
            log.info("Order not found for task {}", task.getId());
            return TaskStatus.FAILED_NON_RETRYABLE;
        }

        OrderEntity orderEntity = optionalOrderEntity.get();

        CompletableFuture<AuthorizePaymentResponseDto> authFuture = CompletableFuture
                .supplyAsync(() -> httpClient.authorizePayment(new AuthorizePaymentRequestDto(
                        orderEntity.getCustomerId(),
                        orderEntity.getClientEstimate()
                )), taskProcessorThreadPool);

        CompletableFuture<CalculatePricingResponseDto> pricingFuture = CompletableFuture
                .supplyAsync(() -> httpClient.calculatePricing(new CalculatePricingRequestDto(
                        orderEntity.getId()
                )), taskProcessorThreadPool);

        AuthorizePaymentResponseDto authResponse;
        try {
            authResponse = authFuture.join();
        } catch (Exception e) {
            log.error("Failed to authorize payment", e);
            return TaskStatus.FAILED_RETRYABLE;
        }

        TaskStatus taskStatus;

        taskStatus = handleAuthResult(authResponse, orderEntity);
        if (Objects.isNull(taskStatus)) {
            return taskStatus;
        }


        CalculatePricingResponseDto pricingResponse;
        try {
            pricingResponse = pricingFuture.join();
        } catch (Exception e) {
            log.error("Failed to calculate a price", e);
            return TaskStatus.FAILED_RETRYABLE;
        }

        taskStatus = handlePricingResult(pricingResponse, authResponse, orderEntity);
        if (Objects.nonNull(taskStatus)) {
            return taskStatus;
        }

        CapturePaymentResponseDto capturePaymentResponseDto;
        try {
            capturePaymentResponseDto = httpClient.capturePayment(new CapturePaymentRequestDto(
                    pricingResponse.finalAmount(),
                    orderEntity.getCustomerId())
            );
        } catch (Exception e) {
            log.error("Failed to capture payment", e);
            return TaskStatus.FAILED_RETRYABLE;
        }

        return handleCaptureResult(capturePaymentResponseDto, orderEntity);


    }

    private TaskStatus handleCaptureResult(CapturePaymentResponseDto captureResponse,
                                           OrderEntity orderEntity
    ) {
        CaptureStatus status = captureResponse.status();
        log.info("Capture response status: {}", status);

        if (Objects.equals(status, CaptureStatus.CAPTURED)) {
            orderEntity.setPaymentStatus(PaymentStatus.SUCCESS_PAID);
            orderEntity.setCaptureAmount(captureResponse.capturedAmount());
            orderJpaRepository.save(orderEntity);
            return TaskStatus.SUCCEEDED;
        }
        return TaskStatus.FAILED_NON_RETRYABLE;

    }

    private TaskStatus handlePricingResult(CalculatePricingResponseDto pricingResponse,
                                           AuthorizePaymentResponseDto authResponse,
                                           OrderEntity orderEntity
    ) {
        log.info("Pricing final amount: {}", pricingResponse.finalAmount());
        BigDecimal finalAmount = pricingResponse.finalAmount();
        BigDecimal authorizedAmount = authResponse.authorizedAmount();

        if (finalAmount.compareTo(authorizedAmount) <= 0) {
            return null;
        }
        orderEntity.setPaymentStatus(PaymentStatus.PRICE_CHANGED_FAILED);
        orderEntity.setFailureReason(pricingResponse.reason());
        return TaskStatus.FAILED_NON_RETRYABLE;

    }

    private TaskStatus handleAuthResult(AuthorizePaymentResponseDto authResponse,
                                        OrderEntity orderEntity
    ) {
        AuthorizationStatus status = authResponse.status();
        log.info("Auth response status: {}", status);
        if (Objects.equals(status, AuthorizationStatus.AUTHORIZED)) {
            orderEntity.setAuthorizedAmount(authResponse.authorizedAmount());
            return null;
        }
        orderEntity.setPaymentStatus(PaymentStatus.AUTHORIZATION_FAILED);
        orderEntity.setFailureReason(authResponse.message());
        orderJpaRepository.save(orderEntity);
        return TaskStatus.FAILED_NON_RETRYABLE;
    }
}
