package dev.sorokin.async;


import dev.sorokin.api.payment.*;
import dev.sorokin.api.warehouse.CalculatePricingRequestDto;
import dev.sorokin.api.warehouse.CalculatePricingResponseDto;
import dev.sorokin.client.StubHttpClient;
import dev.sorokin.repository.OrderJpaRepository;
import dev.sorokin.repository.PaymentTaskJpaRepository;
import dev.sorokin.repository.entity.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@Slf4j
@Component
@RequiredArgsConstructor
public class TaskProcessor {

    private final OrderJpaRepository orderJpaRepository;
    private final PaymentTaskJpaRepository taskJpaRepository;
    private final StubHttpClient httpClient;
    private final ExecutorService taskProcessorThreadPool;

    public TaskStatus processTask(PaymentTaskEntity task) {
        Optional<OrderEntity> optionalOrder = orderJpaRepository.findById(task.getOrderId());
        if (optionalOrder.isEmpty()) {
            log.info("Order not found for task {}", task.getId());
            return TaskStatus.FAILED_NON_RETRYABLE;
        }
        OrderEntity order = optionalOrder.get();
        Step currentStep = task.getStep();

        log.info("Processing task {} with step: {}", task.getId(), currentStep);

        try {
            return switch (currentStep) {
                case AUTH -> handleAuthStep(task, order);
                case REPRICE -> handleRepriceStep(task, order, supplyAsync(() ->
                                httpClient.calculatePricing(
                                        new CalculatePricingRequestDto(order.getId()))
                        )
                );
                case CAPTURE -> handleCaptureStep(order);
            };
        } catch (Exception e) {
            log.error("Unexpected error during task processing", e);
            return TaskStatus.FAILED_RETRYABLE;
        }
    }

    private TaskStatus handleAuthStep(PaymentTaskEntity task, OrderEntity order) {
        CompletableFuture<AuthorizePaymentResponseDto> authFuture = supplyAsync(
                () -> httpClient.authorizePayment(new AuthorizePaymentRequestDto(
                        order.getCustomerId(),
                        order.getClientEstimate()
                ))
        );

        CompletableFuture<CalculatePricingResponseDto> pricingFuture = supplyAsync(
                () -> httpClient.calculatePricing(new CalculatePricingRequestDto(order.getId()))
        );

        AuthorizePaymentResponseDto authResponse = joinFuture(
                authFuture,
                "Failed to authorize payment for order: {}",
                order.getId()
        );
        if (authResponse == null) {
            return TaskStatus.FAILED_RETRYABLE;
        }

        TaskStatus authStatus = handleAuthResult(authResponse, order);
        if (authStatus != null) {
            return authStatus;
        }

        task.setStep(Step.REPRICE);
        taskJpaRepository.save(task);

        return handleRepriceStep(task, order, pricingFuture);
    }

    private TaskStatus handleRepriceStep(PaymentTaskEntity task,
                                         OrderEntity order,
                                         CompletableFuture<CalculatePricingResponseDto> pricingFuture
    ) {
        CalculatePricingResponseDto pricingResponse = joinFuture(
                pricingFuture,
                "Failed to calculate a price for order: {}",
                order.getId()
        );

        if (pricingResponse == null) {
            return TaskStatus.FAILED_RETRYABLE;
        }

        TaskStatus pricingStatus = handlePricingResult(pricingResponse, order);
        if (pricingStatus != null) {
            return pricingStatus;
        }

        task.setStep(Step.CAPTURE);
        taskJpaRepository.save(task);

        return handleCaptureStep(order);
    }

    private TaskStatus handleCaptureStep(OrderEntity order) {
        try {
            CapturePaymentResponseDto captureResponse = httpClient.capturePayment(
                    new CapturePaymentRequestDto(order.getFinalAmount(), order.getCustomerId())
            );
            return handleCaptureResult(captureResponse, order);
        } catch (Exception e) {
            log.error("Failed to capture payment for order {}", order.getId(), e);
            return TaskStatus.FAILED_RETRYABLE;
        }
    }

    private <T> CompletableFuture<T> supplyAsync(java.util.function.Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, taskProcessorThreadPool);
    }

    private <T> T joinFuture(CompletableFuture<T> future, String errorMsg, UUID orderId) {
        try {
            return future.join();
        } catch (Exception e) {
            log.error(errorMsg, orderId, e);
            return null;
        }
    }

    private TaskStatus handleCaptureResult(CapturePaymentResponseDto captureResponse,
                                           OrderEntity order
    ) {
        CaptureStatus status = captureResponse.status();
        UUID orderId = order.getId();
        if (CaptureStatus.CAPTURED.equals(status)) {
            log.info("Order {} captured successfully", orderId);
            order.setPaymentStatus(PaymentStatus.SUCCESS_PAID);
            order.setCaptureAmount(captureResponse.capturedAmount());
            orderJpaRepository.save(order);
            return TaskStatus.SUCCEEDED;
        }
        log.info("Order {} capture failed: {}", orderId, captureResponse.message());
        return TaskStatus.FAILED_NON_RETRYABLE;
    }

    private TaskStatus handlePricingResult(CalculatePricingResponseDto pricingResponse,
                                           OrderEntity order
    ) {
        UUID orderId = order.getId();
        BigDecimal finalAmount = pricingResponse.finalAmount();
        BigDecimal authorizedAmount = order.getAuthorizedAmount();

        if (finalAmount.compareTo(authorizedAmount) <= 0) {
            log.info("Pricing match: orderId={}, finalAmount={}, authorizedAmount={}",
                    orderId, finalAmount, authorizedAmount
            );
            order.setFinalAmount(finalAmount);
            orderJpaRepository.save(order);
            return null;
        }

        String failureReason = "Price after calculation is higher than authorized amount";
        log.info("Pricing mismatch: {}. OrderId={}, finalAmount={}, authorizedAmount={}",
                failureReason, orderId, finalAmount, authorizedAmount);
        order.setPaymentStatus(PaymentStatus.PRICE_CHANGED_FAILED);
        order.setFailureReason(failureReason);
        orderJpaRepository.save(order);

        return TaskStatus.FAILED_NON_RETRYABLE;
    }

    private TaskStatus handleAuthResult(AuthorizePaymentResponseDto authResponse,
                                        OrderEntity order
    ) {
        AuthorizationStatus status = authResponse.status();
        UUID orderId = order.getId();
        if (AuthorizationStatus.AUTHORIZED.equals(status)) {
            log.info("Order {} authorized successfully", orderId);
            order.setAuthorizedAmount(authResponse.authorizedAmount());
            orderJpaRepository.save(order);
            return null;
        }
        String errorMessage = authResponse.message();
        log.info("Order {} authorization failed: {}", orderId, errorMessage);
        order.setPaymentStatus(PaymentStatus.AUTHORIZATION_FAILED);
        order.setFailureReason(errorMessage);
        orderJpaRepository.save(order);
        return TaskStatus.FAILED_NON_RETRYABLE;
    }
}

