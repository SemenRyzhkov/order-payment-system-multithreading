package dev.sorokin.api;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder(toBuilder = true)
public record OrderDto(
        UUID id,
        String address,
        String paymentStatus,
        BigDecimal clientEstimate,
        BigDecimal authorizedAmount,
        BigDecimal captureAmount,
        String failureReason
) { }
