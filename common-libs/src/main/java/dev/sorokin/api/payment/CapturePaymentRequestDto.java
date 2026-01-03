package dev.sorokin.api.payment;

import java.math.BigDecimal;

/**
 * Запрос на списание (capture) по ранее авторизованной карте
 */
public record CapturePaymentRequestDto(
        BigDecimal captureAmount,
        Long customerId
) {}
