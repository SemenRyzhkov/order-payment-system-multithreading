package dev.sorokin.api;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder(toBuilder = true)
public record OrderDto(
        UUID id,
        Long customerId,
        String address,
        String paymentStatus,
        BigDecimal clientEstimate,
        BigDecimal authorizedAmount,
        BigDecimal capturedAmount,
        BigDecimal finalAmount,
        String failureReason
) { }
