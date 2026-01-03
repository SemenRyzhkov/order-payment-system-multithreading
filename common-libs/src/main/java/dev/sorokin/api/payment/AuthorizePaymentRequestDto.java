package dev.sorokin.api.payment;

import java.math.BigDecimal;

/**
 * Запрос на авторизацию карты на указанную сумму
 */
public record AuthorizePaymentRequestDto(
    Long customerId,
    BigDecimal amount
) { }
